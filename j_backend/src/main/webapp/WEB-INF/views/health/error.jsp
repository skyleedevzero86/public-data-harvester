<%@ page language="java" contentType="text/html; charset=UTF-8"
pageEncoding="UTF-8"%> <%@ taglib prefix="c"
uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko">
  <head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>오류 발생 - 통신판매자사업관리시스템</title>
    <link
      href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css"
      rel="stylesheet"
    />
    <link
      href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.7.2/font/bootstrap-icons.css"
      rel="stylesheet"
    />
    <style>
      .error-card {
        min-height: 300px;
        display: flex;
        align-items: center;
        justify-content: center;
        flex-direction: column;
      }
      .pulse {
        animation: pulse 2s infinite;
      }
      @keyframes pulse {
        0% {
          transform: scale(1);
        }
        50% {
          transform: scale(1.05);
        }
        100% {
          transform: scale(1);
        }
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
          <c:if test="${member.role == 'ADMIN' || member.role == 'MANAGER'}">
            <a class="nav-link" href="/members/admin/pending">
              <i class="bi bi-clock"></i> 승인 대기
            </a>
          </c:if>
          <a class="nav-link" href="/web/files">
            <i class="bi bi-clock"></i> 파일 관리
          </a>
          <a class="nav-link" href="/members/logout">
            <i class="bi bi-box-arrow-right"></i> 로그아웃
          </a>
        </div>
      </div>
    </nav>

    <div class="container-fluid mt-4">
      <div class="row justify-content-center">
        <div class="col-md-8 col-lg-6">
          <div class="card error-card border-danger">
            <div class="card-body text-center">
              <i
                class="bi bi-exclamation-triangle-fill"
                style="font-size: 5rem"
                class="text-danger pulse"
              ></i>
              <h1 class="mt-3 text-danger">오류 발생</h1>
              <p class="text-muted">요청을 처리하는 중 오류가 발생했습니다.</p>
              <c:if test="${not empty error}">
                <div class="alert alert-danger mt-4">
                  <i class="bi bi-bug"></i> ${error}
                </div>
              </c:if>
              <div class="mt-4">
                <a href="/health" class="btn btn-primary">
                  <i class="bi bi-house"></i> 대시보드로 돌아가기
                </a>
                <button
                  onclick="history.back()"
                  class="btn btn-outline-secondary"
                >
                  <i class="bi bi-arrow-left"></i> 이전 페이지
                </button>
              </div>
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
        const card = document.querySelector(".error-card");
        card.style.opacity = "0";
        card.style.transform = "scale(0.8)";
        card.style.transition = "all 0.5s ease";
        setTimeout(() => {
          card.style.opacity = "1";
          card.style.transform = "scale(1)";
        }, 100);
      });
    </script>
  </body>
</html>
