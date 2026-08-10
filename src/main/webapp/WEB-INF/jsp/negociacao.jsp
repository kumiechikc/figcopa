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
        <div class="alerta alerta-sucesso anima-confirmado">
          <svg class="icone"><use href="#i-ok"/></svg><span><c:out value="${avisoFlash}"/></span>
        </div>
      </c:if>
      <c:if test="${not empty erroFlash}">
        <div class="alerta alerta-erro surge">
          <svg class="icone"><use href="#i-alerta"/></svg><span><c:out value="${erroFlash}"/></span>
        </div>
      </c:if>

      <c:if test="${troca.pendente or troca.status eq 'ACEITA'}">
        <div style="display:flex;justify-content:flex-end;margin-bottom:12px">
          <span class="contador-expira">
            <svg class="icone" style="width:13px;height:13px"><use href="#i-relogio"/></svg>
            expira em ${troca.tempoParaExpirar}
          </span>
        </div>
      </c:if>

      <div class="negociacao">

        <%-- ================= LADO ESQUERDO: o que EU envio ================= --%>
        <section class="card">
          <div style="display:flex;align-items:center;gap:12px;margin-bottom:16px">
            <span class="avatar avatar-g">${usuarioLogado.iniciais}</span>
            <div style="flex:1">
              <b style="font-family:'Saira',sans-serif"><c:out value="${usuarioLogado.nome}"/>
                <span class="secundario pequeno">(você)</span></b>
              <span class="secundario pequeno" style="display:flex;align-items:center;gap:5px">
                <svg class="icone" style="width:12px;height:12px;color:var(--aviso)"><use href="#i-estrela"/></svg>
                <span class="num"><fmt:formatNumber value="${usuarioLogado.reputacaoMedia}" pattern="0.0"/></span>
              </span>
            </div>
            <c:choose>
              <c:when test="${euConfirmei}"><span class="tag tag-sucesso">confirmou</span></c:when>
              <c:otherwise><span class="tag tag-aviso">falta confirmar</span></c:otherwise>
            </c:choose>
          </div>

          <h4 style="font-family:'Manrope',sans-serif;font-size:10px;font-weight:700;letter-spacing:.09em;
                     text-transform:uppercase;color:var(--cinza-400);margin:0 0 8px">
            Você envia · ${euEnvio.size()} figurinhas
          </h4>

          <div style="display:flex;flex-direction:column;gap:8px">
            <c:forEach var="f" items="${euEnvio}">
              <div class="fig-linha ${f.raridade.classeCss}">
                <span class="mini"></span>
                <span class="info">
                  <b><c:out value="${f.nomeJogador}"/></b>
                  <small><c:out value="${f.selecao}"/> · ${f.posicaoCurta} · Nº ${f.numeroAlbum}</small>
                </span>
                <span class="tag ${f.raridade.classeTag}">${f.raridade.nomeExibicao}</span>
              </div>
            </c:forEach>
          </div>

          <p class="secundario pequeno" style="margin:16px 0 0;display:flex;gap:6px;align-items:flex-start">
            <svg class="icone" style="width:13px;height:13px;flex:0 0 auto;margin-top:2px"><use href="#i-cadeado"/></svg>
            Estas figurinhas ficam <b>reservadas</b> até a troca ser concluída ou cancelada.
          </p>
        </section>

        <%-- ================= CENTRO: stepper e acoes ================= --%>
        <section>
          <div class="card" style="text-align:center;margin-bottom:12px">
            <span class="escudo" style="margin:0 auto 8px;width:36px;height:36px">
              <svg class="icone icone-g"><use href="#i-troca"/></svg>
            </span>
            <h3>Troca ${troca.placar}</h3>
            <p class="secundario pequeno" style="margin:4px 0 0">
              proposta por ${troca.proponente.apelido}<br>
              <span class="mono-min">${troca.quando}</span>
            </p>
          </div>

          <div class="card">
            <div class="nav-grupo" style="padding:0 0 4px">Etapas da troca</div>

            <div class="stepper">
              <div class="etapa feita">
                <span class="marca-etapa"><svg class="icone" style="width:11px;height:11px"><use href="#i-check"/></svg></span>
                Proposta enviada
                <span class="quando"><c:out value="${troca.quando}"/></span>
              </div>

              <div class="etapa ${parceiroConfirmou ? 'feita' : 'futura'}">
                <span class="marca-etapa">
                  <c:if test="${parceiroConfirmou}">
                    <svg class="icone" style="width:11px;height:11px"><use href="#i-check"/></svg>
                  </c:if>
                </span>
                ${parceiro.apelido} confirmou
                <span class="quando">${parceiroConfirmou ? 'ok' : 'pendente'}</span>
              </div>

              <div class="etapa ${euConfirmei ? 'feita' : 'atual'}">
                <span class="marca-etapa">
                  <c:if test="${euConfirmei}">
                    <svg class="icone" style="width:11px;height:11px"><use href="#i-check"/></svg>
                  </c:if>
                </span>
                Sua confirmação
                <span class="quando">${euConfirmei ? 'ok' : 'pendente'}</span>
              </div>

              <div class="etapa ${troca.concluida ? 'feita' : 'futura'}">
                <span class="marca-etapa">
                  <c:if test="${troca.concluida}">
                    <svg class="icone" style="width:11px;height:11px"><use href="#i-check"/></svg>
                  </c:if>
                </span>
                Figurinhas transferidas
                <span class="quando">${troca.concluida ? 'concluída' : '—'}</span>
              </div>
            </div>

            <c:choose>
              <%-- Troca ja finalizada --%>
              <c:when test="${troca.concluida}">
                <div class="alerta alerta-sucesso anima-confirmado" style="margin:0">
                  <svg class="icone"><use href="#i-ok"/></svg>
                  <span><b>Troca concluída.</b><br>
                    <span class="pequeno">As figurinhas já estão nos álbuns dos dois lados.</span></span>
                </div>
                <a href="${ctx}/album" class="btn btn-primario btn-bloco" style="margin-top:12px">
                  <svg class="icone"><use href="#i-album"/></svg> Ver no meu álbum
                </a>
              </c:when>

              <c:when test="${troca.recusada or troca.status eq 'CANCELADA'}">
                <div class="alerta alerta-erro" style="margin:0">
                  <svg class="icone"><use href="#i-x-circulo"/></svg>
                  <span><b>Proposta ${troca.statusExibicao}.</b><br>
                    <span class="pequeno">Nenhuma figurinha mudou de dono.</span></span>
                </div>
                <a href="${ctx}/trocas" class="btn btn-secundario btn-bloco" style="margin-top:12px">
                  Ver outros matches
                </a>
              </c:when>

              <%-- Ja confirmei, falta o outro --%>
              <c:when test="${euConfirmei}">
                <div class="alerta alerta-aviso" style="margin:0">
                  <svg class="icone"><use href="#i-relogio"/></svg>
                  <span><b>Sua parte está confirmada.</b><br>
                    <span class="pequeno">A troca executa sozinha assim que ${parceiro.apelido} confirmar.</span></span>
                </div>
              </c:when>

              <%-- Acao principal: confirmar minha parte --%>
              <c:otherwise>
                <form method="post" action="${ctx}/troca" style="margin-top:16px">
                  <input type="hidden" name="idTroca" value="${troca.id}">
                  <input type="hidden" name="codigo" value="${troca.codigo}">
                  <button type="submit" name="acao" value="confirmar" class="btn btn-sucesso btn-bloco">
                    <svg class="icone"><use href="#i-ok"/></svg> Confirmar minha parte
                  </button>
                </form>

                <form method="post" action="${ctx}/troca" style="margin-top:8px"
                      onsubmit="return confirm('Recusar a proposta ${troca.codigo}? Nenhuma figurinha muda de dono.');">
                  <input type="hidden" name="idTroca" value="${troca.id}">
                  <input type="hidden" name="codigo" value="${troca.codigo}">
                  <button type="submit" name="acao" value="recusar" class="btn btn-perigo btn-bloco">
                    <svg class="icone"><use href="#i-x"/></svg> Recusar proposta
                  </button>
                </form>
              </c:otherwise>
            </c:choose>

            <p class="secundario pequeno" style="margin:16px 0 0;text-align:center;line-height:1.6">
              A troca só é executada quando <b>os dois lados confirmam</b>.<br>
              Nenhuma figurinha muda de dono antes disso.
            </p>
          </div>
        </section>

        <%-- ================= LADO DIREITO: o que EU recebo ================= --%>
        <section class="card">
          <div style="display:flex;align-items:center;gap:12px;margin-bottom:16px">
            <span class="avatar avatar-g">${parceiro.iniciais}</span>
            <div style="flex:1">
              <b class="mono-min" style="font-size:13px">${parceiro.apelido}</b>
              <span class="secundario pequeno" style="display:flex;align-items:center;gap:5px">
                <svg class="icone" style="width:12px;height:12px;color:var(--aviso)"><use href="#i-estrela"/></svg>
                <span class="num"><fmt:formatNumber value="${parceiro.reputacaoMedia}" pattern="0.0"/></span>
                <c:if test="${not empty parceiro.membroDesde}">
                  · membro desde ${parceiro.membroDesde}
                </c:if>
              </span>
            </div>
            <c:choose>
              <c:when test="${parceiroConfirmou}"><span class="tag tag-sucesso">confirmou</span></c:when>
              <c:otherwise><span class="tag tag-neutro">pendente</span></c:otherwise>
            </c:choose>
          </div>

          <h4 style="font-family:'Manrope',sans-serif;font-size:10px;font-weight:700;letter-spacing:.09em;
                     text-transform:uppercase;color:var(--cinza-400);margin:0 0 8px">
            Você recebe · ${euRecebo.size()} figurinhas
          </h4>

          <div style="display:flex;flex-direction:column;gap:8px">
            <c:forEach var="f" items="${euRecebo}">
              <div class="fig-linha ${f.raridade.classeCss}">
                <span class="mini"></span>
                <span class="info">
                  <b><c:out value="${f.nomeJogador}"/></b>
                  <small><c:out value="${f.selecao}"/> · ${f.posicaoCurta} · Nº ${f.numeroAlbum}</small>
                </span>
                <span class="tag ${f.raridade.classeTag}">${f.raridade.nomeExibicao}</span>
              </div>
            </c:forEach>
          </div>

          <c:if test="${parceiroConfirmou and not euConfirmei}">
            <div class="alerta alerta-sucesso" style="margin:16px 0 0">
              <svg class="icone"><use href="#i-ok"/></svg>
              <span class="pequeno">
                <b>${parceiro.apelido} já confirmou a parte dele.</b>
                Agora é com você — confirme ou recuse antes do prazo expirar.
              </span>
            </div>
          </c:if>
        </section>
      </div>

    </main>
    <%@ include file="frag/rodape.jsp" %>
  </div>
</div>

<script src="${ctx}/assets/js/app.js"></script>
</body>
</html>
