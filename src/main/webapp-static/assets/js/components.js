// Carta de figurinha — mesmo desenho do carta.tag do backend, em JS.
// Se a figurinha tiver imagem licenciada, usa a foto; senao cai no SVG.

const POSICOES_CURTAS = {
  'Goleiro': 'GOL', 'Zagueiro': 'ZAG', 'Lateral': 'LAT',
  'Volante': 'VOL', 'Meia': 'MEI', 'Atacante': 'ATA'
};

export function escapar(valor) {
  if (valor === null || valor === undefined) return '';
  const mapa = { '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#039;' };
  return String(valor).replace(/[&<>"']/g, c => mapa[c]);
}

export function posicaoCurta(fig) {
  if (!fig.posicao) return 'ESC';
  return POSICOES_CURTAS[fig.posicao] || fig.posicao.slice(0, 3).toUpperCase();
}

export function carta(fig, opcoes = {}) {
  const { mostrarStatus = false, etiqueta = '', classeExtra = '' } = opcoes;
  const faltante = mostrarStatus && fig.quantidade === 0;

  const classes = [
    'figurinha',
    `r${fig.raridade.ordem}`,
    faltante ? 'faltante' : '',
    classeExtra
  ].filter(Boolean).join(' ');

  let selo = '';
  if (etiqueta) selo = `<span class="repetida">${escapar(etiqueta)}</span>`;
  else if (mostrarStatus && fig.quantidade > 1) selo = `<span class="repetida">&times;${fig.quantidade}</span>`;

  const titulo = `${fig.nomeJogador} · ${fig.selecao} · ${fig.raridade.nomeExibicao}`;

  return `
    <article class="${classes}" title="${escapar(titulo)}">
      <span class="numero">${escapar(fig.numeroAlbum)}</span>
      ${selo}
      <div class="busto">${busto(fig)}</div>
      <div class="legenda">
        <b>${escapar(fig.nomeJogador)}</b>
        <small>${escapar(fig.raridade.nomeExibicao)} · ${posicaoCurta(fig)}</small>
      </div>
    </article>`;
}

function busto(fig) {
  const foto = fig.escudo ? fig.urlImagemEscudo : fig.urlImagemJogador;
  if (foto) {
    return `<img src="${escapar(foto)}" alt="${escapar(fig.nomeJogador)}" loading="lazy"
                 style="width:100%;height:100%;object-fit:cover;border-radius:8px">`;
  }

  const cor = fig.raridade.corHex;

  if (fig.escudo) {
    return `
      <svg viewBox="0 0 80 96" style="width:52%;margin-bottom:14px">
        <path d="M40 8 L70 20 V52 C70 70 56 82 40 88 C24 82 10 70 10 52 V20 Z"
              fill="none" stroke="${cor}" stroke-width="3" opacity=".85"/>
        <path d="M40 30 L48 42 H32 Z M40 66 L32 54 H48 Z" fill="${cor}" opacity=".8"/>
      </svg>`;
  }

  return `
    <svg viewBox="0 0 80 96" preserveAspectRatio="xMidYMax meet">
      <defs>
        <linearGradient id="camisa${fig.id}" x1="0" y1="0" x2="0" y2="1">
          <stop offset="0%" stop-color="${cor}" stop-opacity=".55"/>
          <stop offset="100%" stop-color="${cor}" stop-opacity=".14"/>
        </linearGradient>
      </defs>
      <circle cx="40" cy="34" r="15" fill="#2A3350"/>
      <path d="M22 96 V62 C22 52 30 46 40 46 C50 46 58 52 58 62 V96 Z"
            fill="url(#camisa${fig.id})" stroke="${cor}" stroke-width="1.2" stroke-opacity=".5"/>
    </svg>`;
}

export function moeda(valor) {
  return valor.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });
}
