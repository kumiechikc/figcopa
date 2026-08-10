package br.com.thechampions.servlet;

import br.com.thechampions.dao.MatchDAO;
import br.com.thechampions.dao.TrocaDAO;
import br.com.thechampions.model.Troca;
import br.com.thechampions.model.Usuario;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Buscar Trocas: lista os parceiros compativeis e permite propor a troca
 * sugerida pelo matching.
 */
@WebServlet("/trocas")
public class TrocasServlet extends ServletBase {

    private final MatchDAO matchDAO = new MatchDAO();
    private final TrocaDAO trocaDAO = new TrocaDAO();
    private final br.com.thechampions.dao.ColecaoDAO colecaoDAO = new br.com.thechampions.dao.ColecaoDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        Usuario usuario = logado(req);
        consumirFlash(req);

        try {
            carregarContadores(req, usuario.getId());

            Integer raridadeMinima = null;
            String filtroRaridade = req.getParameter("raridadeMinima");
            if (filtroRaridade != null && !filtroRaridade.isBlank()) {
                try {
                    raridadeMinima = Integer.valueOf(filtroRaridade);
                } catch (NumberFormatException ignorada) {
                    // filtro invalido: ignora em vez de quebrar a tela
                }
            }
            String selecao = req.getParameter("selecao");

            var matches = matchDAO.buscarMatches(usuario.getId(), raridadeMinima, selecao);

            // Propostas em aberto, separadas por quem propos.
            List<Troca> recebidas = new ArrayList<>();
            List<Troca> enviadas = new ArrayList<>();
            for (Troca t : trocaDAO.listarDoUsuario(usuario.getId(), 20)) {
                if (!t.isPendente() && !"ACEITA".equals(t.getStatus())) continue;
                if (t.ehProponente(usuario.getId())) enviadas.add(t);
                else recebidas.add(t);
            }

            req.setAttribute("matches", matches);
            req.setAttribute("totalMatches", matches.size());
            req.setAttribute("resumo", colecaoDAO.resumo(usuario.getId()));
            req.setAttribute("selecoes", colecaoDAO.progressoPorSelecao(usuario.getId()));
            req.setAttribute("recebidas", recebidas);
            req.setAttribute("enviadas", enviadas);
            req.setAttribute("raridadeMinima", filtroRaridade);
            req.setAttribute("selecaoFiltro", selecao);

            req.setAttribute("titulo", "Buscar Trocas");
            req.setAttribute("trilhaMeio", "Trocas");
            req.setAttribute("aba", "trocas");
            req.getRequestDispatcher("/WEB-INF/jsp/trocas.jsp").forward(req, resp);

        } catch (SQLException e) {
            tratarFalhaDeBanco(req, resp, "buscar trocas", e);
        }
    }

    /** Cria a proposta a partir de um match sugerido. */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        Usuario usuario = logado(req);
        int idParceiro = parametroInt(req, "idParceiro", 0);

        List<Integer> euEnvio = idsDoParametro(req, "envio");
        List<Integer> euRecebo = idsDoParametro(req, "recebo");

        if (idParceiro <= 0 || euEnvio.isEmpty() || euRecebo.isEmpty()) {
            definirFlash(req, "erroFlash", "Proposta incompleta. Tente novamente pela lista de matches.");
            resp.sendRedirect(req.getContextPath() + "/trocas");
            return;
        }

        try {
            String codigo = trocaDAO.propor(usuario.getId(), idParceiro, euEnvio, euRecebo);
            definirFlash(req, "avisoFlash",
                    "Proposta " + codigo + " enviada. Ela se concretiza quando o parceiro confirmar.");
            resp.sendRedirect(req.getContextPath() + "/troca?codigo=" + codigo);

        } catch (IllegalArgumentException e) {
            definirFlash(req, "erroFlash", e.getMessage());
            resp.sendRedirect(req.getContextPath() + "/trocas");

        } catch (SQLException e) {
            tratarFalhaDeBanco(req, resp, "propor troca", e);
        }
    }

    private List<Integer> idsDoParametro(HttpServletRequest req, String nome) {
        String[] valores = req.getParameterValues(nome);
        List<Integer> ids = new ArrayList<>();
        if (valores == null) return ids;
        for (String v : valores) {
            try {
                ids.add(Integer.valueOf(v.trim()));
            } catch (NumberFormatException ignorada) {
                // id invalido no formulario: descarta em silencio
            }
        }
        return ids;
    }
}
