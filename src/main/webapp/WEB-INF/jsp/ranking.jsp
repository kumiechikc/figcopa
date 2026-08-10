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
          <svg class="icone icone-g" style="color:var(--aviso)"><use href="#i-ranking"/></svg>
          <h3>Colecionadores com mais figurinhas no álbum</h3>
          <span class="secundario pequeno">empate desfeito por trocas concluídas</span>
        </div>

        <c:choose>
          <c:when test="${empty ranking}">
            <div class="vazio">
              <svg><use href="#i-usuarios"/></svg>
              <strong>Ranking vazio</strong>
              Ainda não há colecionadores com figurinhas cadastradas.
            </div>
          </c:when>

          <c:otherwise>
            <div class="rolagem-x"><table class="tabela">
              <thead>
                <tr><th>#</th><th>Colecionador</th><th>Álbum</th><th>Trocas</th><th>Reputação</th></tr>
              </thead>
              <tbody>
                <c:forEach var="u" items="${ranking}" varStatus="s">
                  <tr style="${u.id eq usuarioLogado.id ? 'background:rgba(124,58,227,.09)' : ''}">
                    <td class="num" style="width:36px">
                      <c:choose>
                        <c:when test="${s.index eq 0}">
                          <svg class="icone" style="color:var(--aviso)"><use href="#i-coroa"/></svg>
                        </c:when>
                        <c:otherwise>${s.index + 1}</c:otherwise>
                      </c:choose>
                    </td>
                    <td>
                      <span style="display:flex;align-items:center;gap:8px">
                        <span class="avatar" style="width:26px;height:26px;font-size:10px">${u.iniciais}</span>
                        <span>
                          <b style="display:block;font-size:13px"><c:out value="${u.nome}"/></b>
                          <span class="mono-min secundario">${u.apelido}</span>
                        </span>
                        <c:if test="${u.id eq usuarioLogado.id}">
                          <span class="tag tag-roxo">você</span>
                        </c:if>
                      </span>
                    </td>
                    <td style="width:200px">
                      <span class="num pequeno">${u.figurinhasObtidas}/${resumo.totalCatalogo}</span>
                      <span class="barra fina" style="margin-top:4px">
                        <i data-largura="${resumo.totalCatalogo eq 0 ? 0 :
                             (u.figurinhasObtidas * 100) / resumo.totalCatalogo}"></i>
                      </span>
                    </td>
                    <td class="num">${u.trocasConcluidas}</td>
                    <td class="num" style="color:var(--aviso)">
                      <fmt:formatNumber value="${u.reputacaoMedia}" pattern="0.0"/>
                    </td>
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
