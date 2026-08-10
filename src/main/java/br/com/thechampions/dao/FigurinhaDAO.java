package br.com.thechampions.dao;

import br.com.thechampions.config.ConexaoDB;
import br.com.thechampions.model.Figurinha;
import br.com.thechampions.model.Raridade;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Acesso ao catalogo (figurinha + raridade). */
public class FigurinhaDAO {

    /** Colunas usadas por todas as consultas que montam uma Figurinha completa. */
    static final String SELECT_BASE = """
            SELECT f.id_figurinha, f.numero_album, f.nome_jogador, f.selecao,
                   f.sigla_selecao, f.posicao,
                   r.id_raridade, r.nome AS raridade_nome, r.cor_hex,
                   r.valor_referencia, r.probabilidade, r.ordem
              FROM figurinha f
              JOIN raridade r ON r.id_raridade = f.id_raridade
            """;

    public int contarTotal() throws SQLException {
        try (Connection con = ConexaoDB.obter();
             PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM figurinha");
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    public Figurinha buscarPorId(int id) throws SQLException {
        String sql = SELECT_BASE + " WHERE f.id_figurinha = ?";
        try (Connection con = ConexaoDB.obter();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapear(rs) : null;
            }
        }
    }

    public List<Raridade> listarRaridades() throws SQLException {
        String sql = """
                SELECT id_raridade, nome AS raridade_nome, cor_hex,
                       valor_referencia, probabilidade, ordem
                  FROM raridade ORDER BY ordem
                """;
        List<Raridade> lista = new ArrayList<>();
        try (Connection con = ConexaoDB.obter();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapearRaridade(rs));
        }
        return lista;
    }

    /**
     * Catalogo completo com a quantidade que o usuario possui de cada item.
     * LEFT JOIN: figurinha que o usuario nao tem vem com quantidade 0 (faltante).
     */
    public List<Figurinha> listarCatalogoCom(int idUsuario, String siglaSelecao,
                                             Integer ordemRaridade) throws SQLException {
        StringBuilder sql = new StringBuilder("""
                SELECT f.id_figurinha, f.numero_album, f.nome_jogador, f.selecao,
                       f.sigla_selecao, f.posicao,
                       r.id_raridade, r.nome AS raridade_nome, r.cor_hex,
                       r.valor_referencia, r.probabilidade, r.ordem,
                       COALESCE(c.quantidade, 0) AS quantidade
                  FROM figurinha f
                  JOIN raridade r ON r.id_raridade = f.id_raridade
                  LEFT JOIN colecao c ON c.id_figurinha = f.id_figurinha AND c.id_usuario = ?
                 WHERE 1 = 1
                """);

        List<Object> parametros = new ArrayList<>();
        parametros.add(idUsuario);

        if (siglaSelecao != null && !siglaSelecao.isBlank()) {
            sql.append(" AND f.sigla_selecao = ? ");
            parametros.add(siglaSelecao);
        }
        if (ordemRaridade != null) {
            sql.append(" AND r.ordem = ? ");
            parametros.add(ordemRaridade);
        }
        sql.append(" ORDER BY f.selecao, r.ordem DESC, f.numero_album ");

        List<Figurinha> lista = new ArrayList<>();
        try (Connection con = ConexaoDB.obter();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {
            for (int i = 0; i < parametros.size(); i++) {
                ps.setObject(i + 1, parametros.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Figurinha f = mapear(rs);
                    f.setQuantidade(rs.getInt("quantidade"));
                    lista.add(f);
                }
            }
        }
        return lista;
    }

    /** Figurinhas mais raras do catalogo, para a tela de valores de referencia. */
    public List<Figurinha> listarMaisRaras(int idUsuario, int limite) throws SQLException {
        String sql = """
                SELECT f.id_figurinha, f.numero_album, f.nome_jogador, f.selecao,
                       f.sigla_selecao, f.posicao,
                       r.id_raridade, r.nome AS raridade_nome, r.cor_hex,
                       r.valor_referencia, r.probabilidade, r.ordem,
                       COALESCE(c.quantidade, 0) AS quantidade
                  FROM figurinha f
                  JOIN raridade r ON r.id_raridade = f.id_raridade
                  LEFT JOIN colecao c ON c.id_figurinha = f.id_figurinha AND c.id_usuario = ?
                 ORDER BY r.ordem DESC, r.valor_referencia DESC, f.nome_jogador
                 LIMIT ?
                """;
        List<Figurinha> lista = new ArrayList<>();
        try (Connection con = ConexaoDB.obter();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            ps.setInt(2, limite);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Figurinha f = mapear(rs);
                    f.setQuantidade(rs.getInt("quantidade"));
                    lista.add(f);
                }
            }
        }
        return lista;
    }

    /** Todos os ids do catalogo agrupados por raridade — base do sorteio de pacote. */
    public List<Integer> idsPorOrdemDeRaridade(Connection con, int ordem) throws SQLException {
        String sql = """
                SELECT f.id_figurinha FROM figurinha f
                  JOIN raridade r ON r.id_raridade = f.id_raridade
                 WHERE r.ordem = ?
                """;
        List<Integer> ids = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, ordem);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) ids.add(rs.getInt(1));
            }
        }
        return ids;
    }

    static Figurinha mapear(ResultSet rs) throws SQLException {
        Figurinha f = new Figurinha();
        f.setId(rs.getInt("id_figurinha"));
        f.setNumeroAlbum(rs.getString("numero_album"));
        f.setNomeJogador(rs.getString("nome_jogador"));
        f.setSelecao(rs.getString("selecao"));
        f.setSiglaSelecao(rs.getString("sigla_selecao"));
        f.setPosicao(rs.getString("posicao"));
        f.setRaridade(mapearRaridade(rs));
        return f;
    }

    static Raridade mapearRaridade(ResultSet rs) throws SQLException {
        Raridade r = new Raridade();
        r.setId(rs.getInt("id_raridade"));
        r.setNome(rs.getString("raridade_nome"));
        r.setCorHex(rs.getString("cor_hex"));
        r.setValorReferencia(rs.getBigDecimal("valor_referencia"));
        r.setProbabilidade(rs.getBigDecimal("probabilidade"));
        r.setOrdem(rs.getInt("ordem"));
        return r;
    }
}
