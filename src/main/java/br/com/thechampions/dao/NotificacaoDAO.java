package br.com.thechampions.dao;

import br.com.thechampions.config.ConexaoDB;
import br.com.thechampions.model.Notificacao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/** Feed de avisos do dashboard. */
public class NotificacaoDAO {

    public List<Notificacao> listar(int idUsuario, int limite) throws SQLException {
        String sql = """
                SELECT id_notificacao, tipo, mensagem, lida, data_criacao
                  FROM notificacao
                 WHERE id_usuario = ?
                 ORDER BY lida ASC, data_criacao DESC
                 LIMIT ?
                """;
        List<Notificacao> lista = new ArrayList<>();
        try (Connection con = ConexaoDB.obter();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            ps.setInt(2, limite);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Notificacao n = new Notificacao();
                    n.setId(rs.getInt("id_notificacao"));
                    n.setTipo(rs.getString("tipo"));
                    n.setMensagem(rs.getString("mensagem"));
                    n.setLida(rs.getBoolean("lida"));
                    Timestamp t = rs.getTimestamp("data_criacao");
                    if (t != null) n.setDataCriacao(t.toLocalDateTime());
                    lista.add(n);
                }
            }
        }
        return lista;
    }

    public int contarNaoLidas(int idUsuario) throws SQLException {
        String sql = "SELECT COUNT(*) FROM notificacao WHERE id_usuario = ? AND lida = FALSE";
        try (Connection con = ConexaoDB.obter();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    /** Usada dentro das transacoes de troca e pacote — por isso recebe a conexao. */
    public void criar(Connection con, int idUsuario, String tipo, String mensagem) throws SQLException {
        String sql = "INSERT INTO notificacao (id_usuario, tipo, mensagem, lida) VALUES (?, ?, ?, FALSE)";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            ps.setString(2, tipo);
            ps.setString(3, mensagem);
            ps.executeUpdate();
        }
    }
}
