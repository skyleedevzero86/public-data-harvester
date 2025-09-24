<%@ page language="java" contentType="text/html; charset=UTF-8"
pageEncoding="UTF-8"%> <%@ taglib prefix="c"
uri="http://java.sun.com/jsp/jstl/core" %> <%@ taglib prefix="fmt"
uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="ko">
  <head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>시스템 헬스 대시보드 - 통신판매자사업관리시스템</title>
    <link
      href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css"
      rel="stylesheet"
    />
    <link
      href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.7.2/font/bootstrap-icons.css"
      rel="stylesheet"
    />
    <style>
      .health-card {
        transition: transform 0.2s;
      }
      .health-card:hover {
        transform: translateY(-2px);
      }
      .status-up {
        color: #28a745;
      }
      .status-down {
        color: #dc3545;
      }
      .status-unknown {
        color: #ffc107;
      }
      .component-status {
        display: flex;
        align-items: center;
        gap: 8px;
      }
      .refresh-btn {
        position: fixed;
        bottom: 20px;
        right: 20px;
        z-index: 1000;
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
        <h2><i class="bi bi-heart-pulse"></i> 시스템 헬스 대시보드</h2>
        <div>
          <a href="/health/check" class="btn btn-primary">
            <i class="bi bi-arrow-clockwise"></i> 수동 체크
          </a>
          <a href="/health/history" class="btn btn-outline-secondary">
            <i class="bi bi-clock-history"></i> 이력 보기
          </a>
        </div>
      </div>
      <div class="row mb-4">
        <div class="col-xl-3 col-md-6 col-sm-6">
          <div class="card bg-primary text-white mb-4">
            <div class="card-body">
              <div class="d-flex justify-content-between align-items-center">
                <div>
                  <div class="text-white-50 small">전체 상태</div>
                  <div
                    class="fs-4 fw-bold status-${systemHealth.overallStatus.code.toLowerCase()}"
                  >
                    <i
                      class="bi bi-${systemHealth.healthy ? 'check-circle' : 'exclamation-triangle'}"
                    ></i>
                    ${systemHealth.overallStatus.code}
                  </div>
                </div>
                <div class="icon-align">
                  <i class="bi bi-server stats-icon"></i>
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
                  <div class="text-white-50 small">정상</div>
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
                  <div class="text-white-50 small">장애</div>
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
      </div>

      <div class="row mb-4">
        <div class="col-12">
          <div class="card health-card">
            <div class="card-header">
              <h5 class="mb-0">
                <i class="bi bi-speedometer2"></i> 시스템 상태 개요
              </h5>
            </div>
            <div class="card-body">
              <div class="row">
                <div class="col-md-6">
                  <h6 class="text-muted">전체 상태 설명</h6>
                  <p class="mb-3">${systemHealth.overallStatusDescription}</p>
                </div>
                <div class="col-md-6">
                  <h6 class="text-muted">헬스 비율</h6>
                  <div class="progress" style="height: 25px">
                    <div
                      class="progress-bar bg-success"
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
      <div class="row mb-4">
        <div class="col-12">
          <div class="card health-card">
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
                        <div class="component-status">
                          <i
                            class="bi bi-${component.healthy ? 'check-circle' : 'x-circle'} status-${component.status.code.toLowerCase()}"
                          ></i>
                          <strong>${component.component}</strong>
                        </div>
                        <p class="text-muted mb-1">${component.message}</p>
                        <small class="text-muted">
                          응답시간: ${component.responseTime}ms |
                          <fmt:formatDate
                            value="${component.checkedAtAsDate}"
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
      <div class="row">
        <div class="col-12">
          <div class="card health-card">
            <div class="card-header">
              <h5 class="mb-0">
                <i class="bi bi-clock-history"></i> 최근 체크 이력 (24시간)
              </h5>
            </div>
            <div class="card-body p-0">
              <div class="table-responsive">
                <table class="table table-hover mb-0">
                  <thead class="table-dark">
                    <tr>
                      <th>컴포넌트</th>
                      <th>상태</th>
                      <th>메시지</th>
                      <th>응답시간</th>
                      <th>체크시간</th>
                    </tr>
                  </thead>
                  <tbody>
                    <c:forEach var="check" items="${recentChecks}">
                      <tr>
                        <td><i class="bi bi-gear"></i> ${check.component}</td>
                        <td>
                          <span
                            class="badge bg-${check.healthy ? 'success' : 'danger'}"
                          >
                            ${check.status.code}
                          </span>
                        </td>
                        <td>${check.message}</td>
                        <td>${check.responseTime}ms</td>
                        <td>
                          <fmt:formatDate
                            value="${check.checkedAtAsDate}"
                            pattern="MM-dd HH:mm:ss"
                          />
                        </td>
                      </tr>
                    </c:forEach>
                  </tbody>
                </table>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    <button class="btn btn-primary refresh-btn" onclick="location.reload()">
      <i class="bi bi-arrow-clockwise"></i>
    </button>

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
      }, 300000);

      document.addEventListener("DOMContentLoaded", function () {
        const cards = document.querySelectorAll(".health-card");
        cards.forEach((card, index) => {
          setTimeout(() => {
            card.style.opacity = "0";
            card.style.transform = "translateY(20px)";
            card.style.transition = "all 0.5s ease";
            setTimeout(() => {
              card.style.opacity = "1";
              card.style.transform = "translateY(0)";
            }, 100);
          }, index * 100);
        });
      });
    </script>
  </body>
</html>
