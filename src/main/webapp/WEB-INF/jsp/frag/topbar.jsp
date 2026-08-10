<%-- Barra superior: breadcrumb, titulo, busca e notificacoes. --%>
<header class="topbar">
  <div>
    <div class="breadcrumb">
      <a href="${ctx}/dashboard">Início</a>
      <svg class="icone" style="width:12px;height:12px"><use href="#i-seta-dir"/></svg>
      <c:if test="${not empty trilhaMeio}">
        <span>${trilhaMeio}</span>
        <svg class="icone" style="width:12px;height:12px"><use href="#i-seta-dir"/></svg>
      </c:if>
      <span class="atual"><c:out value="${titulo}"/></span>
    </div>
    <div class="topbar-titulo"><c:out value="${titulo}"/></div>
  </div>

  <div class="topbar-dir">
    <c:if test="${not empty acaoTopo}">${acaoTopo}</c:if>
    <label class="busca">
      <svg class="icone"><use href="#i-busca"/></svg>
      <span class="apenas-leitor">Buscar</span>
      <input type="search" placeholder="Buscar jogador, seleção, usuário" name="q">
    </label>
    <a href="${ctx}/dashboard" class="sino" aria-label="Notificações">
      <svg class="icone icone-g"><use href="#i-sino"/></svg>
      <c:if test="${not empty naoLidas and naoLidas gt 0}"><i class="ponto"></i></c:if>
    </a>
  </div>
</header>
