package br.com.thechampions.model;

import java.math.BigDecimal;

/**
 * Classificacao da figurinha. E o nucleo visual do produto: a cor da moldura
 * comunica o valor sem depender de texto.
 */
public class Raridade {

    private int id;
    private String nome;
    private String corHex;
    private BigDecimal valorReferencia;
    private BigDecimal probabilidade;
    private int ordem;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getCorHex() { return corHex; }
    public void setCorHex(String corHex) { this.corHex = corHex; }

    public BigDecimal getValorReferencia() { return valorReferencia; }
    public void setValorReferencia(BigDecimal valorReferencia) { this.valorReferencia = valorReferencia; }

    public BigDecimal getProbabilidade() { return probabilidade; }
    public void setProbabilidade(BigDecimal probabilidade) { this.probabilidade = probabilidade; }

    public int getOrdem() { return ordem; }
    public void setOrdem(int ordem) { this.ordem = ordem; }

    /** Sufixo usado nas classes CSS: r1 (comum) ... r4 (lendaria). */
    public String getClasseCss() { return "r" + ordem; }

    /** Nome da variante de etiqueta no CSS (tag-comum, tag-lendaria, ...). */
    public String getClasseTag() {
        return switch (ordem) {
            case 2 -> "tag-brilhante";
            case 3 -> "tag-rara";
            case 4 -> "tag-lendaria";
            default -> "tag-comum";
        };
    }

    /** Nome acentuado para exibicao (o banco guarda sem acento). */
    public String getNomeExibicao() {
        return switch (ordem) {
            case 4 -> "Lendária";
            case 3 -> "Rara";
            case 2 -> "Brilhante";
            default -> "Comum";
        };
    }

    public boolean isLendaria() { return ordem == 4; }
}
