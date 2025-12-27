<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="pageTitle" value="로그인" />
<c:set var="pageCSS" value="${['member.css']}" />
<c:set var="pageJS" value="${['member.js']}" />

<%@ include file="../common/header.jsp" %>
<body>
<%@ include file="../common/navigation.jsp" %>

<div class="container-fluid mt-4">
    <div class="d-flex justify-content-center align-items-center login-center-wrapper">
        <div class="row justify-content-center w-100">
            <div class="col-md-6 col-lg-4">
                <div class="card shadow-lg border-0">
                    <div class="card-header bg-primary text-white text-center py-4">
                        <h2 class="mb-0"><i class="bi bi-shield-lock"></i> 로그인</h2>
                        <p class="mb-0 mt-2">통신판매사업자관리 시스템에 로그인하세요</p>
                    </div>
                    <div class="card-body p-4">
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

                        <form id="loginForm" method="post" action="/members/login" class="needs-validation" novalidate>
                            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />

                            <div class="mb-3">
                                <label for="username" class="form-label">
                                    <i class="bi bi-person"></i> 아이디
                                </label>
                                <input type="text" class="form-control form-control-lg" id="username" name="username"
                                       value="${username}" required autofocus
                                       placeholder="아이디를 입력하세요">
                                <div class="invalid-feedback">
                                    아이디를 입력해주세요.
                                </div>
                            </div>

                            <div class="mb-3">
                                <label for="password" class="form-label">
                                    <i class="bi bi-lock"></i> 비밀번호
                                </label>
                                <input type="password" class="form-control form-control-lg" id="password" name="password"
                                       required placeholder="비밀번호를 입력하세요">
                                <div class="invalid-feedback">
                                    비밀번호를 입력해주세요.
                                </div>
                            </div>

                            <div class="mb-3 form-check">
                                <input type="checkbox" class="form-check-input" id="rememberMe" name="rememberMe">
                                <label class="form-check-label" for="rememberMe">
                                    로그인 상태 유지
                                </label>
                            </div>

                            <div class="d-grid gap-2">
                                <button type="submit" class="btn btn-primary btn-lg">
                                    <i class="bi bi-box-arrow-in-right"></i> 로그인
                                </button>
                            </div>
                        </form>

                        <hr class="my-4">

                        <div class="text-center">
                            <div class="row">
                                <div class="col-6">
                                    <a href="/members/password/find" class="btn btn-outline-secondary w-100">
                                        <i class="bi bi-key"></i> 비밀번호 찾기
                                    </a>
                                </div>
                                <div class="col-6">
                                    <a href="/members/join" class="btn btn-outline-primary w-100">
                                        <i class="bi bi-person-plus"></i> 회원가입
                                    </a>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="card mt-4 border-0 bg-light">
                    <div class="card-body text-center py-3">
                        <h6 class="card-title mb-2"><i class="bi bi-info-circle text-primary"></i> 시스템 안내</h6>
                        <p class="card-text small text-muted mb-0">
                            통신판매사업자 정보를 체계적으로 관리하고 모니터링할 수 있는 통합 관리 시스템입니다.
                        </p>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<%@ include file="../common/footer.jsp" %>
</body>
</html>