package br.com.thechampions.servlet;

import br.com.thechampions.dao.ColecaoDAO;
import br.com.thechampions.dao.FigurinhaDAO;
import br.com.thechampions.dao.MatchDAO;
import br.com.thechampions.dao.NotificacaoDAO;
import br.com.thechampions.dao.PacoteDAO;
import br.com.thechampions.dao.TrocaDAO;
import br.com.thechampions.dao.UsuarioDAO;
import br.com.thechampions.model.Figurinha;
import br.com.thechampions.model.Pacote;
import br.com.thechampions.model.Raridade;
import br.com.thechampions.model.Troca;
import br.com.thechampions.model.Usuario;
import br.com.thechampions.util.Json;
import br.com.thechampions.util.Senha;
import br.com.thechampions.util.Tokens;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * API JSON consumida pela vitrine estatica (src/main/webapp-static).
 *
 * As telas JSP continuam funcionando como antes: este servlet nao substitui
 * nenhuma delas, so expoe os mesmos DAOs num formato que o navegador consegue
 * consumir de outro dominio. Autenticacao e CORS ficam no ApiFilter.
 *
 * Roteia por prefixo em vez de um servlet por rota: sao chamadas curtas, todas
 * lendo dos mesmos DAOs, e um @WebServlet para cada uma multiplicaria arquivo
 * sem separar responsabilidade nenhuma.
 */
@WebServlet("/api/*")
public class ApiServlet extends HttpServlet {

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();
    private final FigurinhaDAO figurinhaDAO = new FigurinhaDAO();
    private final ColecaoDAO colecaoDAO = new ColecaoDAO();
    private final PacoteDAO pacoteDAO = new PacoteDAO();
    private final MatchDAO matchDAO = new MatchDAO();
    private final TrocaDAO trocaDAO = new TrocaDAO();
    private final NotificacaoDAO notificacaoDAO = new NotificacaoDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        rotear(req, resp, false);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        rotear(req, resp, true);
    }

    private void rotear(HttpServletRequest req, HttpServletResponse resp, boolean post)
            throws IOException {

        String rota = req.getPathInfo() == null ? "/" : req.getPathInfo();
        resp.setContentType("application/json;charset=UTF-8");

        try {
            // Rotas abertas (o ApiFilter deixa passar sem token).
            //
            // /status existe para a vitrine descobrir se ha backend vivo. Nao da
            // para sondar /diagnostico: ele fica fora de /api/*, entao nao recebe
            // cabecalho CORS e a sondagem cross-origin morre no navegador — a
            // vitrine concluia "nao ha backend" mesmo com o Tomcat no ar.
            if (rota.equals("/status")) {
                escrever(resp, Json.obj().put("ok", true).put("app", "the-champions").toString());
                return;
            }
            if (post && rota.equals("/login")) { login(req, resp); return; }

            int id = idDoUsuario(req);

            switch (rota) {
                case "/usuario/perfil" -> escrever(resp, Json.doUsuario(usuarioDAO.buscarPorId(id)));
                case "/album"          -> album(req, resp, id);
                case "/album/resumo"   -> resumo(resp, id);
                case "/pacotes"        -> escrever(resp, Json.lista(pacoteDAO.listarNaoAbertos(id), Json::doPacote));
                case "/trocas"         -> escrever(resp, Json.lista(matchDAO.buscarMatches(id, null, null), Json::doMatch));
                case "/ranking"        -> ranking(resp);
                case "/historico"      -> escrever(resp, Json.lista(trocaDAO.listarDoUsuario(id, 20),
                                                                     t -> Json.daTroca(t, id)));
                case "/trocas/propor"  -> proporTroca(req, resp, id, post);
                case "/notificacoes"   -> escrever(resp, Json.lista(notificacaoDAO.listar(id, 10), Json::daNotificacao));
                default -> {
                    if (!post && rota.startsWith("/troca/")) {
                        detalheTroca(resp, rota.substring("/troca/".length()), id);
                    } else if (post && rota.startsWith("/pacotes/") && rota.endsWith("/abrir")) {
                        abrirPacote(resp, rota, id);
                    } else if (post && rota.startsWith("/troca/") && rota.endsWith("/confirmar")) {
                        confirmarTroca(resp, rota, id);
                    } else {
                        erro(resp, HttpServletResponse.SC_NOT_FOUND, "Rota inexistente: " + rota);
                    }
                }
            }
        } catch (SQLException e) {
            log("Falha de banco na API em " + rota, e);
            erro(resp, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                 "Falha ao consultar o banco. Veja /diagnostico.");
        }
    }

    // ---------------------------------------------------------------- rotas

    private void login(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, SQLException {

        String email = req.getParameter("email");
        String senha = req.getParameter("senha");

        // A vitrine manda JSON, nao formulario: os parametros vem nulos.
        if (email == null || senha == null) {
            Map<String, String> corpo = corpoJson(req);
            email = corpo.get("email");
            senha = corpo.get("senha");
        }

        Usuario usuario = email == null ? null : usuarioDAO.buscarPorEmail(email);

        // Mensagem unica para email inexistente e senha errada: dizer qual dos
        // dois falhou entrega ao atacante qual e-mail existe na base.
        if (usuario == null || senha == null || !Senha.confere(senha, usuario.getSenhaHash())) {
            erro(resp, HttpServletResponse.SC_UNAUTHORIZED, "E-mail ou senha invalidos.");
            return;
        }

        escrever(resp, Json.obj()
                .put("token", Tokens.emitir(usuario.getId()))
                .bruto("usuario", Json.doUsuario(usuario))
                .toString());
    }

    private void album(HttpServletRequest req, HttpServletResponse resp, int id)
            throws IOException, SQLException {

        String selecao = vazioVira(req.getParameter("selecao"), null);
        List<Figurinha> lista = figurinhaDAO.listarCatalogoCom(id, selecao, null);

        // O filtro de status nao existe no DAO (ele nao sabe o que e "faltante"
        // do ponto de vista da tela); aplicado aqui sobre a lista ja montada.
        String status = req.getParameter("status");
        if (status != null) {
            lista = lista.stream().filter(f -> switch (status) {
                case "faltantes" -> f.getQuantidade() == 0;
                case "repetidas" -> f.getQuantidade() > 1;
                case "obtidas"   -> f.getQuantidade() > 0;
                default -> true;
            }).toList();
        }
        escrever(resp, Json.lista(lista, Json::daFigurinha));
    }

    private void resumo(HttpServletResponse resp, int id) throws IOException, SQLException {
        // A quebra por raridade e montada aqui porque o ResumoColecao so guarda
        // os totais de brilhantes/raras/lendarias, sem o denominador do catalogo.
        Map<Raridade, int[]> porRaridade = new LinkedHashMap<>();
        List<Figurinha> catalogo = figurinhaDAO.listarCatalogoCom(id, null, null);

        for (Raridade r : figurinhaDAO.listarRaridades()) {
            List<Figurinha> doTipo = catalogo.stream()
                    .filter(f -> f.getRaridade().getOrdem() == r.getOrdem()).toList();
            porRaridade.put(r, new int[]{
                    (int) doTipo.stream().filter(Figurinha::isObtida).count(),
                    doTipo.size()
            });
        }

        // Valor do acervo: cada copia vale o valor de referencia da sua raridade.
        java.math.BigDecimal valor = catalogo.stream()
                .filter(Figurinha::isObtida)
                .map(f -> f.getRaridade().getValorReferencia()
                        .multiply(java.math.BigDecimal.valueOf(f.getQuantidade())))
                .reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

        escrever(resp, Json.doResumo(colecaoDAO.resumo(id), porRaridade, valor));
    }

    private void ranking(HttpServletResponse resp) throws IOException, SQLException {
        List<Usuario> top = usuarioDAO.listarRanking(10);
        StringBuilder out = new StringBuilder("[");
        for (int i = 0; i < top.size(); i++) {
            Usuario u = top.get(i);
            if (i > 0) out.append(',');
            out.append(Json.obj()
                    .put("posicao", i + 1)
                    .put("nome", u.getNome())
                    .put("figurinhas", u.getFigurinhasObtidas())
                    .put("trocas", u.getTrocasConcluidas())
                    .put("reputacao", u.getReputacaoMedia()));
        }
        escrever(resp, out.append(']').toString());
    }

    private void abrirPacote(HttpServletResponse resp, String rota, int id)
            throws IOException, SQLException {

        int idPacote = numeroNoMeio(rota, "/pacotes/", "/abrir");
        if (idPacote == 0) {
            erro(resp, HttpServletResponse.SC_BAD_REQUEST, "Id de pacote invalido.");
            return;
        }

        try {
            Pacote pacote = pacoteDAO.abrir(idPacote, id);
            escrever(resp, Json.lista(pacote.getConteudo(), Json::daFigurinha));
        } catch (IllegalStateException recusado) {
            // "ja foi aberto", "nao pertence a voce", "nao encontrado": o DAO
            // sinaliza recusa com excecao, e a mensagem dele ja serve ao usuario.
            // Sem este catch a recusa vira 500 e a tela mostra falha de servidor.
            erro(resp, HttpServletResponse.SC_CONFLICT, recusado.getMessage());
        }
    }

    /**
     * Transforma um match em troca gravada e devolve o codigo dela.
     *
     * A lista de matches vem sem codigo porque ate aqui nada foi gravado: o
     * match e uma sugestao calculada na hora. E esta chamada que cria a linha em
     * "troca" e abre a negociacao.
     */
    private void proporTroca(HttpServletRequest req, HttpServletResponse resp, int id, boolean post)
            throws IOException, SQLException {

        if (!post) {
            erro(resp, HttpServletResponse.SC_METHOD_NOT_ALLOWED, "Use POST para propor.");
            return;
        }

        Map<String, String> corpo = corpoJson(req);
        int idParceiro = inteiro(corpo.get("idParceiro"));
        List<Integer> envio = idsDe(corpo.get("envio"));
        List<Integer> recebo = idsDe(corpo.get("recebo"));

        if (idParceiro == 0 || envio.isEmpty() || recebo.isEmpty()) {
            erro(resp, HttpServletResponse.SC_BAD_REQUEST,
                 "Informe idParceiro e as figurinhas dos dois lados.");
            return;
        }

        try {
            String codigo = trocaDAO.propor(id, idParceiro, envio, recebo);
            escrever(resp, Json.obj().put("codigo", codigo).put("ok", true).toString());
        } catch (IllegalArgumentException | IllegalStateException recusado) {
            erro(resp, HttpServletResponse.SC_CONFLICT, recusado.getMessage());
        }
    }

    /**
     * Uma troca gravada, com os dois lados montados na perspectiva de quem pede:
     * "recebo" e "envio" trocam de lugar conforme eu seja proponente ou receptor.
     * A tela de negociacao consome exatamente as mesmas chaves de um match.
     */
    private void detalheTroca(HttpServletResponse resp, String codigo, int id)
            throws IOException, SQLException {

        Troca t = trocaDAO.buscarPorCodigo(codigo);
        if (t == null) {
            erro(resp, HttpServletResponse.SC_NOT_FOUND, "Troca nao encontrada.");
            return;
        }

        boolean souProponente = t.getProponente().getId() == id;
        if (!souProponente && t.getReceptor().getId() != id) {
            erro(resp, HttpServletResponse.SC_FORBIDDEN, "Esta troca nao e sua.");
            return;
        }

        var parceiro = souProponente ? t.getReceptor() : t.getProponente();
        var euEnvio  = souProponente ? t.getItensProponente() : t.getItensReceptor();
        var euRecebo = souProponente ? t.getItensReceptor()   : t.getItensProponente();
        boolean parceiroConfirmou = souProponente ? t.isConfirmouReceptor() : t.isConfirmouProponente();
        boolean euConfirmei = souProponente ? t.isConfirmouProponente() : t.isConfirmouReceptor();

        escrever(resp, Json.obj()
                .put("codigo", t.getCodigo())
                .put("idParceiro", parceiro.getId())
                .put("nome", parceiro.getNome())
                .put("reputacao", parceiro.getReputacaoMedia())
                .put("qualidade", t.getStatusExibicao())
                .put("status", t.getStatus())
                .put("euConfirmei", euConfirmei)
                .put("parceiroConfirmou", parceiroConfirmou)
                .bruto("recebo", Json.lista(euRecebo, Json::daFigurinha))
                .bruto("envio", Json.lista(euEnvio, Json::daFigurinha))
                .toString());
    }

    private void confirmarTroca(HttpServletResponse resp, String rota, int id)
            throws IOException, SQLException {

        String codigo = entre(rota, "/troca/", "/confirmar");
        Troca troca = codigo == null ? null : trocaDAO.buscarPorCodigo(codigo);
        if (troca == null) {
            erro(resp, HttpServletResponse.SC_NOT_FOUND, "Troca nao encontrada.");
            return;
        }

        TrocaDAO.Resultado resultado = trocaDAO.confirmar(troca.getId(), id);
        int http = switch (resultado) {
            case EXECUTADA, AGUARDANDO_OUTRO -> HttpServletResponse.SC_OK;
            case NAO_PARTICIPA -> HttpServletResponse.SC_FORBIDDEN;
            default -> HttpServletResponse.SC_CONFLICT;
        };
        resp.setStatus(http);
        escrever(resp, Json.obj()
                .put("resultado", resultado.name())
                .put("codigo", troca.getCodigo())
                .put("ok", http == HttpServletResponse.SC_OK)
                .toString());
    }

    // ------------------------------------------------------------ utilidades

    /** O ApiFilter ja validou o token e deixou o id aqui. */
    private int idDoUsuario(HttpServletRequest req) {
        Object id = req.getAttribute("idUsuarioApi");
        return id == null ? 0 : (int) id;
    }

    private void escrever(HttpServletResponse resp, String json) throws IOException {
        resp.getWriter().write(json);
    }

    private void erro(HttpServletResponse resp, int status, String mensagem) throws IOException {
        resp.setStatus(status);
        escrever(resp, Json.obj().put("erro", mensagem).toString());
    }

    private static String vazioVira(String valor, String padrao) {
        return valor == null || valor.isBlank() ? padrao : valor;
    }

    private static String entre(String texto, String prefixo, String sufixo) {
        if (!texto.startsWith(prefixo) || !texto.endsWith(sufixo)) return null;
        String miolo = texto.substring(prefixo.length(), texto.length() - sufixo.length());
        return miolo.isBlank() ? null : miolo;
    }

    private static int inteiro(String valor) {
        try {
            return valor == null ? 0 : Integer.parseInt(valor.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /** Le "[3,7,12]" do corpo JSON raso. */
    private static List<Integer> idsDe(String bruto) {
        if (bruto == null || bruto.isBlank()) return List.of();
        List<Integer> ids = new java.util.ArrayList<>();
        for (String pedaco : bruto.replaceAll("[\\[\\]\"]", "").split(",")) {
            int n = inteiro(pedaco);
            if (n > 0) ids.add(n);
        }
        return ids;
    }

    private static int numeroNoMeio(String texto, String prefixo, String sufixo) {
        String miolo = entre(texto, prefixo, sufixo);
        try {
            return miolo == null ? 0 : Integer.parseInt(miolo);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * Le um corpo JSON de um nivel sem trazer um parser para o projeto.
     *
     * O valor pode ser texto, numero ou uma lista de numeros; o que a lista
     * contem fica cru, para o idsDe() abrir. Nao trata objeto aninhado nem
     * virgula dentro de string — o que basta para /login e /trocas/propor.
     * Um corpo mais complexo que isso pede um parser de verdade.
     */
    private static Map<String, String> corpoJson(HttpServletRequest req) throws IOException {
        String cru = new String(req.getInputStream().readAllBytes(),
                                java.nio.charset.StandardCharsets.UTF_8).trim();
        cru = cru.replaceAll("^\\{", "").replaceAll("}$", "");

        Map<String, String> mapa = new LinkedHashMap<>();
        int inicio = 0;
        int profundidade = 0;

        // Corta nas virgulas de primeiro nivel: as de dentro de "[...]" pertencem
        // a lista e nao podem separar pares.
        for (int i = 0; i <= cru.length(); i++) {
            char c = i < cru.length() ? cru.charAt(i) : ',';
            if (c == '[') profundidade++;
            else if (c == ']') profundidade--;
            else if (c == ',' && profundidade == 0) {
                String par = cru.substring(inicio, i);
                inicio = i + 1;
                String[] lados = par.split(":", 2);
                if (lados.length == 2) mapa.put(limpar(lados[0]), limpar(lados[1]));
            }
        }
        return mapa;
    }

    private static String limpar(String valor) {
        return valor.trim().replaceAll("^\"|\"$", "").replace("\\\"", "\"");
    }
}
