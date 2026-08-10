package br.com.thechampions.util;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Escritor de JSON minimo.
 *
 * O projeto nao usa framework nem ORM; puxar Jackson so para devolver quatro
 * listas seria a unica dependencia pesada do .war. Aqui basta serializar —
 * ninguem precisa ler JSON de volta.
 *
 * Uso:
 *   Json.obj().put("nome", u.getNome()).put("id", u.getId()).toString()
 *   Json.lista(figurinhas, Json::daFigurinha)
 */
public final class Json {

    private final StringBuilder sb = new StringBuilder("{");
    private boolean primeiro = true;

    private Json() {
    }

    public static Json obj() {
        return new Json();
    }

    public Json put(String chave, Object valor) {
        if (!primeiro) sb.append(',');
        primeiro = false;
        sb.append(texto(chave)).append(':').append(valorDe(valor));
        return this;
    }

    /** Insere um trecho ja serializado (objeto ou lista) sem escapar de novo. */
    public Json bruto(String chave, String jsonPronto) {
        if (!primeiro) sb.append(',');
        primeiro = false;
        sb.append(texto(chave)).append(':').append(jsonPronto == null ? "null" : jsonPronto);
        return this;
    }

    @Override
    public String toString() {
        return sb + "}";
    }

    /** Serializa uma colecao aplicando o conversor item a item. */
    public static <T> String lista(Collection<T> itens, Function<T, String> conversor) {
        if (itens == null) return "[]";
        StringBuilder out = new StringBuilder("[");
        boolean primeiro = true;
        for (T item : itens) {
            if (!primeiro) out.append(',');
            primeiro = false;
            out.append(conversor.apply(item));
        }
        return out.append(']').toString();
    }

    private static String valorDe(Object valor) {
        if (valor == null) return "null";
        if (valor instanceof Number || valor instanceof Boolean) return valor.toString();
        if (valor instanceof BigDecimal bd) return bd.toPlainString();
        return texto(valor.toString());
    }

    /**
     * Escapa uma string para JSON. Alem das aspas e da barra, escapa os
     * caracteres de controle (< 0x20): um \n cru dentro de uma string quebra o
     * parse no navegador.
     */
    static String texto(String valor) {
        StringBuilder out = new StringBuilder(valor.length() + 2).append('"');
        for (int i = 0; i < valor.length(); i++) {
            char c = valor.charAt(i);
            switch (c) {
                case '"'  -> out.append("\\\"");
                case '\\' -> out.append("\\\\");
                case '\n' -> out.append("\\n");
                case '\r' -> out.append("\\r");
                case '\t' -> out.append("\\t");
                default -> {
                    if (c < 0x20) out.append(String.format("\\u%04x", (int) c));
                    else out.append(c);
                }
            }
        }
        return out.append('"').toString();
    }

    // ------------------------------------------------------------- conversores
    //
    // Os nomes dos campos seguem o que a vitrine (webapp-static) ja consome:
    // mudar um nome aqui quebra o components.js do outro lado.

    public static String daRaridade(br.com.thechampions.model.Raridade r) {
        return obj()
                .put("id", r.getId())
                .put("ordem", r.getOrdem())
                .put("nome", r.getNome())
                .put("nomeExibicao", r.getNomeExibicao())
                .put("corHex", r.getCorHex())
                .put("valorReferencia", r.getValorReferencia())
                .put("probabilidade", r.getProbabilidade())
                .toString();
    }

    public static String daFigurinha(br.com.thechampions.model.Figurinha f) {
        return obj()
                .put("id", f.getId())
                .put("numeroAlbum", f.getNumeroAlbum())
                .put("nomeJogador", f.getNomeJogador())
                .put("selecao", f.getSelecao())
                .put("siglaSelecao", f.getSiglaSelecao())
                .put("posicao", f.getPosicao())
                .put("escudo", f.isEscudo())
                .put("quantidade", f.getQuantidade())
                .put("novaNoAlbum", f.isNovaNoAlbum())
                .put("urlImagemJogador", f.getUrlImagemJogador())
                .put("urlImagemEscudo", f.getUrlImagemEscudo())
                .bruto("raridade", daRaridade(f.getRaridade()))
                .toString();
    }

    public static String doUsuario(br.com.thechampions.model.Usuario u) {
        // Sem senhaHash: este objeto vai para o navegador.
        return obj()
                .put("id", u.getId())
                .put("nome", u.getNome())
                .put("email", u.getEmail())
                .put("iniciais", u.getIniciais())
                .put("reputacao", u.getReputacaoMedia())
                .put("trocasConcluidas", u.getTrocasConcluidas())
                .put("figurinhasObtidas", u.getFigurinhasObtidas())
                .put("membroDesde", u.getMembroDesde())
                .toString();
    }

    public static String doPacote(br.com.thechampions.model.Pacote p) {
        return obj()
                .put("id", p.getId())
                .put("tipo", p.getTipo())
                .put("nome", p.getTipoExibicao())
                .put("descricao", p.getDescricao())
                .put("cartas", p.getQtdFigurinhas())
                .put("aberto", p.isAberto())
                .put("disponivel", !p.isAberto())
                .toString();
    }

    /**
     * Um match nao tem codigo: ele e uma sugestao, nao uma troca gravada. O
     * codigo so nasce quando a proposta e criada (POST /api/trocas/propor), e e
     * por isso que aqui vai o id do parceiro no lugar dele.
     */
    public static String doMatch(br.com.thechampions.model.Match m) {
        return obj()
                .put("idParceiro", m.getParceiro().getId())
                .put("nome", m.getParceiro().getNome())
                .put("reputacao", m.getParceiro().getReputacaoMedia())
                .put("qualidade", m.getQualidade())
                .put("trocasDoParceiro", m.getTrocasDoParceiro())
                .bruto("recebo", lista(m.getEuRecebo(), Json::daFigurinha))
                .bruto("envio", lista(m.getEuEnvio(), Json::daFigurinha))
                .toString();
    }

    /**
     * "parceiro" e sempre o outro lado, do ponto de vista de quem pediu: a mesma
     * troca aparece como "com Pedro" para mim e "com Vinicius" para ele. Sem o id
     * de quem consulta nao da para decidir isso, por isso ele vem por parametro.
     */
    public static String daTroca(br.com.thechampions.model.Troca t, int idDeQuemVe) {
        boolean souProponente = t.getProponente().getId() == idDeQuemVe;
        var parceiro = souProponente ? t.getReceptor() : t.getProponente();

        return obj()
                .put("codigo", t.getCodigo())
                .put("status", t.getStatus())
                .put("statusExibicao", t.getStatusExibicao())
                .put("quando", t.getQuando())
                .put("parceiro", parceiro.getNome())
                .put("recebidas", t.getItensReceptor() == null ? 0 : t.getItensReceptor().size())
                .put("enviadas", t.getItensProponente() == null ? 0 : t.getItensProponente().size())
                .bruto("proponente", doUsuario(t.getProponente()))
                .bruto("receptor", doUsuario(t.getReceptor()))
                .toString();
    }

    public static String daNotificacao(br.com.thechampions.model.Notificacao n) {
        return obj()
                .put("tipo", n.getTipo())
                .put("texto", n.getMensagem())
                .put("quando", n.getQuando())
                .put("lida", n.isLida())
                .toString();
    }

    /**
     * O formato de "porRaridade" segue o que o dashboard da vitrine ja consumia
     * dos dados de exemplo: cada item traz a raridade inteira, porque a tela usa
     * dela o nome de exibicao E a cor da barra. Emitir so o nome aqui deixava a
     * API respondendo 200 e a tela quebrando em "nomeExibicao de undefined".
     */
    public static String doResumo(br.com.thechampions.model.ResumoColecao r,
                                  Map<br.com.thechampions.model.Raridade, int[]> porRaridade,
                                  java.math.BigDecimal valorEstimado) {
        Json j = obj()
                .put("total", r.getTotalCatalogo())
                .put("obtidas", r.getObtidas())
                .put("faltantes", r.getFaltantes())
                .put("repetidas", r.getRepetidasDisponiveis())
                .put("percentual", r.getPercentual())
                .put("valorEstimado", valorEstimado);

        StringBuilder arr = new StringBuilder("[");
        boolean primeiro = true;
        for (Map.Entry<br.com.thechampions.model.Raridade, int[]> e : porRaridade.entrySet()) {
            if (!primeiro) arr.append(',');
            primeiro = false;
            arr.append(obj()
                    .bruto("raridade", daRaridade(e.getKey()))
                    .put("obtidas", e.getValue()[0])
                    .put("total", e.getValue()[1]));
        }
        return j.bruto("porRaridade", arr.append(']').toString()).toString();
    }

    /** Mapa simples chave->valor, usado nas respostas de erro e de status. */
    public static String deMapa(LinkedHashMap<String, Object> mapa) {
        Json j = obj();
        mapa.forEach(j::put);
        return j.toString();
    }
}
