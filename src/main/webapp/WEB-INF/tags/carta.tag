<%--
  Carta de figurinha — componente reaproveitado no album, nos pacotes,
  na landing e nas telas de troca.

  A moldura muda de cor conforme a raridade (classe r1..r4) e o item faltante
  aparece escurecido com "?", como definido no guia visual.
--%>
<%@ tag pageEncoding="UTF-8" body-content="empty" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<%@ attribute name="fig" required="true" type="br.com.thechampions.model.Figurinha" %>
<%@ attribute name="mostrarStatus" required="false" type="java.lang.Boolean" %>
<%@ attribute name="etiqueta" required="false" type="java.lang.String" %>
<%@ attribute name="classeExtra" required="false" type="java.lang.String" %>

<c:set var="faltante" value="${mostrarStatus and fig.faltante}"/>

<article class="figurinha ${fig.raridade.classeCss} ${faltante ? 'faltante' : ''} ${classeExtra}"
         title="${fig.nomeJogador} · ${fig.selecao} · ${fig.raridade.nomeExibicao}">

  <span class="numero">${fig.numeroAlbum}</span>

  <c:if test="${not empty etiqueta}">
    <span class="repetida">${etiqueta}</span>
  </c:if>
  <c:if test="${empty etiqueta and mostrarStatus and fig.repetida}">
    <span class="repetida">&times;${fig.quantidade}</span>
  </c:if>

  <div class="busto">
    <c:choose>
      <%-- Imagem real disponível para escudo --%>
      <c:when test="${fig.escudo and not empty fig.urlImagemEscudo}">
        <img src="${fig.urlImagemEscudo}" alt="${fig.nomeJogador}" style="width:100%;height:100%;object-fit:cover;border-radius:8px"/>
      </c:when>
      <%-- Imagem real disponível para jogador --%>
      <c:when test="${not fig.escudo and not empty fig.urlImagemJogador}">
        <img src="${fig.urlImagemJogador}" alt="${fig.nomeJogador}" style="width:100%;height:100%;object-fit:cover;border-radius:8px"/>
      </c:when>
      <%-- Escudos de selecao sem imagem: desenham o brasao em SVG. --%>
      <c:when test="${fig.escudo}">
        <svg viewBox="0 0 80 96" style="width:52%;margin-bottom:14px">
          <path d="M40 8 L70 20 V52 C70 70 56 82 40 88 C24 82 10 70 10 52 V20 Z"
                fill="none" stroke="${fig.raridade.corHex}" stroke-width="3" opacity=".85"/>
          <path d="M40 30 L48 42 H32 Z M40 66 L32 54 H48 Z"
                fill="${fig.raridade.corHex}" opacity=".8"/>
        </svg>
      </c:when>
      <%-- Jogadores sem imagem: desenham busto em SVG. --%>
      <c:otherwise>
        <svg viewBox="0 0 80 96" preserveAspectRatio="xMidYMax meet">
          <defs>
            <linearGradient id="camisa${fig.id}" x1="0" y1="0" x2="0" y2="1">
              <stop offset="0%" stop-color="${fig.raridade.corHex}" stop-opacity=".55"/>
              <stop offset="100%" stop-color="${fig.raridade.corHex}" stop-opacity=".14"/>
            </linearGradient>
          </defs>
          <circle cx="40" cy="34" r="15" fill="#2A3350"/>
          <path d="M22 96 V62 C22 52 30 46 40 46 C50 46 58 52 58 62 V96 Z"
                fill="url(#camisa${fig.id})" stroke="${fig.raridade.corHex}"
                stroke-width="1.2" stroke-opacity=".5"/>
        </svg>
      </c:otherwise>
    </c:choose>
  </div>

  <div class="legenda">
    <b>${fig.nomeJogador}</b>
    <small>${fig.raridade.nomeExibicao} · ${fig.posicaoCurta}</small>
  </div>
</article>
