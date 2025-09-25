<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="pageTitle" value="오류 발생" />
<c:set var="pageCSS" value="${['health.css']}" />

<!DOCTYPE html>
<html lang="ko">
<head>
    <%@ include file="../common/head.jsp" %>
</head>
<body>
<nav class="navbar navbar-expand-lg navbar-dark bg-dark">
    <div class="container">
        <a class="navbar-brand" href="/">
            <i class="bi bi-shield-check"></i> 통신판매사업자관리 시스템
        </a>
        <div class="navbar-nav ms-auto">
            <a class="nav-link" href="/health">
                <i class="bi bi-speedometer2"></i> 헬스 대시보드
            </a>
            <a class="nav-link" href="/health/status">
                <i class="bi bi-heart-pulse"></i> 상태 조회
            </a>
            <a class="nav-link" href="/members/logout">
                <i class="bi bi-box-arrow-right"></i> 로그아웃
            </a>
        </div>
    </div>
</nav>

<div class="container mt-5">
    <div class="row justify-content-center">
        <div class="col-md-8">
            <div class="card error-card">
                <div class="card-body text-center">
                    <div class="error-icon mb-4">
                        <i class="bi bi-exclamation-triangle-fill text-danger" style="font-size: 4rem;"></i>
                    </div>
                    <h2 class="card-title text-danger mb-3">오류가 발생했습니다</h2>
                    <c:choose>
                        <c:when test="${not empty errorMessage}">
                            <p class="card-text text-muted mb-4">${errorMessage}</p>
                        </c:when>
                        <c:otherwise>
                            <p class="card-text text-muted mb-4">
                                요청을 처리하는 중에 예상치 못한 오류가 발생했습니다.<br>
                                잠시 후 다시 시도해 주세요.
                            </p>
                        </c:otherwise>
                    </c:choose>

                    <c:if test="${not empty errorCode}">
                        <div class="alert alert-light border">
                            <small class="text-muted">
                                <i class="bi bi-info-circle"></i> 오류 코드: ${errorCode}
                            </small>
                        </div>
                    </c:if>

                    <div class="d-grid gap-2 d-md-flex justify-content-md-center">
                        <a href="javascript:history.back()" class="btn btn-outline-secondary">
                            <i class="bi bi-arrow-left"></i> 이전 페이지
                        </a>
                        <a href="/health" class="btn btn-primary">
                            <i class="bi bi-house"></i> 헬스 대시보드
                        </a>
                        <a href="javascript:location.reload()" class="btn btn-outline-primary">
                            <i class="bi bi-arrow-clockwise"></i> 새로고침
                        </a>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<%@ include file="../common/footer.jsp" %>

<%@ include file="../common/scripts.jsp" %>
<script>
    document.addEventListener('DOMContentLoaded', function() {
        const card = document.querySelector('.error-card');
        if (card) {
            card.style.opacity = "0";
            card.style.transform = "scale(0.8)";
            card.style.transition = "all 0.5s ease";
            setTimeout(() => {
                card.style.opacity = "1";
                card.style.transform = "scale(1)";
            }, 100);
        }
    });
</script>
</body>
</html>