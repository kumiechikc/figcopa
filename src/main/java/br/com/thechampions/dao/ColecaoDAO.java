package br.com.thechampions.dao;

import br.com.thechampions.config.ConexaoDB;
import br.com.thechampions.model.Figurinha;
import br.com.thechampions.model.ProgressoSelecao;
import br.com.thechampions.model.ResumoColecao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Acesso a tabela colecao — o que cada usuario possui e em que quantidade. */
public class ColecaoDAO {

    /**
     * Numeros do topo do dashboard em uma unica consulta.
     * As somas condicionais evitam quatro idas ao banco so para contar raridades.
     */
    public ResumoColecao resumo(int idUsuario) throws SQLException {
        String sql = """
                SELECT (SELECT COUNT(*) FROM figurinha) AS total_catalogo,
                       COUNT(c.id_colecao) AS obtidas,
                       COALESCE(SUM(GREATEST(c.quantidade - 1, 0)), 0) AS repetidas,
                       COALESCE(SUM(CASE WHEN r.ordem = 2 THEN 1 ELSE 0 END), 0) AS brilhantes,
                       COALESCE(SUM(CASE WHEN r.ordem = 3 THEN 1 ELSE 0 END), 0) AS raras,
                       COALESCE(SUM(CASE WHEN r.ordem = 4 THEN 1 ELSE 0 END), 0) AS lendarias,
                       COALESCE(SUM(r.valor_referencia * c.quantidade), 0) AS valor
                  FROM colecao c
                  JOIN figurinha f ON f.id_figurinha = c.id_figurinha
                  JOIN raridade  r ON r.id_raridade  = f.id_raridade
                 WHERE c.id_usuario = ? AND c.quantidade > 0
                """;

        ResumoColecao resumo = new ResumoColecao();
        try (Connection con = ConexaoDB.obter();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    resumo.setTotalCatalogo(rs.getInt("total_catalogo"));
                    resumo.setObtidas(rs.getInt("obtidas"));
                    resumo.setRepetidasDisponiveis(rs.getInt("repetidas"));
                    resumo.setBrilhantes(rs.getInt("brilhantes"));
                    resumo.setRaras(rs.getInt("raras"));
                    resumo.setLendarias(rs.getInt("lendarias"));
                    BigDecimal valor = rs.getBigDecimal("valor");
                    resumo.setValorEstimado(valor == null ? BigDecimal.ZERO : valor);
                }
            }
        }
        resumo.setFaltantes(resumo.getTotalCatalogo() - resumo.getObtidas());
        return resumo;
    }

    /** Barra de progresso de cada selecao no painel esquerdo do album. */
    public List<ProgressoSelecao> progressoPorSelecao(int idUsuario) throws SQLException {
        String sql = """
                SELECT f.selecao, f.sigla_selecao,
                       COUNT(*) AS total,
                       COUNT(c.id_colecao) AS obtidas
                  FROM figurinha f
                  LEFT JOIN colecao c
                         ON c.id_figurinha = f.id_figurinha
                        AND c.id_usuario = ?
                        AND c.quantidade > 0
                 GROUP BY f.selecao, f.sigla_selecao
                 ORDER BY obtidas DESC, f.selecao
                """;
        List<ProgressoSelecao> lista = new ArrayList<>();
        try (Connection con = ConexaoDB.obter();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ProgressoSelecao p = new ProgressoSelecao();
                    p.setSelecao(rs.getString("selecao"));
                    p.setSigla(rs.getString("sigla_selecao"));
                    p.setTotal(rs.getInt("total"));
                    p.setObtidas(rs.getInt("obtidas"));
                    lista.add(p);
                }
            }
        }
        return lista;
    }

    /** Repetidas do usuario (quantidade > 1) — o que ele pode ofertar. */
    public List<Figurinha> listarRepetidas(int idUsuario) throws SQLException {
        String sql = FigurinhaDAO.SELECT_BASE + """
                  JOIN colecao c ON c.id_figurinha = f.id_figurinha
                 WHERE c.id_usuario = ? AND c.quantidade > 1
                 ORDER BY r.ordem DESC, c.quantidade DESC, f.nome_jogador
                """;
        List<Figurinha> lista = new ArrayList<>();
        try (Connection con = ConexaoDB.obter();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) lista.add(FigurinhaDAO.mapear(rs));
            }
        }
        return lista;
    }

    // -----------------------------------------------------------------------
    // Operacoes que participam de transacoes maiores (abertura de pacote e
    // execucao de troca). Recebem a Connection de fora de proposito: quem abriu
    // a transacao e quem decide commit ou rollback.
    // -----------------------------------------------------------------------

    /**
     * Credita uma figurinha na colecao do usuario.
     * Retorna true se ela entrou NOVA no album (nao existia antes).
     *
     * O ON DUPLICATE KEY aproveita a UNIQUE (id_usuario, id_figurinha) do schema:
     * uma instrucao so resolve INSERT ou incremento, sem SELECT previo.
     */
    public boolean creditar(Connection con, int idUsuario, int idFigurinha) throws SQLException {
        boolean eraNova = !possui(con, idUsuario, idFigurinha);

        String sql = """
                INSERT INTO colecao (id_usuario, id_figurinha, quantidade)
                VALUES (?, ?, 1)
                ON DUPLICATE KEY UPDATE quantidade = quantidade + 1
                """;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            ps.setInt(2, idFigurinha);
            ps.executeUpdate();
        }
        return eraNova;
    }

    /**
     * Debita uma copia da colecao. Se sobrar zero, remove a linha — assim a
     * figurinha volta a contar como faltante no album.
     * Retorna false se o usuario nao tinha a figurinha (troca invalida).
     */
    public boolean debitar(Connection con, int idUsuario, int idFigurinha, int quantidade)
            throws SQLException {

        String sqlAtual = "SELECT quantidade FROM colecao WHERE id_usuario = ? AND id_figurinha = ?";
        int atual;
        try (PreparedStatement ps = con.prepareStatement(sqlAtual)) {
            ps.setInt(1, idUsuario);
            ps.setInt(2, idFigurinha);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return false;
                atual = rs.getInt(1);
            }
        }
        if (atual < quantidade) return false;

        if (atual - quantidade == 0) {
            try (PreparedStatement ps = con.prepareStatement(
                    "DELETE FROM colecao WHERE id_usuario = ? AND id_figurinha = ?")) {
                ps.setInt(1, idUsuario);
                ps.setInt(2, idFigurinha);
                ps.executeUpdate();
            }
        } else {
            try (PreparedStatement ps = con.prepareStatement(
                    "UPDATE colecao SET quantidade = quantidade - ? WHERE id_usuario = ? AND id_figurinha = ?")) {
                ps.setInt(1, quantidade);
                ps.setInt(2, idUsuario);
                ps.setInt(3, idFigurinha);
                ps.executeUpdate();
            }
        }
        return true;
    }

    private boolean possui(Connection con, int idUsuario, int idFigurinha) throws SQLException {
        String sql = "SELECT 1 FROM colecao WHERE id_usuario = ? AND id_figurinha = ? AND quantidade > 0";
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            ps.setInt(2, idFigurinha);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }
}
