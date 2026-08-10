package br.com.thechampions.dao;

import br.com.thechampions.config.ConexaoDB;
import br.com.thechampions.model.Usuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/** Acesso a tabela usuario. */
public class UsuarioDAO {

    private static final String COLUNAS =
            "id_usuario, nome, email, senha_hash, tipo_conta, reputacao_media, data_cadastro, ativo";

    public Usuario buscarPorEmail(String email) throws SQLException {
        String sql = "SELECT " + COLUNAS + " FROM usuario WHERE email = ? AND ativo = TRUE";
        try (Connection con = ConexaoDB.obter();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    public Usuario buscarPorId(int id) throws SQLException {
        String sql = "SELECT " + COLUNAS + " FROM usuario WHERE id_usuario = ?";
        try (Connection con = ConexaoDB.obter();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    public boolean emailJaExiste(String email) throws SQLException {
        String sql = "SELECT 1 FROM usuario WHERE email = ?";
        try (Connection con = ConexaoDB.obter();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /**
     * Cadastra o usuario e ja credita um pacote de boas-vindas, para que a
     * primeira tela apos o cadastro nao seja um album completamente vazio.
     * As duas gravacoes acontecem na mesma transacao.
     */
    public int inserir(Usuario u) throws SQLException {
        String sqlUsuario = """
                INSERT INTO usuario (nome, email, senha_hash, tipo_conta, reputacao_media)
                VALUES (?, ?, ?, 'COMUM', 0.00)
                """;
        String sqlPacote = "INSERT INTO pacote (id_usuario, tipo, qtd_figurinhas, aberto) VALUES (?, 'ESPECIAL', 7, FALSE)";
        String sqlAviso = """
                INSERT INTO notificacao (id_usuario, tipo, mensagem, lida)
                VALUES (?, 'PACOTE', 'Bem-vindo ao The Champions. Voce ganhou um Pacote Especial de boas-vindas.', FALSE)
                """;

        Connection con = null;
        try {
            con = ConexaoDB.obter();
            con.setAutoCommit(false);

            int novoId;
            try (PreparedStatement ps = con.prepareStatement(sqlUsuario, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, u.getNome());
                ps.setString(2, u.getEmail());
                ps.setString(3, u.getSenhaHash());
                ps.executeUpdate();
                try (ResultSet chaves = ps.getGeneratedKeys()) {
                    if (!chaves.next()) throw new SQLException("Banco nao devolveu o id do usuario criado.");
                    novoId = chaves.getInt(1);
                }
            }

            try (PreparedStatement ps = con.prepareStatement(sqlPacote)) {
                ps.setInt(1, novoId);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = con.prepareStatement(sqlAviso)) {
                ps.setInt(1, novoId);
                ps.executeUpdate();
            }

            con.commit();
            return novoId;

        } catch (SQLException e) {
            if (con != null) con.rollback();
            throw e;
        } finally {
            if (con != null) {
                con.setAutoCommit(true);
                con.close();
            }
        }
    }

    /**
     * Ranking de colecionadores: ordena por figurinhas distintas no album e,
     * em empate, por trocas concluidas.
     */
    public List<Usuario> listarRanking(int limite) throws SQLException {
        String sql = """
                SELECT u.id_usuario, u.nome, u.email, u.senha_hash, u.tipo_conta,
                       u.reputacao_media, u.data_cadastro, u.ativo,
                       (SELECT COUNT(*) FROM colecao c
                         WHERE c.id_usuario = u.id_usuario AND c.quantidade > 0) AS obtidas,
                       (SELECT COUNT(*) FROM troca t
                         WHERE t.status = 'CONCLUIDA'
                           AND (t.id_usuario_proponente = u.id_usuario
                                OR t.id_usuario_receptor = u.id_usuario)) AS trocas
                  FROM usuario u
                 WHERE u.ativo = TRUE AND u.tipo_conta = 'COMUM'
                 ORDER BY obtidas DESC, trocas DESC, u.reputacao_media DESC
                 LIMIT ?
                """;
        List<Usuario> lista = new ArrayList<>();
        try (Connection con = ConexaoDB.obter();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, limite);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Usuario u = mapear(rs);
                    u.setFigurinhasObtidas(rs.getInt("obtidas"));
                    u.setTrocasConcluidas(rs.getInt("trocas"));
                    lista.add(u);
                }
            }
        }
        return lista;
    }

    /** Trocas concluidas do usuario — usado no perfil e no cartao de parceiro. */
    public int contarTrocasConcluidas(int idUsuario) throws SQLException {
        String sql = """
                SELECT COUNT(*) FROM troca
                 WHERE status = 'CONCLUIDA'
                   AND (id_usuario_proponente = ? OR id_usuario_receptor = ?)
                """;
        try (Connection con = ConexaoDB.obter();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            ps.setInt(2, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    static Usuario mapear(ResultSet rs) throws SQLException {
        Usuario u = new Usuario();
        u.setId(rs.getInt("id_usuario"));
        u.setNome(rs.getString("nome"));
        u.setEmail(rs.getString("email"));
        u.setSenhaHash(rs.getString("senha_hash"));
        u.setTipoConta(rs.getString("tipo_conta"));
        u.setReputacaoMedia(rs.getBigDecimal("reputacao_media"));
        Timestamp t = rs.getTimestamp("data_cadastro");
        if (t != null) u.setDataCadastro(t.toLocalDateTime());
        u.setAtivo(rs.getBoolean("ativo"));
        return u;
    }
}
