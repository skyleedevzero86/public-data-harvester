<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8"%> <%@ taglib prefix="c"
                                           uri="http://java.sun.com/jsp/jstl/core" %> <%@ taglib prefix="fmt"
                                                                                                 uri="http://java.sun.com/jsp/jstl/fmt" %>

<c:set var="pageTitle" value="실시간 메트릭" />
<c:set var="pageCSS" value="${['health.css']}" />
<c:set var="pageJS" value="${['health.js']}" />

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
      <a class="nav-link active" href="/health/metrics/realtime">
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
    <h2><i class="bi bi-lightning"></i> 실시간 메트릭</h2>
    <div>
      <button
              class="btn btn-success"
              onclick="startRealtime()"
              id="startBtn"
      >
        <i class="bi bi-play"></i> 시작
      </button>
      <button
              class="btn btn-danger"
              onclick="stopRealtime()"
              id="stopBtn"
              disabled
      >
        <i class="bi bi-stop"></i> 중지
      </button>
      <a href="/health/metrics" class="btn btn-outline-secondary">
        <i class="bi bi-arrow-left"></i> 메트릭 대시보드
      </a>
    </div>
  </div>

  <div class="row mb-4">
    <div class="col-12">
      <div class="card">
        <div class="card-header">
          <h5 class="mb-0">
            <i class="bi bi-broadcast"></i> 실시간 상태
            <span class="badge bg-secondary ms-2" id="statusBadge"
            >중지됨</span
            >
          </h5>
        </div>
        <div class="card-body">
          <div class="row">
            <div class="col-md-3">
              <div class="text-center">
                <h6 class="text-muted">업데이트 간격</h6>
                <h4 id="updateInterval">5초</h4>
              </div>
            </div>
            <div class="col-md-3">
              <div class="text-center">
                <h6 class="text-muted">마지막 업데이트</h6>
                <h4 id="lastUpdate">-</h4>
              </div>
            </div>
            <div class="col-md-3">
              <div class="text-center">
                <h6 class="text-muted">활성 컴포넌트</h6>
                <h4 id="activeComponents">${activeComponents}</h4>
              </div>
            </div>
            <div class="col-md-3">
              <div class="text-center">
                <h6 class="text-muted">평균 응답시간</h6>
                <h4 id="avgResponseTime">-</h4>
              </div>
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
            <i class="bi bi-graph-up"></i> 응답시간 추이
          </h5>
        </div>
        <div class="card-body">
          <canvas id="responseTimeChart" height="300"></canvas>
        </div>
      </div>
    </div>
    <div class="col-md-6">
      <div class="card">
        <div class="card-header">
          <h5 class="card-title mb-0">
            <i class="bi bi-activity"></i> 성공률 추이
          </h5>
        </div>
        <div class="card-body">
          <canvas id="successRateChart" height="300"></canvas>
        </div>
      </div>
    </div>
  </div>

  <div class="row">
    <div class="col-12">
      <div class="card">
        <div class="card-header">
          <h5 class="mb-0">
            <i class="bi bi-gear"></i> 컴포넌트별 실시간 상태
          </h5>
        </div>
        <div class="card-body p-0">
          <div class="table-responsive">
            <table class="table table-hover mb-0">
              <thead class="table-dark">
              <tr>
                <th>컴포넌트</th>
                <th>상태</th>
                <th>응답시간</th>
                <th>성공률</th>
                <th>마지막 체크</th>
              </tr>
              </thead>
              <tbody id="realtimeTableBody">
              <c:forEach var="component" items="${components}">
                <tr id="component-${component.name}">
                  <td><i class="bi bi-gear"></i> ${component.name}</td>
                  <td>
                          <span
                                  class="badge bg-${component.healthy ? 'success' : 'danger'}"
                          >
                            <i
                                    class="bi bi-${component.healthy ? 'check-circle' : 'x-circle'}"
                            ></i>
                            ${component.status}
                          </span>
                  </td>
                  <td class="response-time">
                          <span
                                  class="badge bg-${component.responseTime < 1000 ? 'success' : component.responseTime < 3000 ? 'warning' : 'danger'}"
                          >
                            ${component.responseTime}ms
                          </span>
                  </td>
                  <td class="success-rate">
                    <div class="progress" style="height: 20px">
                      <div
                              class="progress-bar bg-${component.successRate >= 95 ? 'success' : component.successRate >= 80 ? 'warning' : 'danger'}"
                              style="width: ${component.successRate}%"
                      >
                        <fmt:formatNumber
                                value="${component.successRate}"
                                pattern="#.##"
                        />%
                      </div>
                    </div>
                  </td>
                  <td class="last-check">
                    <fmt:formatDate
                            value="${component.lastCheckTime}"
                            pattern="HH:mm:ss"
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

<%@ include file="../common/footer.jsp" %> <%@ include
        file="../common/scripts.jsp" %>
<script>
  let realtimeInterval;
  let responseTimeChart;
  let successRateChart;

  function initializeCharts() {
    const responseTimeCtx = document
            .getElementById("responseTimeChart")
            .getContext("2d");
    responseTimeChart = new Chart(responseTimeCtx, {
      type: "line",
      data: {
        labels: [],
        datasets: [
          {
            label: "평균 응답시간 (ms)",
            data: [],
            borderColor: "rgb(75, 192, 192)",
            backgroundColor: "rgba(75, 192, 192, 0.1)",
            tension: 0.1,
          },
        ],
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        scales: {
          y: {
            beginAtZero: true,
            title: {
              display: true,
              text: "응답시간 (ms)",
            },
          },
        },
      },
    });

    const successRateCtx = document
            .getElementById("successRateChart")
            .getContext("2d");
    successRateChart = new Chart(successRateCtx, {
      type: "line",
      data: {
        labels: [],
        datasets: [
          {
            label: "성공률 (%)",
            data: [],
            borderColor: "rgb(54, 162, 235)",
            backgroundColor: "rgba(54, 162, 235, 0.1)",
            tension: 0.1,
          },
        ],
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        scales: {
          y: {
            beginAtZero: true,
            max: 100,
            title: {
              display: true,
              text: "성공률 (%)",
            },
          },
        },
      },
    });
  }

  function startRealtime() {
    document.getElementById("startBtn").disabled = true;
    document.getElementById("stopBtn").disabled = false;
    document.getElementById("statusBadge").textContent = "실행중";
    document.getElementById("statusBadge").className =
            "badge bg-success ms-2";

    realtimeInterval = setInterval(updateRealtimeData, 5000);
    updateRealtimeData();
  }

  function stopRealtime() {
    document.getElementById("startBtn").disabled = false;
    document.getElementById("stopBtn").disabled = true;
    document.getElementById("statusBadge").textContent = "중지됨";
    document.getElementById("statusBadge").className =
            "badge bg-secondary ms-2";

    if (realtimeInterval) {
      clearInterval(realtimeInterval);
    }
  }

  function updateRealtimeData() {
    fetch("/api/health/realtime")
            .then((response) => response.json())
            .then((data) => {
              updateCharts(data);
              updateTable(data);
              updateStats(data);
              updateLastUpdateTime();
            })
            .catch((error) => {
              console.error("실시간 데이터 업데이트 오류:", error);
            });
  }

  function updateCharts(data) {
    const now = new Date().toLocaleTimeString();

    if (responseTimeChart && data.avgResponseTime !== undefined) {
      responseTimeChart.data.labels.push(now);
      responseTimeChart.data.datasets[0].data.push(data.avgResponseTime);

      if (responseTimeChart.data.labels.length > 20) {
        responseTimeChart.data.labels.shift();
        responseTimeChart.data.datasets[0].data.shift();
      }

      responseTimeChart.update("none");
    }

    if (successRateChart && data.successRate !== undefined) {
      successRateChart.data.labels.push(now);
      successRateChart.data.datasets[0].data.push(data.successRate);

      if (successRateChart.data.labels.length > 20) {
        successRateChart.data.labels.shift();
        successRateChart.data.datasets[0].data.shift();
      }

      successRateChart.update("none");
    }
  }

  function updateTable(data) {
    if (data.components) {
      data.components.forEach((component) => {
        const row = document.getElementById(`component-${component.name}`);
        if (row) {
          const statusCell = row.cells[1];
          statusCell.innerHTML = `
                        <span class="badge bg-${
                          component.healthy ? "success" : "danger"
                        }">
                            <i class="bi bi-${
                              component.healthy ? "check-circle" : "x-circle"
                            }"></i>
                            ${component.status}
                        </span>
                    `;

          const responseTimeCell = row.cells[2];
          responseTimeCell.innerHTML = `
                        <span class="badge bg-${
                          component.responseTime < 1000
                            ? "success"
                            : component.responseTime < 3000
                            ? "warning"
                            : "danger"
                        }">
                            ${component.responseTime}ms
                        </span>
                    `;

          const successRateCell = row.cells[3];
          successRateCell.innerHTML = `
                        <div class="progress" style="height: 20px;">
                            <div class="progress-bar bg-${
                              component.successRate >= 95
                                ? "success"
                                : component.successRate >= 80
                                ? "warning"
                                : "danger"
                            }"
                                 style="width: ${component.successRate}%">
                                ${component.successRate.toFixed(2)}%
                            </div>
                        </div>
                    `;

          const lastCheckCell = row.cells[4];
          lastCheckCell.textContent = new Date().toLocaleTimeString();
        }
      });
    }
  }

  function updateStats(data) {
    if (data.avgResponseTime !== undefined) {
      document.getElementById("avgResponseTime").textContent =
              data.avgResponseTime + "ms";
    }

    if (data.activeComponents !== undefined) {
      document.getElementById("activeComponents").textContent =
              data.activeComponents;
    }
  }

  function updateLastUpdateTime() {
    document.getElementById("lastUpdate").textContent =
            new Date().toLocaleTimeString();
  }

  document.addEventListener("DOMContentLoaded", function () {
    initializeCharts();
  });

  window.addEventListener("beforeunload", function () {
    stopRealtime();
  });
</script>
</body>
</html>