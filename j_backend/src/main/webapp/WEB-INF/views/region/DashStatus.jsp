<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %> <%@
taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %> <%@ taglib
prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<html>
  <head>
    <title>지역별 통계 현황</title>
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
        display: inline-block;
        margin: 20px;
      }
      .chart-container canvas {
        max-width: 500px;
        max-height: 300px;
      }
      .donut-container {
        display: inline-block;
        margin: 20px;
      }
      .donut-container canvas {
        max-width: 400px;
        max-height: 400px;
      }
    </style>
  </head>
  <body>
    <button onclick="history.back()" style="margin-bottom: 10px">
      이전 페이지로
    </button>
    <h2>지역별 통계 현황</h2>
    <table class="table">
      <thead>
        <tr>
          <th>지역명</th>
          <th>총 업체 수</th>
          <th>법인등록번호</th>
          <th>행정구역코드</th>
          <th>완성도</th>
        </tr>
      </thead>
      <tbody>
        <c:forEach var="stat" items="${regionStats}">
          <tr>
            <td>${stat.city} ${stat.district}</td>
            <td>
              <fmt:formatNumber value="${stat.totalCount}" pattern="#,##0" />
            </td>
            <td>
              <fmt:formatNumber
                value="${stat.validCorpRegNoCount}"
                pattern="#,##0"
              />
              <span style="color: #888">
                (<fmt:formatNumber
                  value="${stat.totalCount > 0 ? (stat.validCorpRegNoCount * 100.0 / stat.totalCount) : 0}"
                  pattern="0.0"
                />%)
              </span>
            </td>
            <td>
              <fmt:formatNumber
                value="${stat.validRegionCdCount}"
                pattern="#,##0"
              />
              <span style="color: #888">
                (<fmt:formatNumber
                  value="${stat.totalCount > 0 ? (stat.validRegionCdCount * 100.0 / stat.totalCount) : 0}"
                  pattern="0.0"
                />%)
              </span>
            </td>
            <td
              class="heatmap-cell ${stat.completionRate >= 80 ? 'heatmap-high' : stat.completionRate >= 50 ? 'heatmap-mid' : 'heatmap-low'}"
            >
              <div
                style="
                  width: 100px;
                  background: #eee;
                  border-radius: 8px;
                  overflow: hidden;
                "
              >
                <div
                  style="width:${stat.completionRate}%; background:#7e57c2; height:12px;"
                ></div>
              </div>
              <span style="font-size: 0.95em; font-weight: bold"
                >${stat.completionRate}%</span
              >
            </td>
          </tr>
        </c:forEach>
      </tbody>
    </table>

    <div class="chart-container">
      <h3>지역별 업체수(상위 10) - 차트</h3>
      <canvas id="barChart" width="500" height="300"></canvas>
    </div>

    <div class="donut-container">
      <h3>지역별 완성도(상위 10) - 도넛 차트</h3>
      <canvas id="donutChart" width="350" height="350"></canvas>
    </div>

    <script>
      // regionStats 데이터를 JS 배열로 변환
      const regionStats = [
          <c:forEach var="stat" items="${regionStats}" varStatus="loop">
          {
              label: "${stat.city} ${stat.district}",
              total: ${stat.totalCount},
              completion: ${stat.completionRate}
          }<c:if test="${!loop.last}">,</c:if>
          </c:forEach>
      ];

      // 막대 차트 (상위 10)
      const top10 = regionStats.slice(0, 10);
      const barCtx = document.getElementById('barChart').getContext('2d');
      new Chart(barCtx, {
          type: 'bar',
          data: {
              labels: top10.map(r => r.label),
              datasets: [{
                  label: '총 업체 수',
                  data: top10.map(r => r.total),
                  backgroundColor: '#7e57c2'
              }]
          },
          options: {
              responsive: true,
              maintainAspectRatio: false,
              plugins: {
                  legend: { display: false }
              }
          }
      });

      // 도넛 차트 (상위 10)
      const donutCtx = document.getElementById('donutChart').getContext('2d');
      new Chart(donutCtx, {
          type: 'doughnut',
          data: {
              labels: top10.map(r => r.label),
              datasets: [{
                  label: '완성도',
                  data: top10.map(r => r.completion),
                  backgroundColor: [
                      '#7e57c2', '#43e97b', '#fa709a', '#fee140', '#f09819',
                      '#b39ddb', '#ede7f6', '#ff5858', '#38f9d7', '#91eac9'
                  ]
              }]
          },
          options: {
              responsive: true,
              maintainAspectRatio: false,
              cutout: '70%'
          }
      });
    </script>
  </body>
</html>
