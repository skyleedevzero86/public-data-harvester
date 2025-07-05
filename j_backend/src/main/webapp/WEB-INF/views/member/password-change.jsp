<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>비밀번호 변경 - Antock System</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.7.2/font/bootstrap-icons.css" rel="stylesheet">
</head>
<body>

<nav class="navbar navbar-expand-lg navbar-dark bg-dark">
    <div class="container">
        <a class="navbar-brand" href="/">
            <i class="bi bi-shield-check"></i> Antock System
        </a>
        <div class="navbar-nav ms-auto">
            <a class="nav-link" href="/members/profile">
                <i class="bi bi-person-circle"></i> 내 프로필
            </a>
            <a class="nav-link" href="/members/logout">
                <i class="bi bi-box-arrow-right"></i> 로그아웃
            </a>
        </div>
    </div>
</nav>

<div class="container mt-4">
    <div class="row justify-content-center">
        <div class="col-md-6">
            <div class="card">
                <div class="card-header">
                    <h4><i class="bi bi-key"></i> 비밀번호 변경</h4>
                </div>
                <div class="card-body">

                    <c:if test="${param.debug == 'true'}">
                        <div class="alert alert-info">
                            <strong>디버그 정보:</strong><br>
                            - isPasswordChangeRequired: ${isPasswordChangeRequired}<br>
                            - isPasswordChangeRecommended: ${isPasswordChangeRecommended}<br>
                            - todayChangeCount: ${todayChangeCount}<br>
                            - passwordChangeRequest: ${passwordChangeRequest != null ? 'exists' : 'null'}
                        </div>
                    </c:if>

                    <c:if test="${isPasswordChangeRequired}">
                        <div class="alert alert-danger" role="alert">
                            <i class="bi bi-exclamation-triangle"></i>
                            <strong>비밀번호 변경 필요</strong><br>
                            90일이 지난 비밀번호입니다. 보안을 위해 새 비밀번호로 변경해주세요.
                        </div>
                    </c:if>

                    <c:if test="${isPasswordChangeRecommended && !isPasswordChangeRequired}">
                        <div class="alert alert-warning" role="alert">
                            <i class="bi bi-info-circle"></i>
                            <strong>비밀번호 변경 권장</strong><br>
                            80일이 지난 비밀번호입니다. 보안을 위해 새 비밀번호로 변경을 권장합니다.
                        </div>
                    </c:if>

                    <c:if test="${not empty successMessage}">
                        <div class="alert alert-success alert-dismissible fade show" role="alert">
                            <i class="bi bi-check-circle"></i> ${successMessage}
                            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
                        </div>
                    </c:if>

                    <c:if test="${not empty errorMessage}">
                        <div class="alert alert-danger alert-dismissible fade show" role="alert">
                            <i class="bi bi-exclamation-triangle"></i> ${errorMessage}
                            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
                        </div>
                    </c:if>

                    <c:if test="${not empty bindingResult && bindingResult.hasGlobalErrors()}">
                        <div class="alert alert-danger">
                            <c:forEach var="error" items="${bindingResult.globalErrors}">
                                <i class="bi bi-exclamation-triangle"></i> ${error.defaultMessage}<br>
                            </c:forEach>
                        </div>
                    </c:if>

                    <div class="alert ${todayChangeCount >= 3 ? 'alert-danger' : 'alert-info'}">
                        <i class="bi bi-info-circle"></i>
                        <small>
                            오늘 비밀번호 변경 횟수: <strong>${todayChangeCount}/3</strong>
                            <br>하루 최대 3회까지 변경 가능합니다.
                            <c:if test="${todayChangeCount >= 3}">
                                <br><strong>⚠️ 오늘은 더 이상 변경할 수 없습니다.</strong>
                            </c:if>
                        </small>
                    </div>

                    <form:form modelAttribute="passwordChangeRequest" method="post" id="passwordChangeForm">
                        <div class="mb-3">
                            <label for="oldPassword" class="form-label">현재 비밀번호 <span class="text-danger">*</span></label>
                            <div class="input-group">
                                <form:password path="oldPassword" class="form-control" id="oldPassword"
                                               placeholder="현재 비밀번호를 입력하세요"
                                               disabled="${todayChangeCount >= 3}" />
                                <button type="button" class="btn btn-outline-secondary"
                                        onclick="togglePassword('oldPassword')"
                                    ${todayChangeCount >= 3 ? 'disabled' : ''}>
                                    <i class="bi bi-eye" id="oldPasswordIcon"></i>
                                </button>
                            </div>
                            <form:errors path="oldPassword" class="text-danger" />
                        </div>

                        <div class="mb-3">
                            <label for="newPassword" class="form-label">새 비밀번호 <span class="text-danger">*</span></label>
                            <div class="input-group">
                                <form:password path="newPassword" class="form-control" id="newPassword"
                                               placeholder="새 비밀번호를 입력하세요"
                                               disabled="${todayChangeCount >= 3}" />
                                <button type="button" class="btn btn-outline-secondary"
                                        onclick="togglePassword('newPassword')"
                                    ${todayChangeCount >= 3 ? 'disabled' : ''}>
                                    <i class="bi bi-eye" id="newPasswordIcon"></i>
                                </button>
                            </div>
                            <div class="form-text">
                                8-20자, 영문 대/소문자, 숫자, 특수문자(@$!%*?&) 포함
                            </div>
                            <form:errors path="newPassword" class="text-danger" />
                        </div>

                        <div class="mb-3">
                            <label for="newPasswordConfirm" class="form-label">새 비밀번호 확인 <span class="text-danger">*</span></label>
                            <div class="input-group">
                                <form:password path="newPasswordConfirm" class="form-control" id="newPasswordConfirm"
                                               placeholder="새 비밀번호를 다시 입력하세요"
                                               disabled="${todayChangeCount >= 3}" />
                                <button type="button" class="btn btn-outline-secondary"
                                        onclick="togglePassword('newPasswordConfirm')"
                                    ${todayChangeCount >= 3 ? 'disabled' : ''}>
                                    <i class="bi bi-eye" id="newPasswordConfirmIcon"></i>
                                </button>
                            </div>
                            <form:errors path="newPasswordConfirm" class="text-danger" />
                        </div>

                        <div class="mb-3">
                            <label class="form-label">비밀번호 강도</label>
                            <div class="progress" style="height: 10px;">
                                <div class="progress-bar" role="progressbar" id="passwordStrength" style="width: 0%"></div>
                            </div>
                            <small class="form-text" id="passwordStrengthText">비밀번호를 입력하세요</small>
                        </div>

                        <div class="alert alert-warning">
                            <i class="bi bi-exclamation-triangle"></i>
                            <small>
                                <strong>보안 안내:</strong><br>
                                • 비밀번호 변경 후 자동으로 로그아웃됩니다.<br>
                                • 최근 사용한 5개 비밀번호는 재사용할 수 없습니다.<br>
                                • 90일마다 비밀번호를 변경해주세요.<br>
                                • 언제든 비밀번호 변경이 가능합니다 (일일 3회 제한).
                            </small>
                        </div>

                        <div class="d-grid gap-2">
                            <button type="submit" class="btn btn-primary"
                                ${todayChangeCount >= 3 ? 'disabled' : ''}
                                    id="submitButton">
                                <i class="bi bi-key"></i>
                                <c:choose>
                                    <c:when test="${todayChangeCount >= 3}">변경 불가 (일일 한도 초과)</c:when>
                                    <c:otherwise>비밀번호 변경</c:otherwise>
                                </c:choose>
                            </button>
                            <a href="/members/profile" class="btn btn-secondary">
                                <i class="bi bi-arrow-left"></i> 프로필로 돌아가기
                            </a>
                        </div>
                    </form:form>
                </div>
            </div>
        </div>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/js/bootstrap.bundle.min.js"></script>
<script>
    function togglePassword(inputId) {
        const input = document.getElementById(inputId);
        const icon = document.getElementById(inputId + 'Icon');

        if (input.disabled) return;

        if (input.type === 'password') {
            input.type = 'text';
            icon.classList.remove('bi-eye');
            icon.classList.add('bi-eye-slash');
        } else {
            input.type = 'password';
            icon.classList.remove('bi-eye-slash');
            icon.classList.add('bi-eye');
        }
    }

    document.getElementById('newPassword').addEventListener('input', function() {
        if (this.disabled) return;

        const password = this.value;
        const strengthBar = document.getElementById('passwordStrength');
        const strengthText = document.getElementById('passwordStrengthText');

        let strength = 0;
        let message = '';

        if (password.length >= 8) strength++;
        if (password.match(/[a-z]/)) strength++;
        if (password.match(/[A-Z]/)) strength++;
        if (password.match(/[0-9]/)) strength++;
        if (password.match(/[@$!%*?&]/)) strength++;

        switch(strength) {
            case 0:
            case 1:
                strengthBar.className = 'progress-bar bg-danger';
                strengthBar.style.width = '20%';
                message = '매우 약함';
                break;
            case 2:
                strengthBar.className = 'progress-bar bg-warning';
                strengthBar.style.width = '40%';
                message = '약함';
                break;
            case 3:
                strengthBar.className = 'progress-bar bg-info';
                strengthBar.style.width = '60%';
                message = '보통';
                break;
            case 4:
                strengthBar.className = 'progress-bar bg-success';
                strengthBar.style.width = '80%';
                message = '강함';
                break;
            case 5:
                strengthBar.className = 'progress-bar bg-success';
                strengthBar.style.width = '100%';
                message = '매우 강함';
                break;
        }

        strengthText.textContent = message;
    });

    document.getElementById('newPasswordConfirm').addEventListener('input', function() {
        if (this.disabled) return;

        const newPassword = document.getElementById('newPassword').value;
        const confirmPassword = this.value;

        if (confirmPassword && newPassword !== confirmPassword) {
            this.classList.add('is-invalid');
        } else {
            this.classList.remove('is-invalid');
        }
    });

    document.getElementById('passwordChangeForm').addEventListener('submit', function(e) {
        const todayChangeCount = ${todayChangeCount};
        if (todayChangeCount >= 3) {
            e.preventDefault();
            alert('오늘은 더 이상 비밀번호를 변경할 수 없습니다.');
            return false;
        }

        const newPassword = document.getElementById('newPassword').value;
        const confirmPassword = document.getElementById('newPasswordConfirm').value;

        if (newPassword !== confirmPassword) {
            e.preventDefault();
            alert('새 비밀번호와 확인 비밀번호가 일치하지 않습니다.');
            return false;
        }
    });
</script>
</body>
</html>