<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isErrorPage="true" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="titulo" value="Algo deu errado" scope="request"/>
<%@ include file="frag/cabeca.jsp" %>
<body class="tela-acesso">
<%@ include file="frag/icones.jsp" %>

<main class="caixa-acesso" style="text-align:center">
  <svg class="icone" style="width:38px;height:38px;color:var(--erro);margin-bottom:12px"><use href="#i-alerta"/></svg>
  <h2 style="margin-bottom:8px">Não foi possível abrir esta página</h2>
  <p class="secundario">
    <c:choose>
      <c:when test="${pageContext.errorData.statusCode eq 404}">
        O endereço não existe neste sistema.
      </c:when>
      <c:otherwise>
        Houve uma falha ao processar a requisição. Se o problema for de banco,
        a página de diagnóstico aponta a causa exata.
      </c:otherwise>
    </c:choose>
  </p>
  <p class="mono-min secundario">código ${pageContext.errorData.statusCode}</p>
  <div style="display:flex;gap:12px;justify-content:center;margin-top:16px">
    <a href="${ctx}/dashboard" class="btn btn-primario">Voltar ao início</a>
    <a href="${ctx}/diagnostico" class="btn btn-secundario">Diagnóstico</a>
  </div>
</main>
</body>
</html>
