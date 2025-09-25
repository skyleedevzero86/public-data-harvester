<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8"%> <%@ taglib prefix="c"
                                           uri="http://java.sun.com/jsp/jstl/core" %> <%@ taglib prefix="form"
                                                                                                 uri="http://www.springframework.org/tags/form" %>

<c:set var="pageTitle" value="비밀번호 재설정" />
<c:set var="pageCSS" value="${['member.css']}" />
<c:set var="pageJS" value="${['member.js']}" />

<!DOCTYPE html>
<html>
<head>
  <%@ include file="../common/head.jsp" %>
  <meta name="_csrf" content="${_csrf.token}" />
  <meta name="_csrf_header" content="${_csrf.headerName}" />
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

<div class="container mt-5">
  <div class="row justify-content-center">
    <div class="col-md-6">
      <div class="reset-container">
        <div class="text-center mb-4">
          <i
                  class="bi bi-key-fill text-primary"
                  style="font-size: 3rem"
          ></i>
          <h2 class="mt-3">비밀번호 재설정</h2>
          <p class="text-muted">새로운 비밀번호를 입력해 주세요.</p>
        </div>

        <c:if test="${not empty error}">
          <div
                  class="alert alert-danger alert-dismissible fade show"
                  role="alert"
          >
            <i class="bi bi-exclamation-triangle"></i> ${error}
            <button
                    type="button"
                    class="btn-close"
                    data-bs-dismiss="alert"
            ></button>
          </div>
        </c:if>

        <form:form
                modelAttribute="passwordResetRequest"
                method="post"
                action="/members/password/reset"
        >
          <input type="hidden" name="token" value="${token}" />

          <div class="mb-3">
            <label for="newPassword" class="form-label"
            >새 비밀번호 <span class="text-danger">*</span></label
            >
            <div class="input-group">
                  <span class="input-group-text">
                    <i class="bi bi-lock"></i>
                  </span>
              <form:password
                      path="newPassword"
                      class="form-control"
                      id="newPassword"
                      placeholder="8자 이상, 대소문자, 숫자, 특수문자 포함"
              />
              <button
                      class="btn btn-outline-secondary"
                      type="button"
                      id="togglePassword"
              >
                <i class="bi bi-eye"></i>
              </button>
            </div>
            <form:errors path="newPassword" class="text-danger small" />
            <div class="password-strength mt-2">
              <div class="progress" style="height: 5px">
                <div
                        class="progress-bar"
                        id="passwordStrengthBar"
                        style="width: 0%"
                ></div>
              </div>
              <small class="text-muted" id="passwordStrengthText"
              >비밀번호 강도: 약함</small
              >
            </div>
          </div>

          <div class="mb-3">
            <label for="confirmPassword" class="form-label"
            >비밀번호 확인 <span class="text-danger">*</span></label
            >
            <div class="input-group">
                  <span class="input-group-text">
                    <i class="bi bi-lock-fill"></i>
                  </span>
              <form:password
                      path="confirmPassword"
                      class="form-control"
                      id="confirmPassword"
                      placeholder="비밀번호를 다시 입력하세요"
              />
              <button
                      class="btn btn-outline-secondary"
                      type="button"
                      id="toggleConfirmPassword"
              >
                <i class="bi bi-eye"></i>
              </button>
            </div>
            <form:errors path="confirmPassword" class="text-danger small" />
            <div class="password-match mt-2">
              <small class="text-muted" id="passwordMatchText"></small>
            </div>
          </div>

          <div class="mb-4">
            <div class="form-text">
              <i class="bi bi-info-circle"></i> 비밀번호는 다음 조건을
              만족해야 합니다:
              <ul class="mt-2">
                <li>8자 이상</li>
                <li>대문자와 소문자 포함</li>
                <li>숫자 포함</li>
                <li>특수문자 포함</li>
              </ul>
            </div>
          </div>

          <div class="d-grid">
            <button
                    type="submit"
                    class="btn btn-primary"
                    id="resetButton"
                    disabled
            >
              <i class="bi bi-check-circle"></i> 비밀번호 재설정
            </button>
          </div>

          <div class="text-center mt-3">
            <a href="/members/login" class="text-decoration-none">
              <i class="bi bi-arrow-left"></i> 로그인 페이지로 돌아가기
            </a>
          </div>
        </form:form>
      </div>
    </div>
  </div>
</div>

<%@ include file="../common/footer.jsp" %> <%@ include
        file="../common/scripts.jsp" %>
<script>
  document
          .getElementById("togglePassword")
          .addEventListener("click", function () {
            const passwordInput = document.getElementById("newPassword");
            const icon = this.querySelector("i");

            if (passwordInput.type === "password") {
              passwordInput.type = "text";
              icon.classList.remove("bi-eye");
              icon.classList.add("bi-eye-slash");
            } else {
              passwordInput.type = "password";
              icon.classList.remove("bi-eye-slash");
              icon.classList.add("bi-eye");
            }
          });

  document
          .getElementById("toggleConfirmPassword")
          .addEventListener("click", function () {
            const passwordInput = document.getElementById("confirmPassword");
            const icon = this.querySelector("i");

            if (passwordInput.type === "password") {
              passwordInput.type = "text";
              icon.classList.remove("bi-eye");
              icon.classList.add("bi-eye-slash");
            } else {
              passwordInput.type = "password";
              icon.classList.remove("bi-eye-slash");
              icon.classList.add("bi-eye");
            }
          });

  document
          .getElementById("newPassword")
          .addEventListener("input", function () {
            const password = this.value;
            const strength = checkPasswordStrength(password);
            updatePasswordStrength(strength);
            checkPasswordMatch();
          });

  document
          .getElementById("confirmPassword")
          .addEventListener("input", function () {
            checkPasswordMatch();
          });

  function checkPasswordStrength(password) {
    let score = 0;

    if (password.length >= 8) score++;
    if (/[a-z]/.test(password)) score++;
    if (/[A-Z]/.test(password)) score++;
    if (/[0-9]/.test(password)) score++;
    if (/[^a-zA-Z0-9]/.test(password)) score++;

    return {
      score: score,
      percentage: (score / 5) * 100,
      text: score < 2 ? "약함" : score < 4 ? "보통" : "강함",
      color: score < 2 ? "danger" : score < 4 ? "warning" : "success",
    };
  }

  function updatePasswordStrength(strength) {
    const progressBar = document.getElementById("passwordStrengthBar");
    const strengthText = document.getElementById("passwordStrengthText");

    progressBar.style.width = strength.percentage + "%";
    progressBar.className = `progress-bar bg-${strength.color}`;
    strengthText.textContent = `비밀번호 강도: ${strength.text}`;
    strengthText.className = `text-${strength.color}`;
  }

  function checkPasswordMatch() {
    const password = document.getElementById("newPassword").value;
    const confirmPassword =
            document.getElementById("confirmPassword").value;
    const matchText = document.getElementById("passwordMatchText");
    const resetButton = document.getElementById("resetButton");

    if (confirmPassword === "") {
      matchText.textContent = "";
      resetButton.disabled = true;
      return;
    }

    if (password === confirmPassword) {
      matchText.textContent = "비밀번호가 일치합니다.";
      matchText.className = "text-success";
      resetButton.disabled = false;
    } else {
      matchText.textContent = "비밀번호가 일치하지 않습니다.";
      matchText.className = "text-danger";
      resetButton.disabled = true;
    }
  }
</script>
</body>
</html>