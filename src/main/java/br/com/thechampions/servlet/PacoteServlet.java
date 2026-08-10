package br.com.thechampions.servlet;

import br.com.thechampions.dao.ColecaoDAO;
import br.com.thechampions.model.Pacote;
import br.com.thechampions.model.Usuario;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Abertura de pacotes.
 *
 * O POST abre o pacote e redireciona (padrao POST-Redirect-GET): assim um F5
 * depois da abertura nao tenta abrir o pacote de novo.
 */
@WebServlet("/pacotes")
public class PacoteServlet extends ServletBase {

    private final ColecaoDAO colecaoDAO = new ColecaoDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        Usuario usuario = logado(req);
        consumirFlash(req);

        try {
            carregarContadores(req, usuario.getId());

            req.setAttribute("meusPacotes", pacoteDAO.listarNaoAbertos(usuario.getId()));
            req.setAttribute("ultimasAberturas", pacoteDAO.listarAbertos(usuario.getId(), 5));
            req.setAttribute("resumo", colecaoDAO.resumo(usuario.getId()));

            int idAberto = parametroInt(req, "aberto", 0);
            if (idAberto > 0) {
                Pacote aberto = pacoteDAO.buscarComConteudo(idAberto, usuario.getId());
                req.setAttribute("pacoteAberto", aberto);
            }

            req.setAttribute("titulo", "Abrir Pacotes");
            req.setAttribute("trilhaMeio", "Pacotes");
            req.setAttribute("aba", "pacotes");
            req.getRequestDispatcher("/WEB-INF/jsp/pacotes.jsp").forward(req, resp);

        } catch (SQLException e) {
            tratarFalhaDeBanco(req, resp, "pacotes", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        Usuario usuario = logado(req);
        int idPacote = parametroInt(req, "idPacote", 0);

        if (idPacote <= 0) {
            definirFlash(req, "erroFlash", "Pacote inválido.");
            resp.sendRedirect(req.getContextPath() + "/pacotes");
            return;
        }

        try {
            Pacote aberto = pacoteDAO.abrir(idPacote, usuario.getId());
            resp.sendRedirect(req.getContextPath() + "/pacotes?aberto=" + aberto.getId());

        } catch (IllegalStateException e) {
            definirFlash(req, "erroFlash", e.getMessage());
            resp.sendRedirect(req.getContextPath() + "/pacotes");

        } catch (SQLException e) {
            tratarFalhaDeBanco(req, resp, "abertura de pacote", e);
        }
    }
}
