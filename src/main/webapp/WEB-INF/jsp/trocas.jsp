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

      <%-- Explicacao do matching --%>
      <div class="card" style="border-color:rgba(52,211,153,.28);
                  background:linear-gradient(120deg,rgba(52,211,153,.06),transparent 55%),var(--azul-900);
                  display:flex;flex-wrap:wrap;gap:16px;align-items:center;margin-bottom:16px">
        <svg class="icone icone-g" style="color:var(--sucesso)"><use href="#i-ok"/></svg>
        <div style="flex:1">
          <b style="font-family:'Saira',sans-serif;color:var(--sucesso)">Matching automático ativo</b>
          <p class="secundario pequeno" style="margin:2px 0 0">
            Cruzamos as suas <b class="num">${resumo.repetidasDisponiveis}</b> repetidas com as faltantes
            de outros colecionadores — e as <b class="num">${resumo.faltantes}</b> que você procura com as
            repetidas deles. Só aparecem trocas em que os dois lados ganham.
          </p>
        </div>
        <div style="text-align:right">
          <b class="num" style="font-size:26px;color:var(--sucesso)">${totalMatches}</b>
          <small class="secundario pequeno" style="display:block">matches hoje</small>
        </div>
      </div>

      <div class="grade grade-filtros" style="align-items:start">

        <section>
          <%-- Propostas em aberto --%>
          <c:if test="${not empty recebidas or not empty enviadas}">
            <div class="card" style="margin-bottom:16px">
              <div class="card-cabecalho">
                <svg class="icone" style="color:var(--aviso)"><use href="#i-relogio"/></svg>
                <h3>Propostas em aberto</h3>
                <span class="mono-min secundario">
                  ${recebidas.size()} recebidas · ${enviadas.size()} enviadas
                </span>
              </div>

              <div class="rolagem-x"><table class="tabela">
                <thead><tr><th>Código</th><th>Parceiro</th><th>Troca</th><th>Situação</th><th></th></tr></thead>
                <tbody>
                  <c:forEach var="t" items="${recebidas}">
                    <tr>
                      <td class="mono-min">#${t.codigo}</td>
                      <td>
                        <span style="display:flex;align-items:center;gap:8px">
                          <span class="avatar" style="width:24px;height:24px;font-size:9px">${t.proponente.iniciais}</span>
                          <span class="mono-min">${t.proponente.apelido}</span>
                        </span>
                      </td>
                      <td class="num secundario">${t.placar}</td>
                      <td><span class="tag tag-aviso">falta você confirmar</span></td>
                      <td style="text-align:right">
                        <a href="${ctx}/troca?codigo=${t.codigo}" class="btn btn-primario"
                           style="padding:5px 12px;font-size:12px">Abrir</a>
                      </td>
                    </tr>
                  </c:forEach>
                  <c:forEach var="t" items="${enviadas}">
                    <tr>
                      <td class="mono-min">#${t.codigo}</td>
                      <td>
                        <span style="display:flex;align-items:center;gap:8px">
                          <span class="avatar" style="width:24px;height:24px;font-size:9px">${t.receptor.iniciais}</span>
                          <span class="mono-min">${t.receptor.apelido}</span>
                        </span>
                      </td>
                      <td class="num secundario">${t.placar}</td>
                      <td><span class="tag tag-neutro">aguardando o parceiro</span></td>
                      <td style="text-align:right">
                        <a href="${ctx}/troca?codigo=${t.codigo}" class="btn btn-secundario"
                           style="padding:5px 12px;font-size:12px">Ver</a>
                      </td>
                    </tr>
                  </c:forEach>
                </tbody>
              </table></div>
            </div>
          </c:if>

          <%-- Lista de matches --%>
          <h2 style="margin-bottom:12px">Melhores matches <span class="num secundario">${totalMatches}</span></h2>

          <c:choose>
            <c:when test="${empty matches}">
              <div class="card">
                <div class="vazio">
                  <svg><use href="#i-troca"/></svg>
                  <strong>Nenhum parceiro compatível ainda</strong>
                  Um match só aparece quando os dois lados ganham: você precisa ter repetidas
                  que faltam pra alguém, e essa pessoa precisa ter repetidas que faltam pra você.
                  <div style="margin-top:16px">
                    <a href="${ctx}/pacotes" class="btn btn-primario">Abrir pacotes e gerar repetidas</a>
                  </div>
                </div>
              </div>
            </c:when>

            <c:otherwise>
              <c:forEach var="m" items="${matches}">
                <article class="match ${m.perfeito ? 'perfeito' : ''}">

                  <div class="cabecalho">
                    <span class="avatar avatar-g">${m.parceiro.iniciais}</span>
                    <div>
                      <div style="display:flex;align-items:center;gap:8px">
                        <b class="mono-min" style="font-size:13px">${m.parceiro.apelido}</b>
                        <c:choose>
                          <c:when test="${m.perfeito}">
                            <span class="tag tag-roxo">
                              <svg class="icone" style="width:11px;height:11px"><use href="#i-brilho"/></svg>
                              match perfeito
                            </span>
                          </c:when>
                          <c:when test="${m.desigual}">
                            <span class="tag tag-aviso">troca desigual</span>
                          </c:when>
                          <c:otherwise><span class="tag tag-neutro">match parcial</span></c:otherwise>
                        </c:choose>
                      </div>
                      <span class="secundario pequeno" style="display:flex;align-items:center;gap:6px">
                        <svg class="icone" style="width:12px;height:12px;color:var(--aviso)"><use href="#i-estrela"/></svg>
                        <span class="num">
                          <fmt:formatNumber value="${m.parceiro.reputacaoMedia}" pattern="0.0"/>
                        </span>
                        · <span class="num">${m.trocasDoParceiro}</span> trocas concluídas
                      </span>
                    </div>

                    <div class="ganho">
                      <b class="num">+${m.faltantesResolvidas}</b>
                      <small>faltantes suas<br>resolvidas</small>
                    </div>
                  </div>

                  <div class="troca-lados">
                    <div>
                      <h4>Você envia <span class="secundario">(das suas repetidas)</span></h4>
                      <div style="display:flex;flex-direction:column;gap:6px">
                        <c:forEach var="f" items="${m.euEnvio}">
                          <div class="fig-linha ${f.raridade.classeCss}">
                            <span class="mini"></span>
                            <span class="info">
                              <b><c:out value="${f.nomeJogador}"/></b>
                              <small>${f.siglaSelecao} · ${f.raridade.nomeExibicao}</small>
                            </span>
                          </div>
                        </c:forEach>
                      </div>
                    </div>

                    <div class="seta">
                      <svg class="icone icone-g"><use href="#i-troca"/></svg>
                    </div>

                    <div>
                      <h4>Você recebe <span class="secundario">(faltam no seu álbum)</span></h4>
                      <div style="display:flex;flex-direction:column;gap:6px">
                        <c:forEach var="f" items="${m.euRecebo}">
                          <div class="fig-linha ${f.raridade.classeCss}">
                            <span class="mini"></span>
                            <span class="info">
                              <b><c:out value="${f.nomeJogador}"/></b>
                              <small>${f.siglaSelecao} · ${f.raridade.nomeExibicao}</small>
                            </span>
                          </div>
                        </c:forEach>
                      </div>
                    </div>
                  </div>

                  <form method="post" action="${ctx}/trocas"
                        style="display:flex;align-items:center;gap:12px;margin-top:16px;flex-wrap:wrap">
                    <input type="hidden" name="idParceiro" value="${m.parceiro.id}">
                    <c:forEach var="f" items="${m.euEnvio}">
                      <input type="hidden" name="envio" value="${f.id}">
                    </c:forEach>
                    <c:forEach var="f" items="${m.euRecebo}">
                      <input type="hidden" name="recebo" value="${f.id}">
                    </c:forEach>

                    <button type="submit" class="btn ${m.desigual ? 'btn-secundario' : 'btn-primario'}">
                      ${m.desigual ? 'Propor mesmo assim' : 'Propor esta troca'}
                    </button>

                    <c:if test="${m.desigual}">
                      <span class="secundario pequeno" style="margin-left:auto">
                        você envia raridade maior do que recebe
                      </span>
                    </c:if>
                  </form>
                </article>
              </c:forEach>

              <nav class="paginacao" aria-label="Paginação dos matches">
                <span class="atual">1</span>
                <span class="secundario pequeno" style="min-width:auto;padding:0 8px">
                  mostrando ${matches.size()} de ${totalMatches}
                </span>
              </nav>
            </c:otherwise>
          </c:choose>
        </section>

        <%-- Filtros --%>
        <aside class="card">
          <div class="card-cabecalho">
            <svg class="icone"><use href="#i-filtro"/></svg>
            <h3 style="font-size:14px">Filtros</h3>
          </div>

          <form method="get" action="${ctx}/trocas">
            <div class="campo">
              <label for="raridadeMinima">Raridade mínima recebida</label>
              <select id="raridadeMinima" name="raridadeMinima">
                <option value="" ${empty raridadeMinima ? 'selected' : ''}>Qualquer</option>
                <option value="2" ${raridadeMinima eq '2' ? 'selected' : ''}>Brilhante ou acima</option>
                <option value="3" ${raridadeMinima eq '3' ? 'selected' : ''}>Rara ou acima</option>
                <option value="4" ${raridadeMinima eq '4' ? 'selected' : ''}>Somente lendárias</option>
              </select>
            </div>

            <div class="campo">
              <label for="selecao">Seleção de interesse</label>
              <select id="selecao" name="selecao">
                <option value="">Todas as seleções</option>
                <c:forEach var="s" items="${selecoes}">
                  <option value="${s.sigla}" ${selecaoFiltro eq s.sigla ? 'selected' : ''}>
                    <c:out value="${s.selecao}"/>
                  </option>
                </c:forEach>
              </select>
            </div>

            <button type="submit" class="btn btn-secundario btn-bloco">Aplicar filtros</button>
            <a href="${ctx}/trocas" class="btn btn-fantasma btn-bloco" style="margin-top:8px">Limpar filtros</a>
          </form>
        </aside>
      </div>

    </main>
    <%@ include file="frag/rodape.jsp" %>
  </div>
</div>

<script src="${ctx}/assets/js/app.js"></script>
</body>
</html>
