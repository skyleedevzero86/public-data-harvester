<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>회원가입 - Antock System</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body>
<div class="container mt-5">
    <div class="row justify-content-center">
        <div class="col-md-6">
            <div class="card">
                <div class="card-header">
                    <h3 class="text-center">회원가입</h3>
                </div>
                <div class="card-body">

                    <c:if test="${not empty errorMessage}">
                        <div class="alert alert-danger" role="alert">
                                ${errorMessage}
                        </div>
                    </c:if>

                    <form:errors path="memberJoinRequest.*" cssClass="alert alert-danger" element="div" />

                    <form:form modelAttribute="memberJoinRequest" method="post" action="/members/join">
                        <div class="mb-3">
                            <label for="username" class="form-label">사용자명 *</label>
                            <form:input path="username" class="form-control" id="username" placeholder="4~20자의 영문, 숫자, 언더스코어" />
                            <form:errors path="username" class="text-danger small" />
                        </div>

                        <div class="mb-3">
                            <label for="password" class="form-label">비밀번호 *</label>
                            <form:password path="password" class="form-control" id="password" placeholder="8자 이상, 대소문자, 숫자, 특수문자 포함" />
                            <form:errors path="password" class="text-danger small" />
                        </div>

                        <div class="mb-3">
                            <label for="nickname" class="form-label">닉네임 *</label>
                            <form:input path="nickname" class="form-control" id="nickname" placeholder="2~20자" />
                            <form:errors path="nickname" class="text-danger small" />
                        </div>

                        <div class="mb-3">
                            <label for="email" class="form-label">이메일 *</label>
                            <form:input path="email" type="email" class="form-control" id="email" placeholder="example@domain.com" />
                            <form:errors path="email" class="text-danger small" />
                        </div>

                        <div class="d-grid">
                            <button type="submit" class="btn btn-primary">회원가입</button>
                        </div>

                        <div class="text-center mt-3">
                            <a href="/members/login">이미 계정이 있으신가요? 로그인</a>
                        </div>
                    </form:form>
                </div>
            </div>
        </div>
    </div>
</div>

<script>
    document.querySelector('form').addEventListener('submit', function(e) {
        console.log('Form submitted');
        console.log('Username:', document.getElementById('username').value);
        console.log('Nickname:', document.getElementById('nickname').value);
        console.log('Email:', document.getElementById('email').value);
    });
</script>
</body>
</html>