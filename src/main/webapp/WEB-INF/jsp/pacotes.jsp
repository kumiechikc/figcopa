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

      <c:if test="${not empty avisoFlash}">
        <div class="alerta alerta-sucesso surge">
          <svg class="icone"><use href="#i-ok"/></svg><span><c:out value="${avisoFlash}"/></span>
        </div>
      </c:if>
      <c:if test="${not empty erroFlash}">
        <div class="alerta alerta-erro surge">
          <svg class="icone"><use href="#i-alerta"/></svg><span><c:out value="${erroFlash}"/></span>
        </div>
      </c:if>

      <div class="grade grade-lista" style="align-items:start">

        <%-- Coluna esquerda: meus pacotes --%>
        <aside>
          <div class="nav-grupo" style="padding:0 0 8px">Meus pacotes</div>

          <c:choose>
            <c:when test="${empty meusPacotes}">
              <div class="card">
                <div class="vazio" style="padding:24px 12px">
                  <svg><use href="#i-pacote"/></svg>
                  <strong>Sem pacotes agora</strong>
                  Você ganha um pacote diário por login e pacotes extras por trocas concluídas.
                </div>
              </div>
            </c:when>

            <c:otherwise>
              <c:forEach var="p" items="${meusPacotes}">
                <article class="pacote-card">
                  <span class="arte ${p.classeArte}">
                    <svg class="icone icone-g">
                      <use href="${p.tipo eq 'ESPECIAL' ? '#i-brilho' : (p.tipo eq 'DIARIO' ? '#i-presente' : '#i-pacote')}"/>
                    </svg>
                  </span>
                  <div style="flex:1;min-width:0">
                    <b style="font-family:'Saira',sans-serif;font-size:13px">${p.tipoExibicao}</b>
                    <p class="secundario pequeno" style="margin:2px 0 8px">${p.descricao}</p>
                    <form method="post" action="${ctx}/pacotes">
                      <input type="hidden" name="idPacote" value="${p.id}">
                      <button type="submit"
                              class="btn ${p.tipo eq 'ESPECIAL' ? 'btn-primario' : 'btn-secundario'} btn-bloco"
                              style="padding:7px 12px;font-size:12px">
                        <svg class="icone"><use href="#i-pacote"/></svg> Abrir agora
                      </button>
                    </form>
                  </div>
                </article>
              </c:forEach>
            </c:otherwise>
          </c:choose>

          <c:if test="${not empty ultimasAberturas}">
            <div class="nav-grupo" style="padding:16px 0 8px">Últimas aberturas</div>
            <div class="card" style="padding:12px">
              <c:forEach var="p" items="${ultimasAberturas}">
                <div style="display:flex;align-items:center;gap:8px;padding:6px 0;font-size:12px">
                  <svg class="icone" style="color:var(--cinza-400)"><use href="#i-pacote"/></svg>
                  <span style="flex:1;min-width:0">
                    ${p.tipoExibicao} — <span class="num">${p.qtdNovas}</span> novas,
                    <span class="num">${p.qtdRepetidas}</span> repetidas
                  </span>
                  <span class="mono-min secundario" style="white-space:nowrap">${p.abertoEm}</span>
                </div>
              </c:forEach>
            </div>
          </c:if>
        </aside>

        <%-- Coluna direita: resultado da abertura --%>
        <section class="card">
          <c:choose>

            <c:when test="${empty pacoteAberto}">
              <div class="vazio" style="padding:64px 24px">
                <svg><use href="#i-pacote"/></svg>
                <strong>Escolha um pacote para abrir</strong>
                Cada pacote traz 7 figurinhas sorteadas pela probabilidade real de cada raridade:
                78% comum, 15% brilhante, 4% rara e 0,53% lendária.
              </div>
            </c:when>

            <c:otherwise>
              <div class="card-cabecalho">
                <svg class="icone icone-g" style="color:var(--roxo-400)"><use href="#i-brilho"/></svg>
                <h3>${pacoteAberto.tipoExibicao} aberto!</h3>
                <span class="secundario pequeno">
                  <b class="num">${pacoteAberto.conteudo.size()}</b> figurinhas reveladas ·
                  <b class="num" style="color:var(--sucesso)">${pacoteAberto.qtdNovas}</b> novas ·
                  <b class="num" style="color:var(--aviso)">${pacoteAberto.qtdRepetidas}</b> repetidas
                </span>
              </div>

              <%-- As cartas comecam viradas e giram em sequencia (stagger ~120ms) --%>
              <div class="revelacao" id="revelacao">
                <c:forEach var="f" items="${pacoteAberto.conteudo}" varStatus="s">
                  <div class="carta-flip" data-ordem="${s.index}" data-lendaria="${f.raridade.lendaria}">
                    <div class="giro">
                      <div class="verso"><span>?</span></div>
                      <div class="frente">
                        <tc:carta fig="${f}"
                                  etiqueta="${f.novaNoAlbum ? 'NOVA' : 'REPETIDA'}"
                                  classeExtra="${f.raridade.lendaria ? 'brilho-lendaria' : ''}"/>
                      </div>
                    </div>
                  </div>
                </c:forEach>
              </div>

              <c:if test="${pacoteAberto.temLendaria}">
                <c:forEach var="f" items="${pacoteAberto.conteudo}">
                  <c:if test="${f.raridade.lendaria}">
                    <div class="alerta alerta-info" style="margin:24px 0 0;border-color:rgba(168,85,247,.45)">
                      <svg class="icone" style="color:var(--r-lendaria)"><use href="#i-coroa"/></svg>
                      <span>
                        <b>Lendária desbloqueada — <c:out value="${f.nomeJogador}"/>
                          (<c:out value="${f.selecao}"/>)</b><br>
                        <span class="secundario pequeno">
                          Apenas 0,53% dos pacotes trazem uma lendária. Valor de referência de mercado:
                          <span class="num">R$ <fmt:formatNumber value="${f.raridade.valorReferencia}" pattern="#,##0"/></span>.
                        </span>
                      </span>
                    </div>
                  </c:if>
                </c:forEach>
              </c:if>

              <div style="display:flex;align-items:center;gap:12px;margin-top:24px;
                          padding-top:16px;border-top:1px solid var(--azul-800);flex-wrap:wrap">
                <span class="pequeno secundario">
                  <b class="num" style="color:var(--sucesso)">${pacoteAberto.qtdNovas}</b> novas no álbum ·
                  <b class="num" style="color:var(--aviso)">${pacoteAberto.qtdRepetidas}</b> liberadas para troca
                </span>
                <span style="margin-left:auto;display:flex;gap:8px">
                  <a href="${ctx}/trocas" class="btn btn-secundario">
                    <svg class="icone"><use href="#i-troca"/></svg> Ofertar repetidas
                  </a>
                  <c:if test="${not empty meusPacotes}">
                    <form method="post" action="${ctx}/pacotes" style="display:inline">
                      <input type="hidden" name="idPacote" value="${meusPacotes[0].id}">
                      <button type="submit" class="btn btn-primario">Abrir outro pacote</button>
                    </form>
                  </c:if>
                  <c:if test="${empty meusPacotes}">
                    <a href="${ctx}/album" class="btn btn-primario">Ver no álbum</a>
                  </c:if>
                </span>
              </div>
            </c:otherwise>
          </c:choose>
        </section>
      </div>

    </main>
    <%@ include file="frag/rodape.jsp" %>
  </div>
</div>

<script src="${ctx}/assets/js/app.js"></script>
<script src="${ctx}/assets/js/revelacao.js"></script>
</body>
</html>
