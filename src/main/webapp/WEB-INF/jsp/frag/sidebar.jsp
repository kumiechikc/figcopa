<%--
  Menu lateral do sistema. A pagina define "aba" para marcar o item ativo.
--%>
<aside class="sidebar">
  <a href="${ctx}/dashboard" class="marca" style="text-decoration:none;color:inherit">
    <span class="escudo"><svg class="icone" viewBox="0 0 24 24"><path d="M12 4l4 4h-8zM12 20l-4-4h8z" fill="currentColor" stroke="none"/></svg></span>
    <span class="marca-nome">The<br>Champ<span>ions</span></span>
  </a>

  <div class="nav-grupo">Principal</div>
  <nav>
    <a href="${ctx}/dashboard" class="nav-item ${aba eq 'dashboard' ? 'ativo' : ''}">
      <svg class="icone"><use href="#i-dashboard"/></svg> Dashboard
    </a>
    <a href="${ctx}/album" class="nav-item ${aba eq 'album' ? 'ativo' : ''}">
      <svg class="icone"><use href="#i-album"/></svg> Meu Álbum
    </a>
    <a href="${ctx}/catalogo" class="nav-item ${aba eq 'catalogo' ? 'ativo' : ''}">
      <svg class="icone"><use href="#i-catalogo"/></svg> Catálogo
    </a>
    <a href="${ctx}/trocas" class="nav-item ${aba eq 'trocas' ? 'ativo' : ''}">
      <svg class="icone"><use href="#i-troca"/></svg> Buscar Trocas
      <c:if test="${not empty totalMatches and totalMatches gt 0}">
        <span class="nav-badge">${totalMatches}</span>
      </c:if>
    </a>
    <a href="${ctx}/pacotes" class="nav-item ${aba eq 'pacotes' ? 'ativo' : ''}">
      <svg class="icone"><use href="#i-pacote"/></svg> Abrir Pacotes
      <c:if test="${not empty pacotesDisponiveis and pacotesDisponiveis gt 0}">
        <span class="nav-badge">${pacotesDisponiveis}</span>
      </c:if>
    </a>
  </nav>

  <div class="nav-grupo">Minha conta</div>
  <nav>
    <a href="${ctx}/perfil" class="nav-item ${aba eq 'perfil' ? 'ativo' : ''}">
      <svg class="icone"><use href="#i-usuario"/></svg> Perfil e Reputação
    </a>
    <a href="${ctx}/ranking" class="nav-item ${aba eq 'ranking' ? 'ativo' : ''}">
      <svg class="icone"><use href="#i-ranking"/></svg> Ranking
    </a>
    <a href="${ctx}/historico" class="nav-item ${aba eq 'historico' ? 'ativo' : ''}">
      <svg class="icone"><use href="#i-historico"/></svg> Histórico de Trocas
    </a>
  </nav>

  <div style="margin-top:auto"></div>
  <nav>
    <a href="${ctx}/diagnostico" class="nav-item">
      <svg class="icone"><use href="#i-banco"/></svg> Diagnóstico
    </a>
    <a href="${ctx}/logout" class="nav-item">
      <svg class="icone"><use href="#i-sair"/></svg> Sair
    </a>
  </nav>

  <div class="sidebar-rodape">
    <span class="avatar">${usuarioLogado.iniciais}</span>
    <span style="min-width:0">
      <b style="display:block;font-size:12px;white-space:nowrap;overflow:hidden;text-overflow:ellipsis">
        <c:out value="${usuarioLogado.nome}"/></b>
      <small class="secundario pequeno" style="display:block;white-space:nowrap;overflow:hidden;text-overflow:ellipsis">
        <c:out value="${usuarioLogado.email}"/></small>
    </span>
  </div>
</aside>
