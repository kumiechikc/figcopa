package br.com.thechampions.servlet;

import br.com.thechampions.dao.NotificacaoDAO;
import br.com.thechampions.dao.PacoteDAO;
import br.com.thechampions.model.Usuario;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Base das telas internas.
 *
 * Concentra o que toda tela logada precisa: quem esta logado e os contadores
 * que aparecem no menu lateral (pacotes disponiveis, avisos nao lidos).
 * Sem isso, cada servlet repetiria as mesmas tres consultas.
 */
public abstract class ServletBase extends HttpServlet {

    protected final PacoteDAO pacoteDAO = new PacoteDAO();
    protected final NotificacaoDAO notificacaoDAO = new NotificacaoDAO();

    /** Usuario da sessao. O filtro garante que nunca e nulo nas telas internas. */
    protected Usuario logado(HttpServletRequest req) {
        HttpSession sessao = req.getSession(false);
        return sessao == null ? null : (Usuario) sessao.getAttribute("usuarioLogado");
    }

    /** Contadores do menu lateral e do sino. */
    protected void carregarContadores(HttpServletRequest req, int idUsuario) throws SQLException {
        req.setAttribute("pacotesDisponiveis", pacoteDAO.contarDisponiveis(idUsuario));
        req.setAttribute("naoLidas", notificacaoDAO.contarNaoLidas(idUsuario));
    }

    /** Mensagem de sucesso/erro guardada na sessao entre um redirect e o outro. */
    protected void consumirFlash(HttpServletRequest req) {
        HttpSession sessao = req.getSession(false);
        if (sessao == null) return;
        for (String chave : new String[]{"avisoFlash", "erroFlash"}) {
            Object valor = sessao.getAttribute(chave);
            if (valor != null) {
                req.setAttribute(chave, valor);
                sessao.removeAttribute(chave);
            }
        }
    }

    protected void definirFlash(HttpServletRequest req, String chave, String mensagem) {
        req.getSession(true).setAttribute(chave, mensagem);
    }

    /**
     * Erro de banco vira tela de erro amigavel em vez de stack trace.
     * O apontamento para /diagnostico e proposital: e onde a causa aparece.
     */
    protected void tratarFalhaDeBanco(HttpServletRequest req, HttpServletResponse resp,
                                      String contexto, SQLException e)
            throws ServletException, IOException {
        log("Falha de banco em " + contexto, e);
        req.setAttribute("erroBanco", e.getMessage());
        req.setAttribute("titulo", "Erro de banco de dados");
        resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        req.getRequestDispatcher("/WEB-INF/jsp/erro-banco.jsp").forward(req, resp);
    }

    protected int parametroInt(HttpServletRequest req, String nome, int padrao) {
        String valor = req.getParameter(nome);
        if (valor == null || valor.isBlank()) return padrao;
        try {
            return Integer.parseInt(valor.trim());
        } catch (NumberFormatException e) {
            return padrao;
        }
    }
}
