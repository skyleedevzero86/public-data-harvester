<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="_csrf" content="${_csrf.token}"/>
    <meta name="_csrf_header" content="${_csrf.headerName}"/>
    <title>비밀번호 변경 - 통신판매자사업관리시스템</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.7.2/font/bootstrap-icons.css" rel="stylesheet">

    <style>
        .dropdown-menu {
            min-width: 200px !important;
            padding: 10px 0;
        }

        .dropdown-menu .dropdown-item {
            padding: 10px 20px;
            font-size: 1rem;
        }

        .dropdown-menu .dropdown-header {
            font-size: 1.05rem;
            font-weight: bold;
            padding: 10px 20px 5px 20px;
        }

        .custom-dropdown .dropdown-menu {
            min-width: 220px !important;
        }

        .table th, .table td {
            vertical-align: middle;
            text-align: center;
        }

        .table th:first-child, .table td:first-child {
            text-align: center;
        }

        .table th:nth-child(2), .table td:nth-child(2) {
            text-align: left;
        }

        .table th:nth-child(3), .table td:nth-child(3) {
            text-align: left;
        }

        .table th:nth-child(4), .table td:nth-child(4) {
            text-align: left;
        }

        .table th:last-child, .table td:last-child {
            min-width: 50px;
            text-align: center;
        }

        .table th:nth-last-child(2), .table td:nth-last-child(2) {
            min-width: 250px;
            text-align: center;
        }

        .table-responsive {
            overflow: visible !important;
        }

        .btn-group-sm .btn {
            margin-right: 2px;
        }

        .locked-indicator {
            color: #dc3545;
            font-weight: bold;
        }

        .login-fail-count {
            color: #dc3545;
            font-weight: bold;
            font-size: 0.9em;
        }

        .badge {
            font-size: 0.8em;
        }

        .filter-container {
            background: #f8f9fa;
            border: 1px solid #dee2e6;
            border-radius: 8px;
            padding: 15px;
            margin-bottom: 20px;
        }

        .filter-active {
            border-color: #007bff !important;
            box-shadow: 0 0 0 0.2rem rgba(0, 123, 255, 0.25) !important;
        }

        .status-cell {
            white-space: nowrap;
        }

        .btn-group .dropdown-menu {
            border: 1px solid #dee2e6;
            box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
        }

        .page-link:hover {
            background-color: #e9ecef;
        }

        .icon-align {
            display: flex;
            align-items: center;
            justify-content: center;
        }

        .stats-icon {
            font-size: 2.5rem;
            opacity: 0.8;
        }

        .footer {
            background-color: #343a40;
            color: white;
            padding: 40px 0 20px 0;
            margin-top: 60px;
        }

        .footer-logo {
            margin-bottom: 30px;
        }

        .footer-logo .festival-number {
            font-size: 0.9rem;
            color: #adb5bd;
            margin-bottom: 5px;
            position: relative;
        }

        .footer-contact {
            margin-bottom: 25px;
        }

        .footer-contact .contact-title {
            font-size: 1.1rem;
            font-weight: bold;
            margin-bottom: 8px;
            color: #f8f9fa;
        }

        .footer-contact .contact-address {
            font-size: 0.9rem;
            color: #adb5bd;
            margin-bottom: 5px;
            line-height: 1.4;
        }

        .footer-contact .contact-phone {
            font-size: 0.9rem;
            color: #adb5bd;
        }

        .footer-contact .contact-email {
            font-size: 0.9rem;
            color: #adb5bd;
            margin-top: 5px;
        }

        .footer-copyright {
            border-top: 1px solid #495057;
            padding-top: 20px;
            text-align: left;
            font-size: 0.8rem;
            color: #adb5bd;
        }

        .footer-container {
            max-width: 1200px;
            margin: 0 auto;
            padding: 0 20px;
        }
    </style>
</head>
<body>
<nav class="navbar navbar-expand-lg navbar-dark bg-dark">
    <div class="container">
        <a class="navbar-brand" href="/">
            <i class="bi bi-shield-check"></i> 통신판매사업자관리 시스템
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

<div class="container-fluid mt-4">
    <div class="d-flex justify-content-between align-items-center mb-4">
        <h2><i class="bi bi-key"></i> 비밀번호 변경</h2>
    </div>

    <div class="row justify-content-center">
        <div class="col-md-6">
            <div class="card">
                <div class="card-header">
                    <div class="d-flex justify-content-between align-items-center">
                        <h5 class="mb-0"><i class="bi bi-table"></i> 비밀번호 변경</h5>
                    </div>
                </div>
                <div class="card-body">
                    <c:if test="${param.debug == 'true'}">
                        <div class="alert alert-info alert-dismissible fade show" role="alert">
                            <i class="bi bi-info-circle"></i>
                            <strong>디버그 정보:</strong><br>
                            - isPasswordChangeRequired: ${isPasswordChangeRequired}<br>
                            - isPasswordChangeRecommended: ${isPasswordChangeRecommended}<br>
                            - todayChangeCount: ${todayChangeCount}<br>
                            - passwordChangeRequest: ${passwordChangeRequest != null ? 'exists' : 'null'}
                            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
                        </div>
                    </c:if>

                    <c:if test="${isPasswordChangeRequired}">
                        <div class="alert alert-danger alert-dismissible fade show" role="alert">
                            <i class="bi bi-exclamation-triangle"></i>
                            <strong>비밀번호 변경 필요</strong><br>
                            90일이 지난 비밀번호입니다. 보안을 위해 새 비밀번호로 변경해주세요.
                            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
                        </div>
                    </c:if>

                    <c:if test="${isPasswordChangeRecommended && !isPasswordChangeRequired}">
                        <div class="alert alert-warning alert-dismissible fade show" role="alert">
                            <i class="bi bi-info-circle"></i>
                            <strong>비밀번호 변경 권장</strong><br>
                            80일이 지난 비밀번호입니다. 보안을 위해 새 비밀번호로 변경을 권장합니다.
                            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
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

                    <div class="alert ${todayChangeCount >= 3 ? 'alert-danger' : 'alert-info'} alert-dismissible fade show" role="alert">
                        <i class="bi bi-info-circle"></i>
                        <small>
                            오늘 비밀번호 변경 횟수: <strong>${todayChangeCount}/3</strong>
                            <br>하루 최대 3회까지 변경 가능합니다.
                            <c:if test="${todayChangeCount >= 3}">
                                <br><strong>⚠️ 오늘은 더 이상 변경할 수 없습니다.</strong>
                            </c:if>
                        </small>
                        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
                    </div>

                    <form:form modelAttribute="passwordChangeRequest" method="post" id="passwordChangeForm" novalidate="true">
                        <div class="mb-3">
                            <label for="oldPassword" class="form-label">현재 비밀번호 <span class="text-danger">*</span></label>
                            <div class="input-group">
                                <form:password path="oldPassword" class="form-control ${not empty bindingResult && bindingResult.hasFieldErrors('oldPassword') ? 'is-invalid' : ''}"
                                               id="oldPassword" placeholder="현재 비밀번호를 입력하세요"
                                               disabled="${todayChangeCount >= 3}" />
                                <button type="button" class="btn btn-outline-secondary"
                                        onclick="togglePassword('oldPassword')"
                                    ${todayChangeCount >= 3 ? 'disabled' : ''}>
                                    <i class="bi bi-eye" id="oldPasswordIcon"></i>
                                </button>
                            </div>
                            <form:errors path="oldPassword" class="invalid-feedback d-block" />
                        </div>

                        <div class="mb-3">
                            <label for="newPassword" class="form-label">새 비밀번호 <span class="text-danger">*</span></label>
                            <div class="input-group">
                                <form:password path="newPassword" class="form-control ${not empty bindingResult && bindingResult.hasFieldErrors('newPassword') ? 'is-invalid' : ''}"
                                               id="newPassword" placeholder="새 비밀번호를 입력하세요"
                                               disabled="${todayChangeCount >= 3}" />
                                <button type="button" class="btn btn-outline-secondary"
                                        onclick="togglePassword('newPassword')"
                                    ${todayChangeCount >= 3 ? 'disabled' : ''}>
                                    <i class="bi bi-eye" id="newPasswordIcon"></i>
                                </button>
                            </div>
                            <div class="form-text">
                                8-20자, 영문 대/소문자, 숫자, 특수문자 포함 필요
                            </div>
                            <form:errors path="newPassword" class="invalid-feedback d-block" />
                        </div>

                        <div class="mb-3">
                            <label for="newPasswordConfirm" class="form-label">새 비밀번호 확인 <span class="text-danger">*</span></label>
                            <div class="input-group">
                                <form:password path="newPasswordConfirm" class="form-control ${not empty bindingResult && bindingResult.hasFieldErrors('newPasswordConfirm') ? 'is-invalid' : ''}"
                                               id="newPasswordConfirm" placeholder="새 비밀번호를 다시 입력하세요"
                                               disabled="${todayChangeCount >= 3}" />
                                <button type="button" class="btn btn-outline-secondary"
                                        onclick="togglePassword('newPasswordConfirm')"
                                    ${todayChangeCount >= 3 ? 'disabled' : ''}>
                                    <i class="bi bi-eye" id="newPasswordConfirmIcon"></i>
                                </button>
                            </div>
                            <form:errors path="newPasswordConfirm" class="invalid-feedback d-block" />
                        </div>

                        <div class="mb-3">
                            <label class="form-label">비밀번호 강도</label>
                            <div class="progress" style="height: 10px;">
                                <div class="progress-bar" role="progressbar" id="passwordStrength" style="width: 0%"></div>
                            </div>
                            <small class="form-text" id="passwordStrengthText">비밀번호를 입력하세요</small>
                        </div>

                        <div class="alert alert-info alert-dismissible fade show" role="alert">
                            <i class="bi bi-info-circle"></i>
                            <small>
                                <strong>비밀번호 정책:</strong><br>
                                • 길이: 8자 이상 20자 이하<br>
                                • 포함 필수: 영문 대문자, 소문자, 숫자, 특수문자<br>
                                • 사용 가능한 특수문자: !@#$%^&*(),.?":{}<>_+-=[]\;'`~<br>
                                • 최근 5개 비밀번호는 재사용 불가<br>
                                • 하루 최대 3회까지 변경 가능
                            </small>
                            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
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

<footer class="footer">
    <div class="footer-container">
        <div class="row">
            <div class="col-md-6">
                <div class="footer-logo">
                    <div class="festival-number"></div>
                    <div class="main-title">public-data-harvester</div>
                    <div class="sub-title">CHUNGJANG STREET FESTIVAL OF RECOLLECTION</div>
                </div>

                <div class="footer-contact">
                    <div class="contact-title">통신판매사업자 정보 관리시스템</div>
                    <div class="contact-address">대한민국 광주광역시 서구</div>
                    <div class="contact-phone">TEL: 010-xxx-ㄱㄴㄷㄹ</div>
                </div>
            </div>

            <div class="col-md-6">
                <div class="footer-contact">
                    <div class="contact-title">궁금하면 500원</div>
                    <div class="contact-address">대한민국 광주광역시 서구</div>
                    <div class="contact-phone">TEL: 010-xxx-ㄱㄴㄷㄹ</div>
                    <div class="contact-email">E-MAIL: 2025chungjang@gmail.com</div>
                </div>
            </div>
        </div>

        <div class="footer-copyright">
            ⓒ public-data-harvester. ALL RIGHT RESERVED.
        </div>
    </div>
</footer>

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
        let requirements = [];

        if (password.length >= 8) {
            strength++;
        } else {
            requirements.push('8자 이상');
        }

        if (password.match(/[a-z]/)) {
            strength++;
        } else {
            requirements.push('영문 소문자');
        }

        if (password.match(/[A-Z]/)) {
            strength++;
        } else {
            requirements.push('영문 대문자');
        }

        if (password.match(/[0-9]/)) {
            strength++;
        } else {
            requirements.push('숫자');
        }

        if (password.match(/[!@#$%^&*(),.?":{}|<>_+=\[\]\\;'`~-]/)) {
            strength++;
        } else {
            requirements.push('특수문자');
        }

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

        if (requirements.length > 0) {
            message += ' (필요: ' + requirements.join(', ') + ')';
        }

        strengthText.textContent = message;
        strengthText.className = strength === 5 ? 'form-text text-success' : 'form-text text-warning';
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
        const todayChangeCountValue = ${todayChangeCount};
        if (todayChangeCountValue >= 3) {
            e.preventDefault();
            alert('오늘은 더 이상 비밀번호를 변경할 수 없습니다. (일일 3회 제한)');
            return false;
        }

        const newPassword = document.getElementById('newPassword').value;
        const confirmPassword = document.getElementById('newPasswordConfirm').value;

        if (newPassword !== confirmPassword) {
            e.preventDefault();
            alert('새 비밀번호와 확인 비밀번호가 일치하지 않습니다.');
            return false;
        }

        const passwordPattern = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[!@#$%^&*(),.?":{}|<>_+=\[\]\\;'`~-])[A-Za-z\d!@#$%^&*(),.?":{}|<>_+=\[\]\\;'`~-]{8,20}$/;
        if (!passwordPattern.test(newPassword)) {
            e.preventDefault();
            alert('비밀번호는 8-20자이며, 영문 대/소문자, 숫자, 특수문자를 포함해야 합니다.');
            return false;
        }

        const submitButton = document.getElementById('submitButton');
        submitButton.disabled = true;
        submitButton.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>변경 중...';
    });
</script>
</body>
</html>