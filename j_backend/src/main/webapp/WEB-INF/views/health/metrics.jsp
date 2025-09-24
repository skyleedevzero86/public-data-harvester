<%@ page language="java" contentType="text/html; charset=UTF-8"
pageEncoding="UTF-8"%> <%@ taglib prefix="c"
uri="http://java.sun.com/jsp/jstl/core" %> <%@ taglib prefix="fmt"
uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="ko">
  <head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>헬스 체크 상세 메트릭 - 통신판매자사업관리시스템</title>
    <link
      href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css"
      rel="stylesheet"
    />
    <link
      href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.7.2/font/bootstrap-icons.css"
      rel="stylesheet"
    />
    <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
    <style>
      .metric-card {
        transition: transform 0.2s;
      }
      .metric-card:hover {
        transform: translateY(-2px);
      }
      .status-indicator {
        width: 12px;
        height: 12px;
        border-radius: 50%;
        display: inline-block;
        margin-right: 8px;
      }
      .status-up {
        background-color: #28a745;
      }
      .status-down {
        background-color: #dc3545;
      }
      .status-unknown {
        background-color: #ffc107;
      }
      .metric-value {
        font-size: 2rem;
        font-weight: bold;
      }
      .metric-label {
        font-size: 0.9rem;
        color: #6c757d;
      }
      .trend-up {
        color: #28a745;
      }
      .trend-down {
        color: #dc3545;
      }
      .trend-stable {
        color: #6c757d;
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
        <h2><i class="bi bi-graph-up"></i> 헬스 체크 상세 메트릭</h2>
        <div class="btn-toolbar mb-2 mb-md-0">
          <div class="btn-group me-2">
            <a
              href="/health/metrics?days=1"
              class="btn btn-sm ${days == 1 ? 'btn-primary' : 'btn-outline-secondary'}"
              >1일</a
            >
            <a
              href="/health/metrics?days=7"
              class="btn btn-sm ${days == 7 ? 'btn-primary' : 'btn-outline-secondary'}"
              >7일</a
            >
            <a
              href="/health/metrics?days=30"
              class="btn btn-sm ${days == 30 ? 'btn-primary' : 'btn-outline-secondary'}"
              >30일</a
            >
          </div>
          <button
            type="button"
            class="btn btn-sm btn-primary"
            onclick="location.reload()"
          >
            <i class="bi bi-arrow-clockwise"></i> 새로고침
          </button>
        </div>
      </div>
      <c:if test="${not empty metrics}">
        <div class="row mb-4">
          <div class="col-xl-3 col-md-6 col-sm-6">
            <div class="card bg-success text-white mb-4">
              <div class="card-body">
                <div class="d-flex justify-content-between align-items-center">
                  <div>
                    <div class="text-white-50 small">전체 가용성</div>
                    <div class="fs-4 fw-bold">
                      <fmt:formatNumber
                        value="${metrics.overallAvailability}"
                        pattern="#.##"
                      />%
                    </div>
                  </div>
                  <div class="icon-align">
                    <i class="bi bi-percent stats-icon"></i>
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
                      <fmt:formatNumber
                        value="${metrics.averageResponseTime}"
                        pattern="#.#"
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
          <div class="col-xl-3 col-md-6 col-sm-6">
            <div class="card bg-primary text-white mb-4">
              <div class="card-body">
                <div class="d-flex justify-content-between align-items-center">
                  <div>
                    <div class="text-white-50 small">성공률</div>
                    <div class="fs-4 fw-bold">
                      <fmt:formatNumber
                        value="${metrics.successRate}"
                        pattern="#.##"
                      />%
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
            <div class="card bg-warning text-white mb-4">
              <div class="card-body">
                <div class="d-flex justify-content-between align-items-center">
                  <div>
                    <div class="text-white-50 small">총 체크 횟수</div>
                    <div class="fs-4 fw-bold">
                      <fmt:formatNumber
                        value="${metrics.totalChecks}"
                        pattern="#,###"
                      />
                    </div>
                  </div>
                  <div class="icon-align">
                    <i class="bi bi-list-check stats-icon"></i>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
        <div class="row mb-4">
          <div class="col-xl-3 col-md-6 col-sm-6">
            <div class="card bg-secondary text-white mb-4">
              <div class="card-body">
                <div class="d-flex justify-content-between align-items-center">
                  <div>
                    <div class="text-white-50 small">최대 응답시간</div>
                    <div class="fs-4 fw-bold">
                      <fmt:formatNumber
                        value="${metrics.maxResponseTime}"
                        pattern="#.#"
                      />ms
                    </div>
                  </div>
                  <div class="icon-align">
                    <i class="bi bi-arrow-up stats-icon"></i>
                  </div>
                </div>
              </div>
            </div>
          </div>
          <div class="col-xl-3 col-md-6 col-sm-6">
            <div class="card bg-dark text-white mb-4">
              <div class="card-body">
                <div class="d-flex justify-content-between align-items-center">
                  <div>
                    <div class="text-white-50 small">최소 응답시간</div>
                    <div class="fs-4 fw-bold">
                      <fmt:formatNumber
                        value="${metrics.minResponseTime}"
                        pattern="#.#"
                      />ms
                    </div>
                  </div>
                  <div class="icon-align">
                    <i class="bi bi-arrow-down stats-icon"></i>
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
                    <div class="text-white-50 small">평균 가동시간</div>
                    <div class="fs-4 fw-bold">
                      <fmt:formatNumber
                        value="${metrics.averageUptime}"
                        pattern="#.#"
                      />h
                    </div>
                  </div>
                  <div class="icon-align">
                    <i class="bi bi-clock-fill stats-icon"></i>
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
                    <div class="text-white-50 small">평균 복구시간</div>
                    <div class="fs-4 fw-bold">
                      <fmt:formatNumber
                        value="${metrics.mttr}"
                        pattern="#.#"
                      />h
                    </div>
                  </div>
                  <div class="icon-align">
                    <i class="bi bi-tools stats-icon"></i>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
        <div class="row mb-4">
          <div class="col-md-8">
            <div class="card metric-card">
              <div class="card-header">
                <h5 class="mb-0">
                  <i class="bi bi-graph-up"></i> 시간대별 가용성 트렌드
                </h5>
              </div>
              <div class="card-body">
                <canvas id="availabilityChart" height="300"></canvas>
              </div>
            </div>
          </div>
          <div class="col-md-4">
            <div class="card metric-card">
              <div class="card-header">
                <h5 class="mb-0">
                  <i class="bi bi-pie-chart"></i> 상태별 분포
                </h5>
              </div>
              <div class="card-body">
                <canvas id="statusChart" height="300"></canvas>
              </div>
            </div>
          </div>
        </div>
        <div class="row">
          <div class="col-12">
            <div class="card metric-card">
              <div class="card-header">
                <h5 class="mb-0">
                  <i class="bi bi-gear"></i> 컴포넌트별 상세 메트릭
                </h5>
              </div>
              <div class="card-body p-0">
                <div class="table-responsive">
                  <table class="table table-hover mb-0">
                    <thead class="table-dark">
                      <tr>
                        <th>컴포넌트</th>
                        <th>상태</th>
                        <th>가용성</th>
                        <th>평균 응답시간</th>
                        <th>성공률</th>
                        <th>연속 성공</th>
                        <th>마지막 체크</th>
                        <th>액션</th>
                      </tr>
                    </thead>
                    <tbody>
                      <c:forEach
                        var="component"
                        items="${metrics.componentMetrics}"
                      >
                        <tr>
                          <td>
                            <i
                              class="bi bi-${component.component == 'database' ? 'database' : component.component == 'redis' ? 'memory' : component.component == 'cache' ? 'layers' : component.component == 'member-service' ? 'people' : 'speedometer2'}"
                            ></i>
                            ${component.component}
                          </td>
                          <td>
                            <span
                              class="status-indicator status-${component.lastStatus.code.toLowerCase()}"
                            ></span>
                            ${component.lastStatus.code}
                          </td>
                          <td>
                            <fmt:formatNumber
                              value="${component.availability}"
                              pattern="#.##"
                            />%
                          </td>
                          <td>
                            <fmt:formatNumber
                              value="${component.averageResponseTime}"
                              pattern="#.#"
                            />ms
                          </td>
                          <td>
                            <fmt:formatNumber
                              value="${component.successRate}"
                              pattern="#.##"
                            />%
                          </td>
                          <td>
                            <span class="badge bg-success"
                              >${component.consecutiveSuccesses}</span
                            >
                          </td>
                          <td>
                            <fmt:formatDate
                              value="${component.lastCheckTimeAsDate}"
                              pattern="MM-dd HH:mm"
                            />
                          </td>
                          <td>
                            <a
                              href="/health/metrics/component/${component.component}?days=${days}"
                              class="btn btn-sm btn-outline-primary"
                            >
                              <i class="bi bi-bar-chart"></i> 상세
                            </a>
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
      </c:if>
      <c:if test="${empty metrics}">
        <div class="alert alert-warning" role="alert">
          <i class="bi bi-exclamation-triangle"></i>
          메트릭 데이터를 불러올 수 없습니다. 잠시 후 다시 시도해주세요.
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
      <c:if test="${not empty metrics}">
      const availabilityCtx = document.getElementById('availabilityChart').getContext('2d');
      const timeBasedData = [
          <c:forEach var="timeData" items="${metrics.timeBasedMetrics}" varStatus="status">
          {
              x: '${timeData.timeSlot}',
              y: ${timeData.availability}
          }<c:if test="${!status.last}">,</c:if>
          </c:forEach>
      ];
      new Chart(availabilityCtx, {
          type: 'line',
          data: {
              datasets: [{
                  label: '가용성 (%)',
                  data: timeBasedData,
                  borderColor: 'rgb(75, 192, 192)',
                  backgroundColor: 'rgba(75, 192, 192, 0.1)',
                  tension: 0.1
              }]
          },
          options: {
              responsive: true,
              maintainAspectRatio: false,
              scales: {
                  x: {
                      type: 'time',
                      time: {
                          unit: 'hour'
                      }
                  },
                  y: {
                      beginAtZero: false,
                      min: 95,
                      max: 100
                  }
              }
          }
      });

      const statusCtx = document.getElementById('statusChart').getContext('2d');
      const statusData = {
          <c:forEach var="statusEntry" items="${metrics.statusDistribution}" varStatus="status">
          '${statusEntry.key.code}': ${statusEntry.value}<c:if test="${!status.last}">,</c:if>
          </c:forEach>
      };
      new Chart(statusCtx, {
          type: 'doughnut',
          data: {
              labels: Object.keys(statusData),
              datasets: [{
                  data: Object.values(statusData),
                  backgroundColor: [
                      'rgb(75, 192, 192)',
                      'rgb(255, 99, 132)',
                      'rgb(255, 205, 86)'
                  ]
              }]
          },
          options: {
              responsive: true,
              maintainAspectRatio: false
          }
      });
      </c:if>
    </script>
  </body>
</html>
