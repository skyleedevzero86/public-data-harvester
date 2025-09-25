<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8"%> <%@ taglib prefix="c"
                                           uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html>
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <meta name="_csrf" content="${_csrf.token}" />
  <meta name="_csrf_header" content="${_csrf.headerName}" />

  <title>
    <c:if test="${not empty pageTitle}">${pageTitle} -
    </c:if>통신판매사업자관리 시스템
  </title>

  <link
          href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css"
          rel="stylesheet"
  />

  <link
          href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.7.2/font/bootstrap-icons.css"
          rel="stylesheet"
  />

  <c:if test="${not empty includeCharts}">
    <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
  </c:if>

  <c:if test="${not empty includeCKEditor}">
    <script src="/static/ckeditor/ckeditor.js?v=4.23.1"></script>
  </c:if>

  <link href="/static/css/common.css" rel="stylesheet" />

  <c:if test="${not empty pageCSS}">
    <c:forEach var="css" items="${pageCSS}">
      <link href="/static/css/${css}" rel="stylesheet" />
    </c:forEach>
  </c:if>

  <script src="https://cdn.jsdelivr.net/npm/jquery@3.6.0/dist/jquery.min.js"></script>

  <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/js/bootstrap.bundle.min.js"></script>

  <script src="/static/js/common.js"></script>

  <c:if test="${not empty pageJS}">
    <c:forEach var="js" items="${pageJS}">
      <script src="/static/js/${js}"></script>
    </c:forEach>
  </c:if>
</head>
<body></body>
</html>