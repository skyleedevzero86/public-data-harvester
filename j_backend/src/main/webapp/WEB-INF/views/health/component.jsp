<%@ page language="java" contentType="text/html; charset=UTF-8"
pageEncoding="UTF-8"%> <%@ taglib prefix="c"
uri="http://java.sun.com/jsp/jstl/core" %> <%@ taglib prefix="fmt"
uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html>
  <head>
    <meta charset="UTF-8" />
    <meta name="_csrf" content="${_csrf.token}" />
    <meta name="_csrf_header" content="${_csrf.headerName}" />
    <title>${component} 헬스 상태 - 통신판매자사업관리시스템</title>
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
      .health-card {
        transition: transform 0.2s;
      }
      .health-card:hover {
        transform: translateY(-2px);
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
            <i class="bi bi-lightning"></i> 실시간
          </a>
          <a class="nav-link" href="/health/history">
            <i class="bi bi-clock-history"></i> 이력 조회
          </a>
          <a class="nav-link" href="/members/logout">
            <i class="bi bi-box-arrow-right"></i> 로그아웃
          </a>
        </div>
      </div>
    </nav>

    <div class="container-fluid mt-4">
      <div class="d-flex justify-content-between align-items-center mb-4">
        <h2><i class="bi bi-gear"></i> ${component} 헬스 상태</h2>
        <div>
          <a href="/health" class="btn btn-outline-secondary">
            <i class="bi bi-arrow-left"></i> 대시보드
          </a>
          <a href="/health/check" class="btn btn-primary">
            <i class="bi bi-arrow-clockwise"></i> 수동 체크
          </a>
        </div>
      </div>
      <div class="row mb-4">
        <div class="col-xl-3 col-md-6 col-sm-6">
          <div class="card bg-primary text-white mb-4">
            <div class="card-body">
              <div class="d-flex justify-content-between align-items-center">
                <div>
                  <div class="text-white-50 small">총 체크 수</div>
                  <div class="fs-4 fw-bold">${totalElements}</div>
                </div>
                <div class="icon-align">
                  <i class="bi bi-list-check stats-icon"></i>
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
                    <c:set var="upCount" value="0" />
                    <c:forEach var="check" items="${healthChecks.content}">
                      <c:if test="${check.healthy}">
                        <c:set var="upCount" value="${upCount + 1}" />
                      </c:if>
                    </c:forEach>
                    ${upCount}
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
                    <c:set var="downCount" value="0" />
                    <c:forEach var="check" items="${healthChecks.content}">
                      <c:if test="${!check.healthy}">
                        <c:set var="downCount" value="${downCount + 1}" />
                      </c:if>
                    </c:forEach>
                    ${downCount}
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
                  <div class="text-white-50 small">평균 응답시간</div>
                  <div class="fs-4 fw-bold">
                    <c:set var="avgResponseTime" value="0" />
                    <c:forEach var="check" items="${healthChecks.content}">
                      <c:set
                        var="avgResponseTime"
                        value="${avgResponseTime + check.responseTime}"
                      />
                    </c:forEach>
                    <c:if test="${healthChecks.numberOfElements > 0}">
                      <c:set
                        var="avgResponseTime"
                        value="${avgResponseTime / healthChecks.numberOfElements}"
                      />
                    </c:if>
                    <fmt:formatNumber
                      value="${avgResponseTime}"
                      pattern="#.##"
                    />ms
                  </div>
                </div>
                <div class="icon-align">
                  <i class="bi bi-clock stats-icon"></i>
                </div>
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
                <i class="bi bi-clock-history"></i> 체크 이력
              </h5>
            </div>
            <div class="card-body">
              <div class="table-responsive">
                <table class="table table-hover">
                  <thead>
                    <tr>
                      <th>상태</th>
                      <th>메시지</th>
                      <th>응답시간</th>
                      <th>체크 타입</th>
                      <th>체크시간</th>
                      <th>만료시간</th>
                    </tr>
                  </thead>
                  <tbody>
                    <c:forEach var="check" items="${healthChecks.content}">
                      <tr
                        class="${check.healthy ? 'table-success' : 'table-danger'}"
                      >
                        <td>
                          <span
                            class="badge bg-${check.healthy ? 'success' : 'danger'}"
                          >
                            <i
                              class="bi bi-${check.healthy ? 'check-circle' : 'x-circle'}"
                            ></i>
                            ${check.status.code}
                          </span>
                        </td>
                        <td>${check.message}</td>
                        <td>
                          <span
                            class="badge bg-${check.responseTime < 1000 ? 'success' : check.responseTime < 3000 ? 'warning' : 'danger'}"
                          >
                            ${check.responseTime}ms
                          </span>
                        </td>
                        <td>
                          <span class="badge bg-info">${check.checkType}</span>
                        </td>
                        <td>
                          <fmt:formatDate
                            value="${check.checkedAtAsDate}"
                            pattern="MM-dd HH:mm:ss"
                          />
                        </td>
                        <td>
                          <fmt:formatDate
                            value="${check.expiresAtAsDate}"
                            pattern="MM-dd HH:mm:ss"
                          />
                          <c:if test="${check.expired}">
                            <span class="badge bg-warning">만료</span>
                          </c:if>
                        </td>
                      </tr>
                    </c:forEach>
                  </tbody>
                </table>
              </div>
              <c:if test="${totalPages > 1}">
                <nav aria-label="페이지 네비게이션">
                  <ul class="pagination justify-content-center">
                    <li class="page-item ${currentPage == 0 ? 'disabled' : ''}">
                      <a
                        class="page-link"
                        href="?page=${currentPage - 1}&size=20"
                        >이전</a
                      >
                    </li>
                    <c:forEach begin="0" end="${totalPages - 1}" var="pageNum">
                      <li
                        class="page-item ${currentPage == pageNum ? 'active' : ''}"
                      >
                        <a class="page-link" href="?page=${pageNum}&size=20"
                          >${pageNum + 1}</a
                        >
                      </li>
                    </c:forEach>
                    <li
                      class="page-item ${currentPage == totalPages - 1 ? 'disabled' : ''}"
                    >
                      <a
                        class="page-link"
                        href="?page=${currentPage + 1}&size=20"
                        >다음</a
                      >
                    </li>
                  </ul>
                </nav>
              </c:if>
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
