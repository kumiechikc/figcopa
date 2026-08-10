package br.com.thechampions.model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidade central do sistema: liga dois usuarios e as figurinhas de cada lado.
 *
 * A regra que define o produto esta na dupla confirmacao — enquanto os dois
 * booleanos nao forem TRUE, nenhuma figurinha muda de dono.
 */
public class Troca {

    private int id;
    private String codigo;
    private Usuario proponente;
    private Usuario receptor;
    private String status;
    private boolean confirmouProponente;
    private boolean confirmouReceptor;
    private LocalDateTime dataCriacao;
    private LocalDateTime dataExpiracao;
    private LocalDateTime dataConclusao;

    /** Figurinhas enviadas por cada lado, ja separadas por remetente. */
    private List<Figurinha> itensProponente = new ArrayList<>();
    private List<Figurinha> itensReceptor = new ArrayList<>();

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public Usuario getProponente() { return proponente; }
    public void setProponente(Usuario proponente) { this.proponente = proponente; }

    public Usuario getReceptor() { return receptor; }
    public void setReceptor(Usuario receptor) { this.receptor = receptor; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public boolean isConfirmouProponente() { return confirmouProponente; }
    public void setConfirmouProponente(boolean v) { this.confirmouProponente = v; }

    public boolean isConfirmouReceptor() { return confirmouReceptor; }
    public void setConfirmouReceptor(boolean v) { this.confirmouReceptor = v; }

    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public void setDataCriacao(LocalDateTime d) { this.dataCriacao = d; }

    public LocalDateTime getDataExpiracao() { return dataExpiracao; }
    public void setDataExpiracao(LocalDateTime d) { this.dataExpiracao = d; }

    public LocalDateTime getDataConclusao() { return dataConclusao; }
    public void setDataConclusao(LocalDateTime d) { this.dataConclusao = d; }

    public List<Figurinha> getItensProponente() { return itensProponente; }
    public void setItensProponente(List<Figurinha> l) { this.itensProponente = l; }

    public List<Figurinha> getItensReceptor() { return itensReceptor; }
    public void setItensReceptor(List<Figurinha> l) { this.itensReceptor = l; }

    public boolean isPendente()  { return "PENDENTE".equals(status); }
    public boolean isConcluida() { return "CONCLUIDA".equals(status); }
    public boolean isRecusada()  { return "RECUSADA".equals(status); }

    /** Classe da etiqueta de status na lista de trocas. */
    public String getClasseTag() {
        return switch (status) {
            case "CONCLUIDA" -> "tag-sucesso";
            case "PENDENTE", "ACEITA" -> "tag-aviso";
            default -> "tag-erro";
        };
    }

    public String getStatusExibicao() {
        return switch (status) {
            case "CONCLUIDA" -> "concluída";
            case "PENDENTE"  -> "aguardando";
            case "ACEITA"    -> "aceita";
            case "RECUSADA"  -> "recusada";
            default          -> "cancelada";
        };
    }

    /** Descricao curta "3 × 3" usada no resumo da negociacao. */
    public String getPlacar() {
        return itensProponente.size() + " × " + itensReceptor.size();
    }

    public boolean isAmbosConfirmaram() { return confirmouProponente && confirmouReceptor; }

    /** true se o usuario informado e o proponente desta troca. */
    public boolean ehProponente(int idUsuario) {
        return proponente != null && proponente.getId() == idUsuario;
    }

    /** Quanto falta expirar, no formato "22h 10min". Vazio se nao expira. */
    public String getTempoParaExpirar() {
        if (dataExpiracao == null) return "";
        Duration d = Duration.between(LocalDateTime.now(), dataExpiracao);
        if (d.isNegative() || d.isZero()) return "expirada";
        long horas = d.toHours();
        long minutos = d.toMinutesPart();
        return horas > 0 ? horas + "h " + minutos + "min" : minutos + "min";
    }
}
