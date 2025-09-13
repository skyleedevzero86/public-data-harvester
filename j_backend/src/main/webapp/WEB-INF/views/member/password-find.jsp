<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>비밀번호 찾기 - 통신판매사업자관리 시스템</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.7.2/font/bootstrap-icons.css" rel="stylesheet">
</head>
<body>
<div class="container mt-5">
    <div class="row justify-content-center">
        <div class="col-md-6">
            <div class="card">
                <div class="card-header text-center">
                    <h4><i class="bi bi-key"></i> 비밀번호 찾기</h4>
                </div>
                <div class="card-body">
                    <c:if test="${not empty errorMessage}">
                        <div class="alert alert-danger" role="alert">
                            <i class="bi bi-exclamation-triangle"></i> ${errorMessage}
                        </div>
                    </c:if>

                    <form method="post" action="/members/password/find">
                        <div class="mb-3">
                            <label for="username" class="form-label">사용자명</label>
                            <input type="text" class="form-control" id="username" name="username"
                                   value="${passwordFindRequest.username}" required>
                        </div>

                        <div class="mb-3">
                            <label for="email" class="form-label">이메일</label>
                            <input type="email" class="form-control" id="email" name="email"
                                   value="${passwordFindRequest.email}" required>
                        </div>

                        <div class="d-grid">
                            <button type="submit" class="btn btn-primary">
                                <i class="bi bi-send"></i> 재설정 링크 전송
                            </button>
                        </div>
                    </form>

                    <div class="text-center mt-3">
                        <a href="/members/login" class="text-decoration-none">
                            <i class="bi bi-arrow-left"></i> 로그인으로 돌아가기
                        </a>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>
</body>
</html>