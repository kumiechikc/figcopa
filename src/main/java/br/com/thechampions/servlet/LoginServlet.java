package br.com.thechampions.servlet;

import br.com.thechampions.dao.UsuarioDAO;
import br.com.thechampions.model.Usuario;
import br.com.thechampions.util.Senha;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;

/** Entrada na plataforma. */
@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        if (req.getSession(false) != null && req.getSession().getAttribute("usuarioLogado") != null) {
            resp.sendRedirect(req.getContextPath() + "/dashboard");
            return;
        }
        req.getRequestDispatcher("/WEB-INF/jsp/login.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String email = valorLimpo(req, "email");
        String senha = req.getParameter("senha");

        if (email.isEmpty() || senha == null || senha.isEmpty()) {
            recusar(req, resp, email, "Informe e-mail e senha.");
            return;
        }

        try {
            Usuario usuario = usuarioDAO.buscarPorEmail(email);

            // Mensagem generica de proposito: dizer "e-mail nao existe" entrega
            // a um atacante quais contas sao validas.
            if (usuario == null || !Senha.confere(senha, usuario.getSenhaHash())) {
                recusar(req, resp, email, "E-mail ou senha incorretos.");
                return;
            }

            // Sessao nova apos autenticar: evita fixacao de sessao.
            HttpSession sessaoAntiga = req.getSession(false);
            if (sessaoAntiga != null) sessaoAntiga.invalidate();

            HttpSession sessao = req.getSession(true);
            usuario.setSenhaHash(null);   // o hash nao precisa circular pela view
            sessao.setAttribute("usuarioLogado", usuario);
            sessao.setMaxInactiveInterval(60 * 60);

            String destino = req.getParameter("destino");
            boolean destinoValido = destino != null && destino.startsWith("/") && !destino.startsWith("//");
            resp.sendRedirect(req.getContextPath() + (destinoValido ? destino : "/dashboard"));

        } catch (SQLException e) {
            log("Falha ao consultar usuario no login", e);
            recusar(req, resp, email,
                    "Não foi possível falar com o banco de dados. Abra /diagnostico para ver a causa.");
        }
    }

    private void recusar(HttpServletRequest req, HttpServletResponse resp, String email, String mensagem)
            throws ServletException, IOException {
        req.setAttribute("erro", mensagem);
        req.setAttribute("emailInformado", email);
        resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        req.getRequestDispatcher("/WEB-INF/jsp/login.jsp").forward(req, resp);
    }

    static String valorLimpo(HttpServletRequest req, String nome) {
        String v = req.getParameter(nome);
        return v == null ? "" : v.trim();
    }
}
