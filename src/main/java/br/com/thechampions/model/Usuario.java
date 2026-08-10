package br.com.thechampions.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Colecionador da plataforma. O senhaHash nunca sai para a camada de view. */
public class Usuario {

    private int id;
    private String nome;
    private String email;
    private String senhaHash;
    private String tipoConta;
    private BigDecimal reputacaoMedia;
    private LocalDateTime dataCadastro;
    private boolean ativo;

    /** Campos calculados por consultas de agregacao (perfil, ranking). */
    private int trocasConcluidas;
    private int figurinhasObtidas;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getSenhaHash() { return senhaHash; }
    public void setSenhaHash(String senhaHash) { this.senhaHash = senhaHash; }

    public String getTipoConta() { return tipoConta; }
    public void setTipoConta(String tipoConta) { this.tipoConta = tipoConta; }

    public BigDecimal getReputacaoMedia() { return reputacaoMedia; }
    public void setReputacaoMedia(BigDecimal reputacaoMedia) { this.reputacaoMedia = reputacaoMedia; }

    public LocalDateTime getDataCadastro() { return dataCadastro; }
    public void setDataCadastro(LocalDateTime dataCadastro) { this.dataCadastro = dataCadastro; }

    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }

    public int getTrocasConcluidas() { return trocasConcluidas; }
    public void setTrocasConcluidas(int trocasConcluidas) { this.trocasConcluidas = trocasConcluidas; }

    public int getFigurinhasObtidas() { return figurinhasObtidas; }
    public void setFigurinhasObtidas(int figurinhasObtidas) { this.figurinhasObtidas = figurinhasObtidas; }

    public boolean isAdmin() { return "ADMIN".equals(tipoConta); }

    /** Iniciais para o avatar circular (ex.: "Vinicius K." -> "VK"). */
    public String getIniciais() {
        if (nome == null || nome.isBlank()) return "?";
        String[] partes = nome.trim().split("\\s+");
        StringBuilder sb = new StringBuilder();
        sb.append(Character.toUpperCase(partes[0].charAt(0)));
        if (partes.length > 1) {
            String ultima = partes[partes.length - 1];
            if (!ultima.isEmpty() && Character.isLetter(ultima.charAt(0))) {
                sb.append(Character.toUpperCase(ultima.charAt(0)));
            }
        }
        return sb.toString();
    }

    /** Apelido no estilo das telas: "vinicius.k" a partir do e-mail. */
    public String getApelido() {
        if (email == null) return "";
        int arroba = email.indexOf('@');
        return arroba > 0 ? email.substring(0, arroba) : email;
    }
}
