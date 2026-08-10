package br.com.thechampions.servlet;

import br.com.thechampions.config.ConexaoDB;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Pagina de diagnostico da Etapa 1: prova que Java, Tomcat, driver JDBC e MySQL
 * estao conversando. Se esta tela mostrar 51 figurinhas, a fundacao esta de pe.
 *
 * Deixada no projeto de proposito: e a primeira coisa a abrir quando o ambiente
 * de outra maquina nao sobe.
 */
@WebServlet("/diagnostico")
public class DiagnosticoServlet extends HttpServlet {

    /** Contagens esperadas apos importar 01_schema.sql e 02_dados.sql. */
    private static final Map<String, Integer> ESPERADO = Map.of(
            "figurinha", 51,
            "usuario", 6,
            "colecao", 63,
            "troca", 4);

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        Map<String, Integer> contagens = new LinkedHashMap<>();
        String erro = null;

        String sql = """
                SELECT (SELECT COUNT(*) FROM usuario)   AS usuarios,
                       (SELECT COUNT(*) FROM figurinha) AS figurinhas,
                       (SELECT COUNT(*) FROM colecao)   AS itens_colecao,
                       (SELECT COUNT(*) FROM troca)     AS trocas
                """;

        try (Connection con = ConexaoDB.obter();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            if (rs.next()) {
                contagens.put("usuario", rs.getInt("usuarios"));
                contagens.put("figurinha", rs.getInt("figurinhas"));
                contagens.put("colecao", rs.getInt("itens_colecao"));
                contagens.put("troca", rs.getInt("trocas"));
            }
        } catch (Exception e) {
            erro = e.getMessage();
        }

        req.setAttribute("contagens", contagens);
        req.setAttribute("esperado", ESPERADO);
        req.setAttribute("erro", erro);
        req.setAttribute("origemConfig", ConexaoDB.origemDaConfiguracao());
        req.setAttribute("urlBanco", ConexaoDB.urlVisivel());
        req.setAttribute("versaoJava", System.getProperty("java.version"));
        req.setAttribute("servidor", getServletContext().getServerInfo());

        req.getRequestDispatcher("/WEB-INF/jsp/diagnostico.jsp").forward(req, resp);
    }
}
