package br.com.thechampions.servlet;

import br.com.thechampions.dao.ColecaoDAO;
import br.com.thechampions.dao.FigurinhaDAO;
import br.com.thechampions.model.Figurinha;
import br.com.thechampions.model.ProgressoSelecao;
import br.com.thechampions.model.Usuario;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * Meu Album: selecoes a esquerda, grid de figurinhas a direita.
 * Serve tambem o /catalogo, que e a mesma tela sem restringir por selecao.
 */
@WebServlet({"/album", "/catalogo"})
public class AlbumServlet extends ServletBase {

    private final ColecaoDAO colecaoDAO = new ColecaoDAO();
    private final FigurinhaDAO figurinhaDAO = new FigurinhaDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        Usuario usuario = logado(req);
        boolean ehCatalogo = req.getServletPath().endsWith("/catalogo");

        try {
            carregarContadores(req, usuario.getId());

            List<ProgressoSelecao> selecoes = colecaoDAO.progressoPorSelecao(usuario.getId());
            req.setAttribute("selecoes", selecoes);
            req.setAttribute("resumo", colecaoDAO.resumo(usuario.getId()));

            // Sem selecao na URL, abre a primeira da lista (a mais completa).
            String sigla = req.getParameter("selecao");
            if (!ehCatalogo && (sigla == null || sigla.isBlank()) && !selecoes.isEmpty()) {
                sigla = selecoes.get(0).getSigla();
            }
            if (sigla != null && sigla.isBlank()) sigla = null;

            Integer ordemRaridade = null;
            String raridade = req.getParameter("raridade");
            if (raridade != null && !raridade.isBlank()) {
                try {
                    ordemRaridade = Integer.valueOf(raridade);
                } catch (NumberFormatException ignorada) {
                    // filtro invalido na URL: mostra tudo em vez de quebrar
                }
            }

            List<Figurinha> figurinhas = figurinhaDAO.listarCatalogoCom(usuario.getId(), sigla, ordemRaridade);

            String status = req.getParameter("status");
            if (status != null) {
                figurinhas = switch (status) {
                    case "obtidas"   -> figurinhas.stream().filter(Figurinha::isObtida).toList();
                    case "faltantes" -> figurinhas.stream().filter(Figurinha::isFaltante).toList();
                    case "repetidas" -> figurinhas.stream().filter(Figurinha::isRepetida).toList();
                    default          -> figurinhas;
                };
            }

            // Dados do cabecalho da selecao aberta.
            final String siglaAberta = sigla;
            selecoes.stream()
                    .filter(s -> s.getSigla().equals(siglaAberta))
                    .findFirst()
                    .ifPresent(s -> req.setAttribute("selecaoAtiva", s));

            req.setAttribute("figurinhas", figurinhas);
            req.setAttribute("siglaSelecionada", sigla);
            req.setAttribute("statusSelecionado", status);
            req.setAttribute("raridadeSelecionada", raridade);
            req.setAttribute("ehCatalogo", ehCatalogo);

            req.setAttribute("titulo", ehCatalogo ? "Catálogo de Figurinhas" : "Meu Álbum Digital");
            req.setAttribute("aba", ehCatalogo ? "catalogo" : "album");
            req.getRequestDispatcher("/WEB-INF/jsp/album.jsp").forward(req, resp);

        } catch (SQLException e) {
            tratarFalhaDeBanco(req, resp, "album", e);
        }
    }
}
