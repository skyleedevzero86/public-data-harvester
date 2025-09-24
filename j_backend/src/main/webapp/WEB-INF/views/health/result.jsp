<%@ page language="java" contentType="text/html; charset=UTF-8"
pageEncoding="UTF-8"%> <%@ taglib prefix="c"
uri="http://java.sun.com/jsp/jstl/core" %> <%@ taglib prefix="fmt"
uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html>
  <head>
    <meta charset="UTF-8" />
    <title>헬스 체크 결과 - 통신판매자사업관리시스템</title>
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
      .result-card {
        transition: transform 0.2s;
      }
      .result-card:hover {
        transform: translateY(-2px);
      }
      .table th,
      .table td {
        vertical-align: middle;
        text-align: center;
      }
      .table th:first-child,
      .table td:first-child {
        text-align: center;
      }
      .table th:nth-child(2),
      .table td:nth-child(2) {
        text-align: left;
      }
      .table th:nth-child(3),
      .table td:nth-child(3) {
        text-align: left;
      }
      .table th:nth-child(4),
      .table td:nth-child(4) {
        text-align: left;
      }
      .table th:last-child,
      .table td:last-child {
        min-width: 50px;
        text-align: center;
      }
      .table th:nth-last-child(2),
      .table td:nth-last-child(2) {
        min-width: 250px;
        text-align: center;
      }
      .table-responsive {
        overflow: visible !important;
      }
      .btn-group-sm .btn {
        margin-right: 2px;
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
          <a class="nav-link" href="/health">
            <i class="bi bi-speedometer2"></i> 헬스 대시보드
          </a>
          <a class="nav-link" href="/health/status">
            <i class="bi bi-heart-pulse"></i> 상태 조회
          </a>
          <a class="nav-link" href="/health/metrics">
            <i class="bi bi-graph-up"></i> 메트릭
          </a>
          <a class="nav-link" href="/health/metrics/realtime">
            <i class="bi bi-lightning-charge"></i> 실시간
          </a>
          <a class="nav-link" href="/health/history">
            <i class="bi bi-clock-history"></i> 이력 조회
          </a>
        </div>
      </div>
    </nav>

    <div class="container-fluid mt-4">
      <div class="d-flex justify-content-between align-items-center mb-4">
        <h2><i class="bi bi-clipboard-check"></i> 헬스 체크 결과</h2>
        <div>
          <a href="/health" class="btn btn-outline-secondary me-2">
            <i class="bi bi-arrow-left"></i> 대시보드
          </a>
          <a href="/health/check" class="btn btn-primary">
            <i class="bi bi-arrow-clockwise"></i> 다시 체크
          </a>
        </div>
      </div>
      <c:if test="${not empty message}">
        <div class="row mb-4">
          <div class="col-12">
            <div
              class="alert alert-success alert-dismissible fade show"
              role="alert"
            >
              <i class="bi bi-check-circle"></i> ${message}
              <button
                type="button"
                class="btn-close"
                data-bs-dismiss="alert"
              ></button>
            </div>
          </div>
        </div>
      </c:if>
      <c:if test="${not empty systemHealth}">
        <div class="row mb-4">
          <div class="col-12">
            <div class="card result-card">
              <div class="card-header">
                <h5 class="mb-0">
                  <i class="bi bi-server"></i> 전체 시스템 상태
                </h5>
              </div>
              <div class="card-body">
                <div class="row">
                  <div class="col-md-3">
                    <div class="text-center">
                      <h2
                        class="status-${systemHealth.overallStatus.code.toLowerCase()}"
                      >
                        <i
                          class="bi bi-${systemHealth.healthy ? 'check-circle' : 'exclamation-triangle'}"
                        ></i>
                        ${systemHealth.overallStatus.code}
                      </h2>
                      <p class="text-muted">
                        ${systemHealth.overallStatusDescription}
                      </p>
                    </div>
                  </div>
                  <div class="col-md-3">
                    <div class="text-center">
                      <h3>${systemHealth.totalComponents}</h3>
                      <p class="text-muted">총 컴포넌트</p>
                    </div>
                  </div>
                  <div class="col-md-3">
                    <div class="text-center">
                      <h3 class="status-up">
                        ${systemHealth.healthyComponents}
                      </h3>
                      <p class="text-muted">정상</p>
                    </div>
                  </div>
                  <div class="col-md-3">
                    <div class="text-center">
                      <h3 class="status-down">
                        ${systemHealth.unhealthyComponents}
                      </h3>
                      <p class="text-muted">장애</p>
                    </div>
                  </div>
                </div>
                <div class="row mt-3">
                  <div class="col-12">
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
                </div>
              </div>
            </div>
          </div>
        </div>
        <div class="row">
          <div class="col-12">
            <div class="card result-card">
              <div class="card-header">
                <h5 class="mb-0"><i class="bi bi-gear"></i> 컴포넌트별 상태</h5>
              </div>
              <div class="card-body">
                <div class="row">
                  <c:forEach var="component" items="${systemHealth.components}">
                    <div class="col-md-6 col-lg-4 mb-3">
                      <div
                        class="card border-${component.healthy ? 'success' : 'danger'}"
                      >
                        <div class="card-body">
                          <div class="d-flex align-items-center mb-2">
                            <i
                              class="bi bi-${component.healthy ? 'check-circle' : 'x-circle'} status-${component.status.code.toLowerCase()} me-2"
                            ></i>
                            <strong>${component.component}</strong>
                          </div>
                          <p class="text-muted mb-1">${component.message}</p>
                          <small class="text-muted">
                            응답시간: ${component.responseTime}ms |
                            <fmt:formatDate
                              value="${component.checkedAt}"
                              pattern="HH:mm:ss"
                            />
                          </small>
                        </div>
                      </div>
                    </div>
                  </c:forEach>
                </div>
              </div>
            </div>
          </div>
        </div>
      </c:if>
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
        const cards = document.querySelectorAll(".result-card");
        cards.forEach((card, index) => {
          setTimeout(() => {
            card.style.opacity = "0";
            card.style.transform = "translateY(20px)";
            card.style.transition = "all 0.5s ease";
            setTimeout(() => {
              card.style.opacity = "1";
              card.style.transform = "translateY(0)";
            }, 100);
          }, index * 200);
        });
      });
    </script>
  </body>
</html>
