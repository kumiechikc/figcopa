const { chromium } = require('playwright');

const BASE = 'http://localhost:8123';
const SHOTS = __dirname + '/shots-static';
let ok = 0, falhas = 0;

function checar(nome, cond, detalhe) {
  if (cond) { ok++; console.log('  OK    ' + nome + (detalhe ? '  → ' + detalhe : '')); }
  else { falhas++; console.log('  FALHA ' + nome + (detalhe ? '  → ' + detalhe : '')); }
}

(async () => {
  const browser = await chromium.launch({ executablePath: '/opt/pw-browsers/chromium-1194/chrome-linux/chrome' });
  const ctx = await browser.newContext({ viewport: { width: 1440, height: 900 } });
  const page = await ctx.newPage();

  const errosJs = [];
  page.on('pageerror', e => errosJs.push(e.message));
  page.on('console', m => { if (m.type() === 'error') errosJs.push('console: ' + m.text()); });

  console.log('\n== Landing ==');
  await page.goto(BASE + '/index.html', { waitUntil: 'networkidle' });
  checar('titulo', (await page.title()).includes('The Champions'), await page.title());
  checar('h1 presente', (await page.locator('h1').count()) > 0);
  await page.screenshot({ path: SHOTS + '/01-landing.png' });

  console.log('\n== Dashboard ==');
  await page.goto(BASE + '/app.html?demo=1#/dashboard', { waitUntil: 'networkidle' });
  await page.waitForSelector('.sidebar', { timeout: 5000 });
  checar('sidebar renderizou', (await page.locator('.sidebar').count()) === 1);
  checar('itens de menu', (await page.locator('.nav-item').count()) >= 7,
         (await page.locator('.nav-item').count()) + ' itens');
  checar('faixa de demo visivel', (await page.locator('.faixa-demo').count()) === 1);
  const pct = await page.locator('.percentual').innerText();
  checar('percentual de progresso', /\d+%/.test(pct), pct);
  checar('barras por raridade', (await page.locator('.barra').count()) === 4,
         (await page.locator('.barra').count()) + '');
  checar('notificacoes', (await page.locator('.notificacao').count()) > 0,
         (await page.locator('.notificacao').count()) + '');
  checar('tabela de trocas', (await page.locator('.tabela tbody tr').count()) > 0,
         (await page.locator('.tabela tbody tr').count()) + ' linhas');
  await page.waitForTimeout(900);
  const larg = await page.locator('.barra > i').first().evaluate(el => el.style.width);
  checar('barra animou', larg !== '' && larg !== '0%', larg);
  checar('icones do sprite carregaram',
         await page.locator('.nav-item svg use').first().evaluate(
           el => !!document.getElementById(el.getAttribute('href').slice(1))));
  await page.screenshot({ path: SHOTS + '/02-dashboard.png' });

  console.log('\n== Album ==');
  await page.goto(BASE + '/app.html?demo=1#/album', { waitUntil: 'networkidle' });
  await page.waitForSelector('.figurinha', { timeout: 5000 });
  const total = await page.locator('.figurinha').count();
  checar('grid de figurinhas', total === 51, total + ' cartas (esperado 51)');
  checar('tem faltantes', (await page.locator('.figurinha.faltante').count()) > 0,
         (await page.locator('.figurinha.faltante').count()) + '');
  checar('tem repetidas', (await page.locator('.figurinha .repetida').count()) > 0,
         (await page.locator('.figurinha .repetida').count()) + '');
  checar('molduras por raridade',
         (await page.locator('.figurinha.r4').count()) > 0 &&
         (await page.locator('.figurinha.r1').count()) > 0);
  await page.screenshot({ path: SHOTS + '/03-album.png' });

  await page.click('a[href="#/album?status=faltantes"]');
  await page.waitForTimeout(400);
  const nFalt = await page.locator('.figurinha').count();
  checar('filtro faltantes', nFalt > 0 && nFalt === await page.locator('.figurinha.faltante').count(),
         nFalt + ' faltantes');

  await page.click('a[href*="selecao=BRA"]');
  await page.waitForTimeout(400);
  checar('filtro por selecao combina com status', (await page.locator('.figurinha').count()) > 0,
         (await page.locator('.figurinha').count()) + ' cartas');

  console.log('\n== Pacotes ==');
  await page.goto(BASE + '/app.html?demo=1#/pacotes', { waitUntil: 'networkidle' });
  await page.waitForSelector('.pacote-card', { timeout: 5000 });
  checar('pacotes listados', (await page.locator('.pacote-card').count()) === 3,
         (await page.locator('.pacote-card').count()) + '');
  checar('pacote indisponivel desabilitado',
         (await page.locator('.pacote-card button[disabled]').count()) === 1);
  await page.locator('[data-abrir]').first().click();
  await page.waitForSelector('.carta-flip', { timeout: 5000 });
  checar('abriu 7 cartas', (await page.locator('.carta-flip').count()) === 7,
         (await page.locator('.carta-flip').count()) + '');
  await page.waitForTimeout(2600);
  const viradas = await page.locator('.carta-flip.virada').count();
  checar('todas viraram', viradas === 7, viradas + '/7');
  await page.screenshot({ path: SHOTS + '/04-pacotes.png' });

  console.log('\n== Trocas ==');
  await page.goto(BASE + '/app.html?demo=1#/trocas', { waitUntil: 'networkidle' });
  await page.waitForSelector('.match', { timeout: 5000 });
  const nm = await page.locator('.match').count();
  checar('matches listados', nm > 0, nm + ' parceiros');
  checar('cada match tem dois lados', (await page.locator('.match .troca-lados').count()) === nm);
  await page.screenshot({ path: SHOTS + '/05-trocas.png' });

  console.log('\n== Negociacao ==');
  await page.locator('.match [data-propor]').first().click();
  await page.waitForSelector('.stepper', { timeout: 5000 });
  checar('stepper de 4 etapas', (await page.locator('.etapa').count()) === 4);
  checar('layout bilateral', (await page.locator('.negociacao > *').count()) === 3);
  await page.click('#confirmar');
  await page.waitForTimeout(300);
  checar('troca confirma', (await page.locator('.alerta-sucesso').count()) === 1);
  checar('etapas concluidas', (await page.locator('.etapa.feita').count()) === 4);
  await page.screenshot({ path: SHOTS + '/06-negociacao.png' });

  console.log('\n== Perfil / Ranking / Historico ==');
  for (const [rota, marca] of [['perfil', '.avatar-g'], ['ranking', '.tabela'], ['historico', '.tabela']]) {
    await page.goto(BASE + '/app.html?demo=1#/' + rota, { waitUntil: 'networkidle' });
    await page.waitForTimeout(400);
    checar('abre ' + rota, (await page.locator(marca).count()) > 0);
  }
  await page.screenshot({ path: SHOTS + '/07-ranking.png' });

  console.log('\n== Rota invalida ==');
  await page.goto(BASE + '/app.html?demo=1#/naoexiste', { waitUntil: 'networkidle' });
  await page.waitForTimeout(500);
  checar('rota desconhecida cai no dashboard', (await page.locator('.percentual').count()) > 0);

  console.log('\n== Responsivo 390x844 ==');
  const mob = await ctx.newPage();
  await mob.setViewportSize({ width: 390, height: 844 });
  await mob.goto(BASE + '/app.html?demo=1#/dashboard', { waitUntil: 'networkidle' });
  await mob.waitForTimeout(600);
  const over = await mob.evaluate(() =>
    document.documentElement.scrollWidth - document.documentElement.clientWidth);
  checar('sem rolagem horizontal', over <= 1, 'overflow=' + over + 'px');
  await mob.screenshot({ path: SHOTS + '/08-mobile.png' });

  await mob.goto(BASE + '/app.html?demo=1#/album', { waitUntil: 'networkidle' });
  await mob.waitForTimeout(600);
  const over2 = await mob.evaluate(() =>
    document.documentElement.scrollWidth - document.documentElement.clientWidth);
  checar('album sem rolagem horizontal', over2 <= 1, 'overflow=' + over2 + 'px');

  await mob.goto(BASE + '/index.html', { waitUntil: 'networkidle' });
  const over3 = await mob.evaluate(() =>
    document.documentElement.scrollWidth - document.documentElement.clientWidth);
  checar('landing sem rolagem horizontal', over3 <= 1, 'overflow=' + over3 + 'px');

  console.log('\n== Erros de JS ==');
  const meus = errosJs.filter(e => !/fonts\.googleapis|Failed to load resource/.test(e));
  checar('nenhum erro de JS proprio', meus.length === 0, meus.slice(0, 3).join(' / ') || 'limpo');

  console.log('\n=======================================');
  console.log('  ' + ok + ' passaram, ' + falhas + ' falharam');
  console.log('=======================================\n');

  await browser.close();
  process.exit(falhas > 0 ? 1 : 0);
})();
