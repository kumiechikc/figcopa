/* =========================================================================
   Revelacao do pacote — o momento assinatura do produto.

   As cartas chegam viradas para baixo e giram uma a uma, com intervalo de
   120ms entre elas. Uma lendaria ganha uma pausa maior antes de virar e um
   brilho pulsante depois: o atraso e o que cria a expectativa.
   ========================================================================= */
(function () {
  'use strict';

  var INTERVALO = 120;          // stagger entre cartas
  var PAUSA_LENDARIA = 420;     // suspense extra antes de uma lendaria

  function revelar() {
    var palco = document.getElementById('revelacao');
    if (!palco) return;

    var cartas = Array.prototype.slice.call(palco.querySelectorAll('.carta-flip'));
    if (cartas.length === 0) return;

    var reduzirMovimento = window.matchMedia('(prefers-reduced-motion: reduce)').matches;

    if (reduzirMovimento) {
      cartas.forEach(function (carta) { carta.classList.add('virada'); });
      return;
    }

    var atraso = 260;
    cartas.forEach(function (carta) {
      var ehLendaria = carta.dataset.lendaria === 'true';
      if (ehLendaria) atraso += PAUSA_LENDARIA;

      window.setTimeout(function () {
        carta.classList.add('virada');
        if (ehLendaria) destacarLendaria(carta);
      }, atraso);

      atraso += INTERVALO;
    });
  }

  /* Lendaria: escala levemente maior no momento em que aparece. */
  function destacarLendaria(carta) {
    carta.animate(
      [
        { transform: 'scale(1)' },
        { transform: 'scale(1.09)', offset: 0.45 },
        { transform: 'scale(1)' }
      ],
      { duration: 720, delay: 300, easing: 'cubic-bezier(.3,1.2,.4,1)' }
    );
  }

  document.addEventListener('DOMContentLoaded', revelar);
})();
