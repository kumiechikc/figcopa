package br.com.thechampions.model;

/**
 * Numeros do topo do dashboard e do album: progresso geral e quebra por raridade.
 * Tudo vem de uma consulta so — nao vale somar isso em laco Java.
 */
public class ResumoColecao {

    private int totalCatalogo;
    private int obtidas;
    private int faltantes;
    private int repetidasDisponiveis;
    private int brilhantes;
    private int raras;
    private int lendarias;
    private java.math.BigDecimal valorEstimado = java.math.BigDecimal.ZERO;

    public int getTotalCatalogo() { return totalCatalogo; }
    public void setTotalCatalogo(int totalCatalogo) { this.totalCatalogo = totalCatalogo; }

    public int getObtidas() { return obtidas; }
    public void setObtidas(int obtidas) { this.obtidas = obtidas; }

    public int getFaltantes() { return faltantes; }
    public void setFaltantes(int faltantes) { this.faltantes = faltantes; }

    public int getRepetidasDisponiveis() { return repetidasDisponiveis; }
    public void setRepetidasDisponiveis(int r) { this.repetidasDisponiveis = r; }

    public int getBrilhantes() { return brilhantes; }
    public void setBrilhantes(int brilhantes) { this.brilhantes = brilhantes; }

    public int getRaras() { return raras; }
    public void setRaras(int raras) { this.raras = raras; }

    public int getLendarias() { return lendarias; }
    public void setLendarias(int lendarias) { this.lendarias = lendarias; }

    public java.math.BigDecimal getValorEstimado() { return valorEstimado; }
    public void setValorEstimado(java.math.BigDecimal v) { this.valorEstimado = v; }

    public int getPercentual() {
        return totalCatalogo == 0 ? 0 : (int) Math.round(obtidas * 100.0 / totalCatalogo);
    }
}
