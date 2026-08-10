// Dados de demonstracao do The Champions.
//
// A vitrine do GitHub Pages e estatica: nao ha Tomcat nem MySQL do outro lado.
// Este arquivo reproduz o mesmo catalogo do 02_dados.sql para que todas as
// telas possam ser navegadas antes do backend estar publicado. Quando a API
// entrar no ar, o api.js passa a usar o servidor e ignora este arquivo.

export const RARIDADES = {
  1: { id: 1, ordem: 1, nome: 'Comum',     nomeExibicao: 'Comum',     corHex: '#8A93A8', valorReferencia: 2.00,   probabilidade: 0.7800 },
  2: { id: 2, ordem: 2, nome: 'Brilhante', nomeExibicao: 'Brilhante', corHex: '#22D3EE', valorReferencia: 25.00,  probabilidade: 0.1500 },
  3: { id: 3, ordem: 3, nome: 'Rara',      nomeExibicao: 'Rara',      corHex: '#F5883D', valorReferencia: 120.00, probabilidade: 0.0400 },
  4: { id: 4, ordem: 4, nome: 'Lendaria',  nomeExibicao: 'Lendária',  corHex: '#A855F7', valorReferencia: 830.00, probabilidade: 0.0053 }
};

// [numero, nome, selecao, sigla, posicao (null = escudo), idRaridade]
const CATALOGO_BRUTO = [
  ['231','Escudo CBF','Brasil','BRA',null,2],
  ['L-07','Vinicius Jr.','Brasil','BRA','Atacante',4],
  ['233','Alisson','Brasil','BRA','Goleiro',1],
  ['234','Marquinhos','Brasil','BRA','Zagueiro',1],
  ['235','Militao','Brasil','BRA','Zagueiro',1],
  ['236','Danilo','Brasil','BRA','Lateral',1],
  ['237','Casemiro','Brasil','BRA','Volante',1],
  ['238','Raphinha','Brasil','BRA','Atacante',3],
  ['239','G. Magalhaes','Brasil','BRA','Zagueiro',1],
  ['240','Endrick','Brasil','BRA','Atacante',3],
  ['241','L. Paqueta','Brasil','BRA','Meia',1],
  ['242','Rodrygo','Brasil','BRA','Atacante',2],
  ['243','Bremer','Brasil','BRA','Zagueiro',1],
  ['244','B. Guimaraes','Brasil','BRA','Volante',1],

  ['151','Escudo AFA','Argentina','ARG',null,2],
  ['L-01','L. Messi','Argentina','ARG','Atacante',4],
  ['153','E. Martinez','Argentina','ARG','Goleiro',3],
  ['154','C. Romero','Argentina','ARG','Zagueiro',1],
  ['155','R. De Paul','Argentina','ARG','Volante',1],
  ['156','J. Alvarez','Argentina','ARG','Atacante',2],
  ['157','E. Fernandez','Argentina','ARG','Meia',1],

  ['101','Escudo DFB','Alemanha','ALE',null,2],
  ['104','J. Musiala','Alemanha','ALE','Meia',3],
  ['105','K. Havertz','Alemanha','ALE','Atacante',1],
  ['106','A. Rudiger','Alemanha','ALE','Zagueiro',1],
  ['107','M. Neuer','Alemanha','ALE','Goleiro',2],
  ['108','J. Kimmich','Alemanha','ALE','Volante',1],

  ['201','Escudo FFF','Franca','FRA',null,2],
  ['L-03','K. Mbappe','Franca','FRA','Atacante',4],
  ['212','A. Tchouameni','Franca','FRA','Volante',1],
  ['214','T. Hernandez','Franca','FRA','Lateral',1],
  ['215','W. Saliba','Franca','FRA','Zagueiro',1],
  ['216','O. Dembele','Franca','FRA','Atacante',3],

  ['301','Escudo FA','Inglaterra','ING',null,2],
  ['318','L. Shaw','Inglaterra','ING','Lateral',1],
  ['319','J. Bellingham','Inglaterra','ING','Meia',3],
  ['320','H. Kane','Inglaterra','ING','Atacante',3],
  ['321','P. Foden','Inglaterra','ING','Meia',2],
  ['322','D. Rice','Inglaterra','ING','Volante',1],

  ['401','Escudo FPF','Portugal','POR',null,2],
  ['402','B. Fernandes','Portugal','POR','Meia',1],
  ['403','R. Leao','Portugal','POR','Atacante',3],

  ['421','Escudo RFEF','Espanha','ESP',null,2],
  ['422','Pedri','Espanha','ESP','Meia',2],
  ['423','Lamine Yamal','Espanha','ESP','Atacante',3],

  ['461','Escudo HNS','Croacia','CRO',null,1],
  ['463','L. Modric','Croacia','CRO','Meia',3],

  ['088','T. Kubo','Japao','JAP','Meia',2],
  ['089','W. Endo','Japao','JAP','Volante',1],

  ['L-11','V. Osimhen','Nigeria','NIG','Atacante',4],
  ['462','A. Iwobi','Nigeria','NIG','Meia',1]
];

/**
 * Quantidades da vitrine. Sem isto todo mundo teria o album igual e as telas de
 * troca ficariam vazias: o demo precisa de faltantes E de repetidas.
 * Semente fixa para a demonstracao ser sempre a mesma entre recarregamentos.
 */
function quantidadeDemo(indice, idRaridade) {
  const ciclo = indice % 7;
  if (idRaridade === 4) return ciclo === 1 ? 1 : 0;      // lendaria quase sempre falta
  if (idRaridade === 3) return ciclo < 2 ? 1 : 0;
  if (ciclo === 0) return 0;                             // faltante
  if (ciclo === 3 || ciclo === 5) return 2;              // repetida
  if (ciclo === 6) return 3;                             // repetida
  return 1;
}

export const CATALOGO = CATALOGO_BRUTO.map((linha, i) => {
  const [numeroAlbum, nomeJogador, selecao, siglaSelecao, posicao, idRaridade] = linha;
  return {
    id: i + 1,
    numeroAlbum,
    nomeJogador,
    selecao,
    siglaSelecao,
    posicao,
    escudo: posicao === null,
    raridade: RARIDADES[idRaridade],
    quantidade: quantidadeDemo(i, idRaridade),
    // Preenchido quando houver imagens licenciadas; ate la o card cai no SVG.
    urlImagemJogador: null,
    urlImagemEscudo: null
  };
});

export const USUARIO_DEMO = {
  id: 1,
  nome: 'Vinicius K.',
  email: 'vinicius.k@email.com',
  papel: 'COMUM',
  reputacao: 4.9,
  membroDesde: '2026-01-14'
};

export const RANKING = [
  { posicao: 1, nome: 'Pedro Marques',   figurinhas: 44, trocas: 27, reputacao: 4.8 },
  { posicao: 2, nome: 'Vinicius K.',     figurinhas: 41, trocas: 31, reputacao: 4.9 },
  { posicao: 3, nome: 'Marcos Vinicius', figurinhas: 38, trocas: 19, reputacao: 4.7 },
  { posicao: 4, nome: 'Arthur Tarrago',  figurinhas: 33, trocas: 22, reputacao: 4.6 },
  { posicao: 5, nome: 'Luana Figus',     figurinhas: 29, trocas: 15, reputacao: 4.3 }
];

export const HISTORICO = [
  { codigo: 'TRC-4102', parceiro: 'Arthur Tarrago',  status: 'CONCLUIDA', quando: 'ontem',        recebidas: 2, enviadas: 2 },
  { codigo: 'TRC-4098', parceiro: 'Pedro Marques',   status: 'CONCLUIDA', quando: 'há 2 dias',    recebidas: 1, enviadas: 1 },
  { codigo: 'TRC-4055', parceiro: 'Luana Figus',     status: 'RECUSADA',  quando: 'há 5 dias',    recebidas: 0, enviadas: 0 },
  { codigo: 'TRC-3980', parceiro: 'Marcos Vinicius', status: 'CONCLUIDA', quando: 'há 8 dias',    recebidas: 3, enviadas: 3 }
];

export const PACOTES = [
  { id: 1, tipo: 'INICIAL',  nome: 'Pacote Inicial',  descricao: '7 figurinhas · chance padrão',          cartas: 7, disponivel: true },
  { id: 2, tipo: 'DIARIO',   nome: 'Pacote Diário',   descricao: '7 figurinhas · liberado a cada 24h',    cartas: 7, disponivel: true },
  { id: 3, tipo: 'ESPECIAL', nome: 'Pacote Especial', descricao: '7 figurinhas · +2% chance de lendária', cartas: 7, disponivel: false }
];

export const NOTIFICACOES = [
  { texto: 'Arthur Tarrago confirmou a troca TRC-4187.', quando: 'há 20 min',  tipo: 'troca' },
  { texto: 'Você tem 3 novos parceiros compatíveis.',    quando: 'há 2 horas', tipo: 'match' },
  { texto: 'Seu pacote diário está disponível.',         quando: 'há 5 horas', tipo: 'pacote' }
];

/** Parceiros compativeis: quem tem o que falta pra mim e quer o que me sobra. */
export function matchesDemo() {
  const faltantes = CATALOGO.filter(f => f.quantidade === 0);
  const repetidas = CATALOGO.filter(f => f.quantidade > 1);

  // Os mesmos tres valores que Match.getQualidade() devolve no backend.
  const parceiros = [
    { nome: 'Pedro Marques',   reputacao: 4.8, qualidade: 'perfeito' },
    { nome: 'Arthur Tarrago',  reputacao: 4.6, qualidade: 'perfeito' },
    { nome: 'Marcos Vinicius', reputacao: 4.7, qualidade: 'desigual' },
    { nome: 'Luana Figus',     reputacao: 4.3, qualidade: 'parcial'  }
  ];

  return parceiros.map((p, i) => ({
    ...p,
    codigo: `TRC-42${10 + i}`,
    recebo: faltantes.slice(i * 2, i * 2 + 2),
    envio: repetidas.slice(i * 2, i * 2 + 2)
  })).filter(m => m.recebo.length > 0 && m.envio.length > 0);
}

/** Resumo do album — alimenta o dashboard. */
export function resumoDemo() {
  const total = CATALOGO.length;
  const obtidas = CATALOGO.filter(f => f.quantidade > 0).length;
  const repetidas = CATALOGO.filter(f => f.quantidade > 1)
                            .reduce((s, f) => s + (f.quantidade - 1), 0);

  const porRaridade = Object.values(RARIDADES).map(r => {
    const doTipo = CATALOGO.filter(f => f.raridade.ordem === r.ordem);
    return {
      raridade: r,
      total: doTipo.length,
      obtidas: doTipo.filter(f => f.quantidade > 0).length
    };
  });

  const valor = CATALOGO
    .filter(f => f.quantidade > 0)
    .reduce((s, f) => s + f.raridade.valorReferencia * f.quantidade, 0);

  return {
    total,
    obtidas,
    faltantes: total - obtidas,
    repetidas,
    percentual: Math.round((obtidas / total) * 100),
    porRaridade,
    valorEstimado: valor
  };
}

/** Sorteio ponderado igual ao do backend (PacoteDAO): 7 cartas por pacote. */
export function abrirPacoteDemo() {
  const faixas = Object.values(RARIDADES)
    .sort((a, b) => b.ordem - a.ordem)
    .map(r => ({ r, p: r.probabilidade }));

  const sorteadas = [];
  for (let i = 0; i < 7; i++) {
    const dado = Math.random();
    let acumulado = 0;
    let escolhida = RARIDADES[1];
    for (const faixa of faixas) {
      acumulado += faixa.p;
      if (dado <= acumulado) { escolhida = faixa.r; break; }
    }
    const candidatas = CATALOGO.filter(f => f.raridade.ordem === escolhida.ordem);
    const carta = candidatas[Math.floor(Math.random() * candidatas.length)];
    if (carta) sorteadas.push({ ...carta, novaNoAlbum: carta.quantidade === 0 });
  }
  return sorteadas;
}
