<%@ page language="java" contentType="text/html; charset=UTF-8"
pageEncoding="UTF-8"%> <%@ taglib prefix="c"
uri="http://java.sun.com/jsp/jstl/core" %> <%@ taglib prefix="form"
uri="http://www.springframework.org/tags/form" %>
<!DOCTYPE html>
<html>
  <head>
    <meta charset="UTF-8" />
    <meta name="_csrf" content="${_csrf.token}" />
    <meta name="_csrf_header" content="${_csrf.headerName}" />
    <title>비밀번호 재설정 - 통신판매사업자관리 시스템</title>
    <link
      href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css"
      rel="stylesheet"
    />
    <link
      href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.7.2/font/bootstrap-icons.css"
      rel="stylesheet"
    />

    <style>
      .password-reset-container {
        background: white;
        border-radius: 15px;
        box-shadow: 0 15px 35px rgba(0, 0, 0, 0.1);
        padding: 3rem;
        text-align: center;
        max-width: 600px;
        margin: 2rem auto;
      }

      .password-reset-icon {
        font-size: 4rem;
        color: #007bff;
        margin-bottom: 1rem;
      }

      .password-reset-title {
        color: #2c3e50;
        font-size: 1.8rem;
        font-weight: 600;
        margin-bottom: 1rem;
      }

      .password-reset-subtitle {
        color: #7f8c8d;
        font-size: 1.1rem;
        margin-bottom: 2rem;
        line-height: 1.6;
      }

      .form-control {
        border-radius: 10px;
        border: 2px solid #e9ecef;
        padding: 12px 15px;
      }

      .form-control:focus {
        border-color: #007bff;
        box-shadow: 0 0 0 0.2rem rgba(0, 123, 255, 0.25);
      }

      .form-label {
        font-weight: 600;
        color: #495057;
        margin-bottom: 8px;
      }

      .form-text {
        font-size: 0.875rem;
        color: #6c757d;
        margin-top: 5px;
      }

      .btn-primary {
        background: linear-gradient(135deg, #007bff 0%, #0056b3 100%);
        border: none;
        padding: 12px 30px;
        border-radius: 25px;
        font-weight: 500;
        transition: all 0.3s ease;
      }

      .btn-primary:hover {
        transform: translateY(-2px);
        box-shadow: 0 5px 15px rgba(0, 123, 255, 0.4);
      }

      .btn-outline-secondary {
        border-radius: 25px;
        padding: 12px 30px;
        font-weight: 500;
        transition: all 0.3s ease;
      }

      .btn-outline-secondary:hover {
        transform: translateY(-2px);
      }

      .alert {
        border-radius: 10px;
        border: none;
        padding: 15px 20px;
        margin-bottom: 20px;
      }

      .alert-danger {
        background-color: #f8d7da;
        color: #721c24;
        border-left: 4px solid #dc3545;
      }

      .alert-success {
        background-color: #d1edff;
        color: #0c5460;
        border-left: 4px solid #17a2b8;
      }

      .text-danger {
        font-size: 0.875rem;
        margin-top: 5px;
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

      .main-title {
        font-size: 1.5rem;
        font-weight: bold;
        color: #f8f9fa;
        margin-bottom: 5px;
      }

      .sub-title {
        font-size: 0.9rem;
        color: #adb5bd;
      }

      .password-requirements {
        background-color: #f8f9fa;
        border: 1px solid #dee2e6;
        border-radius: 8px;
        padding: 15px;
        margin-bottom: 20px;
        text-align: left;
      }

      .password-requirements h6 {
        color: #495057;
        font-weight: 600;
        margin-bottom: 10px;
      }

      .password-requirements ul {
        margin-bottom: 0;
        padding-left: 20px;
      }

      .password-requirements li {
        color: #6c757d;
        font-size: 0.9rem;
        margin-bottom: 5px;
      }

      .password-match-indicator {
        font-size: 0.875rem;
        margin-top: 5px;
      }

      .password-match-indicator.match {
        color: #28a745;
      }

      .password-match-indicator.no-match {
        color: #dc3545;
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
          <a class="nav-link" href="/members/login">
            <i class="bi bi-box-arrow-in-right"></i> 로그인
          </a>
          <a class="nav-link" href="/members/join">
            <i class="bi bi-person-plus"></i> 회원가입
          </a>
        </div>
      </div>
    </nav>

    <div class="container-fluid mt-4">
      <div class="d-flex justify-content-between align-items-center mb-4">
        <h2><i class="bi bi-key"></i> 비밀번호 재설정</h2>
      </div>

      <div class="row justify-content-center">
        <div class="col-md-8 col-lg-6">
          <div class="password-reset-container">
            <div class="password-reset-icon">
              <i class="bi bi-shield-lock-fill"></i>
            </div>
            <h1 class="password-reset-title">비밀번호 재설정</h1>
            <p class="password-reset-subtitle">
              새로운 비밀번호를 입력해주세요
            </p>

            <c:if test="${not empty error}">
              <div class="alert alert-danger" role="alert">
                <i class="bi bi-exclamation-triangle me-2"></i>
                ${error}
              </div>
            </c:if>

            <c:if test="${not empty success}">
              <div class="alert alert-success" role="alert">
                <i class="bi bi-check-circle me-2"></i>
                ${success}
              </div>
            </c:if>

            <div class="password-requirements">
              <h6><i class="bi bi-info-circle me-2"></i>비밀번호 요구사항</h6>
              <ul>
                <li>영문 대소문자를 포함해야 합니다</li>
                <li>숫자를 포함해야 합니다</li>
                <li>특수문자를 포함해야 합니다</li>
                <li>8자 이상 20자 이하여야 합니다</li>
              </ul>
            </div>

            <form:form
              method="post"
              action="/password/reset"
              modelAttribute="passwordResetRequest"
            >
              <input type="hidden" name="token" value="${token}" />

              <div class="mb-3">
                <label for="newPassword" class="form-label">새 비밀번호</label>
                <form:input
                  type="password"
                  class="form-control"
                  id="newPassword"
                  path="newPassword"
                  placeholder="새 비밀번호를 입력하세요"
                  required="true"
                />
                <div class="form-text">
                  영문 대소문자, 숫자, 특수문자를 포함한 8~20자
                </div>
                <form:errors path="newPassword" cssClass="text-danger" />
              </div>

              <div class="mb-3">
                <label for="newPasswordConfirm" class="form-label"
                  >비밀번호 확인</label
                >
                <form:input
                  type="password"
                  class="form-control"
                  id="newPasswordConfirm"
                  path="newPasswordConfirm"
                  placeholder="비밀번호를 다시 입력하세요"
                  required="true"
                />
                <div
                  id="passwordMatchIndicator"
                  class="password-match-indicator"
                ></div>
                <form:errors path="newPasswordConfirm" cssClass="text-danger" />
              </div>

              <div class="d-grid gap-2">
                <button type="submit" class="btn btn-primary btn-lg">
                  <i class="bi bi-check-circle me-2"></i>
                  비밀번호 재설정
                </button>
              </div>
            </form:form>

            <div class="text-center mt-4">
              <a href="/members/login" class="text-decoration-none">
                <i class="bi bi-arrow-left me-1"></i>
                로그인 페이지로 돌아가기
              </a>
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
              <div class="sub-title">
                CHUNGJANG STREET FESTIVAL OF RECOLLECTION
              </div>
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
      document.addEventListener("DOMContentLoaded", function () {
        const newPassword = document.getElementById("newPassword");
        const confirmPassword = document.getElementById("newPasswordConfirm");
        const matchIndicator = document.getElementById(
          "passwordMatchIndicator"
        );

        function checkPasswordMatch() {
          const password = newPassword.value;
          const confirm = confirmPassword.value;

          if (confirm === "") {
            matchIndicator.textContent = "";
            matchIndicator.className = "password-match-indicator";
            return;
          }

          if (password === confirm) {
            matchIndicator.textContent = "✓ 비밀번호가 일치합니다";
            matchIndicator.className = "password-match-indicator match";
          } else {
            matchIndicator.textContent = "✗ 비밀번호가 일치하지 않습니다";
            matchIndicator.className = "password-match-indicator no-match";
          }
        }

        function validatePassword() {
          const password = newPassword.value;
          const confirm = confirmPassword.value;

          if (password !== confirm) {
            confirmPassword.setCustomValidity("비밀번호가 일치하지 않습니다.");
          } else {
            confirmPassword.setCustomValidity("");
          }
        }

        newPassword.addEventListener("input", function () {
          checkPasswordMatch();
          validatePassword();
        });

        confirmPassword.addEventListener("input", function () {
          checkPasswordMatch();
          validatePassword();
        });

        const form = document.querySelector("form");
        form.addEventListener("submit", function (e) {
          const password = newPassword.value;
          const confirm = confirmPassword.value;

          if (password !== confirm) {
            e.preventDefault();
            alert("비밀번호와 비밀번호 확인이 일치하지 않습니다.");
            return false;
          }

          if (password.length < 8 || password.length > 20) {
            e.preventDefault();
            alert("비밀번호는 8자 이상 20자 이하여야 합니다.");
            return false;
          }

          const hasUpperCase = /[A-Z]/.test(password);
          const hasLowerCase = /[a-z]/.test(password);
          const hasNumbers = /\d/.test(password);
          const hasSpecialChar = /[!@#$%^&*(),.?":{}|<>_+=\-\[\]\\;'`~]/.test(
            password
          );

          if (
            !hasUpperCase ||
            !hasLowerCase ||
            !hasNumbers ||
            !hasSpecialChar
          ) {
            e.preventDefault();
            alert(
              "비밀번호는 영문 대소문자, 숫자, 특수문자를 모두 포함해야 합니다."
            );
            return false;
          }
        });
      });
    </script>
  </body>
</html>
