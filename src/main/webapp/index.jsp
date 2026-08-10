<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%-- Landing publica: porta de entrada do visitante. --%>
<c:set var="titulo" value="Troque figurinhas da Copa 2026" scope="request"/>
<%@ include file="WEB-INF/jsp/frag/cabeca.jsp" %>
<body>
<%@ include file="WEB-INF/jsp/frag/icones.jsp" %>

<header class="landing-topo">
  <a href="${ctx}/" class="marca" style="text-decoration:none;color:inherit;padding:0">
    <span class="escudo"><svg class="icone" viewBox="0 0 24 24"><path d="M12 4l4 4h-8zM12 20l-4-4h8z" fill="currentColor" stroke="none"/></svg></span>
    <span class="marca-nome">The Champ<span>ions</span></span>
  </a>
  <nav>
    <a href="#como-funciona">Como funciona</a>
    <a href="#raridades">Raridades</a>
  </nav>
  <div class="acoes">
    <a href="${ctx}/login" class="btn btn-fantasma">Entrar</a>
    <a href="${ctx}/cadastro" class="btn btn-primario">Criar conta</a>
  </div>
</header>

<section class="heroi">
  <div>
    <span class="selo"><i></i> 1.284 colecionadores trocando agora</span>
    <h1>Tem, tem, tem,<br><em>não tem.</em></h1>
    <p class="chamada">
      Fechar o álbum comprando pacote é caro — quanto mais perto do fim, mais repetida
      você acumula. O The Champions cruza suas repetidas com as faltantes de outros
      colecionadores e só fecha a troca quando os dois lados confirmam.
    </p>

    <div style="display:flex;gap:12px;margin-top:24px;flex-wrap:wrap">
      <a href="${ctx}/cadastro" class="btn btn-primario">
        Começar meu álbum <svg class="icone"><use href="#i-avancar"/></svg>
      </a>
      <a href="${ctx}/login" class="btn btn-secundario">
        <svg class="icone"><use href="#i-olho"/></svg> Ver uma conta pronta
      </a>
    </div>

    <div class="numeros-comunidade">
      <div><b class="num">12.418</b><small>trocas concluídas</small></div>
      <div><b class="num">94,6%</b><small>fecham em até 24h</small></div>
      <div><b class="num">4,8</b><small>reputação média</small></div>
    </div>
  </div>

  <div class="deck" aria-hidden="true">
    <article class="figurinha r3 c1">
      <span class="numero">238</span>
      <div class="busto"><svg viewBox="0 0 80 96"><circle cx="40" cy="34" r="15" fill="#2A3350"/>
        <path d="M22 96 V62 C22 52 30 46 40 46 C50 46 58 52 58 62 V96 Z" fill="#F5883D" fill-opacity=".28" stroke="#F5883D" stroke-opacity=".5"/></svg></div>
      <div class="legenda"><b>Raphinha</b><small>Rara · ATA</small></div>
    </article>
    <article class="figurinha r2 c2">
      <span class="numero">088</span>
      <div class="busto"><svg viewBox="0 0 80 96"><circle cx="40" cy="34" r="15" fill="#2A3350"/>
        <path d="M22 96 V62 C22 52 30 46 40 46 C50 46 58 52 58 62 V96 Z" fill="#22D3EE" fill-opacity=".26" stroke="#22D3EE" stroke-opacity=".5"/></svg></div>
      <div class="legenda"><b>T. Kubo</b><small>Brilhante · MEI</small></div>
    </article>
    <article class="figurinha r4 c3 brilho-lendaria">
      <span class="numero">L-07</span>
      <div class="busto"><svg viewBox="0 0 80 96"><circle cx="40" cy="34" r="15" fill="#2A3350"/>
        <path d="M22 96 V62 C22 52 30 46 40 46 C50 46 58 52 58 62 V96 Z" fill="#A855F7" fill-opacity=".3" stroke="#A855F7" stroke-opacity=".6"/></svg></div>
      <div class="legenda"><b>Vinícius Jr.</b><small>Lendária · ATA</small></div>
    </article>
  </div>
</section>

<section class="passos" id="como-funciona">
  <h2 style="margin-bottom:8px">Como funciona</h2>
  <p class="secundario" style="margin-bottom:24px">Quatro passos, do pacote fechado à figurinha no álbum.</p>

  <div class="grade grade-4">
    <div class="passo">
      <div class="topo">
        <span class="ic"><svg class="icone"><use href="#i-pacote"/></svg></span>
        <span class="n num">01</span>
      </div>
      <h3>Abra pacotes</h3>
      <p>Sete figurinhas por pacote, sorteadas pela probabilidade real de cada raridade.</p>
    </div>
    <div class="passo">
      <div class="topo">
        <span class="ic"><svg class="icone"><use href="#i-album"/></svg></span>
        <span class="n num">02</span>
      </div>
      <h3>Monte o álbum</h3>
      <p>Cada seleção com sua barra de progresso. Repetidas viram moeda de troca.</p>
    </div>
    <div class="passo">
      <div class="topo">
        <span class="ic"><svg class="icone"><use href="#i-troca"/></svg></span>
        <span class="n num">03</span>
      </div>
      <h3>Receba matches</h3>
      <p>O sistema cruza o que sobra pra você com o que falta pro outro. Nos dois sentidos.</p>
    </div>
    <div class="passo">
      <div class="topo">
        <span class="ic"><svg class="icone"><use href="#i-ok"/></svg></span>
        <span class="n num">04</span>
      </div>
      <h3>Confirme os dois lados</h3>
      <p>Nenhuma figurinha muda de dono antes das duas confirmações. Ninguém sai no prejuízo.</p>
    </div>
  </div>
</section>

<section class="passos" id="raridades">
  <h2 style="margin-bottom:8px">Quatro níveis de raridade</h2>
  <p class="secundario" style="margin-bottom:24px">
    A cor da moldura comunica o valor em qualquer tela, sem depender de texto.
  </p>

  <div class="grade grade-4">
    <div class="card">
      <span class="tag tag-comum">Comum</span>
      <p style="margin:12px 0 4px"><b class="num">78%</b> dos pacotes</p>
      <p class="secundario pequeno" style="margin:0">Maioria do álbum · moldura neutra · R$ 2,00 de referência</p>
    </div>
    <div class="card">
      <span class="tag tag-brilhante">Brilhante</span>
      <p style="margin:12px 0 4px"><b class="num">15%</b> dos pacotes</p>
      <p class="secundario pequeno" style="margin:0">Acabamento metalizado · R$ 25,00 de referência</p>
    </div>
    <div class="card">
      <span class="tag tag-rara">Rara</span>
      <p style="margin:12px 0 4px"><b class="num">4%</b> dos pacotes</p>
      <p class="secundario pequeno" style="margin:0">Alta demanda · ~1 a cada 25 pacotes · R$ 120,00</p>
    </div>
    <div class="card" style="border-color:rgba(168,85,247,.4)">
      <span class="tag tag-lendaria">Lendária</span>
      <p style="margin:12px 0 4px"><b class="num">0,53%</b> dos pacotes</p>
      <p class="secundario pequeno" style="margin:0">Tier máximo · ~1 a cada 190 pacotes · R$ 830,00</p>
    </div>
  </div>
</section>

<section class="passos" style="padding-top:0">
  <div class="card" style="display:flex;align-items:center;gap:24px;flex-wrap:wrap;
              background:linear-gradient(135deg,rgba(124,58,227,.2),transparent 60%),var(--azul-900)">
    <div style="flex:1;min-width:260px">
      <h2 style="margin-bottom:4px">Seu álbum não fecha sozinho</h2>
      <p class="secundario" style="margin:0">Crie a conta e receba um Pacote Especial de boas-vindas.</p>
    </div>
    <a href="${ctx}/cadastro" class="btn btn-primario">
      Criar conta grátis <svg class="icone"><use href="#i-avancar"/></svg>
    </a>
  </div>
</section>

<%@ include file="WEB-INF/jsp/frag/rodape.jsp" %>
</body>
</html>
