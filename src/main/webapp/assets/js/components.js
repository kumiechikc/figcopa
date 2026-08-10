// Componentes reutilizáveis para renderização de figurinhas

export function renderCard(fig, options = {}) {
  const { showStatus = false, extraClass = '' } = options;
  const classeExtra = extraClass ? ` ${extraClass}` : '';
  const faltante = fig.quantidade === 0;

  const rarity = fig.raridade;
  const rarityClass = `r${rarity.ordem}`;

  const classes = `figurinha ${rarityClass} ${faltante ? 'faltante' : ''}${classeExtra}`;

  let busto = '';

  if (fig.urlImagemJogador || fig.urlImagemEscudo) {
    const url = fig.isEscudo() ? fig.urlImagemEscudo : fig.urlImagemJogador;
    if (url) {
      busto = `<div class="busto" style="background-image: url('${htmlEscape(url)}'); background-size: cover; background-position: center;"></div>`;
    } else {
      busto = renderSvgBusto(fig);
    }
  } else {
    busto = renderSvgBusto(fig);
  }

  let statusHtml = '';
  if (showStatus && fig.quantidade > 1) {
    statusHtml = `<span class="repetida">×${fig.quantidade}</span>`;
  }

  return `
    <article class="${classes}">
      <span class="numero">${htmlEscape(fig.numeroAlbum)}</span>
      ${statusHtml}
      ${busto}
      <div class="legenda">
        <b>${htmlEscape(fig.nomeJogador)}</b>
        <small>${htmlEscape(rarity.nomeExibicao)} · ${htmlEscape(getPosicaoCurta(fig))}</small>
      </div>
    </article>
  `;
}

function renderSvgBusto(fig) {
  const color = fig.raridade.corHex;
  const isShield = fig.posicao === null;

  if (isShield) {
    return `
      <div class="busto">
        <svg viewBox="0 0 80 96">
          <path d="M10 10 L70 10 L70 45 Q70 80 40 90 Q10 80 10 45 Z" fill="${htmlEscape(color)}" stroke="#fff" stroke-width="1"/>
          <text x="40" y="50" text-anchor="middle" font-size="20" fill="#fff" font-weight="bold">${htmlEscape(fig.siglaSelecao)}</text>
        </svg>
      </div>
    `;
  }

  return `
    <div class="busto">
      <svg viewBox="0 0 80 96">
        <defs>
          <linearGradient id="camisa${fig.id}" x1="0%" y1="0%" x2="100%" y2="100%">
            <stop offset="0%" style="stop-color:${htmlEscape(color)};stop-opacity:1" />
            <stop offset="100%" style="stop-color:${htmlEscape(darkenColor(color, 20))};stop-opacity:1" />
          </linearGradient>
        </defs>
        <circle cx="40" cy="34" r="15" fill="#2A3350"/>
        <path d="M25 50 L55 50 L60 80 Q40 85 20 80 Z" fill="url(#camisa${fig.id})"/>
        <ellipse cx="20" cy="60" rx="6" ry="15" fill="#D4AF9E"/>
        <ellipse cx="60" cy="60" rx="6" ry="15" fill="#D4AF9E"/>
      </svg>
    </div>
  `;
}

function getPosicaoCurta(fig) {
  if (!fig.posicao) return 'ESC';
  const map = {
    'Goleiro': 'GOL',
    'Zagueiro': 'ZAG',
    'Lateral': 'LAT',
    'Volante': 'VOL',
    'Meia': 'MEI',
    'Atacante': 'ATA'
  };
  return map[fig.posicao] || fig.posicao.substring(0, 3).toUpperCase();
}

function htmlEscape(str) {
  if (!str) return '';
  return String(str)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#039;');
}

function darkenColor(hex, percent) {
  const num = parseInt(hex.replace('#', ''), 16);
  const amt = Math.round(2.55 * percent);
  const R = (num >> 16) - amt;
  const G = (num >> 8 & 0x00FF) - amt;
  const B = (num & 0x0000FF) - amt;
  return '#' + (
    0x1000000 + (R < 255 ? R < 1 ? 0 : R : 255) * 0x10000 +
    (G < 255 ? G < 1 ? 0 : G : 255) * 0x100 +
    (B < 255 ? B < 1 ? 0 : B : 255)
  ).toString(16).slice(1);
}
