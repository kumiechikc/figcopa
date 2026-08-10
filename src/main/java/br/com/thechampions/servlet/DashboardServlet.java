package br.com.thechampions.servlet;

import br.com.thechampions.dao.ColecaoDAO;
import br.com.thechampions.dao.MatchDAO;
import br.com.thechampions.dao.TrocaDAO;
import br.com.thechampions.model.Usuario;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

/** Painel do usuario logado: progresso, acoes rapidas, trocas e avisos. */
@WebServlet("/dashboard")
public class DashboardServlet extends ServletBase {

    private final ColecaoDAO colecaoDAO = new ColecaoDAO();
    private final TrocaDAO trocaDAO = new TrocaDAO();
    private final MatchDAO matchDAO = new MatchDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        Usuario usuario = logado(req);
        consumirFlash(req);

        try {
            carregarContadores(req, usuario.getId());

            req.setAttribute("resumo", colecaoDAO.resumo(usuario.getId()));
            req.setAttribute("trocasRecentes", trocaDAO.listarDoUsuario(usuario.getId(), 6));
            req.setAttribute("notificacoes", notificacaoDAO.listar(usuario.getId(), 5));
            req.setAttribute("totalMatches", matchDAO.buscarMatches(usuario.getId(), null, null).size());
            req.setAttribute("pendentesRecebidas", trocaDAO.contarPendentesRecebidas(usuario.getId()));

            req.setAttribute("titulo", "Dashboard");
            req.setAttribute("aba", "dashboard");
            req.getRequestDispatcher("/WEB-INF/jsp/dashboard.jsp").forward(req, resp);

        } catch (SQLException e) {
            tratarFalhaDeBanco(req, resp, "dashboard", e);
        }
    }
}
