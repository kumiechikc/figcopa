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

      <%-- Filtros por status e raridade --%>
      <c:set var="base" value="${ehCatalogo ? '/catalogo' : '/album'}"/>
      <c:set var="qSelecao" value="${empty siglaSelecionada ? '' : '&selecao='.concat(siglaSelecionada)}"/>

      <div class="filtros">
        <a href="${ctx}${base}?x=1${qSelecao}"
           class="filtro ${empty statusSelecionado and empty raridadeSelecionada ? 'ativo' : ''}">Todas</a>
        <a href="${ctx}${base}?status=obtidas${qSelecao}"
           class="filtro ${statusSelecionado eq 'obtidas' ? 'ativo' : ''}">
          Obtidas <span class="cont">${resumo.obtidas}</span></a>
        <a href="${ctx}${base}?status=faltantes${qSelecao}"
           class="filtro ${statusSelecionado eq 'faltantes' ? 'ativo' : ''}">
          Faltantes <span class="cont">${resumo.faltantes}</span></a>
        <a href="${ctx}${base}?status=repetidas${qSelecao}"
           class="filtro ${statusSelecionado eq 'repetidas' ? 'ativo' : ''}">
          Repetidas <span class="cont">${resumo.repetidasDisponiveis}</span></a>

        <span style="width:12px"></span>

        <a href="${ctx}${base}?raridade=2${qSelecao}"
           class="filtro ${raridadeSelecionada eq '2' ? 'ativo' : ''}"
           style="border-color:rgba(34,211,238,.35);color:var(--r-brilhante)">Brilhantes</a>
        <a href="${ctx}${base}?raridade=3${qSelecao}"
           class="filtro ${raridadeSelecionada eq '3' ? 'ativo' : ''}"
           style="border-color:rgba(245,136,61,.35);color:var(--r-rara)">Raras</a>
        <a href="${ctx}${base}?raridade=4${qSelecao}"
           class="filtro ${raridadeSelecionada eq '4' ? 'ativo' : ''}"
           style="border-color:rgba(168,85,247,.4);color:var(--r-lendaria)">Lendárias</a>

        <span style="margin-left:auto;display:flex;align-items:center;gap:8px" class="secundario pequeno">
          <svg class="icone"><use href="#i-alta"/></svg>
          ${resumo.percentual}% do álbum completo
        </span>
      </div>

      <div class="album">

        <%-- Painel de selecoes --%>
        <aside class="card" style="padding:12px">
          <div class="nav-grupo" style="padding:4px 8px 8px">
            Seleções <span class="num" style="float:right">${selecoes.size()}</span>
          </div>

          <c:forEach var="s" items="${selecoes}">
            <a href="${ctx}/album?selecao=${s.sigla}" class="selecao-item ${s.sigla eq siglaSelecionada ? 'ativa' : ''}">
              <span class="linha">
                <span class="sigla">${s.sigla}</span>
                <span class="nome"><c:out value="${s.selecao}"/></span>
                <span class="frac">${s.obtidas}/${s.total}</span>
              </span>
              <span class="barra fina"><i data-largura="${s.percentual}"></i></span>
            </a>
          </c:forEach>
        </aside>

        <%-- Grid de figurinhas --%>
        <section>
          <c:if test="${not empty selecaoAtiva}">
            <div style="display:flex;align-items:center;gap:12px;margin-bottom:16px;flex-wrap:wrap">
              <h2><c:out value="${selecaoAtiva.selecao}"/></h2>
              <span class="secundario pequeno">
                <b class="num">${selecaoAtiva.obtidas}</b> de <b class="num">${selecaoAtiva.total}</b> obtidas
              </span>
              <c:if test="${selecaoAtiva.completa}">
                <span class="tag tag-sucesso">seleção completa</span>
              </c:if>
              <a href="${ctx}/trocas" class="btn btn-secundario" style="margin-left:auto;padding:6px 14px;font-size:12px">
                <svg class="icone"><use href="#i-troca"/></svg> Ofertar repetidas
              </a>
            </div>
          </c:if>

          <c:choose>
            <c:when test="${empty figurinhas}">
              <div class="card">
                <div class="vazio">
                  <svg><use href="#i-caixa-vazia"/></svg>
                  <strong>Nenhuma figurinha neste filtro</strong>
                  <c:choose>
                    <c:when test="${statusSelecionado eq 'repetidas'}">
                      Você ainda não tem cópias sobrando. Abra pacotes para gerar repetidas.
                    </c:when>
                    <c:when test="${statusSelecionado eq 'faltantes'}">
                      Nenhuma faltante aqui — esta parte do álbum está completa.
                    </c:when>
                    <c:otherwise>Tente outro filtro ou outra seleção.</c:otherwise>
                  </c:choose>
                  <div style="margin-top:16px">
                    <a href="${ctx}/pacotes" class="btn btn-primario">Abrir um pacote</a>
                  </div>
                </div>
              </div>
            </c:when>

            <c:otherwise>
              <div class="grade-figurinhas">
                <c:forEach var="f" items="${figurinhas}">
                  <tc:carta fig="${f}" mostrarStatus="true"/>
                </c:forEach>
              </div>

              <p class="secundario pequeno" style="margin-top:16px;display:flex;align-items:center;gap:8px">
                <svg class="icone" style="width:13px;height:13px"><use href="#i-info"/></svg>
                Figurinhas com selo <span class="repetida" style="position:static;display:inline-block">&times;N</span>
                são repetidas e podem entrar em ofertas de troca.
                Faltantes aparecem escurecidas com "?".
              </p>
            </c:otherwise>
          </c:choose>
        </section>
      </div>

    </main>
    <%@ include file="frag/rodape.jsp" %>
  </div>
</div>

<script src="${ctx}/assets/js/app.js"></script>
</body>
</html>
