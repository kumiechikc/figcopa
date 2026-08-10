package br.com.thechampions.model;

import br.com.thechampions.util.Tempo;

import java.time.LocalDateTime;

/** Aviso do feed do dashboard. */
public class Notificacao {

    private int id;
    private String tipo;
    private String mensagem;
    private boolean lida;
    private LocalDateTime dataCriacao;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getMensagem() { return mensagem; }
    public void setMensagem(String mensagem) { this.mensagem = mensagem; }

    public boolean isLida() { return lida; }
    public void setLida(boolean lida) { this.lida = lida; }

    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public void setDataCriacao(LocalDateTime d) { this.dataCriacao = d; }

    /** Icone do sprite conforme o tipo do aviso. */
    public String getIcone() {
        return switch (tipo) {
            case "TROCA"     -> "i-troca";
            case "PACOTE"    -> "i-presente";
            case "AVALIACAO" -> "i-estrela";
            case "MATCH"     -> "i-brilho";
            default          -> "i-info";
        };
    }

    public String getQuando() { return Tempo.relativo(dataCriacao); }
}
