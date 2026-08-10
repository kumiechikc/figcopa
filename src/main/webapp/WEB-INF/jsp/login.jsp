<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="titulo" value="Entrar" scope="request"/>
<%@ include file="frag/cabeca.jsp" %>
<body class="tela-acesso">
<%@ include file="frag/icones.jsp" %>

<main class="caixa-acesso surge">

  <a href="${ctx}/" class="marca" style="text-decoration:none;color:inherit">
    <span class="escudo"><svg class="icone" viewBox="0 0 24 24"><path d="M12 4l4 4h-8zM12 20l-4-4h8z" fill="currentColor" stroke="none"/></svg></span>
    <span class="marca-nome">The Champ<span>ions</span></span>
  </a>

  <h2 style="text-align:center;margin-bottom:4px">Entrar na sua conta</h2>
  <p class="secundario pequeno" style="text-align:center;margin-bottom:24px">
    Continue montando seu álbum da Copa 2026.
  </p>

  <c:if test="${param.saiu eq '1'}">
    <div class="alerta alerta-info">
      <svg class="icone"><use href="#i-ok"/></svg>
      <span>Você saiu da sua conta.</span>
    </div>
  </c:if>

  <c:if test="${not empty erro}">
    <div class="alerta alerta-erro" role="alert">
      <svg class="icone"><use href="#i-alerta"/></svg>
      <span><c:out value="${erro}"/></span>
    </div>
  </c:if>

  <form method="post" action="${ctx}/login" novalidate>
    <c:if test="${not empty param.destino}">
      <input type="hidden" name="destino" value="<c:out value='${param.destino}'/>">
    </c:if>

    <div class="campo">
      <label for="email">E-mail</label>
      <input type="email" id="email" name="email" autocomplete="email" required
             value="<c:out value='${emailInformado}'/>" placeholder="voce@email.com">
    </div>

    <div class="campo">
      <label for="senha">Senha</label>
      <input type="password" id="senha" name="senha" autocomplete="current-password" required
             placeholder="••••••">
    </div>

    <button type="submit" class="btn btn-primario btn-bloco">
      <svg class="icone"><use href="#i-cadeado"/></svg> Entrar
    </button>
  </form>

  <p class="secundario pequeno" style="text-align:center;margin:24px 0 0">
    Não tem conta? <a href="${ctx}/cadastro">Criar conta grátis</a>
  </p>

  <div style="border-top:1px solid var(--azul-800);margin-top:24px;padding-top:16px">
    <p class="mono-min secundario" style="margin-bottom:8px">CONTAS DE DEMONSTRAÇÃO · senha 123456</p>
    <div style="display:flex;flex-direction:column;gap:4px">
      <button type="button" class="btn btn-secundario preencher" data-email="vinicius.k@email.com"
              style="justify-content:flex-start;font-size:12px;padding:7px 12px">
        <span class="avatar" style="width:22px;height:22px;font-size:9px">VK</span>
        vinicius.k@email.com
        <span class="tag tag-roxo" style="margin-left:auto">álbum cheio</span>
      </button>
      <button type="button" class="btn btn-secundario preencher" data-email="pedro.marques@email.com"
              style="justify-content:flex-start;font-size:12px;padding:7px 12px">
        <span class="avatar" style="width:22px;height:22px;font-size:9px">PM</span>
        pedro.marques@email.com
        <span class="tag tag-neutro" style="margin-left:auto">parceiro</span>
      </button>
    </div>
  </div>

</main>

<script>
  // Preenche o formulario com a conta de demonstracao escolhida.
  document.querySelectorAll('.preencher').forEach(function (botao) {
    botao.addEventListener('click', function () {
      document.getElementById('email').value = botao.dataset.email;
      document.getElementById('senha').value = '123456';
      document.getElementById('senha').focus();
    });
  });
</script>
</body>
</html>
