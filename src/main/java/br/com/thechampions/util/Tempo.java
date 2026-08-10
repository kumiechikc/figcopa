package br.com.thechampions.util;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Formatacao de datas para a interface.
 *
 * As telas usam timestamps variados de proposito ("há 6 min", "ontem, 21:47",
 * "sex, 11:20") — e o tipo de ruido que faz o sistema parecer real em vez de
 * uma lista de datas identicas.
 */
public final class Tempo {

    private static final DateTimeFormatter HORA =
            DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter DIA_SEMANA_HORA =
            DateTimeFormatter.ofPattern("EEE, HH:mm", new Locale("pt", "BR"));
    private static final DateTimeFormatter DATA_HORA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm");
    private static final DateTimeFormatter DATA_CURTA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private Tempo() {
    }

    /** "agora", "há 6 min", "há 3 h", "ontem, 21:47", "sex, 11:20", "12/06/2026". */
    public static String relativo(LocalDateTime quando) {
        if (quando == null) return "—";

        LocalDateTime agora = LocalDateTime.now();
        Duration d = Duration.between(quando, agora);

        if (d.isNegative()) return quando.format(HORA);

        long minutos = d.toMinutes();
        if (minutos < 1)  return "agora";
        if (minutos < 60) return "há " + minutos + " min";

        long horas = d.toHours();
        if (horas < 24 && quando.toLocalDate().equals(agora.toLocalDate())) {
            return "hoje, " + quando.format(HORA);
        }
        if (quando.toLocalDate().equals(agora.toLocalDate().minusDays(1))) {
            return "ontem, " + quando.format(HORA);
        }
        if (d.toDays() < 7) {
            return quando.format(DIA_SEMANA_HORA).toLowerCase(new Locale("pt", "BR"));
        }
        return quando.format(DATA_CURTA);
    }

    public static String dataHora(LocalDateTime quando) {
        return quando == null ? "—" : quando.format(DATA_HORA);
    }

    public static String hora(LocalDateTime quando) {
        return quando == null ? "—" : quando.format(HORA);
    }

    public static String data(LocalDate quando) {
        return quando == null ? "—" : quando.format(DATA_CURTA);
    }

    /** "membro desde fev/2026" — usado no cartao do parceiro de troca. */
    public static String mesAno(LocalDateTime quando) {
        if (quando == null) return "";
        return quando.format(DateTimeFormatter.ofPattern("MMM/yyyy", new Locale("pt", "BR")));
    }
}
