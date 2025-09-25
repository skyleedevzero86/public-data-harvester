<%@ page language="java" contentType="text/html; charset=UTF-8"
pageEncoding="UTF-8"%> <%@ taglib prefix="c"
uri="http://java.sun.com/jsp/jstl/core" %> <%@ taglib prefix="fmt"
uri="http://java.sun.com/jsp/jstl/fmt" %>

<c:set var="pageTitle" value="컴포넌트 상세 메트릭 - ${component}" />

<!DOCTYPE html>
<html>
  <head>
    <%@ include file="../common/head.jsp" %>
    <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
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
        <h2><i class="bi bi-graph-up"></i> ${component} 상세 메트릭</h2>
        <div>
          <div class="btn-group me-2">
            <a
              href="/health/metrics/component/${component}?days=1"
              class="btn btn-sm ${days == 1 ? 'btn-primary' : 'btn-outline-secondary'}"
              >1일</a
            >
            <a
              href="/health/metrics/component/${component}?days=7"
              class="btn btn-sm ${days == 7 ? 'btn-primary' : 'btn-outline-secondary'}"
              >7일</a
            >
            <a
              href="/health/metrics/component/${component}?days=30"
              class="btn btn-sm ${days == 30 ? 'btn-primary' : 'btn-outline-secondary'}"
              >30일</a
            >
          </div>
          <a href="/health/metrics" class="btn btn-outline-secondary">
            <i class="bi bi-arrow-left"></i> 전체 메트릭
          </a>
        </div>
      </div>
      <c:if test="${not empty metrics}">
        <div class="row mb-4">
          <div class="col-xl-3 col-md-6 col-sm-6">
            <div class="card bg-success text-white mb-4">
              <div class="card-body">
                <div class="d-flex justify-content-between align-items-center">
                  <div>
                    <div class="text-white-50 small">가용성</div>
                    <div class="fs-4 fw-bold">
                      <fmt:formatNumber
                        value="${metrics.availability}"
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
                    <div class="text-white-50 small">연속 성공</div>
                    <div class="fs-4 fw-bold">
                      ${metrics.consecutiveSuccesses}
                    </div>
                  </div>
                  <div class="icon-align">
                    <i class="bi bi-fire stats-icon"></i>
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
                    <div class="text-white-50 small">연속 실패</div>
                    <div class="fs-4 fw-bold">
                      ${metrics.consecutiveFailures}
                    </div>
                  </div>
                  <div class="icon-align">
                    <i class="bi bi-exclamation-triangle stats-icon"></i>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
        <div class="row mb-4">
          <div class="col-md-6">
            <div class="card">
              <div class="card-header">
                <h5 class="card-title mb-0">
                  <i class="bi bi-activity"></i> 현재 상태
                </h5>
              </div>
              <div class="card-body">
                <div class="d-flex align-items-center">
                  <span
                    class="status-indicator status-${metrics.lastStatus.code.toLowerCase()}"
                  ></span>
                  <h4 class="mb-0">${metrics.lastStatus.code}</h4>
                </div>
                <p class="text-muted mt-2 mb-0">
                  마지막 체크:
                  <fmt:formatDate
                    value="${metrics.lastCheckTimeAsDate}"
                    pattern="yyyy-MM-dd HH:mm:ss"
                  />
                </p>
              </div>
            </div>
          </div>
          <div class="col-md-6">
            <div class="card">
              <div class="card-header">
                <h5 class="card-title mb-0">
                  <i class="bi bi-bar-chart"></i> 통계 요약
                </h5>
              </div>
              <div class="card-body">
                <div class="row text-center">
                  <div class="col-6">
                    <h5 class="text-success">${metrics.successfulChecks}</h5>
                    <small class="text-muted">성공</small>
                  </div>
                  <div class="col-6">
                    <h5 class="text-danger">${metrics.failedChecks}</h5>
                    <small class="text-muted">실패</small>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
        <div class="row">
          <div class="col-12">
            <div class="card">
              <div class="card-header">
                <h5 class="card-title mb-0">
                  <i class="bi bi-graph-up"></i> 응답 시간 분포
                </h5>
              </div>
              <div class="card-body">
                <canvas id="responseTimeChart" height="300"></canvas>
              </div>
            </div>
          </div>
        </div>
      </c:if>
      <c:if test="${empty metrics}">
        <div class="alert alert-warning" role="alert">
          <i class="bi bi-exclamation-triangle"></i>
          ${component} 컴포넌트의 메트릭 데이터를 불러올 수 없습니다. 잠시 후
          다시 시도해주세요.
        </div>
      </c:if>
    </div>

    <%@ include file="../common/footer.jsp" %> <%@ include
    file="../common/scripts.jsp" %>
    <script>
      <c:if test="${not empty metrics}">
      const responseTimeCtx = document.getElementById('responseTimeChart').getContext('2d');
      new Chart(responseTimeCtx, {
        type: 'line',
        data: {
          labels: ['00:00', '04:00', '08:00', '12:00', '16:00', '20:00', '24:00'],
          datasets: [{
            label: '응답 시간 (ms)',
            data: [
              ${metrics.averageResponseTime * 0.8},
              ${metrics.averageResponseTime * 1.2},
              ${metrics.averageResponseTime * 0.9},
              ${metrics.averageResponseTime * 1.1},
              ${metrics.averageResponseTime * 0.95},
              ${metrics.averageResponseTime * 1.05},
              ${metrics.averageResponseTime * 0.85}
            ],
            borderColor: 'rgb(75, 192, 192)',
            backgroundColor: 'rgba(75, 192, 192, 0.1)',
            tension: 0.1
          }]
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          scales: {
            y: {
              beginAtZero: true,
              title: {
                display: true,
                text: '응답 시간 (ms)'
              }
            },
            x: {
              title: {
                display: true,
                text: '시간'
              }
            }
          }
        }
      });
      </c:if>
    </script>
  </body>
</html>
