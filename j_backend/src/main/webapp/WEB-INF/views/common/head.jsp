<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<meta name="_csrf" content="${_csrf.token}"/>
<meta name="_csrf_header" content="${_csrf.headerName}"/>

<title><c:choose>
  <c:when test="${not empty pageTitle}">${pageTitle} - 통신판매사업자관리시스템</c:when>
  <c:otherwise>통신판매사업자관리시스템</c:otherwise>
</c:choose></title>

<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css" rel="stylesheet">
<link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.7.2/font/bootstrap-icons.css" rel="stylesheet">
<link rel="stylesheet" href="/static/css/common.css">

<c:if test="${not empty pageCSS}">
  <c:forEach var="css" items="${pageCSS}">
    <link rel="stylesheet" href="/static/css/${css}">
  </c:forEach>
</c:if>