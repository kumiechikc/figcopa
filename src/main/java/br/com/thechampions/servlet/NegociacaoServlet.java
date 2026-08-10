package br.com.thechampions.servlet;

import br.com.thechampions.dao.TrocaDAO;
import br.com.thechampions.model.Troca;
import br.com.thechampions.model.Usuario;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Negociacao de uma troca especifica, com dupla confirmacao.
 *
 * Esta e a tela que fecha o ciclo do produto: enquanto os dois lados nao
 * confirmarem, nenhuma figurinha muda de dono.
 */
@WebServlet("/troca")
public class NegociacaoServlet extends ServletBase {

    private final TrocaDAO trocaDAO = new TrocaDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        Usuario usuario = logado(req);
        consumirFlash(req);

        String codigo = req.getParameter("codigo");
        if (codigo == null || codigo.isBlank()) {
            resp.sendRedirect(req.getContextPath() + "/trocas");
            return;
        }

        try {
            carregarContadores(req, usuario.getId());

            Troca troca = trocaDAO.buscarPorCodigo(codigo.trim());
            if (troca == null) {
                definirFlash(req, "erroFlash", "Troca " + codigo + " não encontrada.");
                resp.sendRedirect(req.getContextPath() + "/trocas");
                return;
            }

            boolean participa = troca.getProponente().getId() == usuario.getId()
                             || troca.getReceptor().getId() == usuario.getId();
            if (!participa) {
                definirFlash(req, "erroFlash", "Você não participa desta troca.");
                resp.sendRedirect(req.getContextPath() + "/trocas");
                return;
            }

            prepararTela(req, troca, usuario);
            req.getRequestDispatcher("/WEB-INF/jsp/negociacao.jsp").forward(req, resp);

        } catch (SQLException e) {
            tratarFalhaDeBanco(req, resp, "negociacao", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        Usuario usuario = logado(req);
        int idTroca = parametroInt(req, "idTroca", 0);
        String acao = req.getParameter("acao");
        String codigo = req.getParameter("codigo");

        if (idTroca <= 0 || acao == null) {
            resp.sendRedirect(req.getContextPath() + "/trocas");
            return;
        }

        try {
            if ("recusar".equals(acao)) {
                boolean recusou = trocaDAO.recusar(idTroca, usuario.getId());
                definirFlash(req, recusou ? "avisoFlash" : "erroFlash",
                        recusou ? "Proposta recusada. Nenhuma figurinha mudou de dono."
                                : "Não foi possível recusar esta proposta.");
                resp.sendRedirect(req.getContextPath() + "/trocas");
                return;
            }

            TrocaDAO.Resultado resultado = trocaDAO.confirmar(idTroca, usuario.getId());

            switch (resultado) {
                case EXECUTADA -> definirFlash(req, "avisoFlash",
                        "Troca concluída. As figurinhas já mudaram de dono no banco de dados.");
                case AGUARDANDO_OUTRO -> definirFlash(req, "avisoFlash",
                        "Sua parte foi confirmada. A troca executa assim que o parceiro confirmar.");
                case JA_CONFIRMADA -> definirFlash(req, "erroFlash",
                        "Você já havia confirmado esta troca.");
                case NAO_PENDENTE -> definirFlash(req, "erroFlash",
                        "Esta troca não está mais aberta.");
                case NAO_PARTICIPA -> definirFlash(req, "erroFlash",
                        "Você não participa desta troca.");
                case FIGURINHA_INDISPONIVEL -> definirFlash(req, "erroFlash",
                        "A troca foi cancelada: uma das figurinhas prometidas não está mais "
                        + "disponível na coleção de origem. Nada foi transferido.");
            }

            String destino = (codigo != null && !codigo.isBlank())
                    ? "/troca?codigo=" + codigo : "/trocas";
            resp.sendRedirect(req.getContextPath() + destino);

        } catch (SQLException e) {
            tratarFalhaDeBanco(req, resp, "confirmacao de troca", e);
        }
    }

    /** Monta os atributos que a JSP usa para desenhar os dois lados. */
    private void prepararTela(HttpServletRequest req, Troca troca, Usuario usuario) {
        boolean souProponente = troca.getProponente().getId() == usuario.getId();

        req.setAttribute("troca", troca);
        req.setAttribute("souProponente", souProponente);
        req.setAttribute("parceiro", souProponente ? troca.getReceptor() : troca.getProponente());
        req.setAttribute("euEnvio", souProponente ? troca.getItensProponente() : troca.getItensReceptor());
        req.setAttribute("euRecebo", souProponente ? troca.getItensReceptor() : troca.getItensProponente());
        req.setAttribute("euConfirmei",
                souProponente ? troca.isConfirmouProponente() : troca.isConfirmouReceptor());
        req.setAttribute("parceiroConfirmou",
                souProponente ? troca.isConfirmouReceptor() : troca.isConfirmouProponente());

        req.setAttribute("titulo", "Negociação de Troca — #" + troca.getCodigo());
        req.setAttribute("trilhaMeio", "Trocas");
        req.setAttribute("aba", "trocas");
    }
}
