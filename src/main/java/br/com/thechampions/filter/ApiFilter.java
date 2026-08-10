package br.com.thechampions.filter;

import br.com.thechampions.util.Tokens;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Set;

/**
 * Porta de entrada da API: CORS e autenticacao por token.
 *
 * Separado do AutenticacaoFilter porque as duas frentes falham de formas
 * diferentes. A tela nao autenticada e redirecionada para /login; a API tem de
 * responder 401 com JSON — um fetch() seguiria o redirect e receberia o HTML da
 * tela de login como se fosse a resposta da chamada.
 */
@WebFilter("/api/*")
public class ApiFilter implements Filter {

    /** Rotas da API abertas a quem ainda nao tem token. */
    private static final Set<String> PUBLICAS = Set.of("/api/status", "/api/login", "/api/cadastro");

    /**
     * Origens autorizadas a chamar a API pelo navegador.
     *
     * Nao usa "*": com credenciais o navegador recusa o coringa, e liberar
     * qualquer site a falar com a API em nome do usuario logado seria o proprio
     * buraco que o CORS existe para fechar. Para publicar em outro dominio,
     * acrescente-o aqui.
     */
    private static final Set<String> ORIGENS = Set.of(
            "https://kumiechikc.github.io",
            "http://localhost:8123",
            "http://127.0.0.1:8123",
            "http://localhost:8080");

    @Override
    public void doFilter(ServletRequest requisicao, ServletResponse resposta, FilterChain corrente)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) requisicao;
        HttpServletResponse resp = (HttpServletResponse) resposta;

        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");

        String origem = req.getHeader("Origin");
        if (origem != null && ORIGENS.contains(origem)) {
            resp.setHeader("Access-Control-Allow-Origin", origem);
            // Diz ao cache que a resposta muda conforme a origem que pediu.
            resp.setHeader("Vary", "Origin");
            resp.setHeader("Access-Control-Allow-Headers", "Content-Type, Authorization");
            resp.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
            resp.setHeader("Access-Control-Max-Age", "3600");
        }

        // Preflight: responde e para aqui, sem chegar ao servlet.
        if ("OPTIONS".equalsIgnoreCase(req.getMethod())) {
            resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
            return;
        }

        String caminho = req.getRequestURI().substring(req.getContextPath().length());

        if (PUBLICAS.contains(caminho)) {
            corrente.doFilter(requisicao, resposta);
            return;
        }

        int idUsuario = Tokens.idDe(tokenDe(req));
        if (idUsuario == 0) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.setContentType("application/json;charset=UTF-8");
            resp.getWriter().write("{\"erro\":\"Token ausente ou expirado.\"}");
            return;
        }

        // O servlet le daqui em vez de repetir a validacao do token.
        req.setAttribute("idUsuarioApi", idUsuario);
        corrente.doFilter(requisicao, resposta);
    }

    /** Extrai o token do cabecalho "Authorization: Bearer xxx". */
    static String tokenDe(HttpServletRequest req) {
        String cabecalho = req.getHeader("Authorization");
        if (cabecalho == null) return null;
        String prefixo = "Bearer ";
        return cabecalho.startsWith(prefixo) ? cabecalho.substring(prefixo.length()).trim() : null;
    }
}
