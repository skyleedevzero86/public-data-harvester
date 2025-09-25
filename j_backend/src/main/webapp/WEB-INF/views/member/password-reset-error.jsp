<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="pageTitle" value="비밀번호 재설정 오류" />
<c:set var="pageCSS" value="${['member.css']}" />

<!DOCTYPE html>
<html>
<head>
    <%@ include file="../common/head.jsp" %>
    <meta name="_csrf" content="${_csrf.token}"/>
    <meta name="_csrf_header" content="${_csrf.headerName}"/>
</head>
<body>
<nav class="navbar navbar-expand-lg navbar-dark bg-dark">
    <div class="container">
        <a class="navbar-brand" href="/">
            <i class="bi bi-shield-check"></i> 통신판매사업자관리 시스템
        </a>
        <div class="navbar-nav ms-auto">
            <a class="nav-link" href="/members/login">
                <i class="bi bi-box-arrow-in-right"></i> 로그인
            </a>
            <a class="nav-link" href="/members/join">
                <i class="bi bi-person-plus"></i> 회원가입
            </a>
        </div>
    </div>
</nav>

<div class="container mt-5">
    <div class="row justify-content-center">
        <div class="col-md-6">
            <div class="error-container">
                <div class="error-icon">
                    <i class="bi bi-exclamation-triangle-fill text-danger"></i>
                </div>
                <h2 class="text-danger mb-4">비밀번호 재설정 오류</h2>
                
                <c:choose>
                    <c:when test="${not empty errorMessage}">
                        <p class="text-muted mb-4">${errorMessage}</p>
                    </c:when>
                    <c:otherwise>
                        <p class="text-muted mb-4">
                            비밀번호 재설정 링크가 유효하지 않거나 만료되었습니다.<br>
                            새로운 재설정 링크를 요청해 주세요.
                        </p>
                    </c:otherwise>
                </c:choose>

                <div class="d-grid gap-2">
                    <a href="/members/password/find" class="btn btn-primary">
                        <i class="bi bi-key"></i> 새로운 재설정 링크 요청
                    </a>
                    <a href="/members/login" class="btn btn-outline-secondary">
                        <i class="bi bi-arrow-left"></i> 로그인 페이지로
                    </a>
                </div>
            </div>
        </div>
    </div>
</div>

<%@ include file="../common/footer.jsp" %>

<%@ include file="../common/scripts.jsp" %>
</body>
</html>