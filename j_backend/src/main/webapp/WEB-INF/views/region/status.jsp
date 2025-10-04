<%@ page language="java" contentType="text/html; charset=UTF-8"
pageEncoding="UTF-8"%> <%@ taglib prefix="c"
uri="http://java.sun.com/jsp/jstl/core" %> <%@ taglib prefix="fmt"
uri="http://java.sun.com/jsp/jstl/fmt" %>

<c:set var="pageTitle" value="지역별 상세 통계" />
<c:set var="pageCSS" value="${['region.css']}" />
<c:set var="pageJS" value="${['region.js']}" />

<!DOCTYPE html>
<html>
  <head>
    <%@ include file="../common/head.jsp" %>
  </head>
  <body>
  <%@ include file="../common/navigation.jsp" %>

    <div class="main-content">
      <div class="container-fluid mt-4">
        <div class="d-flex justify-content-between align-items-center mb-4">
          <h2><i class="bi bi-graph-up"></i> 지역별 상세 통계</h2>
          <div class="d-flex gap-2">
            <div class="btn-group">
              <button
                type="button"
                class="btn btn-primary dropdown-toggle"
                data-bs-toggle="dropdown"
                aria-expanded="false"
              >
                <i class="bi bi-funnel"></i> 검색 조건
              </button>
              <ul class="dropdown-menu">
                <li>
                  <a class="dropdown-item" href="#" onclick="resetFilters()">
                    <i class="bi bi-arrow-clockwise"></i> 필터 초기화</a
                  >
                </li>
                <li><hr class="dropdown-divider" /></li>
                <li>
                  <a
                    class="dropdown-item"
                    href="#"
                    onclick="filterByStatus('전체')"
                  >
                    <i class="bi bi-circle"></i> 전체</a
                  >
                </li>
                <li>
                  <a
                    class="dropdown-item"
                    href="#"
                    onclick="filterByStatus('등록')"
                  >
                    <i class="bi bi-check-circle"></i> 등록</a
                  >
                </li>
                <li>
                  <a
                    class="dropdown-item"
                    href="#"
                    onclick="filterByStatus('폐업')"
                  >
                    <i class="bi bi-x-circle"></i> 폐업</a
                  >
                </li>
              </ul>
            </div>
            <a href="/corp/list" class="btn btn-outline-secondary">
              <i class="bi bi-arrow-left"></i> 전체 목록
            </a>
          </div>
        </div>

        <div class="row mb-4">
          <div class="col-xl-3 col-md-6 col-sm-6">
            <div class="card bg-primary text-white mb-4">
              <div class="card-body">
                <div class="d-flex justify-content-between align-items-center">
                  <div>
                    <div class="text-white-50 small">총 지역 수</div>
                    <div class="fs-4 fw-bold">${totalRegions}</div>
                  </div>
                  <div class="icon-align">
                    <i class="bi bi-geo-alt stats-icon"></i>
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
                    <div class="text-white-50 small">총 법인 수</div>
                    <div class="fs-4 fw-bold">
                      <fmt:formatNumber value="${totalCorps}" pattern="#,###" />
                    </div>
                  </div>
                  <div class="icon-align">
                    <i class="bi bi-building stats-icon"></i>
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
                    <div class="text-white-50 small">평균 법인/지역</div>
                    <div class="fs-4 fw-bold">
                      <fmt:formatNumber
                        value="${averageCorpsPerRegion}"
                        pattern="#.#"
                      />
                    </div>
                  </div>
                  <div class="icon-align">
                    <i class="bi bi-calculator stats-icon"></i>
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
                    <div class="text-white-50 small">상위 10% 지역</div>
                    <div class="fs-4 fw-bold">${topRegions}</div>
                  </div>
                  <div class="icon-align">
                    <i class="bi bi-star stats-icon"></i>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div class="card">
          <div class="card-header">
            <div class="d-flex justify-content-between align-items-center">
              <h5 class="mb-0"><i class="bi bi-table"></i> 지역별 상세 현황</h5>
              <div class="input-group search-input">
                <input
                  type="text"
                  class="form-control"
                  placeholder="지역명 검색..."
                  id="regionSearch"
                />
                <button
                  class="btn btn-outline-secondary"
                  type="button"
                  onclick="searchRegions()"
                >
                  <i class="bi bi-search"></i>
                </button>
              </div>
            </div>
          </div>
          <div class="card-body p-0">
            <div class="table-responsive">
              <table class="table table-hover mb-0">
                <thead class="table-dark">
                  <tr>
                    <th onclick="sortTable(0)" class="sortable">
                      순위 <i class="bi bi-arrow-down-up"></i>
                    </th>
                    <th onclick="sortTable(1)" class="sortable">
                      시/도 <i class="bi bi-arrow-down-up"></i>
                    </th>
                    <th onclick="sortTable(2)" class="sortable">
                      구/군 <i class="bi bi-arrow-down-up"></i>
                    </th>
                    <th onclick="sortTable(3)" class="sortable">
                      법인 수 <i class="bi bi-arrow-down-up"></i>
                    </th>
                    <th onclick="sortTable(4)" class="sortable">
                      비율 <i class="bi bi-arrow-down-up"></i>
                    </th>
                    <th onclick="sortTable(5)" class="sortable">
                      등록 법인 <i class="bi bi-arrow-down-up"></i>
                    </th>
                    <th onclick="sortTable(6)" class="sortable">
                      폐업 법인 <i class="bi bi-arrow-down-up"></i>
                    </th>
                    <th>상세</th>
                  </tr>
                </thead>
                <tbody id="regionTableBody">
                  <c:forEach
                    var="region"
                    items="${regionStats}"
                    varStatus="status"
                  >
                    <tr
                      onclick="selectRow(this)"
                      data-region="${region.city}"
                      data-sgg="${region.district}"
                    >
                      <td>
                        <span
                          class="badge <c:choose> <c:when test='${status.index < 5}'>bg-warning</c:when> <c:when test='${status.index < 10}'>bg-info</c:when> <c:otherwise>bg-secondary</c:otherwise> </c:choose>"
                        >
                          ${status.index + 1}
                        </span>
                      </td>
                      <td><i class="bi bi-geo-alt"></i> ${region.city}</td>
                      <td><i class="bi bi-geo"></i> ${region.district}</td>
                      <td>
                        <span class="fw-bold">
                          <fmt:formatNumber
                            value="${region.totalCount}"
                            pattern="#,###"
                          />
                        </span>
                      </td>
                      <td>
                        <div class="progress" style="height: 20px">
                          <div
                            class="progress-bar bg-primary"
                            role="progressbar"
                            style="width: ${region.completionRate}%"
                          >
                            <fmt:formatNumber
                              value="${region.completionRate}"
                              pattern="#.##"
                            />%
                          </div>
                        </div>
                      </td>
                      <td>
                        <span class="badge bg-success">
                          <fmt:formatNumber
                            value="${region.validCorpRegNoCount}"
                            pattern="#,###"
                          />
                        </span>
                      </td>
                      <td>
                        <span class="badge bg-danger">
                          <fmt:formatNumber
                            value="${region.totalCount - region.validCorpRegNoCount}"
                            pattern="#,###"
                          />
                        </span>
                      </td>
                      <td>
                        <a
                          href="/corp/list?city=${region.city}&district=${region.district}"
                          class="btn btn-sm btn-outline-primary"
                        >
                          <i class="bi bi-eye"></i> 상세
                        </a>
                      </td>
                    </tr>
                  </c:forEach>
                </tbody>
              </table>
            </div>
          </div>
        </div>

        <div class="row mt-4">
          <div class="col-md-6">
            <div class="card">
              <div class="card-header">
                <h5 class="card-title mb-0">
                  <i class="bi bi-pie-chart"></i> 상위 10개 지역 분포
                </h5>
              </div>
              <div class="card-body">
                <canvas id="topRegionsChart" height="300"></canvas>
              </div>
            </div>
          </div>
          <div class="col-md-6">
            <div class="card">
              <div class="card-header">
                <h5 class="card-title mb-0">
                  <i class="bi bi-bar-chart"></i> 시/도별 총계
                </h5>
              </div>
              <div class="card-body">
                <canvas id="provinceChart" height="300"></canvas>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <%@ include file="../common/footer.jsp" %> <%@ include
    file="../common/scripts.jsp" %>
    <script>
      const regionData = [
        <c:forEach var="region" items="${regionStats}" varStatus="status">
        {
          name: '${region.city} ${region.district}',
          count: ${region.totalCount},
          percentage: ${region.completionRate}
        }<c:if test="${!status.last}">,</c:if>
        </c:forEach>
      ];

      const topRegionsCtx = document.getElementById('topRegionsChart').getContext('2d');
      new Chart(topRegionsCtx, {
        type: 'pie',
        data: {
          labels: regionData.slice(0, 10).map(r => r.name),
          datasets: [{
            data: regionData.slice(0, 10).map(r => r.count),
            backgroundColor: [
              '#FF6384', '#36A2EB', '#FFCE56', '#4BC0C0', '#9966FF',
              '#FF9F40', '#FF6384', '#C9CBCF', '#4BC0C0', '#FF6384'
            ]
          }]
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          plugins: {
            legend: {
              position: 'bottom'
            }
          }
        }
      });

      const provinceCtx = document.getElementById('provinceChart').getContext('2d');
      const provinceStats = {};

      <c:forEach var="region" items="${regionStats}">
      if (!provinceStats['${region.city}']) {
        provinceStats['${region.city}'] = 0;
      }
      provinceStats['${region.city}'] += ${region.totalCount};
      </c:forEach>

      new Chart(provinceCtx, {
        type: 'bar',
        data: {
          labels: Object.keys(provinceStats),
          datasets: [{
            label: '법인 수',
            data: Object.values(provinceStats),
            backgroundColor: 'rgba(54, 162, 235, 0.8)',
            borderColor: 'rgba(54, 162, 235, 1)',
            borderWidth: 1
          }]
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          scales: {
            y: {
              beginAtZero: true
            }
          }
        }
      });

      function sortTable(columnIndex) {
        const table = document.querySelector('.table');
        const tbody = table.querySelector('tbody');
        const rows = Array.from(tbody.querySelectorAll('tr'));

        rows.sort((a, b) => {
          const aVal = a.cells[columnIndex].textContent.trim();
          const bVal = b.cells[columnIndex].textContent.trim();

          if (columnIndex === 0 || columnIndex === 3 || columnIndex === 4 || columnIndex === 5 || columnIndex === 6) {
            return parseFloat(aVal.replace(/[^0-9.-]/g, '')) - parseFloat(bVal.replace(/[^0-9.-]/g, ''));
          } else {
            return aVal.localeCompare(bVal);
          }
        });

        rows.forEach(row => tbody.appendChild(row));
      }

      function searchRegions() {
        const searchTerm = document.getElementById('regionSearch').value.toLowerCase();
        const rows = document.querySelectorAll('#regionTableBody tr');

        rows.forEach(row => {
          const city = row.cells[1].textContent.toLowerCase();
          const district = row.cells[2].textContent.toLowerCase();

          if (city.includes(searchTerm) || district.includes(searchTerm)) {
            row.style.display = '';
          } else {
            row.style.display = 'none';
          }
        });
      }

      document.getElementById('regionSearch').addEventListener('keypress', function(e) {
        if (e.key === 'Enter') {
          searchRegions();
        }
      });

      function resetFilters() {
        document.getElementById('regionSearch').value = '';
        const rows = document.querySelectorAll('#regionTableBody tr');
        rows.forEach(row => row.style.display = '');
      }

      function filterByStatus(status) {
        console.log('Filter by status:', status);
      }

      function selectRow(row) {
        if (event.target.closest('button') || event.target.closest('a') || event.target.closest('form')) {
          return;
        }

        document.querySelectorAll('tbody tr').forEach(r => r.classList.remove('table-active'));
        row.classList.add('table-active');
      }

      document.addEventListener('DOMContentLoaded', function() {
        document.querySelectorAll('#regionTableBody tr').forEach(row => {
          row.addEventListener('click', function(e) {
            if (
                    e.target.closest("button") ||
                    e.target.closest("a") ||
                    e.target.closest("form")
            ) {
              return;
            }

            document
                    .querySelectorAll("tbody tr")
                    .forEach((r) => r.classList.remove("table-active"));
            this.classList.add("table-active");
          });
        });
      });
    </script>
  </body>
</html>
