<%--
  Bloco <head> comum a todas as paginas.
  A pagina que inclui define antes o atributo "titulo".
--%>
<%--
  A diretiva "page" fica em cada tela (nao aqui): JSP proibe declarar
  contentType duas vezes no mesmo arquivo traduzido.
--%>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<%@ taglib prefix="tc" tagdir="/WEB-INF/tags" %>
<%-- Sem isto a JSTL formata numeros no locale do navegador do visitante:
     "R$ 2,416.00" em vez de "R$ 2.416,00". --%>
<fmt:setLocale value="pt_BR"/>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title><c:out value="${titulo}" default="The Champions"/> — The Champions</title>
  <meta name="description" content="Marketplace peer-to-peer de troca de figurinhas da Copa do Mundo 2026.">

  <link rel="preconnect" href="https://fonts.googleapis.com">
  <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>

  <%--
    media="print" tira a folha do caminho critico e o onload a promove quando
    chega. Como <link rel=stylesheet> normal, ela BLOQUEIA a renderizacao: numa
    maquina sem internet — ou atras de portal cativo, como costuma ser a rede de
    uma faculdade — o navegador espera a conexao estourar antes de desenhar
    qualquer coisa. Medido aqui: 12,8 s por tela contra 53 ms.
    O display=swap ja garante que o texto aparece na fonte do sistema e troca
    sozinho quando a Saira/Manrope carrega.
  --%>
  <link rel="stylesheet" media="print" onload="this.media='all';this.onload=null"
        href="https://fonts.googleapis.com/css2?family=Saira:wght@700;800;900&family=Manrope:wght@400;600;700;800&family=Space+Mono:wght@400;700&display=swap">
  <noscript>
    <link rel="stylesheet"
          href="https://fonts.googleapis.com/css2?family=Saira:wght@700;800;900&family=Manrope:wght@400;600;700;800&family=Space+Mono:wght@400;700&display=swap">
  </noscript>

  <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/app.css">
  <link rel="icon" href="data:image/svg+xml,<svg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24'><rect width='24' height='24' rx='6' fill='%237C3AE3'/><path d='M12 5l4 4h-8zM12 19l-4-4h8z' fill='white'/></svg>">
</head>
