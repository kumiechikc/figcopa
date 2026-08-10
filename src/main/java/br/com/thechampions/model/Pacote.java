package br.com.thechampions.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Pacote de figurinhas. Fechado ate o usuario abrir; depois guarda o conteudo. */
public class Pacote {

    private int id;
    private int idUsuario;
    private String tipo;
    private int qtdFigurinhas;
    private boolean aberto;
    private LocalDateTime dataCriacao;
    private LocalDateTime dataAbertura;

    /** Preenchido apos a abertura: o que saiu, na ordem sorteada. */
    private List<Figurinha> conteudo = new ArrayList<>();

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getIdUsuario() { return idUsuario; }
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public int getQtdFigurinhas() { return qtdFigurinhas; }
    public void setQtdFigurinhas(int qtdFigurinhas) { this.qtdFigurinhas = qtdFigurinhas; }

    public boolean isAberto() { return aberto; }
    public void setAberto(boolean aberto) { this.aberto = aberto; }

    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public void setDataCriacao(LocalDateTime dataCriacao) { this.dataCriacao = dataCriacao; }

    public LocalDateTime getDataAbertura() { return dataAbertura; }
    public void setDataAbertura(LocalDateTime dataAbertura) { this.dataAbertura = dataAbertura; }

    public List<Figurinha> getConteudo() { return conteudo; }
    public void setConteudo(List<Figurinha> conteudo) { this.conteudo = conteudo; }

    public String getTipoExibicao() {
        return switch (tipo) {
            case "ESPECIAL" -> "Pacote Especial";
            case "DIARIO"   -> "Pacote Diário";
            default          -> "Pacote Padrão";
        };
    }

    public String getClasseArte() { return tipo.toLowerCase(); }

    /** "hoje, 12:04" / "ontem, 19:31" para o historico de aberturas. */
    public String getAbertoEm() {
        return br.com.thechampions.util.Tempo.relativo(dataAbertura);
    }

    /** Texto da promessa de cada tipo, mostrado na lista de pacotes. */
    public String getDescricao() {
        return switch (tipo) {
            case "ESPECIAL" -> qtdFigurinhas + " figurinhas · garante ≥ 1 Rara · 5% de chance de Lendária.";
            case "DIARIO"   -> qtdFigurinhas + " figurinhas · creditado uma vez por dia por login.";
            default          -> qtdFigurinhas + " figurinhas aleatórias · 15% de chance de Brilhante.";
        };
    }

    /** Quantas do conteudo entraram novas no album. */
    public long getQtdNovas() {
        return conteudo.stream().filter(Figurinha::isNovaNoAlbum).count();
    }

    public long getQtdRepetidas() {
        return conteudo.size() - getQtdNovas();
    }

    public boolean isTemLendaria() {
        return conteudo.stream().anyMatch(f -> f.getRaridade() != null && f.getRaridade().isLendaria());
    }
}
