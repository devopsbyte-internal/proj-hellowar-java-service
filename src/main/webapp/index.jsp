<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!doctype html>
<html>
<head>
  <meta charset="UTF-8">
  <title>Hello WAR</title>
</head>
<body>
<h1>Hello WAR</h1>

<p>Basic servlet:</p>
<ul>
  <li><a href="${pageContext.request.contextPath}/hello">/hello</a></li>
  <li><a href="${pageContext.request.contextPath}/hello?name=Phoenix">/hello?name=Phoenix</a></li>
</ul>

<hr>

<h2>Versioned endpoints (backend only for now)</h2>
<ul>
  <li><a href="${pageContext.request.contextPath}/version1">/hello/version1</a></li>
  <li><a href="${pageContext.request.contextPath}/version2">/hello/version2</a></li>
  <li><a href="${pageContext.request.contextPath}/version3">/hello/version3</a></li>
  <li><a href="${pageContext.request.contextPath}/version4">/hello/version4</a></li>
  <li><a href="${pageContext.request.contextPath}/version5">/hello/version5</a></li>
</ul>

<p>These will later be driven via NGINX + React links (/v1..v5) on the frontend EC2.</p>
</body>
</html>

