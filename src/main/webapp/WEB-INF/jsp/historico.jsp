<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ include file="frag/cabeca.jsp" %>
<body>
<%@ include file="frag/icones.jsp" %>

<div class="app">
  <%@ include file="frag/sidebar.jsp" %>

  <div class="conteudo">
    <%@ include file="frag/topbar.jsp" %>

    <main class="pagina">

      <div class="card">
        <div class="card-cabecalho">
          <svg class="icone icone-g" style="color:var(--roxo-400)"><use href="#i-historico"/></svg>
          <h3>Todas as suas trocas</h3>
          <span class="mono-min secundario">${trocas.size()} registros</span>
        </div>

        <c:choose>
          <c:when test="${empty trocas}">
            <div class="vazio">
              <svg><use href="#i-caixa-vazia"/></svg>
              <strong>Nenhuma troca registrada</strong>
              Assim que você fechar a primeira troca, ela aparece aqui com data e status.
              <div style="margin-top:16px">
                <a href="${ctx}/trocas" class="btn btn-primario">Buscar parceiros</a>
              </div>
            </div>
          </c:when>

          <c:otherwise>
            <div class="rolagem-x"><table class="tabela">
              <thead>
                <tr><th>Código</th><th>Parceiro</th><th>Eu envio</th><th>Eu recebo</th><th>Quando</th><th>Status</th></tr>
              </thead>
              <tbody>
                <c:forEach var="t" items="${trocas}">
                  <c:set var="souProponente" value="${t.proponente.id eq usuarioLogado.id}"/>
                  <c:set var="parceiro" value="${souProponente ? t.receptor : t.proponente}"/>
                  <c:set var="meus" value="${souProponente ? t.itensProponente : t.itensReceptor}"/>
                  <c:set var="dele" value="${souProponente ? t.itensReceptor : t.itensProponente}"/>
                  <tr>
                    <td class="mono-min"><a href="${ctx}/troca?codigo=${t.codigo}">#${t.codigo}</a></td>
                    <td>
                      <span style="display:flex;align-items:center;gap:8px">
                        <span class="avatar" style="width:24px;height:24px;font-size:9px">${parceiro.iniciais}</span>
                        <span class="mono-min">${parceiro.apelido}</span>
                      </span>
                    </td>
                    <td class="secundario pequeno">
                      <c:forEach var="f" items="${meus}" varStatus="s"><c:out value="${f.nomeJogador}"/><c:if test="${!s.last}">, </c:if></c:forEach>
                      <c:if test="${empty meus}">—</c:if>
                    </td>
                    <td class="secundario pequeno">
                      <c:forEach var="f" items="${dele}" varStatus="s"><c:out value="${f.nomeJogador}"/><c:if test="${!s.last}">, </c:if></c:forEach>
                      <c:if test="${empty dele}">—</c:if>
                    </td>
                    <td class="mono-min secundario">${t.quando}</td>
                    <td><span class="tag ${t.classeTag}">${t.statusExibicao}</span></td>
                  </tr>
                </c:forEach>
              </tbody>
            </table></div>
          </c:otherwise>
        </c:choose>
      </div>

    </main>
    <%@ include file="frag/rodape.jsp" %>
  </div>
</div>

<script src="${ctx}/assets/js/app.js"></script>
</body>
</html>
