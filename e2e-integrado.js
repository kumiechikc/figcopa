// A vitrine estatica servida em :8123, consumindo o backend real em :8080.
// Prova que api.js sai do modo demonstracao e que o CORS deixa a chamada passar.
const { chromium } = require('playwright');

const VITRINE = 'http://localhost:8123';
const BACKEND = 'http://localhost:8080/the-champions';
let ok = 0, falhas = 0;

function checar(nome, cond, detalhe) {
  if (cond) { ok++; console.log('  OK    ' + nome + (detalhe ? '  → ' + detalhe : '')); }
  else { falhas++; console.log('  FALHA ' + nome + (detalhe ? '  → ' + detalhe : '')); }
}

(async () => {
  const browser = await chromium.launch({
    executablePath: '/opt/pw-browsers/chromium-1194/chrome-linux/chrome'
  });
  const page = await browser.newPage({ viewport: { width: 1440, height: 900 } });

  const errosJs = [];
  page.on('pageerror', e => errosJs.push(e.message));

  // Pega o token numa pagina neutra: abrir a vitrine antes de ter token faz
  // ela disparar chamadas que voltam 401, e o tratamento do 401 apaga o
  // tc_token — inclusive o que o teste tivesse acabado de gravar.
  await page.goto(VITRINE + '/index.html');
  const token = await page.evaluate(async (backend) => {
    const r = await fetch(backend + '/api/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email: 'vinicius.k@email.com', senha: '123456' })
    });
    return (await r.json()).token;
  }, BACKEND);

  checar('login pela vitrine devolve token', !!token, token ? token.slice(0, 12) + '...' : 'sem token');

  // addInitScript roda antes de qualquer script da pagina: o api.js ja encontra
  // a configuracao pronta quando o modulo e avaliado.
  await page.addInitScript(([backend, t]) => {
    localStorage.setItem('tc_backend_url', backend);
    localStorage.setItem('tc_token', t);
  }, [BACKEND, token]);

  console.log('\n== Vitrine ligada ao backend ==');
  await page.goto(VITRINE + '/app.html#/dashboard', { waitUntil: 'networkidle' });
  await page.waitForSelector('.sidebar', { timeout: 8000 });

  checar('saiu do modo demonstracao', (await page.locator('.faixa-demo').count()) === 0);

  const pct = await page.locator('.percentual').innerText();
  checar('progresso veio do banco', pct === '43%', pct + ' (banco: 43%)');

  await page.goto(VITRINE + '/app.html#/album', { waitUntil: 'networkidle' });
  await page.waitForSelector('.figurinha', { timeout: 8000 });
  const cartas = await page.locator('.figurinha').count();
  checar('album do banco tem 51 cartas', cartas === 51, cartas + '');

  await page.goto(VITRINE + '/app.html#/trocas', { waitUntil: 'networkidle' });
  await page.waitForTimeout(1200);
  checar('matches vieram do MatchDAO', (await page.locator('.match').count()) > 0,
         (await page.locator('.match').count()) + ' parceiros');

  await page.goto(VITRINE + '/app.html#/ranking', { waitUntil: 'networkidle' });
  await page.waitForTimeout(800);
  checar('ranking do banco', (await page.locator('.tabela tbody tr').count()) > 0,
         (await page.locator('.tabela tbody tr').count()) + ' linhas');

  await page.goto(VITRINE + '/app.html#/pacotes', { waitUntil: 'networkidle' });
  await page.waitForSelector('.pacote-card', { timeout: 8000 });
  const antes = await page.locator('.pacote-card').count();
  checar('pacotes reais listados', antes > 0, antes + '');

  await page.locator('[data-abrir]').first().click();
  await page.waitForSelector('.carta-flip', { timeout: 8000 });
  checar('pacote real abriu 7 cartas', (await page.locator('.carta-flip').count()) === 7,
         (await page.locator('.carta-flip').count()) + '');

  console.log('\n== Propor e confirmar troca de verdade ==');
  await page.goto(VITRINE + '/app.html#/trocas', { waitUntil: 'networkidle' });
  await page.waitForSelector('.match', { timeout: 8000 });
  const rotuloBotao = await page.locator('[data-propor]').first().innerText();
  checar('match sem codigo vira "Propor troca"', rotuloBotao.includes('Propor'), rotuloBotao.trim());

  await page.locator('[data-propor]').first().click();
  await page.waitForSelector('.stepper', { timeout: 10000 });
  const codigo = await page.evaluate(() => new URLSearchParams(location.hash.split('?')[1]).get('codigo'));
  checar('proposta gravada gerou codigo', /^TRC-/.test(codigo || ''), codigo);

  // Propor ja grava confirmou_proponente=TRUE: propor e confirmar o seu lado.
  // A tela tem de refletir isso em vez de oferecer "confirmar" de novo.
  const rotuloConfirmar = await page.locator('#confirmar').innerText();
  checar('quem propos ja aparece como confirmado',
         rotuloConfirmar.includes('já confirmou'), rotuloConfirmar.trim());
  checar('botao de confirmar fica travado',
         await page.locator('#confirmar').isDisabled());
  const espera = await page.locator('.negociacao').innerText();
  checar('tela diz que espera o parceiro',
         espera.includes('Aguardando a confirmação do parceiro'), 'ok');

  const estado = await page.evaluate(async ([b, c]) => {
    const r = await fetch(`${b}/api/troca/${c}`, {
      headers: { Authorization: 'Bearer ' + localStorage.getItem('tc_token') } });
    const j = await r.json();
    return { eu: j.euConfirmei, parceiro: j.parceiroConfirmou, status: j.status };
  }, [BACKEND, codigo]);
  checar('proposta persistiu como PENDENTE com um lado confirmado',
         estado.eu === true && estado.parceiro === false && estado.status === 'PENDENTE',
         JSON.stringify(estado));

  console.log('\n== Sem token: a vitrine pede login ==');
  // Aba limpa: reaproveitar a anterior traz o token bom ja guardado e o
  // localStorage mexido pelos 401 anteriores, e o teste deixa de medir o caso.
  const limpa = await browser.newPage();
  await limpa.addInitScript((backend) => {
    localStorage.setItem('tc_backend_url', backend);
    localStorage.removeItem('tc_token');
  }, BACKEND);
  await limpa.goto(VITRINE + '/app.html#/album', { waitUntil: 'networkidle' });
  await limpa.waitForSelector('#formLogin', { timeout: 8000 });
  checar('backend vivo sem token mostra o login', (await limpa.locator('#formLogin').count()) === 1);
  checar('login nao mostra faixa de demonstracao', (await limpa.locator('.faixa-demo').count()) === 0);

  await limpa.fill('#senha', 'errada');
  await limpa.click('#formLogin button');
  await limpa.waitForTimeout(1200);
  checar('senha errada volta ao login com aviso',
         (await limpa.locator('.alerta-erro').count()) === 1,
         (await limpa.locator('.alerta-erro').innerText().catch(() => '—')).slice(0, 40));

  await limpa.fill('#senha', '123456');
  await limpa.click('#formLogin button');
  await limpa.waitForSelector('.sidebar', { timeout: 8000 });
  checar('login pela tela entra no dashboard', (await limpa.locator('.percentual').count()) === 1);

  console.log('\n== Token vencido no meio do uso ==');
  const vencida = await browser.newPage();
  await vencida.addInitScript((backend) => {
    localStorage.setItem('tc_backend_url', backend);
    localStorage.setItem('tc_token', 'lixo');
  }, BACKEND);
  await vencida.goto(VITRINE + '/app.html#/album', { waitUntil: 'networkidle' });
  await vencida.waitForSelector('#formLogin', { timeout: 8000 });
  checar('401 devolve ao login em vez de tela morta',
         (await vencida.locator('#formLogin').count()) === 1);
  checar('login avisa que a sessao caiu',
         (await vencida.locator('.alerta-erro').innerText().catch(() => '')).includes('expirada'),
         (await vencida.locator('.alerta-erro').innerText().catch(() => '—')).slice(0, 40));

  const meus = errosJs.filter(e => !/fonts\.googleapis|Failed to load resource/.test(e));
  checar('sem erro de JS proprio', meus.length === 0, meus.slice(0, 2).join(' / ') || 'limpo');

  console.log('\n  ' + ok + ' passaram, ' + falhas + ' falharam\n');
  await browser.close();
  process.exit(falhas > 0 ? 1 : 0);
})();
