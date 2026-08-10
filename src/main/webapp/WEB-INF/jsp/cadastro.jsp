<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="titulo" value="Criar conta" scope="request"/>
<%@ include file="frag/cabeca.jsp" %>
<body class="tela-acesso">
<%@ include file="frag/icones.jsp" %>

<main class="caixa-acesso surge">

  <a href="${ctx}/" class="marca" style="text-decoration:none;color:inherit">
    <span class="escudo"><svg class="icone" viewBox="0 0 24 24"><path d="M12 4l4 4h-8zM12 20l-4-4h8z" fill="currentColor" stroke="none"/></svg></span>
    <span class="marca-nome">The Champ<span>ions</span></span>
  </a>

  <h2 style="text-align:center;margin-bottom:4px">Criar sua conta</h2>
  <p class="secundario pequeno" style="text-align:center;margin-bottom:24px">
    Leva 30 segundos. Você começa com um Pacote Especial.
  </p>

  <c:if test="${not empty erro}">
    <div class="alerta alerta-erro" role="alert">
      <svg class="icone"><use href="#i-alerta"/></svg>
      <span><c:out value="${erro}"/></span>
    </div>
  </c:if>

  <form method="post" action="${ctx}/cadastro" novalidate>
    <div class="campo">
      <label for="nome">Nome completo</label>
      <input type="text" id="nome" name="nome" autocomplete="name" required
             value="<c:out value='${nomeInformado}'/>" placeholder="Seu nome">
    </div>

    <div class="campo">
      <label for="email">E-mail</label>
      <input type="email" id="email" name="email" autocomplete="email" required
             value="<c:out value='${emailInformado}'/>" placeholder="voce@email.com">
    </div>

    <div class="campo">
      <label for="senha">Senha</label>
      <input type="password" id="senha" name="senha" autocomplete="new-password" required
             minlength="6" placeholder="mínimo 6 caracteres">
      <span class="dica">Guardamos apenas o hash BCrypt — nunca a senha em texto puro.</span>
    </div>

    <div class="campo">
      <label for="confirmacao">Repetir senha</label>
      <input type="password" id="confirmacao" name="confirmacao" autocomplete="new-password" required
             minlength="6" placeholder="digite de novo">
    </div>

    <button type="submit" class="btn btn-primario btn-bloco">
      <svg class="icone"><use href="#i-usuario"/></svg> Criar conta
    </button>
  </form>

  <p class="secundario pequeno" style="text-align:center;margin:24px 0 0">
    Já tem conta? <a href="${ctx}/login">Entrar</a>
  </p>

</main>
</body>
</html>
