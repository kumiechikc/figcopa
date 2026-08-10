#!/usr/bin/env bash
# Copia para a vitrine estatica os arquivos cuja fonte de verdade e a aplicacao real.
#
# Sem isto o CSS da vitrine congela no dia em que foi copiado e as duas frentes
# passam a divergir em silencio. O workflow do Pages roda este script antes de
# publicar; rode-o tambem depois de mexer no app.css ou no icones.jsp.
set -euo pipefail

raiz="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
origem="$raiz/src/main/webapp"
destino="$raiz/src/main/webapp-static"

cp "$origem/assets/css/app.css" "$destino/assets/css/app.css"
echo "  css      → assets/css/app.css"

# O sprite mora numa JSP; a vitrine precisa dele como .svg puro.
# As 5 primeiras linhas sao o comentario JSP <%-- ... --%>.
tail -n +6 "$origem/WEB-INF/jsp/frag/icones.jsp" > "$destino/assets/icones.svg"
echo "  sprite   → assets/icones.svg"

echo "vitrine sincronizada."
