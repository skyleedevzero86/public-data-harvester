<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="ko">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <title>실시간 헬스 체크 메트릭 - Antock</title>
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
    .realtime-indicator {
      animation: pulse 2s infinite;
    }
    @keyframes pulse {
      0% {
        opacity: 1;
      }
      50% {
        opacity: 0.5;
      }
      100% {
        opacity: 1;
      }
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
            <a class="nav-link active" href="/health/metrics/realtime">
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
          <i class="fas fa-bolt text-warning realtime-indicator"></i> 실시간 헬스 체크 메트릭
        </h1>
        <div class="btn-toolbar mb-2 mb-md-0">
          <div class="btn-group me-2">
            <button type="button" class="btn btn-sm btn-success" onclick="startAutoRefresh()">
              <i class="fas fa-play"></i> 자동 새로고침
            </button>
            <button type="button" class="btn btn-sm btn-danger" onclick="stopAutoRefresh()">
              <i class="fas fa-stop"></i> 중지
            </button>
          </div>
          <button type="button" class="btn btn-sm btn-primary" onclick="location.reload()">
            <i class="fas fa-sync-alt"></i> 새로고침
          </button>
        </div>
      </div>
      <c:if test="${not empty metrics}">
        <div class="row mb-4">
          <div class="col-md-3">
            <div class="card metric-card">
              <div class="card-body text-center">
                <i class="fas fa-percentage fa-2x text-success mb-2 realtime-indicator"></i>
                <h5 class="card-title">실시간 가용성</h5>
                <div class="metric-value text-success">
                  <fmt:formatNumber value="${metrics.overallAvailability}" pattern="#.##"/>%
                </div>
                <small class="metric-label">최근 1시간</small>
              </div>
            </div>
          </div>
          <div class="col-md-3">
            <div class="card metric-card">
              <div class="card-body text-center">
                <i class="fas fa-clock fa-2x text-info mb-2 realtime-indicator"></i>
                <h5 class="card-title">실시간 응답시간</h5>
                <div class="metric-value text-info">
                  <fmt:formatNumber value="${metrics.averageResponseTime}" pattern="#.#"/>ms
                </div>
                <small class="metric-label">최근 1시간</small>
              </div>
            </div>
          </div>
          <div class="col-md-3">
            <div class="card metric-card">
              <div class="card-body text-center">
                <i class="fas fa-check-circle fa-2x text-success mb-2 realtime-indicator"></i>
                <h5 class="card-title">실시간 성공률</h5>
                <div class="metric-value text-success">
                  <fmt:formatNumber value="${metrics.successRate}" pattern="#.##"/>%
                </div>
                <small class="metric-label">최근 1시간</small>
              </div>
            </div>
          </div>
          <div class="col-md-3">
            <div class="card metric-card">
              <div class="card-body text-center">
                <i class="fas fa-list fa-2x text-warning mb-2 realtime-indicator"></i>
                <h5 class="card-title">실시간 체크 횟수</h5>
                <div class="metric-value text-warning">
                  <fmt:formatNumber value="${metrics.totalChecks}" pattern="#,###"/>
                </div>
                <small class="metric-label">최근 1시간</small>
              </div>
            </div>
          </div>
        </div>
        <div class="row mb-4">
          <div class="col-md-8">
            <div class="card">
              <div class="card-header">
                <h5 class="card-title mb-0">실시간 가용성 트렌드</h5>
              </div>
              <div class="card-body">
                <canvas id="realtimeChart" height="300"></canvas>
              </div>
            </div>
          </div>
          <div class="col-md-4">
            <div class="card">
              <div class="card-header">
                <h5 class="card-title mb-0">컴포넌트별 실시간 상태</h5>
              </div>
              <div class="card-body">
                <canvas id="componentStatusChart" height="300"></canvas>
              </div>
            </div>
          </div>
        </div>
        <div class="row">
          <div class="col-12">
            <div class="card">
              <div class="card-header">
                <h5 class="card-title mb-0">실시간 컴포넌트 상태</h5>
              </div>
              <div class="card-body">
                <div class="table-responsive">
                  <table class="table table-hover">
                    <thead>
                    <tr>
                      <th>컴포넌트</th>
                      <th>현재 상태</th>
                      <th>가용성</th>
                      <th>응답시간</th>
                      <th>성공률</th>
                      <th>연속 성공</th>
                      <th>마지막 체크</th>
                      <th>액션</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach var="component" items="${metrics.componentMetrics}">
                      <tr>
                        <td>
                          <i class="fas fa-${component.component == 'database' ? 'database' : component.component == 'redis' ? 'memory' : component.component == 'cache' ? 'layer-group' : component.component == 'member-service' ? 'users' : 'tachometer-alt'} realtime-indicator"></i>
                            ${component.component}
                        </td>
                        <td>
                          <span class="status-indicator status-${component.lastStatus.code.toLowerCase()} realtime-indicator"></span>
                            ${component.lastStatus.code}
                        </td>
                        <td>
                          <fmt:formatNumber value="${component.availability}" pattern="#.##"/>%
                        </td>
                        <td>
                          <fmt:formatNumber value="${component.averageResponseTime}" pattern="#.#"/>ms
                        </td>
                        <td>
                          <fmt:formatNumber value="${component.successRate}" pattern="#.##"/>%
                        </td>
                        <td>
                          <span class="badge bg-success">${component.consecutiveSuccesses}</span>
                        </td>
                        <td>
                          <fmt:formatDate value="${component.lastCheckTimeAsDate}" pattern="HH:mm:ss"/>
                        </td>
                        <td>
                          <a href="/health/metrics/component/${component.component}?days=1" class="btn btn-sm btn-outline-primary">
                            <i class="fas fa-chart-bar"></i> 상세
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
          <i class="fas fa-exclamation-triangle"></i>
          실시간 메트릭 데이터를 불러올 수 없습니다. 잠시 후 다시 시도해주세요.
        </div>
      </c:if>
    </main>
  </div>
</div>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/js/bootstrap.bundle.min.js"></script>
<script>
  let autoRefreshInterval;
  function startAutoRefresh() {
    if (autoRefreshInterval) {
      clearInterval(autoRefreshInterval);
    }
    autoRefreshInterval = setInterval(() => {
      location.reload();
    }, 30000);
  }
  function stopAutoRefresh() {
    if (autoRefreshInterval) {
      clearInterval(autoRefreshInterval);
      autoRefreshInterval = null;
    }
  }
  <c:if test="${not empty metrics}">
  const realtimeCtx = document.getElementById('realtimeChart').getContext('2d');
  const realtimeChart = new Chart(realtimeCtx, {
    type: 'line',
    data: {
      labels: [],
      datasets: [{
        label: '가용성 (%)',
        data: [],
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
          beginAtZero: false,
          min: 95,
          max: 100
        }
      },
      animation: {
        duration: 0
      }
    }
  });
  const componentStatusCtx = document.getElementById('componentStatusChart').getContext('2d');
  const componentStatusData = {
    <c:forEach var="component" items="${metrics.componentMetrics}" varStatus="status">
    '${component.component}': ${component.availability}<c:if test="${!status.last}">,</c:if>
    </c:forEach>
  };
  new Chart(componentStatusCtx, {
    type: 'bar',
    data: {
      labels: Object.keys(componentStatusData),
      datasets: [{
        label: '가용성 (%)',
        data: Object.values(componentStatusData),
        backgroundColor: [
          'rgba(75, 192, 192, 0.8)',
          'rgba(255, 99, 132, 0.8)',
          'rgba(255, 205, 86, 0.8)',
          'rgba(54, 162, 235, 0.8)',
          'rgba(153, 102, 255, 0.8)'
        ]
      }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      scales: {
        y: {
          beginAtZero: true,
          max: 100
        }
      }
    }
  });
  function updateRealtimeData() {
    const now = new Date();
    const timeLabel = now.getHours() + ':' + now.getMinutes().toString().padStart(2, '0');
    realtimeChart.data.labels.push(timeLabel);
    realtimeChart.data.datasets[0].data.push(${metrics.overallAvailability});
    if (realtimeChart.data.labels.length > 20) {
      realtimeChart.data.labels.shift();
      realtimeChart.data.datasets[0].data.shift();
    }
    realtimeChart.update('none');
  }
  setInterval(updateRealtimeData, 5000);
  </c:if>
</script>
</body>
</html>