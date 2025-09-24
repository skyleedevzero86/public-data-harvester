<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>실시간 헬스 체크 메트릭 - 통신판매자사업관리시스템</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.7.2/font/bootstrap-icons.css" rel="stylesheet">
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
        .table th, .table td {
            vertical-align: middle;
            text-align: center;
        }
        .table th:first-child, .table td:first-child {
            text-align: center;
        }
        .table th:nth-child(2), .table td:nth-child(2) {
            text-align: left;
        }
        .table th:nth-child(3), .table td:nth-child(3) {
            text-align: left;
        }
        .table th:nth-child(4), .table td:nth-child(4) {
            text-align: left;
        }
        .table th:last-child, .table td:last-child {
            min-width: 50px;
            text-align: center;
        }
        .table th:nth-last-child(2), .table td:nth-last-child(2) {
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
            <a class="nav-link active" href="/health/metrics/realtime">
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
        <h2><i class="bi bi-lightning-charge realtime-indicator"></i> 실시간 헬스 체크 메트릭</h2>
        <div>
            <button type="button" class="btn btn-success me-2" onclick="startAutoRefresh()">
                <i class="bi bi-play-circle"></i> 자동 새로고침
            </button>
            <button type="button" class="btn btn-danger me-2" onclick="stopAutoRefresh()">
                <i class="bi bi-stop-circle"></i> 중지
            </button>
            <button type="button" class="btn btn-primary" onclick="location.reload()">
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
                            <div class="text-white-50 small">실시간 가용성</div>
                            <div class="fs-4 fw-bold realtime-indicator">
                                <fmt:formatNumber value="${metrics.overallAvailability}" pattern="#.##"/>%
                            </div>
                        </div>
                        <div class="icon-align">
                            <i class="bi bi-percent stats-icon realtime-indicator"></i>
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
                            <div class="text-white-50 small">실시간 응답시간</div>
                            <div class="fs-4 fw-bold realtime-indicator">
                                <fmt:formatNumber value="${metrics.averageResponseTime}" pattern="#.#"/>ms
                            </div>
                        </div>
                        <div class="icon-align">
                            <i class="bi bi-clock stats-icon realtime-indicator"></i>
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
                            <div class="text-white-50 small">실시간 성공률</div>
                            <div class="fs-4 fw-bold realtime-indicator">
                                <fmt:formatNumber value="${metrics.successRate}" pattern="#.##"/>%
                            </div>
                        </div>
                        <div class="icon-align">
                            <i class="bi bi-check-circle stats-icon realtime-indicator"></i>
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
                            <div class="text-white-50 small">실시간 체크 횟수</div>
                            <div class="fs-4 fw-bold realtime-indicator">
                                <fmt:formatNumber value="${metrics.totalChecks}" pattern="#,###"/>
                            </div>
                        </div>
                        <div class="icon-align">
                            <i class="bi bi-list-ul stats-icon realtime-indicator"></i>
                        </div>
                    </div>
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
    <div class="card">
        <div class="card-header">
            <div class="d-flex justify-content-between align-items-center">
                <h5 class="mb-0"><i class="bi bi-table"></i> 실시간 컴포넌트 상태</h5>
            </div>
        </div>
        <div class="card-body p-0">
            <div class="table-responsive">
                <table class="table table-hover mb-0">
                    <thead class="table-dark">
                    <tr>
                        <th width="15%">컴포넌트</th>
                        <th width="12%">현재 상태</th>
                        <th width="10%">가용성</th>
                        <th width="10%">응답시간</th>
                        <th width="10%">성공률</th>
                        <th width="10%">연속 성공</th>
                        <th width="12%">마지막 체크</th>
                        <th width="21%">액션</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach var="component" items="${metrics.componentMetrics}">
                        <tr>
                            <td style="text-align: left;">
                                <div class="d-flex align-items-center">
                                    <i class="bi bi-${component.component == 'database' ? 'database' : component.component == 'redis' ? 'memory' : component.component == 'cache' ? 'layers' : component.component == 'member-service' ? 'people' : 'speedometer2'} realtime-indicator me-2"></i>
                                    <strong>${component.component}</strong>
                                </div>
                            </td>
                            <td>
                                <span class="status-indicator status-${component.lastStatus.code.toLowerCase()} realtime-indicator"></span>
                                <span class="badge
                                    <c:choose>
                                        <c:when test='${component.lastStatus.code == "UP"}'>bg-success</c:when>
                                        <c:when test='${component.lastStatus.code == "DOWN"}'>bg-danger</c:when>
                                        <c:otherwise>bg-warning text-dark</c:otherwise>
                                    </c:choose>
                                ">${component.lastStatus.code}</span>
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
                                <small>
                                    <fmt:formatDate value="${component.lastCheckTimeAsDate}" pattern="HH:mm:ss"/>
                                </small>
                            </td>
                            <td>
                                <a href="/health/metrics/component/${component.component}?days=1" class="btn btn-sm btn-outline-primary">
                                    <i class="bi bi-graph-up"></i> 상세
                                </a>
                            </td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
    </c:if>
    <c:if test="${empty metrics}">
        <div class="alert alert-warning" role="alert">
            <i class="bi bi-exclamation-triangle"></i>
            실시간 메트릭 데이터를 불러올 수 없습니다. 잠시 후 다시 시도해주세요.
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
                    <div class="sub-title">CHUNGJANG STREET FESTIVAL OF RECOLLECTION</div>
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