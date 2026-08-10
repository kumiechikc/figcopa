/* =========================================================================
   Robustez da aplicacao JSP.

   As outras suites cobrem o caminho feliz. Esta cobre o que so aparece quando
   alguem sai do roteiro — que e exatamente o que acontece numa apresentacao,
   quando o professor clica onde nao devia.

   Quatro frentes:
     1. Toda rota responde (JSP que so quebra quando alguem abre a tela)
     2. Entrada hostil: injecao de SQL e HTML no que vira tela
     3. Concorrencia: clicar duas vezes nao abre o pacote duas vezes
     4. Autorizacao: nao da para mexer na troca dos outros

   Uso:  node e2e-robustez.js
   Exige Tomcat em :8080 e o banco carregado com 01_schema.sql + 02_dados.sql.
   ========================================================================= */
const { chromium } = require('playwright');

const BASE = 'http://localhost:8080/the-champions';
const USUARIO = 'vinicius.k@email.com';
const SENHA = '123456';
let ok = 0, falhas = 0;

function checar(nome, cond, detalhe) {
  if (cond) { ok++; console.log('  OK    ' + nome + (detalhe ? '  → ' + detalhe : '')); }
  else { falhas++; console.log('  FALHA ' + nome + (detalhe ? '  → ' + detalhe : '')); }
}

async function entrar(ctx, email = USUARIO, senha = SENHA) {
  const p = await ctx.newPage();
  await p.goto(BASE + '/login', { waitUntil: 'domcontentloaded' });
  await p.fill('#email', email);
  await p.fill('#senha', senha);
  await p.click('button[type=submit]');
  await p.waitForLoadState('domcontentloaded');
  return p;
}

(async () => {
  const browser = await chromium.launch({
    executablePath: '/opt/pw-browsers/chromium-1194/chrome-linux/chrome'
  });
  const ctx = await browser.newContext();
  const page = await entrar(ctx);

  // ------------------------------------------------------------------------
  console.log('\n== 1. Toda rota responde ==');
  // Uma JSP com erro de sintaxe so estoura quando alguem abre aquela tela.
  // Num projeto com 19 JSPs, descobrir isso ao vivo e o pior momento possivel.
  const ROTAS = ['/', '/dashboard', '/album', '/catalogo', '/pacotes', '/trocas',
                 '/perfil', '/ranking', '/historico', '/diagnostico',
                 '/troca?codigo=TRC-4187'];
  for (const rota of ROTAS) {
    const r = await page.goto(BASE + rota, { waitUntil: 'domcontentloaded' });
    const corpo = await page.locator('body').innerText();
    const estourou = /JasperException|HTTP Status 500|java\.lang\.|SQLException/i.test(corpo);
    checar(`abre ${rota}`, r.status() === 200 && !estourou,
           'HTTP ' + r.status() + (estourou ? ' COM STACK TRACE' : ''));
  }

  // Rota inexistente cai na tela de erro, nao na pagina crua do Tomcat.
  const r404 = await page.goto(BASE + '/naoexiste', { waitUntil: 'domcontentloaded' });
  const corpo404 = await page.locator('body').innerText();
  checar('rota inexistente usa a tela de erro do projeto',
         !/Apache Tomcat|HTTP Status 404 –/i.test(corpo404), 'HTTP ' + r404.status());

  // Codigo de troca inexistente nao pode virar stack trace.
  await page.goto(BASE + '/troca?codigo=TRC-0000', { waitUntil: 'domcontentloaded' });
  const corpoTroca = await page.locator('body').innerText();
  checar('codigo de troca inexistente e tratado',
         !/JasperException|java\.lang\.|NullPointer/i.test(corpoTroca));

  // ------------------------------------------------------------------------
  console.log('\n== 2. Entrada hostil ==');

  // Injecao de SQL no login: com PreparedStatement isto e so uma senha errada.
  const anon = await browser.newContext();
  const pInj = await anon.newPage();
  await pInj.goto(BASE + '/login', { waitUntil: 'domcontentloaded' });
  await pInj.fill('#email', "' OR '1'='1");
  await pInj.fill('#senha', "' OR '1'='1");
  await pInj.click('button[type=submit]');
  await pInj.waitForLoadState('domcontentloaded');
  checar('injecao de SQL no login nao autentica',
         pInj.url().includes('/login') && (await pInj.locator('.alerta-erro').count()) > 0,
         pInj.url().replace(BASE, ''));

  // O mesmo pelos filtros do album, que vao para a clausula WHERE.
  const rInj = await page.goto(BASE + "/album?selecao=' OR 1=1 --", { waitUntil: 'domcontentloaded' });
  const corpoInj = await page.locator('body').innerText();
  checar('injecao de SQL no filtro do album nao quebra a tela',
         rInj.status() === 200 && !/SQLException|You have an error in your SQL/i.test(corpoInj));

  // Parametro numerico recebendo texto.
  const rTexto = await page.goto(BASE + '/album?raridade=abc', { waitUntil: 'domcontentloaded' });
  checar('parametro numerico com texto nao estoura', rTexto.status() === 200);

  // XSS: um nome com <script> tem de aparecer como texto, nunca executar.
  const marca = 'x' + Date.now();
  const anon2 = await browser.newContext();
  const pXss = await anon2.newPage();
  let executou = false;
  pXss.on('dialog', async d => { executou = true; await d.dismiss(); });
  await pXss.goto(BASE + '/cadastro', { waitUntil: 'domcontentloaded' });
  await pXss.fill('#nome', `<script>alert(1)</script>${marca}`);
  await pXss.fill('#email', `${marca}@email.com`);
  await pXss.fill('#senha', '123456');
  await pXss.fill('#confirmacao', '123456');
  await pXss.click('button[type=submit]');
  await pXss.waitForLoadState('domcontentloaded');
  await pXss.goto(BASE + '/perfil', { waitUntil: 'domcontentloaded' });
  await pXss.waitForTimeout(400);
  const htmlPerfil = await pXss.content();
  checar('nome com <script> nao executa', !executou);
  checar('nome com <script> chega escapado no HTML',
         !htmlPerfil.includes('<script>alert(1)</script>'),
         htmlPerfil.includes('&lt;script&gt;') ? 'escapado' : 'nao encontrado na tela');

  // ------------------------------------------------------------------------
  console.log('\n== 3. Concorrencia ==');

  // Dois cliques no mesmo pacote. O SELECT ... FOR UPDATE do PacoteDAO existe
  // para isto: o segundo tem de ser recusado, nunca creditar 14 figurinhas.
  const pPac = await entrar(await browser.newContext());
  await pPac.goto(BASE + '/pacotes', { waitUntil: 'domcontentloaded' });
  const idPacote = await pPac.locator('input[name=idPacote]').first().inputValue();

  const antes = await pPac.evaluate(async ([base, id]) => {
    const corpo = new URLSearchParams({ idPacote: id });
    const duas = await Promise.all([
      fetch(base + '/pacotes', { method: 'POST', body: corpo, redirect: 'follow' }),
      fetch(base + '/pacotes', { method: 'POST', body: corpo, redirect: 'follow' })
    ]);
    return duas.map(r => r.status);
  }, [BASE, idPacote]);
  checar('duas aberturas simultaneas do mesmo pacote nao derrubam o servidor',
         antes.every(s => s < 500), 'HTTP ' + antes.join(' e '));

  await pPac.goto(BASE + '/pacotes', { waitUntil: 'domcontentloaded' });
  const aindaTem = await pPac.locator(`input[name=idPacote][value="${idPacote}"]`).count();
  checar('pacote aberto sai da lista (nao abre duas vezes)', aindaTem === 0);

  // ------------------------------------------------------------------------
  console.log('\n== 4. Autorizacao ==');

  // Confirmar a troca de outro usuario: TRC-4187 e entre vinicius e pedro.
  const pIntruso = await entrar(await browser.newContext(), 'lu.figus@email.com', SENHA);
  await pIntruso.goto(BASE + '/troca?codigo=TRC-4187', { waitUntil: 'domcontentloaded' });
  const corpoIntruso = await pIntruso.locator('body').innerText();
  const temBotao = await pIntruso.locator('button[value=confirmar]').count();
  checar('quem nao participa nao ve o botao de confirmar', temBotao === 0,
         temBotao + ' botao(oes)');
  checar('e a tela nao vaza stack trace',
         !/java\.lang\.|JasperException/i.test(corpoIntruso));

  // Tela interna sem sessao volta para o login, sem vazar conteudo.
  const semSessao = await browser.newContext();
  const pSem = await semSessao.newPage();
  await pSem.goto(BASE + '/album', { waitUntil: 'domcontentloaded' });
  checar('sem sessao o album redireciona ao login', pSem.url().includes('/login'));
  checar('e nao mostra figurinha nenhuma', (await pSem.locator('.figurinha').count()) === 0);

  // Logout invalida a sessao de verdade.
  await page.goto(BASE + '/logout', { waitUntil: 'domcontentloaded' });
  await page.goto(BASE + '/dashboard', { waitUntil: 'domcontentloaded' });
  checar('depois do logout o dashboard exige login de novo', page.url().includes('/login'));

  console.log('\n=======================================');
  console.log('  ' + ok + ' passaram, ' + falhas + ' falharam');
  console.log('=======================================\n');

  await browser.close();
  process.exit(falhas > 0 ? 1 : 0);
})();
