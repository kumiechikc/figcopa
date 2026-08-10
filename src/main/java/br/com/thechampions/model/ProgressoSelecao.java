package br.com.thechampions.model;

/** Linha da lista de selecoes do album: quantas figurinhas o usuario ja tem. */
public class ProgressoSelecao {

    private String selecao;
    private String sigla;
    private int total;
    private int obtidas;

    public String getSelecao() { return selecao; }
    public void setSelecao(String selecao) { this.selecao = selecao; }

    public String getSigla() { return sigla; }
    public void setSigla(String sigla) { this.sigla = sigla; }

    public int getTotal() { return total; }
    public void setTotal(int total) { this.total = total; }

    public int getObtidas() { return obtidas; }
    public void setObtidas(int obtidas) { this.obtidas = obtidas; }

    public int getPercentual() {
        return total == 0 ? 0 : (int) Math.round(obtidas * 100.0 / total);
    }

    public boolean isCompleta() { return total > 0 && obtidas == total; }
}
