package br.com.thechampions.dao;

import br.com.thechampions.config.ConexaoDB;
import br.com.thechampions.model.Figurinha;
import br.com.thechampions.model.Match;
import br.com.thechampions.model.Usuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Matching automatico — o coracao do sistema P2P.
 *
 * A ideia e a mesma da troca no patio da escola: so vale a pena conversar com
 * quem tem o que me falta E precisa do que me sobra. As duas condicoes juntas.
 *
 * A colecao e a fonte da verdade (nao a tabela oferta): "faltante" e
 * simplesmente nao ter linha em colecao, e "repetida" e quantidade > 1. Assim
 * o match funciona mesmo para quem nunca publicou uma oferta manualmente.
 */
public class MatchDAO {

    /** Quantas figurinhas cada lado envia numa proposta sugerida. */
    private static final int ITENS_POR_LADO = 3;

    /**
     * O que EU posso enviar: minhas repetidas que o parceiro nao tem.
     * Ordenado da menor para a maior raridade — a proposta sugerida comeca
     * pelas comuns, como faria qualquer colecionador negociando de verdade.
     */
    private static final String SQL_EU_ENVIO = """
            SELECT u.id_usuario AS parceiro,
                   f.id_figurinha, f.numero_album, f.nome_jogador, f.selecao,
                   f.sigla_selecao, f.posicao,
                   r.id_raridade, r.nome AS raridade_nome, r.cor_hex,
                   r.valor_referencia, r.probabilidade, r.ordem
              FROM colecao minha
              JOIN figurinha f ON f.id_figurinha = minha.id_figurinha
              JOIN raridade  r ON r.id_raridade  = f.id_raridade
              JOIN usuario   u ON u.id_usuario <> minha.id_usuario
                              AND u.ativo = TRUE
                              AND u.tipo_conta = 'COMUM'
             WHERE minha.id_usuario = ?
               AND minha.quantidade > 1
               AND NOT EXISTS (
                     SELECT 1 FROM colecao dele
                      WHERE dele.id_usuario = u.id_usuario
                        AND dele.id_figurinha = f.id_figurinha
                        AND dele.quantidade > 0)
             ORDER BY u.id_usuario, r.ordem ASC, f.nome_jogador
            """;

    /** O que EU recebo: repetidas do parceiro que faltam no meu album. */
    private static final String SQL_EU_RECEBO = """
            SELECT u.id_usuario, u.id_usuario AS parceiro, u.nome, u.email, u.senha_hash,
                   u.tipo_conta, u.reputacao_media, u.data_cadastro, u.ativo,
                   f.id_figurinha, f.numero_album, f.nome_jogador, f.selecao,
                   f.sigla_selecao, f.posicao,
                   r.id_raridade, r.nome AS raridade_nome, r.cor_hex,
                   r.valor_referencia, r.probabilidade, r.ordem
              FROM colecao dele
              JOIN usuario   u ON u.id_usuario   = dele.id_usuario
              JOIN figurinha f ON f.id_figurinha = dele.id_figurinha
              JOIN raridade  r ON r.id_raridade  = f.id_raridade
             WHERE dele.id_usuario <> ?
               AND dele.quantidade > 1
               AND u.ativo = TRUE
               AND u.tipo_conta = 'COMUM'
               AND NOT EXISTS (
                     SELECT 1 FROM colecao minha
                      WHERE minha.id_usuario = ?
                        AND minha.id_figurinha = f.id_figurinha
                        AND minha.quantidade > 0)
             ORDER BY u.id_usuario, r.ordem DESC, f.nome_jogador
            """;

    /**
     * Lista os parceiros compativeis, ja com a proposta de troca montada.
     * Ordena pelos que resolvem mais faltantes minhas.
     */
    public List<Match> buscarMatches(int idUsuario, Integer raridadeMinima,
                                     String siglaSelecao) throws SQLException {

        Map<Integer, List<Figurinha>> possoEnviar = new LinkedHashMap<>();
        Map<Integer, List<Figurinha>> possoReceber = new LinkedHashMap<>();
        Map<Integer, Usuario> parceiros = new LinkedHashMap<>();

        try (Connection con = ConexaoDB.obter()) {

            try (PreparedStatement ps = con.prepareStatement(SQL_EU_ENVIO)) {
                ps.setInt(1, idUsuario);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        possoEnviar.computeIfAbsent(rs.getInt("parceiro"), k -> new ArrayList<>())
                                   .add(FigurinhaDAO.mapear(rs));
                    }
                }
            }

            try (PreparedStatement ps = con.prepareStatement(SQL_EU_RECEBO)) {
                ps.setInt(1, idUsuario);
                ps.setInt(2, idUsuario);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        int idParceiro = rs.getInt("parceiro");
                        Figurinha f = FigurinhaDAO.mapear(rs);

                        // Filtros da coluna direita da tela.
                        if (raridadeMinima != null && f.getRaridade().getOrdem() < raridadeMinima) continue;
                        if (siglaSelecao != null && !siglaSelecao.isBlank()
                                && !siglaSelecao.equals(f.getSiglaSelecao())) continue;

                        possoReceber.computeIfAbsent(idParceiro, k -> new ArrayList<>()).add(f);
                        if (!parceiros.containsKey(idParceiro)) {
                            parceiros.put(idParceiro, UsuarioDAO.mapear(rs));
                        }
                    }
                }
            }

            List<Match> matches = new ArrayList<>();
            for (Map.Entry<Integer, List<Figurinha>> e : possoReceber.entrySet()) {
                int idParceiro = e.getKey();
                List<Figurinha> envio = possoEnviar.get(idParceiro);

                // Sem os dois lados nao ha troca — este e o filtro que define o produto.
                if (envio == null || envio.isEmpty()) continue;

                Match m = new Match();
                m.setParceiro(parceiros.get(idParceiro));
                m.setEuRecebo(limitar(e.getValue()));
                m.setEuEnvio(limitar(envio));
                m.setTrocasDoParceiro(contarTrocas(con, idParceiro));
                matches.add(m);
            }

            matches.sort(Comparator
                    .comparingInt(Match::getFaltantesResolvidas).reversed()
                    .thenComparing(m -> m.getParceiro().getReputacaoMedia(), Comparator.reverseOrder()));

            return matches;
        }
    }

    /** Uma proposta grande demais nao cabe na tela nem fecha facil. */
    private List<Figurinha> limitar(List<Figurinha> lista) {
        return lista.size() <= ITENS_POR_LADO ? lista : new ArrayList<>(lista.subList(0, ITENS_POR_LADO));
    }

    private int contarTrocas(Connection con, int idUsuario) throws SQLException {
        String sql = """
                SELECT COUNT(*) FROM troca
                 WHERE status = 'CONCLUIDA'
                   AND (id_usuario_proponente = ? OR id_usuario_receptor = ?)
                """;
        try (PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            ps.setInt(2, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }
}
