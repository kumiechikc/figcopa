package br.com.thechampions.dao;

import br.com.thechampions.config.ConexaoDB;
import br.com.thechampions.model.Figurinha;
import br.com.thechampions.model.Pacote;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

/** Pacotes do usuario e a regra de sorteio das figurinhas. */
public class PacoteDAO {

    private final ColecaoDAO colecaoDAO = new ColecaoDAO();
    private final NotificacaoDAO notificacaoDAO = new NotificacaoDAO();

    public List<Pacote> listarNaoAbertos(int idUsuario) throws SQLException {
        String sql = """
                SELECT id_pacote, id_usuario, tipo, qtd_figurinhas, aberto, data_criacao, data_abertura
                  FROM pacote
                 WHERE id_usuario = ? AND aberto = FALSE
                 ORDER BY FIELD(tipo, 'ESPECIAL', 'PADRAO', 'DIARIO'), data_criacao
                """;
        return consultarPacotes(sql, idUsuario);
    }

    /** Historico "ultimas aberturas" da coluna esquerda da tela de pacotes. */
    public List<Pacote> listarAbertos(int idUsuario, int limite) throws SQLException {
        String sql = """
                SELECT p.id_pacote, p.id_usuario, p.tipo, p.qtd_figurinhas, p.aberto,
                       p.data_criacao, p.data_abertura
                  FROM pacote p
                 WHERE p.id_usuario = ? AND p.aberto = TRUE
                 ORDER BY p.data_abertura DESC
                 LIMIT %d
                """.formatted(limite);

        List<Pacote> pacotes = consultarPacotes(sql, idUsuario);
        for (Pacote p : pacotes) {
            p.setConteudo(conteudoDoPacote(p.getId()));
        }
        return pacotes;
    }

    public int contarDisponiveis(int idUsuario) throws SQLException {
        String sql = "SELECT COUNT(*) FROM pacote WHERE id_usuario = ? AND aberto = FALSE";
        try (Connection con = ConexaoDB.obter();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    public Pacote buscar(int idPacote) throws SQLException {
        String sql = """
                SELECT id_pacote, id_usuario, tipo, qtd_figurinhas, aberto, data_criacao, data_abertura
                  FROM pacote WHERE id_pacote = ?
                """;
        try (Connection con = ConexaoDB.obter();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idPacote);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    // =======================================================================
    // ABERTURA DE PACOTE — regra de negocio central da Etapa 4
    // =======================================================================

    /**
     * Abre o pacote e devolve o que saiu.
     *
     * Tudo acontece dentro de UMA transacao: se o sorteio grava as figurinhas
     * mas falha ao marcar o pacote como aberto, o usuario abriria o mesmo
     * pacote de novo e ganharia figurinhas de graca. O rollback impede isso.
     *
     * @throws IllegalStateException se o pacote nao existe, nao e do usuario
     *                               ou ja foi aberto
     */
    public Pacote abrir(int idPacote, int idUsuario) throws SQLException {
        Connection con = null;
        try {
            con = ConexaoDB.obter();
            con.setAutoCommit(false);

            // SELECT ... FOR UPDATE trava a linha: dois cliques rapidos no botao
            // "Abrir" nao conseguem abrir o mesmo pacote duas vezes.
            Pacote pacote = travarPacote(con, idPacote);
            if (pacote == null) {
                throw new IllegalStateException("Pacote nao encontrado.");
            }
            if (pacote.getIdUsuario() != idUsuario) {
                throw new IllegalStateException("Este pacote nao pertence a voce.");
            }
            if (pacote.isAberto()) {
                throw new IllegalStateException("Este pacote ja foi aberto.");
            }

            List<Integer> sorteadas = sortear(con, pacote);

            List<Figurinha> conteudo = new ArrayList<>();
            for (int idFigurinha : sorteadas) {
                boolean eraNova = colecaoDAO.creditar(con, idUsuario, idFigurinha);
                registrarItem(con, idPacote, idFigurinha, eraNova);

                Figurinha f = buscarFigurinha(con, idFigurinha);
                f.setNovaNoAlbum(eraNova);
                conteudo.add(f);
            }

            try (PreparedStatement ps = con.prepareStatement(
                    "UPDATE pacote SET aberto = TRUE, data_abertura = NOW() WHERE id_pacote = ?")) {
                ps.setInt(1, idPacote);
                ps.executeUpdate();
            }

            long novas = conteudo.stream().filter(Figurinha::isNovaNoAlbum).count();
            notificacaoDAO.criar(con, idUsuario, "PACOTE",
                    pacote.getTipoExibicao() + " aberto: " + novas + " figurinhas novas no album.");

            con.commit();

            pacote.setAberto(true);
            pacote.setConteudo(conteudo);
            return pacote;

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

    /**
     * Sorteia as figurinhas do pacote respeitando a coluna "probabilidade" da
     * tabela raridade.
     *
     * Como funciona: as probabilidades do banco (0,78 / 0,15 / 0,04 / 0,0053)
     * nao somam exatamente 1, entao normalizamos pela soma real e rodamos uma
     * roleta acumulada. Sorteia-se primeiro a RARIDADE e so depois uma
     * figurinha qualquer daquela raridade — assim uma lendaria nao fica mais
     * provavel so porque existem poucas cadastradas.
     *
     * Regra extra do Pacote Especial: se nada de Rara ou Lendaria saiu, a
     * ultima figurinha e trocada por uma Rara, cumprindo a promessa da tela.
     */
    private List<Integer> sortear(Connection con, Pacote pacote) throws SQLException {
        Map<Integer, List<Integer>> catalogoPorOrdem = new HashMap<>();
        Map<Integer, Double> pesos = new HashMap<>();

        String sqlRaridades = "SELECT ordem, probabilidade FROM raridade ORDER BY ordem";
        try (PreparedStatement ps = con.prepareStatement(sqlRaridades);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                pesos.put(rs.getInt("ordem"), rs.getDouble("probabilidade"));
            }
        }

        FigurinhaDAO figurinhaDAO = new FigurinhaDAO();
        for (int ordem : pesos.keySet()) {
            catalogoPorOrdem.put(ordem, figurinhaDAO.idsPorOrdemDeRaridade(con, ordem));
        }

        double somaPesos = pesos.values().stream().mapToDouble(Double::doubleValue).sum();
        ThreadLocalRandom sorteio = ThreadLocalRandom.current();

        List<Integer> resultado = new ArrayList<>();
        for (int i = 0; i < pacote.getQtdFigurinhas(); i++) {
            int ordemSorteada = girarRoleta(pesos, somaPesos, sorteio.nextDouble());
            resultado.add(escolherFigurinha(catalogoPorOrdem, ordemSorteada, sorteio));
        }

        if ("ESPECIAL".equals(pacote.getTipo())) {
            boolean temAltaRaridade = resultado.stream()
                    .anyMatch(id -> ordemDaFigurinha(catalogoPorOrdem, id) >= 3);
            if (!temAltaRaridade && !resultado.isEmpty()) {
                resultado.set(resultado.size() - 1,
                        escolherFigurinha(catalogoPorOrdem, 3, sorteio));
            }
        }

        return resultado;
    }

    /** Roleta acumulada: percorre as faixas ate o sorteio cair dentro de uma. */
    private int girarRoleta(Map<Integer, Double> pesos, double somaPesos, double sorteado) {
        double alvo = sorteado * somaPesos;
        double acumulado = 0;
        for (int ordem = 1; ordem <= 4; ordem++) {
            acumulado += pesos.getOrDefault(ordem, 0.0);
            if (alvo <= acumulado) return ordem;
        }
        return 1;
    }

    /** Se a raridade sorteada nao tiver figurinhas no catalogo, cai para comum. */
    private int escolherFigurinha(Map<Integer, List<Integer>> catalogo, int ordem,
                                  ThreadLocalRandom sorteio) {
        List<Integer> candidatas = catalogo.get(ordem);
        if (candidatas == null || candidatas.isEmpty()) {
            candidatas = catalogo.get(1);
        }
        return candidatas.get(sorteio.nextInt(candidatas.size()));
    }

    private int ordemDaFigurinha(Map<Integer, List<Integer>> catalogo, int idFigurinha) {
        for (Map.Entry<Integer, List<Integer>> e : catalogo.entrySet()) {
            if (e.getValue().contains(idFigurinha)) return e.getKey();
        }
        return 1;
    }

    // -----------------------------------------------------------------------

    private Pacote travarPacote(Connection con, int idPacote) throws SQLException {
        String sql = """
                SELECT id_pacote, id_usuario, tipo, qtd_figurinhas, aberto, data_criacao, data_abertura
                  FROM pacote WHERE id_pacote = ? FOR UPDATE
                """;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idPacote);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    private void registrarItem(Connection con, int idPacote, int idFigurinha, boolean eraNova)
            throws SQLException {
        String sql = "INSERT INTO pacote_item (id_pacote, id_figurinha, era_nova) VALUES (?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idPacote);
            ps.setInt(2, idFigurinha);
            ps.setBoolean(3, eraNova);
            ps.executeUpdate();
        }
    }

    private Figurinha buscarFigurinha(Connection con, int id) throws SQLException {
        String sql = FigurinhaDAO.SELECT_BASE + " WHERE f.id_figurinha = ?";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new SQLException("Figurinha " + id + " sumiu do catalogo.");
                return FigurinhaDAO.mapear(rs);
            }
        }
    }

    private List<Figurinha> conteudoDoPacote(int idPacote) throws SQLException {
        String sql = FigurinhaDAO.SELECT_BASE + """
                  JOIN pacote_item pi ON pi.id_figurinha = f.id_figurinha
                 WHERE pi.id_pacote = ?
                 ORDER BY pi.id_pacote_item
                """;
        List<Figurinha> lista = new ArrayList<>();
        try (Connection con = ConexaoDB.obter();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idPacote);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(FigurinhaDAO.mapear(rs));
            }
        }
        return lista;
    }

    private List<Pacote> consultarPacotes(String sql, int idUsuario) throws SQLException {
        List<Pacote> lista = new ArrayList<>();
        try (Connection con = ConexaoDB.obter();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(mapear(rs));
            }
        }
        return lista;
    }

    private Pacote mapear(ResultSet rs) throws SQLException {
        Pacote p = new Pacote();
        p.setId(rs.getInt("id_pacote"));
        p.setIdUsuario(rs.getInt("id_usuario"));
        p.setTipo(rs.getString("tipo"));
        p.setQtdFigurinhas(rs.getInt("qtd_figurinhas"));
        p.setAberto(rs.getBoolean("aberto"));
        Timestamp criacao = rs.getTimestamp("data_criacao");
        if (criacao != null) p.setDataCriacao(criacao.toLocalDateTime());
        Timestamp abertura = rs.getTimestamp("data_abertura");
        if (abertura != null) p.setDataAbertura(abertura.toLocalDateTime());
        return p;
    }
}
