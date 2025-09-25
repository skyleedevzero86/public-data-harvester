<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="pageTitle" value="로그인" />
<c:set var="pageCSS" value="${['member.css']}" />
<c:set var="pageJS" value="${['member.js']}" />

<%@ include file="../common/header.jsp" %>

<div class="login-container">
    <div class="login-card">
        <div class="login-header">
            <h2><i class="bi bi-shield-lock"></i> 로그인</h2>
            <p>통신판매사업자관리 시스템에 로그인하세요</p>
        </div>

        <c:if test="${not empty error}">
            <div class="alert alert-danger alert-dismissible fade show" role="alert">
                <i class="bi bi-exclamation-triangle"></i> ${error}
                <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
            </div>
        </c:if>

        <c:if test="${not empty message}">
            <div class="alert alert-success alert-dismissible fade show" role="alert">
                <i class="bi bi-check-circle"></i> ${message}
                <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
            </div>
        </c:if>

        <form id="loginForm" method="post" action="/members/login" class="login-form">
            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
            
            <div class="form-group">
                <label for="username" class="form-label">
                    <i class="bi bi-person"></i> 아이디
                </label>
                <input type="text" class="form-control" id="username" name="username" 
                       value="${username}" required autofocus>
            </div>

            <div class="form-group">
                <label for="password" class="form-label">
                    <i class="bi bi-lock"></i> 비밀번호
                </label>
                <input type="password" class="form-control" id="password" name="password" required>
            </div>

            <div class="form-group form-check">
                <input type="checkbox" class="form-check-input" id="rememberMe" name="rememberMe">
                <label class="form-check-label" for="rememberMe">
                    로그인 상태 유지
                </label>
            </div>

            <button type="submit" class="btn login-btn">
                <i class="bi bi-box-arrow-in-right"></i> 로그인
            </button>
        </form>

        <div class="login-links">
            <a href="/members/password-find">비밀번호 찾기</a>
            <a href="/members/join">회원가입</a>
        </div>
    </div>
</div>

<%@ include file="../common/footer.jsp" %>
</body>
</html>
