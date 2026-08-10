package br.com.thechampions.util;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tokens de acesso da API.
 *
 * As telas JSP usam HttpSession com cookie. A vitrine estatica nao pode: ela
 * roda em outro dominio (github.io) e o cookie de sessao nao acompanha uma
 * requisicao cross-site sem SameSite=None+Secure — o que exigiria HTTPS
 * configurado antes de qualquer teste. O token no cabecalho Authorization
 * resolve isso sem depender do ambiente.
 *
 * Guardado em memoria de proposito: nao ha tabela de sessao no modelo e o
 * projeto e academico. Consequencia — reiniciar o Tomcat desloga todo mundo
 * da API, e num cenario com mais de uma instancia cada uma teria seus tokens.
 * Para producao de verdade isso viraria uma tabela ou um Redis.
 */
public final class Tokens {

    /** Oito horas: cobre uma sessao de uso sem deixar token vivo por dias. */
    private static final long VALIDADE_SEGUNDOS = 8 * 60 * 60;

    private static final SecureRandom SORTEIO = new SecureRandom();
    private static final Map<String, Sessao> ATIVOS = new ConcurrentHashMap<>();

    private record Sessao(int idUsuario, Instant expiraEm) {
        boolean venceu() {
            return Instant.now().isAfter(expiraEm);
        }
    }

    private Tokens() {
    }

    /** Cria um token novo para o usuario. */
    public static String emitir(int idUsuario) {
        limparVencidos();
        byte[] bytes = new byte[32];
        SORTEIO.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        ATIVOS.put(token, new Sessao(idUsuario, Instant.now().plusSeconds(VALIDADE_SEGUNDOS)));
        return token;
    }

    /** Id do usuario dono do token, ou 0 se o token for invalido ou vencido. */
    public static int idDe(String token) {
        if (token == null || token.isBlank()) return 0;
        Sessao s = ATIVOS.get(token);
        if (s == null) return 0;
        if (s.venceu()) {
            ATIVOS.remove(token);
            return 0;
        }
        return s.idUsuario();
    }

    public static void revogar(String token) {
        if (token != null) ATIVOS.remove(token);
    }

    /**
     * Sem isto o mapa so cresce: token vencido nunca mais e consultado, entao
     * nada o removeria. Roda a cada emissao, que e barato e raro o bastante.
     */
    private static void limparVencidos() {
        ATIVOS.entrySet().removeIf(e -> e.getValue().venceu());
    }
}
