<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>비밀번호 재설정 - 통신판매사업자관리 시스템</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.7.2/font/bootstrap-icons.css" rel="stylesheet">
</head>
<body>
<div class="container mt-5">
    <div class="row justify-content-center">
        <div class="col-md-6">
            <div class="card">
                <div class="card-header text-center">
                    <h4><i class="bi bi-shield-lock"></i> 비밀번호 재설정</h4>
                </div>
                <div class="card-body">
                    <c:if test="${not empty errorMessage}">
                        <div class="alert alert-danger" role="alert">
                            <i class="bi bi-exclamation-triangle"></i> ${errorMessage}
                        </div>
                    </c:if>

                    <form method="post" action="/members/password/reset">
                        <input type="hidden" name="token" value="${passwordResetRequest.token}">

                        <div class="mb-3">
                            <label for="newPassword" class="form-label">새 비밀번호</label>
                            <input type="password" class="form-control" id="newPassword" name="newPassword" required>
                            <div class="form-text">
                                영문 대소문자, 숫자, 특수문자를 포함한 8~20자
                            </div>
                        </div>

                        <div class="mb-3">
                            <label for="newPasswordConfirm" class="form-label">새 비밀번호 확인</label>
                            <input type="password" class="form-control" id="newPasswordConfirm" name="newPasswordConfirm" required>
                        </div>

                        <div class="d-grid">
                            <button type="submit" class="btn btn-primary">
                                <i class="bi bi-check-circle"></i> 비밀번호 재설정
                            </button>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    </div>
</div>
</body>
</html>