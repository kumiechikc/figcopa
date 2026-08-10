/* =========================================================================
   The Champions — comportamento compartilhado das telas.
   Sem biblioteca externa: apenas CSS transitions e Web Animations API.
   ========================================================================= */
(function () {
  'use strict';

  var reduzirMovimento = window.matchMedia('(prefers-reduced-motion: reduce)').matches;

  /* Barras de progresso animam da esquerda ate o valor ao carregar a pagina.
     O valor real vem do banco no atributo data-largura. */
  function animarBarras() {
    document.querySelectorAll('.barra > i[data-largura]').forEach(function (barra) {
      var alvo = barra.dataset.largura + '%';
      if (reduzirMovimento) {
        barra.style.width = alvo;
        return;
      }
      requestAnimationFrame(function () {
        requestAnimationFrame(function () {
          barra.style.width = alvo;
        });
      });
    });
  }

  /* Entrada escalonada dos cartoes de uma lista. */
  function escalonarEntrada(seletor, atraso) {
    if (reduzirMovimento) return;
    document.querySelectorAll(seletor).forEach(function (elemento, indice) {
      elemento.style.opacity = '0';
      elemento.animate(
        [{ opacity: 0, transform: 'translateY(8px)' }, { opacity: 1, transform: 'none' }],
        { duration: 260, delay: indice * (atraso || 40), easing: 'cubic-bezier(.22,.8,.3,1)', fill: 'forwards' }
      );
    });
  }

  document.addEventListener('DOMContentLoaded', function () {
    animarBarras();
    escalonarEntrada('.match', 60);
  });

  window.TheChampions = { escalonarEntrada: escalonarEntrada, reduzirMovimento: reduzirMovimento };
})();
