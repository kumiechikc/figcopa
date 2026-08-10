/* =========================================================================
   Contrato de markup entre a vitrine e o app.css.

   A vitrine reaproveita a folha de estilo da aplicacao JSP, e o CSS assume uma
   estrutura interna em varios componentes: ".pacote-card" espera um ".arte" do
   lado, ".carta-flip" so gira se tiver ".giro/.verso/.frente" dentro. Quando a
   vitrine desenha o markup por conta propria, nada quebra ruidosamente — o
   componente simplesmente aparece sem estilo, ou sem a animacao, e passa.

   Foi assim que a revelacao do pacote ficou sem virar carta nenhuma: o teste
   olhava a classe ".virada", que era mesmo aplicada, enquanto o CSS esperava um
   ".giro" que nunca existiu.

   Este arquivo le os seletores descendentes do proprio app.css e cobra que a
   vitrine os produza. Um componente novo no CSS entra na checagem sozinho.
   ========================================================================= */
const { chromium } = require('playwright');
const fs = require('fs');

const BASE = 'http://localhost:8123';
const CSS = __dirname + '/src/main/webapp-static/assets/css/app.css';
let ok = 0, falhas = 0;

function checar(nome, cond, detalhe) {
  if (cond) { ok++; console.log('  OK    ' + nome + (detalhe ? '  → ' + detalhe : '')); }
  else { falhas++; console.log('  FALHA ' + nome + (detalhe ? '  → ' + detalhe : '')); }
}

/**
 * Ganchos de CSS para conteudo que esta tela nao tem — ausencia aqui e decisao,
 * nao esquecimento. Tudo o que nao estiver nesta lista e cobrado.
 */
const OPCIONAIS = {
  // O stepper da vitrine nao marca horario por etapa; o da JSP marca.
  'etapa': ['quando']
};

/** Componentes da vitrine e a rota onde cada um aparece. */
const ONDE = {
  'pacote-card':     '/app.html?demo=1#/pacotes',
  'carta-flip':      '/app.html?demo=1#/pacotes',
  'match':           '/app.html?demo=1#/trocas',
  'notificacao':     '/app.html?demo=1#/dashboard',
  'etapa':           '/app.html?demo=1#/trocas',
  'figurinha':       '/app.html?demo=1#/album',
  'passo':           '/index.html',
  'numeros-comunidade': '/index.html'
};

/** Extrai ".pai .filho" do CSS → { pai: [filho, ...] }. */
function contratosDoCss() {
  const css = fs.readFileSync(CSS, 'utf8');
  const mapa = {};
  // Só seletores de classe simples: ".a .b {" — ignora pseudo, vírgula, ">".
  const re = /^\.([a-z0-9-]+)\s+\.([a-z0-9-]+)\s*\{/gim;
  let m;
  while ((m = re.exec(css)) !== null) {
    const [, pai, filho] = m;
    if (!ONDE[pai]) continue;
    (mapa[pai] = mapa[pai] || new Set()).add(filho);
  }
  return mapa;
}

(async () => {
  const contratos = contratosDoCss();
  const browser = await chromium.launch({
    executablePath: '/opt/pw-browsers/chromium-1194/chrome-linux/chrome'
  });
  const page = await browser.newPage({ viewport: { width: 1440, height: 900 } });

  console.log('\n== Estrutura que o app.css exige ==');

  for (const [pai, filhos] of Object.entries(contratos)) {
    await page.goto(BASE + ONDE[pai], { waitUntil: 'networkidle' });
    await page.waitForTimeout(700);

    // A revelacao so existe depois de abrir um pacote.
    if (pai === 'carta-flip') {
      await page.locator('[data-abrir]').first().click();
      await page.waitForSelector('.carta-flip', { timeout: 5000 });
    }
    // O stepper so existe depois de propor.
    if (pai === 'etapa') {
      await page.locator('[data-propor]').first().click();
      await page.waitForSelector('.etapa', { timeout: 5000 });
    }

    // Varre TODAS as instancias: numa lista de figurinhas so algumas sao
    // repetidas, entao olhar so a primeira reprova um markup que esta correto.
    const presentes = await page.evaluate(p => {
      const raizes = [...document.querySelectorAll('.' + p)];
      if (raizes.length === 0) return null;
      return raizes.flatMap(r => [...r.querySelectorAll('*')].flatMap(el => [...el.classList]));
    }, pai);

    if (presentes === null) { checar(`.${pai} existe na tela`, false, ONDE[pai]); continue; }

    const opcionais = OPCIONAIS[pai] || [];
    for (const filho of filhos) {
      if (opcionais.includes(filho)) continue;
      checar(`.${pai} > .${filho}`, presentes.includes(filho));
    }
  }

  // Classe presente nao prova estilo aplicado. ".avatar-g" so define tamanho —
  // o circulo e o gradiente vem de ".avatar" — entao usar o modificador sozinho
  // rende um texto solto, com a classe certa e nenhuma aparencia.
  console.log('\n== O estilo realmente pegou ==');

  const APARENCIA = [
    ['/app.html?demo=1#/perfil',  '.avatar-g',          'borderRadius',    v => v.includes('50%'),      'circulo'],
    ['/app.html?demo=1#/perfil',  '.avatar-g',          'backgroundImage', v => v.includes('gradient'), 'gradiente'],
    ['/app.html?demo=1#/pacotes', '.pacote-card .arte', 'borderTopWidth',  v => parseFloat(v) > 0,      'moldura'],
    ['/app.html?demo=1#/trocas',  '.fig-linha',         'borderLeftWidth', v => parseFloat(v) >= 3,     'faixa de raridade']
  ];

  for (const [rota, seletor, prop, valido, oque] of APARENCIA) {
    await page.goto(BASE + rota, { waitUntil: 'networkidle' });
    await page.waitForSelector(seletor, { timeout: 5000 }).catch(() => {});
    const valor = await page.locator(seletor).first()
      .evaluate((el, p) => getComputedStyle(el)[p], prop).catch(() => null);
    checar(`${seletor} tem ${oque}`, valor !== null && valido(valor), String(valor).slice(0, 34));
  }

  console.log('\n== A revelacao realmente anima ==');
  await page.goto(BASE + '/app.html?demo=1#/pacotes', { waitUntil: 'networkidle' });
  await page.locator('[data-abrir]').first().click();
  await page.waitForSelector('.carta-flip .giro', { timeout: 5000 });

  // Antes de virar, o giro esta em rotateY(180deg): a carta mostra o verso.
  const antes = await page.locator('.carta-flip .giro').first()
    .evaluate(el => getComputedStyle(el).transform);
  checar('carta comeca virada para baixo', antes !== 'none', antes.slice(0, 30));

  await page.waitForTimeout(2600);
  const depois = await page.locator('.carta-flip .giro').first()
    .evaluate(el => getComputedStyle(el).transform);
  checar('carta girou (transform mudou)', antes !== depois, depois.slice(0, 30));
  checar('todas viraram', (await page.locator('.carta-flip.virada').count()) === 7,
         (await page.locator('.carta-flip.virada').count()) + '/7');

  console.log('\n== Movimento reduzido ==');
  const ctxReduzido = await browser.newContext({ reducedMotion: 'reduce' });
  const pr = await ctxReduzido.newPage();
  await pr.goto(BASE + '/app.html?demo=1#/pacotes', { waitUntil: 'networkidle' });
  await pr.locator('[data-abrir]').first().click();
  await pr.waitForSelector('.carta-flip', { timeout: 5000 });
  await pr.waitForTimeout(400);   // sem stagger: viram de imediato
  checar('com movimento reduzido as cartas ja aparecem viradas',
         (await pr.locator('.carta-flip.virada').count()) === 7,
         (await pr.locator('.carta-flip.virada').count()) + '/7');

  console.log('\n=======================================');
  console.log('  ' + ok + ' passaram, ' + falhas + ' falharam');
  console.log('=======================================\n');

  await browser.close();
  process.exit(falhas > 0 ? 1 : 0);
})();
