<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<c:set var="pageTitle" value="실시간 메트릭" />
<c:set var="pageCSS" value="${['health.css']}" />
<c:set var="pageJS" value="${['health.js']}" />

<!DOCTYPE html>
<html>
<head>
  <%@ include file="../common/head.jsp" %>
</head>
<body>
<%@ include file="../common/navigation.jsp" %>

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
                <c:set var="componentUp" value="${component.lastStatus != null && component.lastStatus.code == 'UP'}"/>
                <c:set var="componentStatus" value="${component.lastStatus != null ? component.lastStatus.code : 'UNKNOWN'}"/>
                <c:set var="componentResponseTime" value="${component.averageResponseTime != null ? component.averageResponseTime : 0}"/>
                <c:set var="componentSuccessRate" value="${component.successRate != null ? component.successRate : 0}"/>
                <tr id="component-${component.component}">
                  <td><i class="bi bi-gear"></i> ${component.component}</td>
                  <td>
                          <span
                                  class="badge bg-${componentUp ? 'success' : 'danger'}"
                          >
                            <i
                                    class="bi bi-${componentUp ? 'check-circle' : 'x-circle'}"
                            ></i>
                            ${componentStatus}
                          </span>
                  </td>
                  <td class="response-time">
                          <span
                                  class="badge bg-${componentResponseTime < 1000 ? 'success' : componentResponseTime < 3000 ? 'warning' : 'danger'}"
                          >
                            <fmt:formatNumber value="${componentResponseTime}" pattern="#"/>ms
                          </span>
                  </td>
                  <td class="success-rate">
                    <div class="progress" style="height: 20px">
                      <div
                              class="progress-bar bg-${componentSuccessRate >= 95 ? 'success' : componentSuccessRate >= 80 ? 'warning' : 'danger'}"
                              style="width: ${componentSuccessRate}%"
                      >
                        <fmt:formatNumber
                                value="${componentSuccessRate}"
                                pattern="#.##"
                        />%
                      </div>
                    </div>
                  </td>
                  <td class="last-check">
                    <fmt:formatDate
                            value="${component.lastCheckTimeAsDate}"
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

<%@ include file="../common/footer.jsp" %>
<%@ include file="../common/scripts.jsp" %>
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
    fetch("/health/api/metrics/realtime")
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

    if (responseTimeChart && data.averageResponseTime !== undefined) {
      responseTimeChart.data.labels.push(now);
      responseTimeChart.data.datasets[0].data.push(data.averageResponseTime);

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
    const components = data.componentMetrics || [];
    components.forEach((component) => {
      const row = document.getElementById("component-" + component.component);
      if (!row) {
        return;
      }
      const healthy = component.lastStatus === "UP";
      const status = component.lastStatus || "UNKNOWN";
      const responseTime = Number(component.averageResponseTime || 0);
      const successRate = Number(component.successRate || 0);
      const lastCheck = component.lastCheckTime
        ? new Date(component.lastCheckTime).toLocaleTimeString()
        : "-";
      const responseClass =
        responseTime < 1000 ? "success" : responseTime < 3000 ? "warning" : "danger";
      const rateClass =
        successRate >= 95 ? "success" : successRate >= 80 ? "warning" : "danger";

      row.cells[1].innerHTML =
        '<span class="badge bg-' + (healthy ? "success" : "danger") + '">' +
        '<i class="bi bi-' + (healthy ? "check-circle" : "x-circle") + '"></i> ' +
        status +
        "</span>";
      row.cells[2].innerHTML =
        '<span class="badge bg-' + responseClass + '">' +
        Math.round(responseTime) +
        "ms</span>";
      row.cells[3].innerHTML =
        '<div class="progress" style="height: 20px;">' +
        '<div class="progress-bar bg-' + rateClass + '" style="width: ' +
        successRate +
        '%">' +
        successRate.toFixed(2) +
        "%</div></div>";
      row.cells[4].textContent = lastCheck;
    });
  }

  function updateStats(data) {
    if (data.averageResponseTime !== undefined && data.averageResponseTime !== null) {
      document.getElementById("avgResponseTime").textContent =
              Math.round(data.averageResponseTime) + "ms";
    }

    const activeCount = (data.componentMetrics || []).length;
    document.getElementById("activeComponents").textContent = activeCount;
  }

  function updateLastUpdateTime() {
    document.getElementById("lastUpdate").textContent =
            new Date().toLocaleTimeString();
  }

  document.addEventListener("DOMContentLoaded", function () {
    if (typeof Chart !== "undefined") {
      initializeCharts();
    } else {
      window.addEventListener("chartjs-loaded", initializeCharts);
      window.addEventListener("chartjs-error", function () {
        console.error(
                "Chart.js 로드 실패로 인해 차트를 표시할 수 없습니다."
        );
      });
    }
  });

  window.addEventListener("beforeunload", function () {
    stopRealtime();
  });
</script>
</body>
</html>