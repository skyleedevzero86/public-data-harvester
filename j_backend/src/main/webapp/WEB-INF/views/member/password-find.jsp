<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<c:set var="pageTitle" value="비밀번호 찾기" />
<c:set var="pageCSS" value="${['member.css']}" />
<c:set var="pageJS" value="${['member.js']}" />

<%@ include file="../common/header.jsp" %>
<body style="display: flex; flex-direction: column; min-height: 100vh;">
<%@ include file="../common/navigation.jsp" %>

<div class="container-fluid mt-4" style="flex: 1;">
    <div class="d-flex justify-content-between align-items-center mb-4">
        <h2><i class="bi bi-key"></i> 비밀번호 찾기</h2>
    </div>

    <div class="row justify-content-center">
        <div class="col-md-6">
            <div class="card">
                <div class="card-header">
                    <div class="d-flex justify-content-between align-items-center">
                        <h5 class="mb-0"><i class="bi bi-key"></i> 비밀번호 재설정</h5>
                    </div>
                </div>
                <div class="card-body">
                    <p class="text-muted mb-4">
                        가입 시 사용한 이메일로 비밀번호 재설정 링크를 보내드립니다
                    </p>

                    <div class="step-indicator mb-4">
                        <span class="step active">
                            <span class="step-number">1</span>
                            <span class="step-text">정보 입력</span>
                        </span>
                        <span class="step-arrow">→</span>
                        <span class="step">
                            <span class="step-number">2</span>
                            <span class="step-text">이메일 확인</span>
                        </span>
                        <span class="step-arrow">→</span>
                        <span class="step">
                            <span class="step-number">3</span>
                            <span class="step-text">비밀번호 재설정</span>
                        </span>
                    </div>

                    <c:if test="${not empty errorMessage}">
                        <div class="alert alert-danger alert-dismissible fade show" role="alert">
                            <i class="bi bi-exclamation-triangle"></i> ${errorMessage}
                            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
                        </div>
                    </c:if>

                    <c:if test="${not empty successMessage}">
                        <div class="alert alert-success alert-dismissible fade show" role="alert">
                            <i class="bi bi-check-circle"></i> ${successMessage}
                            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
                        </div>
                    </c:if>

                    <div class="info-box mb-4">
                        <h6><i class="bi bi-info-circle me-2"></i>비밀번호 찾기 안내</h6>
                        <ul class="mb-0">
                            <li>가입 시 사용한 사용자명과 이메일 주소를 입력해주세요</li>
                            <li>입력하신 이메일로 비밀번호 재설정 링크를 보내드립니다</li>
                            <li>링크는 30분간 유효하며, 한 번만 사용할 수 있습니다</li>
                            <li>이메일이 오지 않는다면 스팸함을 확인해주세요</li>
                        </ul>
                    </div>

                    <form:form
                        method="post"
                        action="/members/password/find"
                        modelAttribute="passwordFindRequest"
                    >
                        <div class="mb-3">
                            <label for="username" class="form-label">사용자명 <span class="text-danger">*</span></label>
                            <form:input
                                type="text"
                                class="form-control"
                                id="username"
                                path="username"
                                placeholder="가입 시 사용한 사용자명을 입력하세요"
                                required="true"
                            />
                            <form:errors path="username" class="text-danger small" />
                        </div>

                        <div class="mb-3">
                            <label for="email" class="form-label">이메일 주소 <span class="text-danger">*</span></label>
                            <form:input
                                type="email"
                                class="form-control"
                                id="email"
                                path="email"
                                placeholder="가입 시 사용한 이메일 주소를 입력하세요"
                                required="true"
                            />
                            <form:errors path="email" class="text-danger small" />
                        </div>

                        <div class="d-grid">
                            <button type="submit" class="btn btn-primary">
                                <i class="bi bi-envelope me-2"></i>
                                비밀번호 재설정 링크 보내기
                            </button>
                        </div>

                        <div class="text-center mt-3">
                            <a href="/members/login" class="text-decoration-none me-3">
                                <i class="bi bi-arrow-left me-1"></i>
                                로그인 페이지로 돌아가기
                            </a>
                            <a href="/members/join" class="text-decoration-none">
                                <i class="bi bi-person-plus me-1"></i>
                                회원가입
                            </a>
                        </div>
                    </form:form>
                </div>
            </div>
        </div>
    </div>
</div>

<%@ include file="../common/footer.jsp" %>
<%@ include file="../common/scripts.jsp" %>
</body>
</html>


