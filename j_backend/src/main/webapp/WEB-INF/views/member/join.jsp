<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<c:set var="pageTitle" value="회원가입" />
<c:set var="pageCSS" value="${['member.css']}" />
<c:set var="pageJS" value="${['member.js']}" />

<%@ include file="../common/header.jsp" %>
<body>
<%@ include file="../common/navigation.jsp" %>

<div class="container-fluid mt-4">
    <div class="d-flex justify-content-between align-items-center mb-4">
        <h2><i class="bi bi-person-plus"></i> 회원가입</h2>
    </div>

    <div class="row justify-content-center">
        <div class="col-md-6">
            <div class="card">
                <div class="card-header">
                    <div class="d-flex justify-content-between align-items-center">
                        <h5 class="mb-0"><i class="bi bi-table"></i> 회원 정보 입력</h5>
                    </div>
                </div>
                <div class="card-body">
                    <c:if test="${not empty errorMessage}">
                        <div class="alert alert-danger alert-dismissible fade show" role="alert">
                            <i class="bi bi-exclamation-triangle"></i> ${errorMessage}
                            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
                        </div>
                    </c:if>

                    <form:errors path="memberJoinRequest.*" cssClass="alert alert-danger alert-dismissible fade show" element="div">
                        <i class="bi bi-exclamation-triangle"></i> 입력 정보를 확인해주세요.
                        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
                    </form:errors>

                    <form:form modelAttribute="memberJoinRequest" method="post" action="/members/join">
                        <div class="mb-3">
                            <label for="username" class="form-label">사용자명 <span class="text-danger">*</span></label>
                            <form:input path="username" class="form-control" id="username" placeholder="4~20자의 영문, 숫자, 언더스코어" />
                            <form:errors path="username" class="text-danger small" />
                        </div>

                        <div class="mb-3">
                            <label for="password" class="form-label">비밀번호 <span class="text-danger">*</span></label>
                            <form:password path="password" class="form-control" id="password" placeholder="8자 이상, 대소문자, 숫자, 특수문자 포함" />
                            <form:errors path="password" class="text-danger small" />
                        </div>

                        <div class="mb-3">
                            <label for="nickname" class="form-label">닉네임 <span class="text-danger">*</span></label>
                            <form:input path="nickname" class="form-control" id="nickname" placeholder="2~20자" />
                            <form:errors path="nickname" class="text-danger small" />
                        </div>

                        <div class="mb-3">
                            <label for="email" class="form-label">이메일 <span class="text-danger">*</span></label>
                            <form:input path="email" type="email" class="form-control" id="email" placeholder="example@domain.com" />
                            <form:errors path="email" class="text-danger small" />
                        </div>

                        <div class="d-grid">
                            <button type="submit" class="btn btn-primary">
                                <i class="bi bi-person-plus"></i> 회원가입
                            </button>
                        </div>

                        <div class="text-center mt-3">
                            <a href="/members/login" class="text-decoration-none">
                                <i class="bi bi-box-arrow-in-right"></i> 이미 계정이 있으신가요? 로그인
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

