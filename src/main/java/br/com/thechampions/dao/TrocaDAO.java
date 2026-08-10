package br.com.thechampions.dao;

import br.com.thechampions.config.ConexaoDB;
import br.com.thechampions.model.Figurinha;
import br.com.thechampions.model.Troca;
import br.com.thechampions.model.Usuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Entidade central do sistema. Aqui mora a regra que define o produto:
 * a troca so executa quando os DOIS lados confirmam.
 */
public class TrocaDAO {

    private final ColecaoDAO colecaoDAO = new ColecaoDAO();
    private final NotificacaoDAO notificacaoDAO = new NotificacaoDAO();

    /** Resultado de uma tentativa de confirmacao, para a servlet decidir a mensagem. */
    public enum Resultado {
        AGUARDANDO_OUTRO,
        EXECUTADA,
        JA_CONFIRMADA,
        NAO_PARTICIPA,
        NAO_PENDENTE,
        FIGURINHA_INDISPONIVEL
    }

    private static final String SELECT_TROCA = """
            SELECT t.id_troca, t.codigo, t.status,
                   t.confirmou_proponente, t.confirmou_receptor,
                   t.data_criacao, t.data_expiracao, t.data_conclusao,
                   p.id_usuario AS p_id, p.nome AS p_nome, p.email AS p_email,
                   p.senha_hash AS p_hash, p.tipo_conta AS p_tipo,
                   p.reputacao_media AS p_rep, p.data_cadastro AS p_cad, p.ativo AS p_ativo,
                   d.id_usuario AS d_id, d.nome AS d_nome, d.email AS d_email,
                   d.senha_hash AS d_hash, d.tipo_conta AS d_tipo,
                   d.reputacao_media AS d_rep, d.data_cadastro AS d_cad, d.ativo AS d_ativo
              FROM troca t
              JOIN usuario p ON p.id_usuario = t.id_usuario_proponente
              JOIN usuario d ON d.id_usuario = t.id_usuario_receptor
            """;

    public Troca buscarPorCodigo(String codigo) throws SQLException {
        try (Connection con = ConexaoDB.obter();
             PreparedStatement ps = con.prepareStatement(SELECT_TROCA + " WHERE t.codigo = ?")) {
            ps.setString(1, codigo);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                Troca t = mapear(rs);
                carregarItens(con, t);
                return t;
            }
        }
    }

    public Troca buscarPorId(int id) throws SQLException {
        try (Connection con = ConexaoDB.obter();
             PreparedStatement ps = con.prepareStatement(SELECT_TROCA + " WHERE t.id_troca = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                Troca t = mapear(rs);
                carregarItens(con, t);
                return t;
            }
        }
    }

    /** Trocas em que o usuario participa, das mais recentes para as mais antigas. */
    public List<Troca> listarDoUsuario(int idUsuario, int limite) throws SQLException {
        String sql = SELECT_TROCA + """
                 WHERE t.id_usuario_proponente = ? OR t.id_usuario_receptor = ?
                 ORDER BY COALESCE(t.data_conclusao, t.data_criacao) DESC
                 LIMIT ?
                """;
        List<Troca> lista = new ArrayList<>();
        try (Connection con = ConexaoDB.obter();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            ps.setInt(2, idUsuario);
            ps.setInt(3, limite);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }
            for (Troca t : lista) carregarItens(con, t);
        }
        return lista;
    }

    public int contarPendentesRecebidas(int idUsuario) throws SQLException {
        String sql = """
                SELECT COUNT(*) FROM troca
                 WHERE status = 'PENDENTE' AND id_usuario_receptor = ? AND confirmou_receptor = FALSE
                """;
        try (Connection con = ConexaoDB.obter();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    // =======================================================================
    // CONFIRMACAO E EXECUCAO — o momento em que as figurinhas mudam de dono
    // =======================================================================

    /**
     * Marca a confirmacao do usuario informado e, se os DOIS lados ja tiverem
     * confirmado, executa a transferencia.
     *
     * Tudo em uma transacao unica (setAutoCommit(false)). O motivo e direto:
     * a transferencia sao varios UPDATE/DELETE/INSERT em colecao. Se a metade
     * do caminho falhasse sem transacao, um usuario ficaria sem a figurinha e
     * o outro sem recebe-la — figurinha sumindo do sistema. Com a transacao,
     * ou tudo acontece, ou nada acontece.
     */
    public Resultado confirmar(int idTroca, int idUsuario) throws SQLException {
        Connection con = null;
        try {
            con = ConexaoDB.obter();
            con.setAutoCommit(false);

            Troca troca = travarTroca(con, idTroca);
            if (troca == null) return Resultado.NAO_PARTICIPA;

            boolean ehProponente = troca.getProponente().getId() == idUsuario;
            boolean ehReceptor = troca.getReceptor().getId() == idUsuario;
            if (!ehProponente && !ehReceptor) {
                con.rollback();
                return Resultado.NAO_PARTICIPA;
            }
            if (!"PENDENTE".equals(troca.getStatus()) && !"ACEITA".equals(troca.getStatus())) {
                con.rollback();
                return Resultado.NAO_PENDENTE;
            }
            boolean jaConfirmou = ehProponente ? troca.isConfirmouProponente() : troca.isConfirmouReceptor();
            if (jaConfirmou) {
                con.rollback();
                return Resultado.JA_CONFIRMADA;
            }

            String coluna = ehProponente ? "confirmou_proponente" : "confirmou_receptor";
            try (PreparedStatement ps = con.prepareStatement(
                    "UPDATE troca SET " + coluna + " = TRUE, status = 'ACEITA' WHERE id_troca = ?")) {
                ps.setInt(1, idTroca);
                ps.executeUpdate();
            }

            boolean outroJaConfirmou = ehProponente ? troca.isConfirmouReceptor() : troca.isConfirmouProponente();

            if (!outroJaConfirmou) {
                // So um lado confirmou: nada muda de dono ainda.
                int idOutro = ehProponente ? troca.getReceptor().getId() : troca.getProponente().getId();
                String quemConfirmou = ehProponente
                        ? troca.getProponente().getApelido() : troca.getReceptor().getApelido();
                notificacaoDAO.criar(con, idOutro, "TROCA",
                        quemConfirmou + " confirmou a parte dele na troca " + troca.getCodigo()
                        + ". Falta a sua confirmacao.");
                con.commit();
                return Resultado.AGUARDANDO_OUTRO;
            }

            // Os dois confirmaram: executa a transferencia.
            carregarItens(con, troca);

            if (!transferir(con, troca)) {
                con.rollback();
                return Resultado.FIGURINHA_INDISPONIVEL;
            }

            try (PreparedStatement ps = con.prepareStatement(
                    "UPDATE troca SET status = 'CONCLUIDA', data_conclusao = NOW() WHERE id_troca = ?")) {
                ps.setInt(1, idTroca);
                ps.executeUpdate();
            }

            notificacaoDAO.criar(con, troca.getProponente().getId(), "TROCA",
                    "Troca " + troca.getCodigo() + " concluida. As figurinhas ja estao no seu album.");
            notificacaoDAO.criar(con, troca.getReceptor().getId(), "TROCA",
                    "Troca " + troca.getCodigo() + " concluida. As figurinhas ja estao no seu album.");

            con.commit();
            return Resultado.EXECUTADA;

        } catch (SQLException e) {
            if (con != null) {
                try { con.rollback(); } catch (SQLException ignorada) { }
            }
            throw e;
        } finally {
            if (con != null) {
                try { con.setAutoCommit(true); con.close(); } catch (SQLException ignorada) { }
            }
        }
    }

    /**
     * Move cada figurinha do remetente para o outro lado.
     * Retorna false se alguem nao tem mais a figurinha prometida — nesse caso
     * quem chamou faz rollback e a troca inteira nao acontece.
     */
    private boolean transferir(Connection con, Troca troca) throws SQLException {
        int idProponente = troca.getProponente().getId();
        int idReceptor = troca.getReceptor().getId();

        String sql = """
                SELECT id_figurinha, id_usuario_remetente, quantidade
                  FROM troca_item WHERE id_troca = ?
                """;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, troca.getId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int idFigurinha = rs.getInt("id_figurinha");
                    int remetente = rs.getInt("id_usuario_remetente");
                    int quantidade = rs.getInt("quantidade");
                    int destinatario = (remetente == idProponente) ? idReceptor : idProponente;

                    if (!colecaoDAO.debitar(con, remetente, idFigurinha, quantidade)) {
                        return false;
                    }
                    for (int i = 0; i < quantidade; i++) {
                        colecaoDAO.creditar(con, destinatario, idFigurinha);
                    }
                }
            }
        }
        return true;
    }

    /** Recusa a proposta. Nenhuma figurinha se move. */
    public boolean recusar(int idTroca, int idUsuario) throws SQLException {
        String sql = """
                UPDATE troca SET status = 'RECUSADA'
                 WHERE id_troca = ?
                   AND status IN ('PENDENTE', 'ACEITA')
                   AND (id_usuario_proponente = ? OR id_usuario_receptor = ?)
                """;
        try (Connection con = ConexaoDB.obter();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idTroca);
            ps.setInt(2, idUsuario);
            ps.setInt(3, idUsuario);
            return ps.executeUpdate() > 0;
        }
    }

    /**
     * Cria uma proposta a partir de um match. O proponente ja entra confirmado
     * — quem propoe esta, por definicao, de acordo com a propria proposta.
     */
    public String propor(int idProponente, int idReceptor,
                         List<Integer> envioDoProponente,
                         List<Integer> envioDoReceptor) throws SQLException {

        if (idProponente == idReceptor) {
            throw new IllegalArgumentException("Nao e possivel trocar consigo mesmo.");
        }
        if (envioDoProponente.isEmpty() || envioDoReceptor.isEmpty()) {
            throw new IllegalArgumentException("A troca precisa de figurinhas nos dois lados.");
        }

        Connection con = null;
        try {
            con = ConexaoDB.obter();
            con.setAutoCommit(false);

            String codigo = gerarCodigo(con);
            int idTroca;

            String sql = """
                    INSERT INTO troca (codigo, id_usuario_proponente, id_usuario_receptor,
                                       status, confirmou_proponente, confirmou_receptor, data_expiracao)
                    VALUES (?, ?, ?, 'PENDENTE', TRUE, FALSE, DATE_ADD(NOW(), INTERVAL 24 HOUR))
                    """;
            try (PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, codigo);
                ps.setInt(2, idProponente);
                ps.setInt(3, idReceptor);
                ps.executeUpdate();
                try (ResultSet chaves = ps.getGeneratedKeys()) {
                    if (!chaves.next()) throw new SQLException("Banco nao devolveu o id da troca.");
                    idTroca = chaves.getInt(1);
                }
            }

            inserirItens(con, idTroca, idProponente, envioDoProponente);
            inserirItens(con, idTroca, idReceptor, envioDoReceptor);

            notificacaoDAO.criar(con, idReceptor, "TROCA",
                    "Nova proposta de troca (" + codigo + "): "
                    + envioDoProponente.size() + " figurinhas por " + envioDoReceptor.size() + ".");

            con.commit();
            return codigo;

        } catch (SQLException | RuntimeException e) {
            if (con != null) {
                try { con.rollback(); } catch (SQLException ignorada) { }
            }
            throw e;
        } finally {
            if (con != null) {
                try { con.setAutoCommit(true); con.close(); } catch (SQLException ignorada) { }
            }
        }
    }

    private void inserirItens(Connection con, int idTroca, int idRemetente, List<Integer> figurinhas)
            throws SQLException {
        String sql = """
                INSERT INTO troca_item (id_troca, id_figurinha, id_usuario_remetente, quantidade)
                VALUES (?, ?, ?, 1)
                """;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            for (int idFigurinha : figurinhas) {
                ps.setInt(1, idTroca);
                ps.setInt(2, idFigurinha);
                ps.setInt(3, idRemetente);
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    /** Codigo no padrao TRC-4187, garantidamente livre. */
    private String gerarCodigo(Connection con) throws SQLException {
        for (int tentativa = 0; tentativa < 30; tentativa++) {
            String codigo = "TRC-" + ThreadLocalRandom.current().nextInt(1000, 9999);
            try (PreparedStatement ps = con.prepareStatement(
                    "SELECT 1 FROM troca WHERE codigo = ?")) {
                ps.setString(1, codigo);
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) return codigo;
                }
            }
        }
        throw new SQLException("Nao foi possivel gerar um codigo de troca livre.");
    }

    // -----------------------------------------------------------------------

    private Troca travarTroca(Connection con, int idTroca) throws SQLException {
        try (PreparedStatement ps = con.prepareStatement(
                SELECT_TROCA + " WHERE t.id_troca = ? FOR UPDATE")) {
            ps.setInt(1, idTroca);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    /**
     * Separa os itens da troca pelo lado que envia.
     * A coluna id_usuario_remetente da troca_item e o que diz de qual lado
     * cada figurinha saiu — sem ela a tela bilateral nao existiria.
     */
    private void carregarItens(Connection con, Troca troca) throws SQLException {
        List<Figurinha> doProponente = new ArrayList<>();
        List<Figurinha> doReceptor = new ArrayList<>();

        String consulta = """
                SELECT f.id_figurinha, f.numero_album, f.nome_jogador, f.selecao,
                       f.sigla_selecao, f.posicao,
                       f.url_imagem_jogador, f.url_imagem_escudo,
                       r.id_raridade, r.nome AS raridade_nome, r.cor_hex,
                       r.valor_referencia, r.probabilidade, r.ordem,
                       ti.id_usuario_remetente AS remetente, ti.quantidade AS qtd_item
                  FROM troca_item ti
                  JOIN figurinha f ON f.id_figurinha = ti.id_figurinha
                  JOIN raridade  r ON r.id_raridade  = f.id_raridade
                 WHERE ti.id_troca = ?
                 ORDER BY r.ordem DESC, f.nome_jogador
                """;

        try (PreparedStatement ps = con.prepareStatement(consulta)) {
            ps.setInt(1, troca.getId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Figurinha f = FigurinhaDAO.mapear(rs);
                    f.setQuantidade(rs.getInt("qtd_item"));
                    if (rs.getInt("remetente") == troca.getProponente().getId()) {
                        doProponente.add(f);
                    } else {
                        doReceptor.add(f);
                    }
                }
            }
        }
        troca.setItensProponente(doProponente);
        troca.setItensReceptor(doReceptor);
    }

    private Troca mapear(ResultSet rs) throws SQLException {
        Troca t = new Troca();
        t.setId(rs.getInt("id_troca"));
        t.setCodigo(rs.getString("codigo"));
        t.setStatus(rs.getString("status"));
        t.setConfirmouProponente(rs.getBoolean("confirmou_proponente"));
        t.setConfirmouReceptor(rs.getBoolean("confirmou_receptor"));

        Timestamp criacao = rs.getTimestamp("data_criacao");
        if (criacao != null) t.setDataCriacao(criacao.toLocalDateTime());
        Timestamp expiracao = rs.getTimestamp("data_expiracao");
        if (expiracao != null) t.setDataExpiracao(expiracao.toLocalDateTime());
        Timestamp conclusao = rs.getTimestamp("data_conclusao");
        if (conclusao != null) t.setDataConclusao(conclusao.toLocalDateTime());

        t.setProponente(usuarioDoPrefixo(rs, "p_"));
        t.setReceptor(usuarioDoPrefixo(rs, "d_"));
        return t;
    }

    private Usuario usuarioDoPrefixo(ResultSet rs, String prefixo) throws SQLException {
        Usuario u = new Usuario();
        u.setId(rs.getInt(prefixo + "id"));
        u.setNome(rs.getString(prefixo + "nome"));
        u.setEmail(rs.getString(prefixo + "email"));
        u.setSenhaHash(rs.getString(prefixo + "hash"));
        u.setTipoConta(rs.getString(prefixo + "tipo"));
        u.setReputacaoMedia(rs.getBigDecimal(prefixo + "rep"));
        Timestamp cad = rs.getTimestamp(prefixo + "cad");
        if (cad != null) u.setDataCadastro(cad.toLocalDateTime());
        u.setAtivo(rs.getBoolean(prefixo + "ativo"));
        return u;
    }
}
