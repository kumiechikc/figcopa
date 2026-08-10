package br.com.thechampions.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Resultado do matching automatico: um parceiro compativel e as duas listas
 * da troca proposta. "faltantesResolvidas" e o argumento de venda da tela —
 * quantos buracos do meu album essa troca fecha.
 */
public class Match {

    private Usuario parceiro;
    private List<Figurinha> euEnvio = new ArrayList<>();
    private List<Figurinha> euRecebo = new ArrayList<>();
    private int trocasDoParceiro;

    public Usuario getParceiro() { return parceiro; }
    public void setParceiro(Usuario parceiro) { this.parceiro = parceiro; }

    public List<Figurinha> getEuEnvio() { return euEnvio; }
    public void setEuEnvio(List<Figurinha> l) { this.euEnvio = l; }

    public List<Figurinha> getEuRecebo() { return euRecebo; }
    public void setEuRecebo(List<Figurinha> l) { this.euRecebo = l; }

    public int getTrocasDoParceiro() { return trocasDoParceiro; }
    public void setTrocasDoParceiro(int t) { this.trocasDoParceiro = t; }

    public int getFaltantesResolvidas() { return euRecebo.size(); }

    /**
     * Qualidade do match, medida em "pontos de raridade" (comum=1 ... lendaria=4):
     *
     *  - desigual: eu entrego 2 pontos ou mais do que recebo. E o caso que a
     *    tela precisa sinalizar — trocar uma Rara por uma Comum, por exemplo.
     *  - perfeito: mesma quantidade dos dois lados e valor equivalente
     *    (diferenca de no maximo 1 ponto, que e ruido normal).
     *  - parcial: o resto — a troca ajuda, mas nao fecha simetrica.
     *
     * A tolerancia de 1 ponto existe de proposito: sem ela quase toda troca
     * cairia em "desigual", e um alerta que aparece sempre nao alerta nada.
     */
    private static final int DESEQUILIBRIO_QUE_MERECE_ALERTA = 2;

    public String getQualidade() {
        if (euEnvio.isEmpty() || euRecebo.isEmpty()) return "parcial";

        int diferenca = somaRaridade(euEnvio) - somaRaridade(euRecebo);
        if (diferenca >= DESEQUILIBRIO_QUE_MERECE_ALERTA) return "desigual";
        if (euEnvio.size() == euRecebo.size() && Math.abs(diferenca) <= 1) return "perfeito";
        return "parcial";
    }

    public boolean isPerfeito() { return "perfeito".equals(getQualidade()); }
    public boolean isDesigual() { return "desigual".equals(getQualidade()); }

    private int somaRaridade(List<Figurinha> lista) {
        return lista.stream()
                .mapToInt(f -> f.getRaridade() == null ? 1 : f.getRaridade().getOrdem())
                .sum();
    }
}
