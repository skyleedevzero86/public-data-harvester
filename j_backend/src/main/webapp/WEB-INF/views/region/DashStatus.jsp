<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html>
<head>
  <meta charset="UTF-8">
  <meta name="_csrf" content="${_csrf.token}"/>
  <meta name="_csrf_header" content="${_csrf.headerName}"/>
  <title>지역별 통계 대시보드 - 통신판매자사업관리시스템</title>
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css" rel="stylesheet">
  <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.7.2/font/bootstrap-icons.css" rel="stylesheet">
  <script src="https://cdn.jsdelivr.net/npm/chart.js@3.9.1/dist/chart.min.js"></script>
  <style>
    .table th,
    .table td {
      vertical-align: middle;
      text-align: center;
    }
    .table th:first-child,
    .table td:first-child {
      text-align: center;
    }
    .table th:nth-child(2),
    .table td:nth-child(2) {
      text-align: left;
    }
    .table th:nth-child(3),
    .table td:nth-child(3) {
      text-align: center;
    }
    .table th:nth-child(4),
    .table td:nth-child(4) {
      text-align: center;
    }
    .table th:nth-child(5),
    .table td:nth-child(5) {
      text-align: center;
    }
    .table th:last-child,
    .table td:last-child {
      text-align: center;
    }
    .table-responsive {
      overflow: visible !important;
    }
    .badge {
      font-size: 0.8em;
    }
    .filter-container {
      background: #f8f9fa;
      border: 1px solid #dee2e6;
      border-radius: 8px;
      padding: 15px;
      margin-bottom: 20px;
    }
    .filter-active {
      border-color: #007bff !important;
      box-shadow: 0 0 0 0.2rem rgba(0, 123, 255, 0.25) !important;
    }
    .status-cell {
      white-space: nowrap;
    }
    .btn-group .dropdown-menu {
      border: 1px solid #dee2e6;
      box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
    }
    .page-link:hover {
      background-color: #e9ecef;
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
    .chart-container {
      position: relative;
      height: 400px;
      margin-bottom: 20px;
    }
    .chart-container.small {
      height: 300px;
    }
    .completion-bar {
      width: 100px;
      height: 8px;
      background-color: #e9ecef;
      border-radius: 4px;
      overflow: hidden;
      margin-right: 10px;
    }
    .completion-fill {
      height: 100%;
      background: linear-gradient(90deg, #ff6b6b, #ffa500, #32cd32);
      transition: width 0.3s ease;
    }
    .region-name {
      font-weight: 600;
      color: #2c3e50;
    }
    .count-badge {
      background: linear-gradient(135deg, #667eea, #764ba2);
      color: white;
      padding: 4px 12px;
      border-radius: 20px;
      font-size: 0.85rem;
      font-weight: 600;
    }
    .text-success {
      color: #28a745 !important;
    }
    .text-warning {
      color: #ffc107 !important;
    }
    .text-danger {
      color: #dc3545 !important;
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
    .pagination {
      margin-bottom: 0;
    }
    .page-link {
      color: #007bff;
      border: 1px solid #dee2e6;
      padding: 0.5rem 0.75rem;
      margin: 0 2px;
      border-radius: 0.375rem;
      transition: all 0.15s ease-in-out;
    }
    .page-link:hover {
      color: #0056b3;
      background-color: #e9ecef;
      border-color: #adb5bd;
    }
    .page-item.active .page-link {
      background-color: #007bff;
      border-color: #007bff;
      color: white;
    }
    .page-item.disabled .page-link {
      color: #6c757d;
      background-color: #fff;
      border-color: #dee2e6;
      cursor: not-allowed;
    }
    .filter-card {
      background: linear-gradient(135deg, #f8f9fa 0%, #e9ecef 100%);
      border: 1px solid #dee2e6;
      border-radius: 0.5rem;
      box-shadow: 0 2px 4px rgba(0,0,0,0.1);
    }
    .stats-header {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      color: white;
      border-radius: 0.5rem 0.5rem 0 0;
    }
    .stats-header .card-title {
      color: white;
      margin-bottom: 0;
    }
    .page-info {
      background: #f8f9fa;
      border: 1px solid #dee2e6;
      border-radius: 0.375rem;
      padding: 0.5rem 1rem;
      font-size: 0.875rem;
      color: #6c757d;
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
      <a class="nav-link" href="/region/status">
        <i class="bi bi-graph-up"></i> 지역별 통계
      </a>
      <a class="nav-link" href="/web/files">
        <i class="bi bi-folder"></i> 파일 관리
      </a>
      <a class="nav-link" href="/members/logout">
        <i class="bi bi-box-arrow-right"></i> 로그아웃
      </a>
    </div>
  </div>
</nav>
<div class="container-fluid mt-4">
  <div class="d-flex justify-content-between align-items-center mb-4">
    <h2><i class="bi bi-graph-up"></i> 지역별 통계 대시보드</h2>
    <div>
      <a href="/region/status" class="btn btn-primary">
        <i class="bi bi-table"></i> 상세 통계 보기
      </a>
    </div>
  </div>
  <div class="card filter-card mb-4">
    <div class="card-body">
      <form method="get" action="/region/detail" class="row g-3">
        <div class="col-md-4">
          <label for="cityFilter" class="form-label">시/도</label>
          <select class="form-select" id="cityFilter" name="city" onchange="updateDistricts()">
            <option value="">전체</option>
            <c:forEach var="cityOption" items="${cities}">
              <option value="${cityOption}" ${city == cityOption ? 'selected' : ''}>${cityOption}</option>
            </c:forEach>
          </select>
        </div>
        <div class="col-md-4">
          <label for="districtFilter" class="form-label">구/군</label>
          <select class="form-select" id="districtFilter" name="district">
            <option value="">전체</option>
            <c:forEach var="districtOption" items="${districts}">
              <option value="${districtOption}" ${district == districtOption ? 'selected' : ''}>${districtOption}</option>
            </c:forEach>
          </select>
        </div>
        <div class="col-md-4 d-flex align-items-end">
          <button type="submit" class="btn btn-primary me-2">
            <i class="bi bi-search"></i> 검색
          </button>
          <a href="/region/detail" class="btn btn-outline-secondary">
            <i class="bi bi-arrow-clockwise"></i> 초기화
          </a>
        </div>
        <input type="hidden" name="page" value="0">
        <input type="hidden" name="size" value="${size}">
      </form>
    </div>
  </div>
  <div class="row mb-4">
    <div class="col-xl-6 col-lg-12">
      <div class="card">
        <div class="card-header">
          <h5 class="card-title mb-0">
            <i class="bi bi-bar-chart"></i> 업체수 기준 상위 10개 지역
          </h5>
        </div>
        <div class="card-body">
          <div class="chart-container">
            <canvas id="barChart"></canvas>
          </div>
        </div>
      </div>
    </div>
    <div class="col-xl-6 col-lg-12">
      <div class="card">
        <div class="card-header">
          <h5 class="card-title mb-0">
            <i class="bi bi-pie-chart"></i> 지역별 완성도 (상위 10)
          </h5>
        </div>
        <div class="card-body">
          <div class="chart-container small">
            <canvas id="donutChart"></canvas>
          </div>
        </div>
      </div>
    </div>
  </div>
  <div class="card">
    <div class="card-header stats-header">
      <div class="d-flex justify-content-between align-items-center">
        <h5 class="card-title mb-0">
          <i class="bi bi-table"></i> 지역별 상세 통계
        </h5>
        <div class="d-flex align-items-center">
                        <span class="text-muted me-3">
                            총 ${totalElements}개 지역 (${currentPage + 1}/${totalPages} 페이지)
                        </span>
          <div class="btn-group" role="group">
            <select class="form-select form-select-sm" id="pageSizeSelect" onchange="changePageSize()">
              <option value="10" ${size == 10 ? 'selected' : ''}>10개씩</option>
              <option value="20" ${size == 20 ? 'selected' : ''}>20개씩</option>
              <option value="50" ${size == 50 ? 'selected' : ''}>50개씩</option>
              <option value="100" ${size == 100 ? 'selected' : ''}>100개씩</option>
            </select>
          </div>
        </div>
      </div>
    </div>
    <div class="card-body">
      <div class="table-responsive">
        <table class="table table-hover">
          <thead class="table-dark">
          <tr>
            <th>순위</th>
            <th>지역</th>
            <th>총 업체 수</th>
            <th>법인등록번호</th>
            <th>행정구역코드</th>
            <th>완성도</th>
            <th>상세 보기</th>
          </tr>
          </thead>
          <tbody>
          <c:forEach var="stat" items="${regionStats}" varStatus="loop">
            <tr>
              <td>
                <span class="count-badge">${(currentPage * size) + loop.index + 1}</span>
              </td>
              <td>
                <div class="region-name">
                    ${stat.city} ${stat.district}
                </div>
              </td>
              <td>
                <div class="fw-bold">${stat.formattedCount}</div>
              </td>
              <td>
                <div class="fw-bold">${stat.validCorpRegNoCount}</div>
                <small class="text-muted">
                  (<fmt:formatNumber value="${stat.totalCount > 0 ? (stat.validCorpRegNoCount * 100.0 / stat.totalCount) : 0}" pattern="0.0"/>%)
                </small>
              </td>
              <td>
                <div class="fw-bold">${stat.validRegionCdCount}</div>
                <small class="text-muted">
                  (<fmt:formatNumber value="${stat.totalCount > 0 ? (stat.validRegionCdCount * 100.0 / stat.totalCount) : 0}" pattern="0.0"/>%)
                </small>
              </td>
              <td>
                <div class="d-flex align-items-center justify-content-center">
                  <div class="completion-bar">
                    <div class="completion-fill" style="width:${stat.completionRate}%"></div>
                  </div>
                  <span class="fw-bold ms-2 ${stat.completionRate >= 80 ? 'text-success' : stat.completionRate >= 50 ? 'text-warning' : 'text-danger'}">
                                                ${stat.completionRate}%
                                            </span>
                </div>
              </td>
              <td>
                <button class="btn btn-sm btn-outline-primary" onclick="viewDetails('${stat.city}', '${stat.district}')">
                  <i class="bi bi-eye"></i> 상세 보기
                </button>
              </td>
            </tr>
          </c:forEach>
          </tbody>
        </table>
      </div>
      <c:if test="${totalPages > 1}">
        <nav aria-label="페이지 네비게이션" class="mt-4">
          <ul class="pagination justify-content-center">
            <li class="page-item ${isFirst ? 'disabled' : ''}">
              <a class="page-link" href="?page=0&size=${size}&city=${city}&district=${district}" aria-label="첫 페이지">
                <span aria-hidden="true">&laquo;&laquo;</span>
              </a>
            </li>
            <li class="page-item ${hasPrevious ? '' : 'disabled'}">
              <a class="page-link" href="?page=${currentPage - 1}&size=${size}&city=${city}&district=${district}" aria-label="이전 페이지">
                <span aria-hidden="true">&laquo;</span>
              </a>
            </li>
            <c:set var="startPage" value="${Math.max(0, currentPage - 2)}"/>
            <c:set var="endPage" value="${Math.min(totalPages - 1, currentPage + 2)}"/>
            <c:forEach var="pageNum" begin="${startPage}" end="${endPage}">
              <li class="page-item ${pageNum == currentPage ? 'active' : ''}">
                <a class="page-link" href="?page=${pageNum}&size=${size}&city=${city}&district=${district}">
                    ${pageNum + 1}
                </a>
              </li>
            </c:forEach>
            <li class="page-item ${hasNext ? '' : 'disabled'}">
              <a class="page-link" href="?page=${currentPage + 1}&size=${size}&city=${city}&district=${district}" aria-label="다음 페이지">
                <span aria-hidden="true">&raquo;</span>
              </a>
            </li>
            <li class="page-item ${isLast ? 'disabled' : ''}">
              <a class="page-link" href="?page=${totalPages - 1}&size=${size}&city=${city}&district=${district}" aria-label="마지막 페이지">
                <span aria-hidden="true">&raquo;&raquo;</span>
              </a>
            </li>
          </ul>
        </nav>
        <div class="text-center mt-3">
          <div class="page-info d-inline-block">
            <i class="bi bi-info-circle me-1"></i>
              ${currentPage * size + 1}-${Math.min((currentPage + 1) * size, totalElements)} / ${totalElements}개 지역 표시
          </div>
        </div>
      </c:if>
    </div>
  </div>
</div>
<footer class="footer">
  <div class="footer-container">
    <div class="row">
      <div class="col-md-6">
        <div class="footer-logo">
          <div class="festival-number">public-data-harvester</div>
          <div class="main-title">
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
<div class="modal fade" id="detailModal" tabindex="-1" aria-labelledby="detailModalLabel" aria-hidden="true">
  <div class="modal-dialog modal-xl">
    <div class="modal-content">
      <div class="modal-header">
        <h5 class="modal-title" id="detailModalLabel">
          <i class="bi bi-building"></i> <span id="modalRegionTitle">지역 상세 법인 목록</span>
        </h5>
        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
      </div>
      <div class="modal-body">
        <div id="detailContent">
          <div class="text-center">
            <div class="spinner-border text-primary" role="status">
              <span class="visually-hidden">로딩 중...</span>
            </div>
            <p class="mt-2">데이터를 불러오는 중입니다...</p>
          </div>
        </div>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">닫기</button>
        <button type="button" class="btn btn-success" onclick="exportToExcel()">
          <i class="bi bi-download"></i> Excel 다운로드
        </button>
      </div>
    </div>
  </div>
</div>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/js/bootstrap.bundle.min.js"></script>
<script>
  function changePageSize() {
    const pageSize = document.getElementById('pageSizeSelect').value;
    const urlParams = new URLSearchParams(window.location.search);
    urlParams.set('size', pageSize);
    urlParams.set('page', '0');
    window.location.href = '?' + urlParams.toString();
  }
  function updateDistricts() {
    const citySelect = document.getElementById('cityFilter');
    const districtSelect = document.getElementById('districtFilter');
    const selectedCity = citySelect.value;
    districtSelect.innerHTML = '<option value="">전체</option>';
    if (selectedCity) {
      const encodedCity = encodeURIComponent(selectedCity);
      fetch(`/api/v1/region-stats/districts?city=${encodedCity}`)
              .then(response => response.json())
              .then(data => {
                if (data.success && data.data) {
                  data.data.forEach(district => {
                    const option = document.createElement('option');
                    option.value = district;
                    option.textContent = district;
                    districtSelect.appendChild(option);
                  });
                }
              })
              .catch(error => {});
    }
  }
  function viewDetails(city, district) {
    console.log('viewDetails called with city:', city, 'district:', district);
    const modal = new bootstrap.Modal(document.getElementById('detailModal'));
    const modalTitle = document.getElementById('modalRegionTitle');
    const detailContent = document.getElementById('detailContent');
    modalTitle.textContent = `${city} ${district} - 상세 법인 목록`;
    detailContent.innerHTML = `
                <div class="text-center">
                    <div class="spinner-border text-primary" role="status">
                        <span class="visually-hidden">로딩 중...</span>
                    </div>
                    <p class="mt-2">데이터를 불러오는 중입니다...</p>
                </div>
            `;
    modal.show();
    fetchRegionDetails(city, district);
  }
  function fetchRegionDetails(city, district) {
    console.log('fetchRegionDetails called with city:', city, 'district:', district);
    const encodedCity = encodeURIComponent(city);
    const encodedDistrict = encodeURIComponent(district);
    console.log('Encoded parameters - city:', encodedCity, 'district:', encodedDistrict);
    
    const url = `/api/v1/region-stats/details?city=${encodedCity}&district=${encodedDistrict}`;
    console.log('Fetching URL:', url);
    
    fetch(url)
            .then(response => {
              console.log('Response status:', response.status);
              return response.json();
            })
            .then(data => {
              console.log('API Response:', data);
              if (data.success && data.data) {
                displayRegionDetails(data.data, city, district);
              } else {
                displayError('데이터를 가져오는 중 오류가 발생했습니다.');
              }
            })
            .catch(error => {
              console.error('Fetch error:', error);
              displayError('데이터를 가져오는 중 오류가 발생했습니다.');
            });
  }
  function displayRegionDetails(corpList, city, district) {
    const detailContent = document.getElementById('detailContent');
    if (corpList.length === 0) {
      detailContent.innerHTML = `
                    <div class="text-center py-4">
                        <i class="bi bi-inbox display-1 text-muted"></i>
                        <h5 class="mt-3 text-muted">등록된 법인이 없습니다</h5>
                        <p class="text-muted">${city} ${district} 지역에 등록된 법인이 없습니다.</p>
                    </div>
                `;
      return;
    }
    let tableHtml = `
                <div class="mb-3">
                    <h6 class="text-muted">총 ${corpList.length}개의 법인이 등록되어 있습니다.</h6>
                </div>
                <div class="table-responsive">
                    <table class="table table-hover table-striped">
                        <thead class="table-dark">
                            <tr>
                                <th>번호</th>
                                <th>법인명</th>
                                <th>법인등록번호</th>
                                <th>판매자ID</th>
                                <th>사업자번호</th>
                                <th>주소</th>
                                <th>지역코드</th>
                                <th>등록자</th>
                                <th>등록일시</th>
                            </tr>
                        </thead>
                        <tbody>
            `;
    corpList.forEach((corp, index) => {
      const id = corp.id || '-';
      const bizNm = corp.bizNm || '-';
      const corpRegNo = corp.corpRegNo || '-';
      const sellerId = corp.sellerId || '-';
      const bizNo = corp.bizNo || '-';
      const regionCd = corp.regionCd || '-';
      const siNm = corp.siNm || '-';
      const sggNm = corp.sggNm || '-';
      const username = corp.username || '-';
      const description = corp.description || '-';
      let formattedDate = '-';
      if (corp.createDate) {
        try {
          const createDate = new Date(corp.createDate);
          if (!isNaN(createDate.getTime())) {
            formattedDate = createDate.toLocaleString('ko-KR', {
              year: 'numeric',
              month: '2-digit',
              day: '2-digit',
              hour: '2-digit',
              minute: '2-digit',
              second: '2-digit'
            });
          }
        } catch (e) {
          console.error('Date parsing error:', e);
        }
      }
      
      tableHtml += `
                    <tr>
                        <td>${index + 1}</td>
                        <td class="fw-bold">${bizNm}</td>
                        <td>${corpRegNo}</td>
                        <td>${sellerId}</td>
                        <td>${bizNo}</td>
                        <td class="text-start">${siNm} ${sggNm}</td>
                        <td>${regionCd}</td>
                        <td>${username}</td>
                        <td>${formattedDate}</td>
                    </tr>
                `;
    });
    tableHtml += `
                        </tbody>
                    </table>
                </div>
            `;
    detailContent.innerHTML = tableHtml;
  }
  function displayError(message) {
    const detailContent = document.getElementById('detailContent');
    detailContent.innerHTML = `
                <div class="alert alert-danger" role="alert">
                    <i class="bi bi-exclamation-triangle"></i>
                    ${message}
                </div>
            `;
  }
  function exportToExcel() {
    const urlParams = new URLSearchParams(window.location.search);
    const city = urlParams.get('city') || '';
    const district = urlParams.get('district') || '';
    let exportUrl = '/api/v1/region-stats/export';
    if (city) {
      exportUrl += `?city=${encodeURIComponent(city)}`;
      if (district) {
        exportUrl += `&district=${encodeURIComponent(district)}`;
      }
    }
    window.open(exportUrl, '_blank');
  }
  const regionStats = [
    <c:forEach var="stat" items="${regionStats}" varStatus="loop">
    {
      label: "${stat.city} ${stat.district}",
      total: ${stat.totalCount},
      completion: ${stat.completionRate}
    }<c:if test="${!loop.last}">,</c:if>
    </c:forEach>
  ];
  const validRegionStats = regionStats.filter(stat =>
          stat &&
          typeof stat.completion === 'number' &&
          !isNaN(stat.completion) &&
          stat.completion >= 0
  );
  const top10ByCount = validRegionStats.slice(0, 10);
  const barCtx = document.getElementById('barChart').getContext('2d');
  new Chart(barCtx, {
    type: 'bar',
    data: {
      labels: top10ByCount.map(r => r.label),
      datasets: [{
        label: '총 업체 수',
        data: top10ByCount.map(r => r.total),
        backgroundColor: 'rgba(13, 110, 253, 0.8)',
        borderColor: 'rgba(13, 110, 253, 1)',
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
  const top10ByCompletion = validRegionStats
          .sort((a, b) => b.completion - a.completion)
          .slice(0, 10);
  const donutCtx = document.getElementById('donutChart').getContext('2d');
  new Chart(donutCtx, {
    type: 'doughnut',
    data: {
      labels: top10ByCompletion.map(r => r.label),
      datasets: [{
        label: '완성도',
        data: top10ByCompletion.map(r => r.completion),
        backgroundColor: [
          '#0d6efd', '#198754', '#fd7e14', '#dc3545', '#6f42c1',
          '#20c997', '#ffc107', '#e83e8c', '#6c757d', '#0dcaf0'
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
              let value = context.parsed;
              if (value === undefined || value === null) {
                value = context.raw;
              }
              if (value === undefined || value === null) {
                value = context.dataset.data[context.dataIndex];
              }
              if (typeof value === 'number' && !isNaN(value)) {
                return `${label}: ${value.toFixed(1)}%`;
              } else {
                const dataValue = top10ByCompletion[context.dataIndex]?.completion;
                if (typeof dataValue === 'number' && !isNaN(dataValue)) {
                  return `${label}: ${dataValue.toFixed(1)}%`;
                }
                return `${label}: 0.0%`;
              }
            }
          }
        }
      }
    }
  });
</script>
</body>
</html>