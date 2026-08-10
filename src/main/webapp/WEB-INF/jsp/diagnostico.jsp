<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="titulo" value="Diagnóstico do ambiente" scope="request"/>
<%@ include file="frag/cabeca.jsp" %>
<body class="tela-acesso" style="align-items:flex-start;padding-top:48px">
<%@ include file="frag/icones.jsp" %>

<main style="width:100%;max-width:680px">

  <a href="${ctx}/" class="marca" style="text-decoration:none;color:inherit;justify-content:center">
    <span class="escudo"><svg class="icone" viewBox="0 0 24 24"><path d="M12 4l4 4h-8zM12 20l-4-4h8z" fill="currentColor" stroke="none"/></svg></span>
    <span class="marca-nome">The Champ<span>ions</span></span>
  </a>

  <div class="card" style="margin-top:24px">
    <div class="card-cabecalho">
      <svg class="icone icone-g" style="color:var(--roxo-400)"><use href="#i-banco"/></svg>
      <h3>Conexão com o banco</h3>
      <c:choose>
        <c:when test="${empty erro}"><span class="tag tag-sucesso">Conectado</span></c:when>
        <c:otherwise><span class="tag tag-erro">Sem conexão</span></c:otherwise>
      </c:choose>
    </div>

    <c:choose>
      <c:when test="${not empty erro}">
        <div class="alerta alerta-erro">
          <svg class="icone"><use href="#i-alerta"/></svg>
          <span>
            <b>O Java não conseguiu falar com o MySQL.</b><br>
            <span class="mono-min"><c:out value="${erro}"/></span>
          </span>
        </div>
        <p class="secundario pequeno" style="margin-bottom:0">
          Verifique nesta ordem: (1) o MySQL está iniciado no XAMPP;
          (2) o banco <span class="mono-min">the_champions</span> existe — importe
          <span class="mono-min">01_schema.sql</span> e <span class="mono-min">02_dados.sql</span> no phpMyAdmin;
          (3) usuário e senha em <span class="mono-min">database.properties</span> batem com os do seu MySQL.
        </p>
      </c:when>

      <c:otherwise>
        <div class="rolagem-x"><table class="tabela">
          <thead>
            <tr><th>Tabela</th><th>Registros</th><th>Esperado</th><th>Situação</th></tr>
          </thead>
          <tbody>
            <c:forEach var="e" items="${contagens}">
              <c:set var="alvo" value="${esperado[e.key]}"/>
              <tr>
                <td class="mono-min">${e.key}</td>
                <td class="num"><b>${e.value}</b></td>
                <td class="num secundario">${alvo}</td>
                <td>
                  <c:choose>
                    <c:when test="${e.value eq alvo}"><span class="tag tag-sucesso">ok</span></c:when>
                    <c:when test="${e.value eq 0}"><span class="tag tag-erro">vazia</span></c:when>
                    <c:otherwise><span class="tag tag-aviso">divergente</span></c:otherwise>
                  </c:choose>
                </td>
              </tr>
            </c:forEach>
          </tbody>
        </table></div>

        <div class="alerta alerta-sucesso" style="margin:16px 0 0">
          <svg class="icone"><use href="#i-ok"/></svg>
          <span>A cadeia <b>Tomcat &rarr; JDBC &rarr; MySQL</b> está funcionando.
            Se as quatro linhas estiverem "ok", a carga de dados está completa.</span>
        </div>
      </c:otherwise>
    </c:choose>
  </div>

  <div class="card" style="margin-top:16px">
    <div class="card-cabecalho">
      <svg class="icone icone-g" style="color:var(--cinza-400)"><use href="#i-info"/></svg>
      <h3>Ambiente</h3>
    </div>
    <div class="rolagem-x"><table class="tabela">
      <tbody>
        <tr><td class="secundario">Servidor</td><td class="mono-min"><c:out value="${servidor}"/></td></tr>
        <tr><td class="secundario">Java</td><td class="mono-min"><c:out value="${versaoJava}"/></td></tr>
        <tr><td class="secundario">URL do banco</td><td class="mono-min"><c:out value="${urlBanco}"/></td></tr>
        <tr><td class="secundario">Origem da config</td><td class="mono-min"><c:out value="${origemConfig}"/></td></tr>
      </tbody>
    </table></div>
  </div>

  <div style="display:flex;gap:12px;margin-top:16px">
    <a href="${ctx}/login" class="btn btn-primario">Ir para o login <svg class="icone"><use href="#i-avancar"/></svg></a>
    <a href="${ctx}/" class="btn btn-secundario">Página inicial</a>
  </div>

</main>
</body>
</html>
