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
                    <form:form modelAttribute="memberJoinRequest" method="post">
                        <div class="mb-3">
                            <label for="username" class="form-label">사용자명</label>
                            <form:input path="username" class="form-control" id="username" />
                            <form:errors path="username" class="text-danger" />
                        </div>

                        <div class="mb-3">
                            <label for="password" class="form-label">비밀번호</label>
                            <form:password path="password" class="form-control" id="password" />
                            <form:errors path="password" class="text-danger" />
                        </div>

                        <div class="mb-3">
                            <label for="nickname" class="form-label">닉네임</label>
                            <form:input path="nickname" class="form-control" id="nickname" />
                            <form:errors path="nickname" class="text-danger" />
                        </div>

                        <div class="mb-3">
                            <label for="email" class="form-label">이메일</label>
                            <form:input path="email" type="email" class="form-control" id="email" />
                            <form:errors path="email" class="text-danger" />
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
</body>
</html>