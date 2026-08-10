package br.com.thechampions.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Set;

/**
 * Barra o acesso as telas internas de quem nao esta logado.
 *
 * Aplicado a "/*" e nao a cada servlet: assim uma tela nova nasce protegida por
 * padrao. Esquecer de proteger uma tela e mais facil do que lembrar de liberar
 * uma publica.
 */
@WebFilter("/*")
public class AutenticacaoFilter implements Filter {

    /** Caminhos que qualquer visitante pode abrir. */
    private static final Set<String> PUBLICOS = Set.of(
            "/", "/index.jsp",
            "/login", "/cadastro", "/logout",
            "/diagnostico");

    private static final Set<String> PREFIXOS_PUBLICOS = Set.of(
            "/assets/");

    @Override
    public void doFilter(ServletRequest requisicao, ServletResponse resposta, FilterChain corrente)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) requisicao;
        HttpServletResponse resp = (HttpServletResponse) resposta;

        // Sem isso, acento em campo de formulario chega quebrado no banco.
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        String caminho = req.getRequestURI().substring(req.getContextPath().length());
        if (caminho.isEmpty()) caminho = "/";

        if (ehPublico(caminho)) {
            corrente.doFilter(requisicao, resposta);
            return;
        }

        HttpSession sessao = req.getSession(false);
        boolean autenticado = sessao != null && sessao.getAttribute("usuarioLogado") != null;

        if (autenticado) {
            corrente.doFilter(requisicao, resposta);
            return;
        }

        // Guarda o destino para devolver o usuario a tela que ele queria abrir.
        String destino = URLEncoder.encode(caminho, StandardCharsets.UTF_8);
        resp.sendRedirect(req.getContextPath() + "/login?destino=" + destino);
    }

    private boolean ehPublico(String caminho) {
        if (PUBLICOS.contains(caminho)) return true;
        for (String prefixo : PREFIXOS_PUBLICOS) {
            if (caminho.startsWith(prefixo)) return true;
        }
        return false;
    }
}
