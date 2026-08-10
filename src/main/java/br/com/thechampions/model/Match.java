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
     * Qualidade do match:
     *  - perfeito: os dois lados enviam a mesma quantidade e ninguem perde raridade
     *  - desigual: eu entrego mais raridade do que recebo
     *  - parcial: o resto
     */
    public String getQualidade() {
        if (euEnvio.isEmpty() || euRecebo.isEmpty()) return "parcial";
        if (somaRaridade(euEnvio) > somaRaridade(euRecebo)) return "desigual";
        if (euEnvio.size() == euRecebo.size()) return "perfeito";
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
