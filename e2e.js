const { chromium } = require('playwright');

const BASE = 'http://localhost:8080/the-champions';
const SHOTS = __dirname + '/shots';
let ok = 0, falhas = 0;

function checar(nome, cond, detalhe) {
  if (cond) { ok++; console.log('  OK    ' + nome + (detalhe ? '  ' + detalhe : '')); }
  else { falhas++; console.log('  FALHA ' + nome + (detalhe ? '  ' + detalhe : '')); }
}

(async () => {
  const browser = await chromium.launch({ executablePath: process.env.CHROMIUM || '/opt/pw-browsers/chromium-1194/chrome-linux/chrome' });
  const ctx = await browser.newContext({ viewport: { width: 1440, height: 900 } });
  const page = await ctx.newPage();

  const errosJs = [];
  page.on('pageerror', e => errosJs.push(e.message));
  page.on('console', m => { if (m.type() === 'error') errosJs.push('console: ' + m.text()); });

  console.log('\n== Landing ==');
  await page.goto(BASE + '/', { waitUntil: 'domcontentloaded' });
  checar('titulo da landing', (await page.title()).includes('The Champions'), await page.title());
  checar('chamada principal', (await page.locator('h1').first().innerText()).includes('não tem'));
  await page.screenshot({ path: SHOTS + '/01-landing.png', fullPage: false });

  console.log('\n== Filtro de autenticacao ==');
  await page.goto(BASE + '/dashboard');
  checar('sem sessao redireciona para login', page.url().includes('/login'), page.url());
  checar('guarda o destino', page.url().includes('destino'), page.url());

  console.log('\n== Login ==');
  await page.goto(BASE + '/login');
  await page.fill('#email', 'vinicius.k@email.com');
  await page.fill('#senha', 'senhaerrada');
  await page.click('button[type=submit]');
  await page.waitForLoadState('domcontentloaded');
  checar('senha errada e recusada', (await page.locator('.alerta-erro').count()) > 0);
  await page.screenshot({ path: SHOTS + '/02-login.png' });

  await page.fill('#email', 'vinicius.k@email.com');
  await page.fill('#senha', '123456');
  await page.click('button[type=submit]');
  await page.waitForLoadState('domcontentloaded');
  checar('login valido entra no dashboard', page.url().includes('/dashboard'), page.url());

  console.log('\n== Dashboard ==');
  const pct = await page.locator('.percentual').innerText();
  checar('progresso do album visivel', /\d+%/.test(pct), pct);
  checar('quebra por raridade', (await page.locator('.quebra-raridade div').count()) === 5);
  checar('trocas recentes', (await page.locator('.tabela tbody tr').count()) > 0,
         (await page.locator('.tabela tbody tr').count()) + ' linhas');
  checar('notificacoes', (await page.locator('.notificacao').count()) > 0,
         (await page.locator('.notificacao').count()) + ' avisos');
  await page.waitForTimeout(1100);
  const larguraBarra = await page.locator('.barra > i').first().evaluate(el => el.style.width);
  checar('barra de progresso animou', larguraBarra !== '' && larguraBarra !== '0%', larguraBarra);
  await page.screenshot({ path: SHOTS + '/03-dashboard.png' });

  console.log('\n== Meu Album ==');
  await page.click('a[href$="/album"]');
  await page.waitForLoadState('domcontentloaded');
  const cartas = await page.locator('.figurinha').count();
  checar('grid de figurinhas', cartas > 0, cartas + ' cartas');
  checar('tem faltantes escurecidas', (await page.locator('.figurinha.faltante').count()) > 0,
         (await page.locator('.figurinha.faltante').count()) + '');
  checar('tem selo de repetida', (await page.locator('.figurinha .repetida').count()) > 0,
         (await page.locator('.figurinha .repetida').count()) + '');
  checar('molduras por raridade',
         (await page.locator('.figurinha.r3, .figurinha.r4, .figurinha.r2').count()) > 0);
  checar('lista de selecoes', (await page.locator('.selecao-item').count()) > 0,
         (await page.locator('.selecao-item').count()) + ' selecoes');
  await page.screenshot({ path: SHOTS + '/04-album.png' });

  await page.click('a[href*="status=faltantes"]');
  await page.waitForLoadState('domcontentloaded');
  checar('filtro faltantes funciona',
         (await page.locator('.figurinha').count()) === (await page.locator('.figurinha.faltante').count()));

  console.log('\n== Abertura de pacote ==');
  await page.goto(BASE + '/pacotes', { waitUntil: 'domcontentloaded' });
  const disponiveis = await page.locator('.pacote-card').count();
  checar('pacotes disponiveis', disponiveis > 0, disponiveis + ' pacotes');
  await page.screenshot({ path: SHOTS + '/05-pacotes-antes.png' });

  await page.locator('.pacote-card form button').first().click();
  await page.waitForLoadState('domcontentloaded');
  checar('abriu 7 cartas', (await page.locator('.carta-flip').count()) === 7,
         (await page.locator('.carta-flip').count()) + '');
  await page.waitForTimeout(3200);
  const viradas = await page.locator('.carta-flip.virada').count();
  checar('todas as cartas viraram', viradas === 7, viradas + '/7');
  await page.screenshot({ path: SHOTS + '/06-pacote-aberto.png' });

  console.log('\n== Buscar trocas ==');
  await page.goto(BASE + '/trocas', { waitUntil: 'domcontentloaded' });
  const matches = await page.locator('.match').count();
  checar('lista de matches', matches > 0, matches + ' parceiros');
  const qualidades = await page.locator('.match .tag').allInnerTexts();
  checar('estados variados na lista', new Set(qualidades).size > 1, qualidades.join(' | '));
  checar('proposta em aberto listada', (await page.locator('text=TRC-4187').count()) > 0);
  await page.screenshot({ path: SHOTS + '/07-trocas.png' });

  console.log('\n== Negociacao TRC-4187 (dupla confirmacao) ==');
  await page.goto(BASE + '/troca?codigo=TRC-4187', { waitUntil: 'domcontentloaded' });
  checar('layout bilateral', (await page.locator('.negociacao > *').count()) === 3);
  checar('stepper de 4 etapas', (await page.locator('.etapa').count()) === 4);
  checar('parceiro ja confirmou', (await page.locator('.tag-sucesso', { hasText: 'confirmou' }).count()) > 0);
  checar('botao de confirmar presente', (await page.locator('button[value=confirmar]').count()) === 1);
  await page.screenshot({ path: SHOTS + '/08-negociacao.png' });

  await page.click('button[value=confirmar]');
  await page.waitForLoadState('domcontentloaded');
  const aviso = await page.locator('.alerta-sucesso').first().innerText();
  checar('troca executou', aviso.includes('concluída'), aviso.trim().slice(0, 60));
  checar('status virou concluida', (await page.locator('text=Troca concluída').count()) > 0);
  await page.screenshot({ path: SHOTS + '/09-troca-concluida.png' });

  console.log('\n== Telas da conta ==');
  for (const [rota, marca] of [['/perfil', '.avatar'], ['/ranking', '.tabela'], ['/historico', '.tabela']]) {
    const r = await page.goto(BASE + rota, { waitUntil: 'domcontentloaded' });
    checar('abre ' + rota, r.status() === 200 && (await page.locator(marca).count()) > 0, 'HTTP ' + r.status());
  }
  await page.screenshot({ path: SHOTS + '/10-ranking.png' });

  console.log('\n== Cadastro ==');
  const emailNovo = 'e2e' + Date.now() + '@email.com';
  await page.goto(BASE + '/cadastro', { waitUntil: 'domcontentloaded' });
  await page.fill('#nome', 'Usuario E2E');
  await page.fill('#email', emailNovo);
  await page.fill('#senha', '123456');
  await page.fill('#confirmacao', '1234567');
  await page.click('button[type=submit]');
  await page.waitForLoadState('domcontentloaded');
  checar('senhas diferentes sao barradas', (await page.locator('.alerta-erro').count()) > 0);

  await page.fill('#nome', 'Usuario E2E');
  await page.fill('#email', emailNovo);
  await page.fill('#senha', '123456');
  await page.fill('#confirmacao', '123456');
  await page.click('button[type=submit]');
  await page.waitForLoadState('domcontentloaded');
  checar('cadastro entra logado em /pacotes', page.url().includes('/pacotes'), page.url());
  checar('ganhou pacote de boas-vindas', (await page.locator('.pacote-card').count()) === 1);

  console.log('\n== Responsivo (390x844) ==');
  const mobile = await ctx.newPage();
  await mobile.setViewportSize({ width: 390, height: 844 });
  await mobile.goto(BASE + '/dashboard', { waitUntil: 'domcontentloaded' });
  const overflow = await mobile.evaluate(() =>
    document.documentElement.scrollWidth - document.documentElement.clientWidth);
  checar('sem rolagem horizontal no mobile', overflow <= 1, 'overflow=' + overflow + 'px');
  await mobile.screenshot({ path: SHOTS + '/11-mobile.png', fullPage: false });

  console.log('\n== Logout ==');
  await page.goto(BASE + '/logout', { waitUntil: 'domcontentloaded' });
  checar('logout volta ao login', page.url().includes('/login'));
  await page.goto(BASE + '/album');
  checar('sessao encerrada bloqueia o album', page.url().includes('/login'), page.url());

  console.log('\n== Erros de JavaScript ==');
  const meus = errosJs.filter(e => !/fonts\.googleapis|Failed to load resource/.test(e));
  checar('nenhum erro de JS proprio (fontes externas ignoradas)', meus.length === 0, meus.slice(0,3).join(' / ') || (errosJs.length + ' falhas de rede externa ignoradas)'));

  console.log('\n=======================================');
  console.log('  ' + ok + ' passaram, ' + falhas + ' falharam');
  console.log('=======================================\n');

  await browser.close();
  process.exit(falhas > 0 ? 1 : 0);
})();
