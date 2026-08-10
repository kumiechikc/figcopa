package br.com.thechampions.servlet;

import br.com.thechampions.dao.ColecaoDAO;
import br.com.thechampions.dao.TrocaDAO;
import br.com.thechampions.dao.UsuarioDAO;
import br.com.thechampions.model.Usuario;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Telas da area "Minha conta": perfil, ranking e historico.
 * Sao tres visoes dos mesmos dados, por isso ficam no mesmo controlador.
 */
@WebServlet({"/perfil", "/ranking", "/historico"})
public class ContaServlet extends ServletBase {

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final ColecaoDAO colecaoDAO = new ColecaoDAO();
    private final TrocaDAO trocaDAO = new TrocaDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        Usuario usuario = logado(req);
        consumirFlash(req);
        String tela = req.getServletPath();

        try {
            carregarContadores(req, usuario.getId());
            req.setAttribute("resumo", colecaoDAO.resumo(usuario.getId()));

            switch (tela) {
                case "/ranking" -> {
                    req.setAttribute("ranking", usuarioDAO.listarRanking(10));
                    req.setAttribute("titulo", "Ranking de Colecionadores");
                    req.setAttribute("aba", "ranking");
                }
                case "/historico" -> {
                    req.setAttribute("trocas", trocaDAO.listarDoUsuario(usuario.getId(), 50));
                    req.setAttribute("titulo", "Histórico de Trocas");
                    req.setAttribute("aba", "historico");
                }
                default -> {
                    req.setAttribute("trocasConcluidas", usuarioDAO.contarTrocasConcluidas(usuario.getId()));
                    req.setAttribute("trocas", trocaDAO.listarDoUsuario(usuario.getId(), 8));
                    req.setAttribute("repetidas", colecaoDAO.listarRepetidas(usuario.getId()));
                    req.setAttribute("titulo", "Perfil e Reputação");
                    req.setAttribute("aba", "perfil");
                }
            }

            String jsp = switch (tela) {
                case "/ranking"   -> "/WEB-INF/jsp/ranking.jsp";
                case "/historico" -> "/WEB-INF/jsp/historico.jsp";
                default            -> "/WEB-INF/jsp/perfil.jsp";
            };
            req.getRequestDispatcher(jsp).forward(req, resp);

        } catch (SQLException e) {
            tratarFalhaDeBanco(req, resp, "area da conta", e);
        }
    }
}
