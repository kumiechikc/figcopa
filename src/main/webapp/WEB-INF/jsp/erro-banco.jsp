<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ include file="frag/cabeca.jsp" %>
<body class="tela-acesso">
<%@ include file="frag/icones.jsp" %>

<main style="width:100%;max-width:560px">
  <div class="card">
    <div class="card-cabecalho">
      <svg class="icone icone-g" style="color:var(--erro)"><use href="#i-banco"/></svg>
      <h3>Falha ao consultar o banco</h3>
    </div>

    <p class="secundario">
      A aplicação subiu, mas a consulta ao MySQL falhou. Isso quase sempre é
      ambiente, não código.
    </p>

    <div class="alerta alerta-erro">
      <svg class="icone"><use href="#i-alerta"/></svg>
      <span class="mono-min"><c:out value="${erroBanco}"/></span>
    </div>

    <ol class="secundario pequeno" style="padding-left:18px;line-height:1.9">
      <li>O MySQL está iniciado no painel do XAMPP?</li>
      <li>O banco <span class="mono-min">the_champions</span> existe? Importe
          <span class="mono-min">01_schema.sql</span> e depois
          <span class="mono-min">02_dados.sql</span> no phpMyAdmin.</li>
      <li>Usuário e senha em <span class="mono-min">src/main/resources/database.properties</span>
          batem com os do seu MySQL?</li>
    </ol>

    <div style="display:flex;gap:12px;margin-top:16px">
      <a href="${ctx}/diagnostico" class="btn btn-primario">Abrir diagnóstico</a>
      <a href="${ctx}/dashboard" class="btn btn-secundario">Tentar de novo</a>
    </div>
  </div>
</main>
</body>
</html>
