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

      <div class="grade grade-perfil" style="align-items:start">

        <section class="card">
          <div style="text-align:center;padding:8px 0 16px">
            <span class="avatar" style="width:64px;height:64px;font-size:22px;margin:0 auto 12px">
              ${usuarioLogado.iniciais}
            </span>
            <h2><c:out value="${usuarioLogado.nome}"/></h2>
            <p class="mono-min secundario" style="margin:2px 0 0">${usuarioLogado.apelido}</p>
            <c:if test="${usuarioLogado.admin}">
              <span class="tag tag-roxo" style="margin-top:8px">administrador</span>
            </c:if>
          </div>

          <div class="rolagem-x"><table class="tabela">
            <tbody>
              <tr>
                <td class="secundario">Reputação</td>
                <td style="text-align:right">
                  <span class="num" style="color:var(--aviso)">
                    <fmt:formatNumber value="${usuarioLogado.reputacaoMedia}" pattern="0.0"/>
                  </span>
                  <svg class="icone" style="width:12px;height:12px;color:var(--aviso);vertical-align:-1px"><use href="#i-estrela"/></svg>
                </td>
              </tr>
              <tr>
                <td class="secundario">Trocas concluídas</td>
                <td class="num" style="text-align:right">${trocasConcluidas}</td>
              </tr>
              <tr>
                <td class="secundario">Álbum</td>
                <td class="num" style="text-align:right">${resumo.obtidas}/${resumo.totalCatalogo} · ${resumo.percentual}%</td>
              </tr>
              <tr>
                <td class="secundario">Repetidas p/ troca</td>
                <td class="num" style="text-align:right">${resumo.repetidasDisponiveis}</td>
              </tr>
              <tr>
                <td class="secundario">Valor estimado</td>
                <td class="num" style="text-align:right">
                  R$ <fmt:formatNumber value="${resumo.valorEstimado}" pattern="#,##0.00"/>
                </td>
              </tr>
              <c:if test="${not empty usuarioLogado.membroDesde}">
                <tr>
                  <td class="secundario">Membro desde</td>
                  <td class="mono-min" style="text-align:right">${usuarioLogado.membroDesde}</td>
                </tr>
              </c:if>
            </tbody>
          </table></div>

          <div class="barra" style="margin-top:16px">
            <i data-largura="${resumo.percentual}"></i>
          </div>
        </section>

        <section>
          <div class="card" style="margin-bottom:16px">
            <div class="card-cabecalho">
              <svg class="icone" style="color:var(--roxo-400)"><use href="#i-troca"/></svg>
              <h3>Trocas recentes</h3>
              <a href="${ctx}/historico" class="pequeno">Ver tudo</a>
            </div>

            <c:choose>
              <c:when test="${empty trocas}">
                <div class="vazio">
                  <svg><use href="#i-caixa-vazia"/></svg>
                  <strong>Nenhuma troca ainda</strong>
                  Sua reputação começa a ser construída na primeira troca concluída.
                </div>
              </c:when>
              <c:otherwise>
                <div class="rolagem-x"><table class="tabela">
                  <thead><tr><th>Código</th><th>Parceiro</th><th>Troca</th><th>Quando</th><th>Status</th></tr></thead>
                  <tbody>
                    <c:forEach var="t" items="${trocas}">
                      <c:set var="souProponente" value="${t.proponente.id eq usuarioLogado.id}"/>
                      <c:set var="parceiro" value="${souProponente ? t.receptor : t.proponente}"/>
                      <tr>
                        <td class="mono-min">
                          <a href="${ctx}/troca?codigo=${t.codigo}">#${t.codigo}</a>
                        </td>
                        <td class="mono-min">${parceiro.apelido}</td>
                        <td class="num secundario">${t.placar}</td>
                        <td class="mono-min secundario">${t.quando}</td>
                        <td><span class="tag ${t.classeTag}">${t.statusExibicao}</span></td>
                      </tr>
                    </c:forEach>
                  </tbody>
                </table></div>
              </c:otherwise>
            </c:choose>
          </div>

          <div class="card">
            <div class="card-cabecalho">
              <svg class="icone" style="color:var(--aviso)"><use href="#i-catalogo"/></svg>
              <h3>Minhas repetidas</h3>
              <span class="mono-min secundario">${repetidas.size()} títulos</span>
            </div>

            <c:choose>
              <c:when test="${empty repetidas}">
                <div class="vazio">
                  <svg><use href="#i-caixa-vazia"/></svg>
                  <strong>Sem repetidas no momento</strong>
                  Abra pacotes para gerar cópias sobrando — são elas que viram moeda de troca.
                  <div style="margin-top:16px">
                    <a href="${ctx}/pacotes" class="btn btn-primario">Abrir pacote</a>
                  </div>
                </div>
              </c:when>
              <c:otherwise>
                <div class="grade-figurinhas">
                  <c:forEach var="f" items="${repetidas}">
                    <tc:carta fig="${f}" mostrarStatus="true"/>
                  </c:forEach>
                </div>
              </c:otherwise>
            </c:choose>
          </div>
        </section>
      </div>

    </main>
    <%@ include file="frag/rodape.jsp" %>
  </div>
</div>

<script src="${ctx}/assets/js/app.js"></script>
</body>
</html>
