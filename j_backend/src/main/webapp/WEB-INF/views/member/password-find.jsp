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
    <title>비밀번호 찾기 - 통신판매사업자관리 시스템</title>
    <link
      href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css"
      rel="stylesheet"
    />
    <link
      href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.7.2/font/bootstrap-icons.css"
      rel="stylesheet"
    />

    <style>
      .password-find-container {
        background: white;
        border-radius: 15px;
        box-shadow: 0 15px 35px rgba(0, 0, 0, 0.1);
        padding: 3rem;
        text-align: center;
        max-width: 600px;
        margin: 2rem auto;
      }

      .password-find-icon {
        font-size: 4rem;
        color: #007bff;
        margin-bottom: 1rem;
      }

      .password-find-title {
        color: #2c3e50;
        font-size: 1.8rem;
        font-weight: 600;
        margin-bottom: 1rem;
      }

      .password-find-subtitle {
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

      .info-box {
        background-color: #f8f9fa;
        border: 1px solid #dee2e6;
        border-radius: 8px;
        padding: 20px;
        margin-bottom: 20px;
        text-align: left;
      }

      .info-box h6 {
        color: #495057;
        font-weight: 600;
        margin-bottom: 15px;
      }

      .info-box ul {
        margin-bottom: 0;
        padding-left: 20px;
      }

      .info-box li {
        color: #6c757d;
        font-size: 0.9rem;
        margin-bottom: 8px;
      }

      .step-indicator {
        display: flex;
        justify-content: center;
        align-items: center;
        margin-bottom: 30px;
      }

      .step {
        display: flex;
        align-items: center;
        margin: 0 10px;
      }

      .step-number {
        width: 30px;
        height: 30px;
        border-radius: 50%;
        background-color: #e9ecef;
        color: #6c757d;
        display: flex;
        align-items: center;
        justify-content: center;
        font-weight: bold;
        margin-right: 8px;
      }

      .step.active .step-number {
        background-color: #007bff;
        color: white;
      }

      .step.completed .step-number {
        background-color: #28a745;
        color: white;
      }

      .step-text {
        font-size: 0.9rem;
        color: #6c757d;
      }

      .step.active .step-text {
        color: #007bff;
        font-weight: 600;
      }

      .step.completed .step-text {
        color: #28a745;
        font-weight: 600;
      }

      .step-arrow {
        color: #dee2e6;
        margin: 0 5px;
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
        <h2><i class="bi bi-key"></i> 비밀번호 찾기</h2>
      </div>

      <div class="row justify-content-center">
        <div class="col-md-8 col-lg-6">
          <div class="password-find-container">
            <div class="password-find-icon">
              <i class="bi bi-search"></i>
            </div>
            <h1 class="password-find-title">비밀번호 찾기</h1>
            <p class="password-find-subtitle">
              가입 시 사용한 이메일로 비밀번호 재설정 링크를 보내드립니다
            </p>

            <div class="step-indicator">
              <div class="step active">
                <div class="step-number">1</div>
                <div class="step-text">정보 입력</div>
              </div>
              <i class="bi bi-arrow-right step-arrow"></i>
              <div class="step">
                <div class="step-number">2</div>
                <div class="step-text">이메일 확인</div>
              </div>
              <i class="bi bi-arrow-right step-arrow"></i>
              <div class="step">
                <div class="step-number">3</div>
                <div class="step-text">비밀번호 재설정</div>
              </div>
            </div>

            <c:if test="${not empty errorMessage}">
              <div class="alert alert-danger" role="alert">
                <i class="bi bi-exclamation-triangle me-2"></i>
                ${errorMessage}
              </div>
            </c:if>

            <c:if test="${not empty successMessage}">
              <div class="alert alert-success" role="alert">
                <i class="bi bi-check-circle me-2"></i>
                ${successMessage}
              </div>
            </c:if>

            <div class="info-box">
              <h6><i class="bi bi-info-circle me-2"></i>비밀번호 찾기 안내</h6>
              <ul>
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
                <label for="username" class="form-label">사용자명</label>
                <form:input
                  type="text"
                  class="form-control"
                  id="username"
                  path="username"
                  placeholder="사용자명을 입력하세요"
                  required="true"
                />
                <div class="form-text">
                  가입 시 사용한 사용자명을 입력해주세요
                </div>
                <form:errors path="username" cssClass="text-danger" />
              </div>

              <div class="mb-3">
                <label for="email" class="form-label">이메일 주소</label>
                <form:input
                  type="email"
                  class="form-control"
                  id="email"
                  path="email"
                  placeholder="이메일 주소를 입력하세요"
                  required="true"
                />
                <div class="form-text">
                  가입 시 사용한 이메일 주소를 입력해주세요
                </div>
                <form:errors path="email" cssClass="text-danger" />
              </div>

              <div class="d-grid gap-2">
                <button type="submit" class="btn btn-primary btn-lg">
                  <i class="bi bi-envelope me-2"></i>
                  비밀번호 재설정 링크 보내기
                </button>
              </div>
            </form:form>

            <div class="text-center mt-4">
              <a href="/members/login" class="text-decoration-none me-3">
                <i class="bi bi-arrow-left me-1"></i>
                로그인 페이지로 돌아가기
              </a>
              <a href="/members/join" class="text-decoration-none">
                <i class="bi bi-person-plus me-1"></i>
                회원가입
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
        const form = document.querySelector("form");
        const username = document.getElementById("username");
        const email = document.getElementById("email");

        form.addEventListener("submit", function (e) {
          if (username.value.trim() === "") {
            e.preventDefault();
            alert("사용자명을 입력해주세요.");
            username.focus();
            return false;
          }

          if (email.value.trim() === "") {
            e.preventDefault();
            alert("이메일 주소를 입력해주세요.");
            email.focus();
            return false;
          }

          const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
          if (!emailRegex.test(email.value)) {
            e.preventDefault();
            alert("올바른 이메일 형식을 입력해주세요.");
            email.focus();
            return false;
          }
        });

        username.addEventListener("input", function () {
          this.value = this.value.trim();
        });

        email.addEventListener("input", function () {
          this.value = this.value.trim().toLowerCase();
        });
      });
    </script>
  </body>
</html>
