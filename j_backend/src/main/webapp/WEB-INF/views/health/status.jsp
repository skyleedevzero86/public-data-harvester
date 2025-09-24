<%@ page language="java" contentType="text/html; charset=UTF-8"
pageEncoding="UTF-8"%> <%@ taglib prefix="c"
uri="http://java.sun.com/jsp/jstl/core" %> <%@ taglib prefix="fmt"
uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="ko">
  <head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>시스템 상태 - 통신판매자사업관리시스템</title>
    <link
      href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css"
      rel="stylesheet"
    />
    <link
      href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.7.2/font/bootstrap-icons.css"
      rel="stylesheet"
    />
    <style>
      .status-up {
        color: #28a745;
      }
      .status-down {
        color: #dc3545;
      }
      .status-unknown {
        color: #ffc107;
      }
      .status-card {
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
      .stats-icon {
        font-size: 2.5rem;
        opacity: 0.8;
      }
      .icon-align {
        display: flex;
        align-items: center;
        justify-content: center;
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
      <div class="d-flex justify-content-between align-items-center mb-4">
        <h2><i class="bi bi-heart-pulse"></i> 시스템 상태</h2>
        <div>
          <a href="/health/metrics" class="btn btn-info">
            <i class="bi bi-graph-up"></i> 상세 메트릭
          </a>
        </div>
      </div>

      <div class="row mb-4">
        <div class="col-xl-3 col-md-6 col-sm-6">
          <div class="card bg-primary text-white mb-4">
            <div class="card-body">
              <div class="d-flex justify-content-between align-items-center">
                <div>
                  <div class="text-white-50 small">총 컴포넌트</div>
                  <div class="fs-4 fw-bold">
                    ${systemHealth.totalComponents}
                  </div>
                </div>
                <div class="icon-align">
                  <i class="bi bi-gear-fill stats-icon"></i>
                </div>
              </div>
            </div>
          </div>
        </div>
        <div class="col-xl-3 col-md-6 col-sm-6">
          <div class="card bg-success text-white mb-4">
            <div class="card-body">
              <div class="d-flex justify-content-between align-items-center">
                <div>
                  <div class="text-white-50 small">정상 컴포넌트</div>
                  <div class="fs-4 fw-bold">
                    ${systemHealth.healthyComponents}
                  </div>
                </div>
                <div class="icon-align">
                  <i class="bi bi-check-circle-fill stats-icon"></i>
                </div>
              </div>
            </div>
          </div>
        </div>
        <div class="col-xl-3 col-md-6 col-sm-6">
          <div class="card bg-danger text-white mb-4">
            <div class="card-body">
              <div class="d-flex justify-content-between align-items-center">
                <div>
                  <div class="text-white-50 small">장애 컴포넌트</div>
                  <div class="fs-4 fw-bold">
                    ${systemHealth.unhealthyComponents}
                  </div>
                </div>
                <div class="icon-align">
                  <i class="bi bi-x-circle-fill stats-icon"></i>
                </div>
              </div>
            </div>
          </div>
        </div>
        <div class="col-xl-3 col-md-6 col-sm-6">
          <div class="card bg-info text-white mb-4">
            <div class="card-body">
              <div class="d-flex justify-content-between align-items-center">
                <div>
                  <div class="text-white-50 small">건강도</div>
                  <div class="fs-4 fw-bold">
                    <fmt:formatNumber
                      value="${systemHealth.healthPercentage}"
                      pattern="#.##"
                    />%
                  </div>
                </div>
                <div class="icon-align">
                  <i class="bi bi-heart-pulse-fill stats-icon"></i>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="row justify-content-center">
        <div class="col-md-8 col-lg-6">
          <div
            class="card status-card ${systemHealth.healthy ? 'border-success' : 'border-danger'}"
          >
            <div class="card-body text-center">
              <c:choose>
                <c:when test="${systemHealth.healthy}">
                  <i class="bi bi-check-circle-fill fa-5x status-up pulse"></i>
                  <h1 class="mt-3 status-up">시스템 정상</h1>
                  <p class="text-muted">
                    모든 서비스가 정상적으로 작동하고 있습니다.
                  </p>
                </c:when>
                <c:otherwise>
                  <i
                    class="bi bi-exclamation-triangle-fill fa-5x status-down pulse"
                  ></i>
                  <h1 class="mt-3 status-down">시스템 장애</h1>
                  <p class="text-muted">일부 서비스에 문제가 발생했습니다.</p>
                </c:otherwise>
              </c:choose>
              <div class="mt-4">
                <h3>${systemHealth.overallStatus.code}</h3>
                <p class="text-muted">
                  ${systemHealth.overallStatusDescription}
                </p>
              </div>
              <div class="mt-4">
                <div class="progress" style="height: 20px">
                  <div
                    class="progress-bar bg-${systemHealth.healthy ? 'success' : 'danger'}"
                    role="progressbar"
                    style="width: ${systemHealth.healthPercentage}%"
                  >
                    <fmt:formatNumber
                      value="${systemHealth.healthPercentage}"
                      pattern="#.##"
                    />%
                  </div>
                </div>
              </div>
              <div class="mt-4">
                <small class="text-muted"
                  >마지막 체크: ${systemHealth.checkedAt}</small
                >
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
      setInterval(function () {
        location.reload();
      }, 30000);

      document.addEventListener("DOMContentLoaded", function () {
        const card = document.querySelector(".status-card");
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
