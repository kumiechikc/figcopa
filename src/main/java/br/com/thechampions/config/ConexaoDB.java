package br.com.thechampions.config;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Ponto unico de acesso ao banco. Toda conexao JDBC do sistema nasce aqui.
 *
 * As credenciais NAO ficam no codigo: sao lidas de "database.properties", que
 * esta no .gitignore. Se o arquivo nao existir, caem os valores padrao do XAMPP
 * (root / senha vazia) — isso deixa o projeto rodavel logo apos o clone, sem
 * expor nenhuma senha real no repositorio.
 */
public final class ConexaoDB {

    private static final String ARQUIVO_CONFIG = "database.properties";

    private static final String URL_PADRAO =
            "jdbc:mysql://localhost:3306/the_champions"
            + "?useSSL=false&allowPublicKeyRetrieval=true"
            + "&serverTimezone=America/Sao_Paulo&characterEncoding=UTF-8";
    private static final String USUARIO_PADRAO = "root";
    private static final String SENHA_PADRAO = "";

    private static final String url;
    private static final String usuario;
    private static final String senha;
    private static final boolean usandoArquivo;

    static {
        Properties p = new Properties();
        boolean carregou = false;

        try (InputStream in = ConexaoDB.class.getClassLoader()
                .getResourceAsStream(ARQUIVO_CONFIG)) {
            if (in != null) {
                p.load(in);
                carregou = true;
            }
        } catch (IOException e) {
            // Arquivo ilegivel: seguimos com o padrao XAMPP em vez de derrubar a aplicacao.
            System.err.println("[ConexaoDB] Falha ao ler " + ARQUIVO_CONFIG + ": " + e.getMessage());
        }

        usandoArquivo = carregou;
        url = p.getProperty("db.url", URL_PADRAO);
        usuario = p.getProperty("db.usuario", USUARIO_PADRAO);
        senha = p.getProperty("db.senha", SENHA_PADRAO);

        try {
            // Registro explicito do driver: em alguns Tomcat o autodiscovery do
            // Connector/J nao acontece quando o jar esta em WEB-INF/lib.
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new ExceptionInInitializerError(
                    "Driver JDBC do MySQL nao encontrado. Confira se o mysql-connector-j "
                    + "esta em WEB-INF/lib (o Maven coloca ali automaticamente).");
        }
    }

    private ConexaoDB() {
    }

    /** Abre uma conexao nova. Quem chama e responsavel por fechar (use try-with-resources). */
    public static Connection obter() throws SQLException {
        return DriverManager.getConnection(url, usuario, senha);
    }

    /** Usado pela pagina de diagnostico para mostrar de onde vieram as credenciais. */
    public static String origemDaConfiguracao() {
        return usandoArquivo
                ? "database.properties (classpath)"
                : "valores padrao do XAMPP (database.properties nao encontrado)";
    }

    /** URL do banco sem usuario/senha — seguro para exibir em tela de diagnostico. */
    public static String urlVisivel() {
        int corte = url.indexOf('?');
        return corte > 0 ? url.substring(0, corte) : url;
    }
}
