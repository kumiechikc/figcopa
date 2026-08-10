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
          <svg class="icone"><use href="#i-ok"/></svg>
          <span><c:out value="${avisoFlash}"/></span>
        </div>
      </c:if>
      <c:if test="${not empty erroFlash}">
        <div class="alerta alerta-erro surge">
          <svg class="icone"><use href="#i-alerta"/></svg>
          <span><c:out value="${erroFlash}"/></span>
        </div>
      </c:if>

      <div class="grade grade-painel" style="margin-bottom:16px">

        <%-- Progresso do album --%>
        <section class="painel-progresso">
          <div style="display:flex;align-items:center;gap:8px;margin-bottom:12px">
            <svg class="icone" style="color:var(--roxo-400)"><use href="#i-album"/></svg>
            <span class="mono-min secundario" style="letter-spacing:.1em">PROGRESSO DO ÁLBUM · COPA 2026</span>
            <a href="${ctx}/album" class="btn btn-primario" style="margin-left:auto;padding:6px 14px;font-size:12px">
              Ver álbum <svg class="icone"><use href="#i-avancar"/></svg>
            </a>
          </div>

          <div style="display:flex;align-items:baseline;gap:12px">
            <span class="percentual num">${resumo.percentual}%</span>
            <span class="secundario">
              <b class="num">${resumo.obtidas}</b> de <b class="num">${resumo.totalCatalogo}</b> figurinhas
            </span>
          </div>

          <div class="barra" style="margin-top:12px" role="progressbar"
               aria-valuenow="${resumo.percentual}" aria-valuemin="0" aria-valuemax="100">
            <i data-largura="${resumo.percentual}"></i>
          </div>

          <div class="quebra-raridade">
            <div><b class="num">${resumo.faltantes}</b><small>faltantes</small></div>
            <div><b class="num" style="color:var(--aviso)">${resumo.repetidasDisponiveis}</b><small>repetidas p/ troca</small></div>
            <div><b class="num" style="color:var(--r-brilhante)">${resumo.brilhantes}</b><small>brilhantes</small></div>
            <div><b class="num" style="color:var(--r-rara)">${resumo.raras}</b><small>raras</small></div>
            <div><b class="num" style="color:var(--r-lendaria)">${resumo.lendarias}</b><small>lendárias</small></div>
          </div>
        </section>

        <%-- Acoes rapidas --%>
        <div class="grade grade-2" style="align-content:start">
          <a href="${ctx}/pacotes" class="acao-rapida">
            <span class="ic"><svg class="icone"><use href="#i-pacote"/></svg></span>
            <span>
              <b>Abrir pacote</b>
              <small>
                <c:choose>
                  <c:when test="${pacotesDisponiveis gt 0}">${pacotesDisponiveis} disponíveis agora</c:when>
                  <c:otherwise>nenhum pacote no momento</c:otherwise>
                </c:choose>
              </small>
            </span>
          </a>

          <a href="${ctx}/trocas" class="acao-rapida">
            <span class="ic"><svg class="icone"><use href="#i-brilho"/></svg></span>
            <span>
              <b>Ver matches</b>
              <small>${totalMatches} compatíveis com você</small>
            </span>
          </a>

          <a href="${ctx}/album?status=repetidas" class="acao-rapida">
            <span class="ic"><svg class="icone"><use href="#i-troca"/></svg></span>
            <span>
              <b>Minhas repetidas</b>
              <small>${resumo.repetidasDisponiveis} cópias sobrando</small>
            </span>
          </a>

          <a href="${ctx}/catalogo?raridade=4" class="acao-rapida">
            <span class="ic"><svg class="icone"><use href="#i-coroa"/></svg></span>
            <span>
              <b>Raras &amp; valores</b>
              <small>coleção vale R$ <fmt:formatNumber value="${resumo.valorEstimado}" pattern="#,##0.00"/></small>
            </span>
          </a>
        </div>
      </div>

      <div class="grade grade-painel">

        <%-- Trocas recentes --%>
        <section class="card">
          <div class="card-cabecalho">
            <svg class="icone" style="color:var(--roxo-400)"><use href="#i-troca"/></svg>
            <h3>Trocas recentes</h3>
            <a href="${ctx}/historico" class="pequeno">Ver histórico completo</a>
          </div>

          <c:choose>
            <c:when test="${empty trocasRecentes}">
              <div class="vazio">
                <svg><use href="#i-caixa-vazia"/></svg>
                <strong>Nenhuma troca ainda</strong>
                O sistema já encontrou ${totalMatches} parceiros compatíveis com o seu álbum.
                <div style="margin-top:16px">
                  <a href="${ctx}/trocas" class="btn btn-primario">Ver matches</a>
                </div>
              </div>
            </c:when>

            <c:otherwise>
              <div class="rolagem-x"><table class="tabela">
                <thead>
                  <tr><th>Parceiro</th><th>Troca</th><th>Quando</th><th>Status</th></tr>
                </thead>
                <tbody>
                  <c:forEach var="t" items="${trocasRecentes}">
                    <c:set var="souProponente" value="${t.proponente.id eq usuarioLogado.id}"/>
                    <c:set var="parceiro" value="${souProponente ? t.receptor : t.proponente}"/>
                    <tr>
                      <td>
                        <a href="${ctx}/troca?codigo=${t.codigo}"
                           style="display:flex;align-items:center;gap:8px;color:inherit">
                          <span class="avatar" style="width:24px;height:24px;font-size:9px">${parceiro.iniciais}</span>
                          <span class="mono-min">${parceiro.apelido}</span>
                        </a>
                      </td>
                      <td class="secundario pequeno">
                        <c:set var="meus" value="${souProponente ? t.itensProponente : t.itensReceptor}"/>
                        <c:set var="dele" value="${souProponente ? t.itensReceptor : t.itensProponente}"/>
                        <c:forEach var="f" items="${meus}" varStatus="s"><c:if test="${s.index lt 2}"
                          ><c:out value="${f.nomeJogador}"/><c:if test="${!s.last and s.index lt 1}">, </c:if></c:if></c:forEach
                        ><c:if test="${meus.size() gt 2}"> +${meus.size() - 2}</c:if>
                        <svg class="icone" style="width:12px;height:12px;vertical-align:-2px;margin:0 4px;color:var(--roxo-400)"><use href="#i-troca"/></svg>
                        <c:forEach var="f" items="${dele}" varStatus="s"><c:if test="${s.index lt 2}"
                          ><c:out value="${f.nomeJogador}"/><c:if test="${!s.last and s.index lt 1}">, </c:if></c:if></c:forEach
                        ><c:if test="${dele.size() gt 2}"> +${dele.size() - 2}</c:if>
                      </td>
                      <td class="mono-min secundario">${t.quando}</td>
                      <td><span class="tag ${t.classeTag}">${t.statusExibicao}</span></td>
                    </tr>
                  </c:forEach>
                </tbody>
              </table></div>

              <c:if test="${pendentesRecebidas gt 0}">
                <div class="alerta alerta-aviso" style="margin:16px 0 0">
                  <svg class="icone"><use href="#i-relogio"/></svg>
                  <span>
                    Você tem <b>${pendentesRecebidas}</b>
                    ${pendentesRecebidas eq 1 ? 'proposta aguardando' : 'propostas aguardando'} sua confirmação.
                  </span>
                </div>
              </c:if>
            </c:otherwise>
          </c:choose>
        </section>

        <%-- Notificacoes --%>
        <section class="card">
          <div class="card-cabecalho">
            <svg class="icone" style="color:var(--roxo-400)"><use href="#i-sino"/></svg>
            <h3>Notificações</h3>
            <span class="mono-min secundario">${naoLidas} não lidas</span>
          </div>

          <c:choose>
            <c:when test="${empty notificacoes}">
              <div class="vazio">
                <svg><use href="#i-sino"/></svg>
                <strong>Tudo em dia</strong>
                Avisos de troca, pacote e avaliação aparecem aqui.
              </div>
            </c:when>
            <c:otherwise>
              <div style="display:flex;flex-direction:column;gap:4px">
                <c:forEach var="n" items="${notificacoes}">
                  <article class="notificacao ${n.lida ? '' : 'nao-lida'}">
                    <span class="ic"><svg class="icone"><use href="#${n.icone}"/></svg></span>
                    <span>
                      <p><c:out value="${n.mensagem}"/></p>
                      <span class="quando">${n.quando}</span>
                    </span>
                  </article>
                </c:forEach>
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
</body>
</html>
