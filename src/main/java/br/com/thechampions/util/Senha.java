package br.com.thechampions.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Hash de senha com BCrypt (jBCrypt).
 *
 * Requisito nao-funcional da Etapa 1: senha nunca em texto puro. BCrypt embute
 * o salt no proprio hash e tem custo configuravel, o que o torna resistente a
 * ataque de forca bruta — diferente de um SHA-256 simples.
 */
public final class Senha {

    /** Custo 10: ~100ms por verificacao. Suficiente e imperceptivel no login. */
    private static final int CUSTO = 10;

    private Senha() {
    }

    public static String gerarHash(String senhaPura) {
        return BCrypt.hashpw(senhaPura, BCrypt.gensalt(CUSTO));
    }

    /**
     * Confere a senha digitada contra o hash guardado.
     * Retorna false em vez de estourar excecao quando o hash do banco esta
     * malformado — sem isso, um registro ruim derrubaria a tela de login.
     */
    public static boolean confere(String senhaPura, String hash) {
        if (senhaPura == null || hash == null || hash.isBlank()) return false;
        try {
            return BCrypt.checkpw(senhaPura, hash);
        } catch (IllegalArgumentException e) {
            System.err.println("[Senha] Hash invalido no banco: " + e.getMessage());
            return false;
        }
    }
}
