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
import java.util.regex.Pattern;

/** Criacao de conta. Ja deixa o usuario logado e com um pacote de boas-vindas. */
@WebServlet("/cadastro")
public class CadastroServlet extends HttpServlet {

    private static final Pattern EMAIL_VALIDO =
            Pattern.compile("^[\\w.+-]+@[\\w-]+\\.[\\w.-]{2,}$");

    private static final int SENHA_MINIMA = 6;

    private final UsuarioDAO usuarioDAO = new UsuarioDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        req.getRequestDispatcher("/WEB-INF/jsp/cadastro.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String nome = LoginServlet.valorLimpo(req, "nome");
        String email = LoginServlet.valorLimpo(req, "email").toLowerCase();
        String senha = req.getParameter("senha");
        String confirmacao = req.getParameter("confirmacao");

        String erro = validar(nome, email, senha, confirmacao);
        if (erro != null) {
            recusar(req, resp, nome, email, erro);
            return;
        }

        try {
            if (usuarioDAO.emailJaExiste(email)) {
                recusar(req, resp, nome, email, "Já existe uma conta com este e-mail.");
                return;
            }

            Usuario novo = new Usuario();
            novo.setNome(nome);
            novo.setEmail(email);
            novo.setSenhaHash(Senha.gerarHash(senha));

            int id = usuarioDAO.inserir(novo);
            novo.setId(id);
            novo.setTipoConta("COMUM");
            novo.setReputacaoMedia(java.math.BigDecimal.ZERO);
            novo.setAtivo(true);
            novo.setSenhaHash(null);

            HttpSession sessao = req.getSession(true);
            sessao.setAttribute("usuarioLogado", novo);
            sessao.setAttribute("avisoFlash",
                    "Conta criada. Voce ganhou um Pacote Especial de boas-vindas.");

            resp.sendRedirect(req.getContextPath() + "/pacotes");

        } catch (SQLException e) {
            log("Falha ao cadastrar usuario", e);
            recusar(req, resp, nome, email,
                    "Não foi possível gravar no banco de dados. Abra /diagnostico para ver a causa.");
        }
    }

    private String validar(String nome, String email, String senha, String confirmacao) {
        if (nome.length() < 3)                      return "Informe seu nome completo.";
        if (!EMAIL_VALIDO.matcher(email).matches()) return "E-mail inválido.";
        if (senha == null || senha.length() < SENHA_MINIMA)
            return "A senha precisa ter ao menos " + SENHA_MINIMA + " caracteres.";
        if (!senha.equals(confirmacao))             return "As senhas não conferem.";
        return null;
    }

    private void recusar(HttpServletRequest req, HttpServletResponse resp,
                         String nome, String email, String mensagem)
            throws ServletException, IOException {
        req.setAttribute("erro", mensagem);
        req.setAttribute("nomeInformado", nome);
        req.setAttribute("emailInformado", email);
        req.getRequestDispatcher("/WEB-INF/jsp/cadastro.jsp").forward(req, resp);
    }
}
