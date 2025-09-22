<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="ko">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <title>컴포넌트 상세 메트릭 - ${component} - Antock</title>
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css" rel="stylesheet" />
  <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css" rel="stylesheet" />
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
  </style>
</head>
<body>
<div class="container-fluid">
  <div class="row">
    <nav class="col-md-3 col-lg-2 d-md-block bg-light sidebar">
      <div class="position-sticky pt-3">
        <ul class="nav flex-column">
          <li class="nav-item">
            <a class="nav-link" href="/health">
              <i class="fas fa-tachometer-alt"></i> 대시보드
            </a>
          </li>
          <li class="nav-item">
            <a class="nav-link" href="/health/status">
              <i class="fas fa-heartbeat"></i> 상태 조회
            </a>
          </li>
          <li class="nav-item">
            <a class="nav-link" href="/health/metrics">
              <i class="fas fa-chart-line"></i> 메트릭
            </a>
          </li>
          <li class="nav-item">
            <a class="nav-link" href="/health/metrics/realtime">
              <i class="fas fa-bolt"></i> 실시간
            </a>
          </li>
          <li class="nav-item">
            <a class="nav-link" href="/health/history">
              <i class="fas fa-history"></i> 이력 조회
            </a>
          </li>
        </ul>
      </div>
    </nav>
    <main class="col-md-9 ms-sm-auto col-lg-10 px-md-4">
      <div class="d-flex justify-content-between flex-wrap flex-md-nowrap align-items-center pt-3 pb-2 mb-3 border-bottom">
        <h1 class="h2">
          <i class="fas fa-chart-bar text-primary"></i> ${component} 상세 메트릭
        </h1>
        <div class="btn-toolbar mb-2 mb-md-0">
          <div class="btn-group me-2">
            <a href="/health/metrics/component/${component}?days=1" class="btn btn-sm ${days == 1 ? 'btn-primary' : 'btn-outline-secondary'}">1일</a>
            <a href="/health/metrics/component/${component}?days=7" class="btn btn-sm ${days == 7 ? 'btn-primary' : 'btn-outline-secondary'}">7일</a>
            <a href="/health/metrics/component/${component}?days=30" class="btn btn-sm ${days == 30 ? 'btn-primary' : 'btn-outline-secondary'}">30일</a>
          </div>
          <a href="/health/metrics" class="btn btn-sm btn-outline-secondary">
            <i class="fas fa-arrow-left"></i> 전체 메트릭
          </a>
        </div>
      </div>
      <c:if test="${not empty metrics}">
        <div class="row mb-4">
          <div class="col-md-3">
            <div class="card metric-card">
              <div class="card-body text-center">
                <i class="fas fa-percentage fa-2x text-success mb-2"></i>
                <h5 class="card-title">가용성</h5>
                <div class="metric-value text-success">
                  <fmt:formatNumber value="${metrics.availability}" pattern="#.##"/>%
                </div>
                <small class="metric-label">지난 ${days}일</small>
              </div>
            </div>
          </div>
          <div class="col-md-3">
            <div class="card metric-card">
              <div class="card-body text-center">
                <i class="fas fa-clock fa-2x text-info mb-2"></i>
                <h5 class="card-title">평균 응답시간</h5>
                <div class="metric-value text-info">
                  <fmt:formatNumber value="${metrics.averageResponseTime}" pattern="#.#"/>ms
                </div>
                <small class="metric-label">지난 ${days}일</small>
              </div>
            </div>
          </div>
          <div class="col-md-3">
            <div class="card metric-card">
              <div class="card-body text-center">
                <i class="fas fa-check-circle fa-2x text-success mb-2"></i>
                <h5 class="card-title">성공률</h5>
                <div class="metric-value text-success">
                  <fmt:formatNumber value="${metrics.successRate}" pattern="#.##"/>%
                </div>
                <small class="metric-label">지난 ${days}일</small>
              </div>
            </div>
          </div>
          <div class="col-md-3">
            <div class="card metric-card">
              <div class="card-body text-center">
                <i class="fas fa-list fa-2x text-warning mb-2"></i>
                <h5 class="card-title">총 체크 횟수</h5>
                <div class="metric-value text-warning">
                  <fmt:formatNumber value="${metrics.totalChecks}" pattern="#,###"/>
                </div>
                <small class="metric-label">지난 ${days}일</small>
              </div>
            </div>
          </div>
        </div>
        <div class="row mb-4">
          <div class="col-md-3">
            <div class="card metric-card">
              <div class="card-body text-center">
                <i class="fas fa-arrow-up fa-2x text-success mb-2"></i>
                <h5 class="card-title">최대 응답시간</h5>
                <div class="metric-value text-success">
                  <fmt:formatNumber value="${metrics.maxResponseTime}" pattern="#.#"/>ms
                </div>
                <small class="metric-label">지난 ${days}일</small>
              </div>
            </div>
          </div>
          <div class="col-md-3">
            <div class="card metric-card">
              <div class="card-body text-center">
                <i class="fas fa-arrow-down fa-2x text-info mb-2"></i>
                <h5 class="card-title">최소 응답시간</h5>
                <div class="metric-value text-info">
                  <fmt:formatNumber value="${metrics.minResponseTime}" pattern="#.#"/>ms
                </div>
                <small class="metric-label">지난 ${days}일</small>
              </div>
            </div>
          </div>
          <div class="col-md-3">
            <div class="card metric-card">
              <div class="card-body text-center">
                <i class="fas fa-fire fa-2x text-primary mb-2"></i>
                <h5 class="card-title">연속 성공</h5>
                <div class="metric-value text-primary">
                    ${metrics.consecutiveSuccesses}
                </div>
                <small class="metric-label">현재 연속</small>
              </div>
            </div>
          </div>
          <div class="col-md-3">
            <div class="card metric-card">
              <div class="card-body text-center">
                <i class="fas fa-exclamation-triangle fa-2x text-danger mb-2"></i>
                <h5 class="card-title">연속 실패</h5>
                <div class="metric-value text-danger">
                    ${metrics.consecutiveFailures}
                </div>
                <small class="metric-label">현재 연속</small>
              </div>
            </div>
          </div>
        </div>
        <div class="row mb-4">
          <div class="col-md-6">
            <div class="card">
              <div class="card-header">
                <h5 class="card-title mb-0">현재 상태</h5>
              </div>
              <div class="card-body">
                <div class="d-flex align-items-center">
                  <span class="status-indicator status-${metrics.lastStatus.code.toLowerCase()}"></span>
                  <h4 class="mb-0">${metrics.lastStatus.code}</h4>
                </div>
                <p class="text-muted mt-2 mb-0">
                  마지막 체크:
                  <fmt:formatDate value="${metrics.lastCheckTimeAsDate}" pattern="yyyy-MM-dd HH:mm:ss"/>
                </p>
              </div>
            </div>
          </div>
          <div class="col-md-6">
            <div class="card">
              <div class="card-header">
                <h5 class="card-title mb-0">통계 요약</h5>
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
                <h5 class="card-title mb-0">응답 시간 분포</h5>
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
          <i class="fas fa-exclamation-triangle"></i>
            ${component} 컴포넌트의 메트릭 데이터를 불러올 수 없습니다. 잠시 후 다시 시도해주세요.
        </div>
      </c:if>
    </main>
  </div>
</div>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/js/bootstrap.bundle.min.js"></script>
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