<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %> <%@
        taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %> <%@ taglib
        prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html>
<head>
  <meta charset="UTF-8" />
  <meta name="_csrf" content="${_csrf.token}" />
  <meta name="_csrf_header" content="${_csrf.headerName}" />
  <title>지역별 통계 현황 - 통신판매자사업관리시스템</title>
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
    .heatmap-cell {
      transition: background 0.3s;
    }

    .heatmap-high {
      background: #7e57c2;
      color: #fff;
    }

    .heatmap-mid {
      background: #b39ddb;
    }

    .heatmap-low {
      background: #ede7f6;
    }

    .chart-container {
      background: white;
      border-radius: 8px;
      padding: 20px;
      margin-bottom: 20px;
      box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
      height: 400px;
      position: relative;
    }

    .chart-container canvas {
      max-width: 100%;
      height: 100% !important;
    }

    .donut-container {
      background: white;
      border-radius: 8px;
      padding: 20px;
      margin-bottom: 20px;
      box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
      height: 400px;
      position: relative;
    }

    .donut-container canvas {
      max-width: 100%;
      height: 100% !important;
    }

    .stats-card {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: white;
      border: none;
      border-radius: 12px;
      transition: transform 0.2s;
    }

    .stats-card:hover {
      transform: translateY(-2px);
    }

    .table th {
      background-color: #343a40;
      color: white;
      border-color: #454d55;
    }

    .table td {
      vertical-align: middle;
    }

    .completion-bar {
      width: 100px;
      background: #eee;
      border-radius: 8px;
      overflow: hidden;
      display: inline-block;
      margin-right: 10px;
    }

    .completion-fill {
      height: 12px;
      background: #7e57c2;
      transition: width 0.3s ease;
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

    .back-button {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      border: none;
      color: white;
      padding: 10px 20px;
      border-radius: 8px;
      transition: all 0.3s ease;
    }

    .back-button:hover {
      transform: translateY(-1px);
      box-shadow: 0 4px 8px rgba(0, 0, 0, 0.2);
      color: white;
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
    <h2><i class="bi bi-graph-up"></i> 지역별 통계 현황</h2>
    <a href="javascript:history.back()" class="btn back-button">
      <i class="bi bi-arrow-left"></i> 이전 페이지로
    </a>
  </div>

  <div class="row mb-4">
    <div class="col-xl-3 col-md-6 col-sm-12">
      <div class="card stats-card text-white mb-4">
        <div class="card-body">
          <div class="d-flex justify-content-between align-items-center">
            <div>
              <div class="text-white-50 small">총 지역 수</div>
              <div class="fs-4 fw-bold">${regionStats.size()}</div>
            </div>
            <div class="icon-align">
              <i class="bi bi-geo-alt-fill stats-icon"></i>
            </div>
          </div>
        </div>
      </div>
    </div>
    <div class="col-xl-3 col-md-6 col-sm-12">
      <div class="card bg-success text-white mb-4">
        <div class="card-body">
          <div class="d-flex justify-content-between align-items-center">
            <div>
              <div class="text-white-50 small">평균 완성도</div>
              <div class="fs-4 fw-bold">
                <c:set var="totalCompletion" value="0" />
                <c:forEach var="stat" items="${regionStats}">
                  <c:set
                          var="totalCompletion"
                          value="${totalCompletion + stat.completionRate}"
                  />
                </c:forEach>
                <fmt:formatNumber
                        value="${regionStats.size() > 0 ? totalCompletion / regionStats.size() : 0}"
                        pattern="0.0"
                />%
              </div>
            </div>
            <div class="icon-align">
              <i class="bi bi-pie-chart-fill stats-icon"></i>
            </div>
          </div>
        </div>
      </div>
    </div>
    <div class="col-xl-3 col-md-6 col-sm-12">
      <div class="card bg-info text-white mb-4">
        <div class="card-body">
          <div class="d-flex justify-content-between align-items-center">
            <div>
              <div class="text-white-50 small">총 업체 수</div>
              <div class="fs-4 fw-bold">
                <c:set var="totalCorpCount" value="0" />
                <c:forEach var="stat" items="${regionStats}">
                  <c:set
                          var="totalCorpCount"
                          value="${totalCorpCount + stat.totalCount}"
                  />
                </c:forEach>
                <fmt:formatNumber
                        value="${totalCorpCount}"
                        pattern="#,##0"
                />
              </div>
            </div>
            <div class="icon-align">
              <i class="bi bi-building-fill stats-icon"></i>
            </div>
          </div>
        </div>
      </div>
    </div>
    <div class="col-xl-3 col-md-6 col-sm-12">
      <div class="card bg-warning text-white mb-4">
        <div class="card-body">
          <div class="d-flex justify-content-between align-items-center">
            <div>
              <div class="text-white-50 small">최고 완성도</div>
              <div class="fs-4 fw-bold">
                <c:set var="maxCompletion" value="0" />
                <c:forEach var="stat" items="${regionStats}">
                  <c:if test="${stat.completionRate > maxCompletion}">
                    <c:set
                            var="maxCompletion"
                            value="${stat.completionRate}"
                    />
                  </c:if>
                </c:forEach>
                <fmt:formatNumber value="${maxCompletion}" pattern="0.0" />%
              </div>
            </div>
            <div class="icon-align">
              <i class="bi bi-trophy-fill stats-icon"></i>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>

  <div class="row mb-4">
    <div class="col-lg-6">
      <div class="chart-container">
        <h5 class="mb-3">
          <i class="bi bi-bar-chart"></i> 지역별 업체수 (상위 10)
        </h5>
        <canvas id="barChart"></canvas>
      </div>
    </div>
    <div class="col-lg-6">
      <div class="donut-container">
        <h5 class="mb-3">
          <i class="bi bi-pie-chart"></i> 지역별 완성도 (상위 10)
        </h5>
        <canvas id="donutChart"></canvas>
      </div>
    </div>
  </div>

  <div class="card mb-4">
    <div class="card-header">
      <h5 class="mb-0"><i class="bi bi-table"></i> 지역별 상세 통계</h5>
    </div>
    <div class="card-body p-0">
      <div class="table-responsive">
        <table class="table table-hover mb-0">
          <thead>
          <tr>
            <th width="20%">지역명</th>
            <th width="15%">총 업체 수</th>
            <th width="20%">법인등록번호</th>
            <th width="20%">행정구역코드</th>
            <th width="25%">완성도</th>
          </tr>
          </thead>
          <tbody>
          <c:forEach var="stat" items="${regionStats}">
            <tr>
              <td>
                <strong>${stat.city} ${stat.district}</strong>
              </td>
              <td>
                      <span class="badge bg-primary fs-6">
                        <fmt:formatNumber
                                value="${stat.totalCount}"
                                pattern="#,##0"
                        />
                      </span>
              </td>
              <td>
                <div class="d-flex flex-column">
                        <span class="fw-bold">
                          <fmt:formatNumber
                                  value="${stat.validCorpRegNoCount}"
                                  pattern="#,##0"
                          />
                        </span>
                  <small class="text-muted">
                    (<fmt:formatNumber
                          value="${stat.totalCount > 0 ? (stat.validCorpRegNoCount * 100.0 / stat.totalCount) : 0}"
                          pattern="0.0"
                  />%)
                  </small>
                </div>
              </td>
              <td>
                <div class="d-flex flex-column">
                        <span class="fw-bold">
                          <fmt:formatNumber
                                  value="${stat.validRegionCdCount}"
                                  pattern="#,##0"
                          />
                        </span>
                  <small class="text-muted">
                    (<fmt:formatNumber
                          value="${stat.totalCount > 0 ? (stat.validRegionCdCount * 100.0 / stat.totalCount) : 0}"
                          pattern="0.0"
                  />%)
                  </small>
                </div>
              </td>
              <td>
                <div class="d-flex align-items-center">
                  <div class="completion-bar">
                    <div
                            class="completion-fill"
                            style="width:${stat.completionRate}%"
                    ></div>
                  </div>
                  <span
                          class="fw-bold ${stat.completionRate >= 80 ? 'text-success' : stat.completionRate >= 50 ? 'text-warning' : 'text-danger'}"
                  >
                          ${stat.completionRate}%
                        </span>
                </div>
              </td>
            </tr>
          </c:forEach>
          </tbody>
        </table>
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
  const regionStats = [
    <c:forEach var="stat" items="${regionStats}" varStatus="loop">
    {
      label: "${stat.city} ${stat.district}",
      total: ${stat.totalCount},
      completion: ${stat.completionRate}
    }<c:if test="${!loop.last}">,</c:if>
    </c:forEach>
  ];

  console.log('차트 데이터:', regionStats);

  const top10 = regionStats.slice(0, 10);
  const barCtx = document.getElementById('barChart').getContext('2d');
  new Chart(barCtx, {
    type: 'bar',
    data: {
      labels: top10.map(r => r.label),
      datasets: [{
        label: '총 업체 수',
        data: top10.map(r => r.total),
        backgroundColor: 'rgba(102, 126, 234, 0.8)',
        borderColor: 'rgba(102, 126, 234, 1)',
        borderWidth: 1
      }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: { display: false },
        tooltip: {
          backgroundColor: 'rgba(0, 0, 0, 0.8)',
          titleColor: 'white',
          bodyColor: 'white'
        }
      },
      scales: {
        y: {
          beginAtZero: true,
          grid: {
            color: 'rgba(0, 0, 0, 0.1)'
          }
        },
        x: {
          grid: {
            display: false
          }
        }
      }
    }
  });

  const donutCtx = document.getElementById('donutChart').getContext('2d');
  new Chart(donutCtx, {
    type: 'doughnut',
    data: {
      labels: top10.map(r => r.label),
      datasets: [{
        label: '완성도',
        data: top10.map(r => r.completion),
        backgroundColor: [
          '#667eea', '#43e97b', '#fa709a', '#fee140', '#f09819',
          '#b39ddb', '#ede7f6', '#ff5858', '#38f9d7', '#91eac9'
        ],
        borderWidth: 2,
        borderColor: 'white'
      }]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      cutout: '70%',
      plugins: {
        legend: {
          position: 'right',
          labels: {
            padding: 20,
            usePointStyle: true,
            pointStyle: 'circle'
          }
        },
        tooltip: {
          backgroundColor: 'rgba(0, 0, 0, 0.8)',
          titleColor: 'white',
          bodyColor: 'white',
          callbacks: {
            label: function(context) {
              const label = context.label || '';
              const value = context.parsed;
              return `${label}: ${value}%`;
            }
          }
        }
      }
    }
  });
</script>
</body>
</html>