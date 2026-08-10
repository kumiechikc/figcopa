// Camada de dados da vitrine.
//
// Duas fontes possiveis:
//   1. Backend Java (Tomcat) — quando existe uma URL configurada e ela responde.
//   2. dados-demo.js — quando nao ha backend, que e o caso do GitHub Pages hoje.
//
// A tela nao sabe de onde vem o dado: chama sempre os mesmos metodos. Quando o
// backend for publicado basta gravar a URL (config()) que tudo passa a ser real.

import * as demo from './dados-demo.js';

const CHAVE_URL = 'tc_backend_url';
const CHAVE_TOKEN = 'tc_token';

class Api {
  constructor() {
    this.baseURL = localStorage.getItem(CHAVE_URL) || this.detectarPadrao();
    this.token = localStorage.getItem(CHAVE_TOKEN);
    this.modoDemo = true;   // confirmado por sondar()

    // ?demo=1 força os dados de exemplo mesmo com backend no ar. Serve para
    // mostrar a vitrine sem depender do servidor e para os testes da vitrine
    // nao mudarem de comportamento so porque ha um Tomcat na mesma maquina.
    this.demoForcado = new URLSearchParams(location.search).has('demo');
  }

  detectarPadrao() {
    const host = window.location.hostname;
    if (host === 'localhost' || host === '127.0.0.1') {
      return 'http://localhost:8080/the-champions';
    }
    return '';   // GitHub Pages: sem backend ate alguem configurar
  }

  /** Aponta a vitrine para um backend publicado. */
  config(url) {
    this.baseURL = url;
    if (url) localStorage.setItem(CHAVE_URL, url);
    else localStorage.removeItem(CHAVE_URL);
  }

  /**
   * Descobre se ha backend vivo. Roda uma vez no boot; enquanto nao responder,
   * a vitrine segue com os dados de demonstracao em vez de mostrar tela vazia.
   */
  async sondar() {
    if (this.demoForcado || !this.baseURL) { this.modoDemo = true; return false; }
    try {
      const controle = new AbortController();
      const prazo = setTimeout(() => controle.abort(), 3000);
      // /api/status e nao /diagnostico: so as rotas sob /api/* recebem cabecalho
      // CORS, e sem ele esta sondagem falha mesmo com o backend no ar.
      const r = await fetch(`${this.baseURL}/api/status`, {
        signal: controle.signal,
        mode: 'cors'
      });
      clearTimeout(prazo);
      this.modoDemo = !r.ok;
      return r.ok;
    } catch {
      this.modoDemo = true;
      return false;
    }
  }

  async pedir(rota, opcoes = {}) {
    const cabecalhos = { 'Content-Type': 'application/json', ...opcoes.headers };
    if (this.token) cabecalhos['Authorization'] = `Bearer ${this.token}`;

    const r = await fetch(`${this.baseURL}${rota}`, {
      method: opcoes.method || 'GET',
      headers: cabecalhos,
      body: opcoes.body,
      mode: 'cors'
    });

    if (r.status === 401) {
      this.definirToken(null);
      throw new Error('Sessão expirada. Entre novamente.');
    }
    if (!r.ok) throw new Error(`Backend respondeu ${r.status}`);
    return r.json();
  }

  definirToken(token) {
    this.token = token;
    if (token) localStorage.setItem(CHAVE_TOKEN, token);
    else localStorage.removeItem(CHAVE_TOKEN);
  }

  // ---------------------------------------------------------------- consultas

  async entrar(email, senha) {
    if (this.modoDemo) {
      // A vitrine aceita qualquer credencial: nao ha o que proteger aqui.
      return { ok: true, usuario: demo.USUARIO_DEMO, demo: true };
    }
    const r = await this.pedir('/api/login', {
      method: 'POST',
      body: JSON.stringify({ email, senha })
    });
    if (r.token) this.definirToken(r.token);
    return r;
  }

  sair() {
    this.definirToken(null);
  }

  async usuario() {
    if (this.modoDemo) return demo.USUARIO_DEMO;
    return this.pedir('/api/usuario/perfil');
  }

  async album(filtros = {}) {
    let lista = this.modoDemo
      ? demo.CATALOGO.slice()
      : await this.pedir(`/api/album?${new URLSearchParams(filtros)}`);

    if (this.modoDemo) {
      if (filtros.selecao) lista = lista.filter(f => f.siglaSelecao === filtros.selecao);
      if (filtros.status === 'faltantes') lista = lista.filter(f => f.quantidade === 0);
      if (filtros.status === 'repetidas') lista = lista.filter(f => f.quantidade > 1);
      if (filtros.status === 'obtidas')   lista = lista.filter(f => f.quantidade > 0);
    }
    return lista;
  }

  async resumo() {
    if (this.modoDemo) return demo.resumoDemo();
    return this.pedir('/api/album/resumo');
  }

  async pacotes() {
    if (this.modoDemo) return demo.PACOTES;
    return this.pedir('/api/pacotes');
  }

  async abrirPacote(id) {
    if (this.modoDemo) return demo.abrirPacoteDemo();
    return this.pedir(`/api/pacotes/${id}/abrir`, { method: 'POST' });
  }

  async matches() {
    if (this.modoDemo) return demo.matchesDemo();
    return this.pedir('/api/trocas');
  }

  /** Uma troca ja gravada. No demo nao ha o que buscar: o match e a troca. */
  async troca(codigo) {
    if (this.modoDemo) {
      return demo.matchesDemo().find(m => m.codigo === codigo) || null;
    }
    return this.pedir(`/api/troca/${encodeURIComponent(codigo)}`);
  }

  async ranking() {
    if (this.modoDemo) return demo.RANKING;
    return this.pedir('/api/ranking');
  }

  async historico() {
    if (this.modoDemo) return demo.HISTORICO;
    return this.pedir('/api/historico');
  }

  async notificacoes() {
    if (this.modoDemo) return demo.NOTIFICACOES;
    return this.pedir('/api/notificacoes');
  }

  async confirmarTroca(codigo) {
    if (this.modoDemo) return { ok: true, status: 'CONCLUIDA', codigo };
    return this.pedir(`/api/troca/${codigo}/confirmar`, { method: 'POST' });
  }

  /**
   * Transforma um match em troca gravada e devolve o codigo.
   * No demo o codigo ja vem pronto no proprio match — nao ha o que gravar.
   */
  async proporTroca(match) {
    if (this.modoDemo) return { codigo: match.codigo, ok: true };
    return this.pedir('/api/trocas/propor', {
      method: 'POST',
      body: JSON.stringify({
        idParceiro: match.idParceiro,
        envio: match.envio.map(f => f.id),
        recebo: match.recebo.map(f => f.id)
      })
    });
  }
}

export const api = new Api();
