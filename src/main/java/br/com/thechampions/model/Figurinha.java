package br.com.thechampions.model;

/**
 * Item do catalogo do album.
 *
 * "quantidade" nao pertence ao catalogo em si — e o quanto o usuario da vez
 * possui desta figurinha. Fica aqui porque toda tela que mostra uma figurinha
 * mostra tambem o estado dela para quem esta olhando: 0 = faltante,
 * 1 = obtida, >1 = repetida.
 */
public class Figurinha {

    private int id;
    private String numeroAlbum;
    private String nomeJogador;
    private String selecao;
    private String siglaSelecao;
    private String posicao;
    private Raridade raridade;

    private int quantidade;

    /** Marcada quando a figurinha acabou de sair de um pacote. */
    private boolean novaNoAlbum;

    private String urlImagemJogador;
    private String urlImagemEscudo;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNumeroAlbum() { return numeroAlbum; }
    public void setNumeroAlbum(String numeroAlbum) { this.numeroAlbum = numeroAlbum; }

    public String getNomeJogador() { return nomeJogador; }
    public void setNomeJogador(String nomeJogador) { this.nomeJogador = nomeJogador; }

    public String getSelecao() { return selecao; }
    public void setSelecao(String selecao) { this.selecao = selecao; }

    public String getSiglaSelecao() { return siglaSelecao; }
    public void setSiglaSelecao(String siglaSelecao) { this.siglaSelecao = siglaSelecao; }

    public String getPosicao() { return posicao; }
    public void setPosicao(String posicao) { this.posicao = posicao; }

    public Raridade getRaridade() { return raridade; }
    public void setRaridade(Raridade raridade) { this.raridade = raridade; }

    public int getQuantidade() { return quantidade; }
    public void setQuantidade(int quantidade) { this.quantidade = quantidade; }

    public boolean isNovaNoAlbum() { return novaNoAlbum; }
    public void setNovaNoAlbum(boolean novaNoAlbum) { this.novaNoAlbum = novaNoAlbum; }

    public boolean isObtida()  { return quantidade > 0; }
    public boolean isFaltante(){ return quantidade == 0; }
    public boolean isRepetida(){ return quantidade > 1; }

    public String getUrlImagemJogador() { return urlImagemJogador; }
    public void setUrlImagemJogador(String urlImagemJogador) { this.urlImagemJogador = urlImagemJogador; }

    public String getUrlImagemEscudo() { return urlImagemEscudo; }
    public void setUrlImagemEscudo(String urlImagemEscudo) { this.urlImagemEscudo = urlImagemEscudo; }

    /** Quantas copias sobrando existem para ofertar em troca. */
    public int getExcedente() { return Math.max(0, quantidade - 1); }

    /** Abreviacao da posicao usada nas etiquetas das cartas. */
    public String getPosicaoCurta() {
        if (posicao == null) return "ESC";
        return switch (posicao) {
            case "Goleiro"  -> "GOL";
            case "Zagueiro" -> "ZAG";
            case "Lateral"  -> "LAT";
            case "Volante"  -> "VOL";
            case "Meia"     -> "MEI";
            case "Atacante" -> "ATA";
            default -> posicao.substring(0, Math.min(3, posicao.length())).toUpperCase();
        };
    }

    /** Escudos de selecao nao tem posicao — mudam o desenho da carta. */
    public boolean isEscudo() { return posicao == null; }
}
