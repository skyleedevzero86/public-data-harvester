<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<!DOCTYPE html>
<html lang="ko">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>지역별 상세 통계</title>
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
  <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.0/font/bootstrap-icons.css">
  <style>
    .dropdown-menu {
      min-width: 200px !important;
      padding: 10px 0;
    }
    .dropdown-menu .dropdown-item {
      padding: 10px 20px;
      font-size: 1rem;
    }
    .dropdown-menu .dropdown-header {
      font-size: 1.05rem;
      font-weight: bold;
      padding: 10px 20px 5px 20px;
    }
    .custom-dropdown .dropdown-menu {
      min-width: 220px !important;
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
    .locked-indicator {
      color: #dc3545;
      font-weight: bold;
    }
    .login-fail-count {
      color: #dc3545;
      font-weight: bold;
      font-size: 0.9em;
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
    .progress-bar-animated {
      animation: progress-bar-stripes 1s linear infinite;
    }
    .completion-high { background-color: #28a745; }
    .completion-medium { background-color: #ffc107; }
    .completion-low { background-color: #dc3545; }
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
    <h2><i class="bi bi-geo-alt-fill"></i> 지역별 상세 통계</h2>
    <div class="d-flex gap-2">
      <span class="badge bg-info">총 ${totalElements}개 지역</span>
      <span class="badge bg-success">${currentPage + 1}/${totalPages} 페이지</span>
    </div>
  </div>

  <div class="filter-container">
    <div class="row align-items-center">
      <div class="col-auto">
        <h6 class="mb-0"><i class="bi bi-filter"></i> 필터</h6>
      </div>
      <div class="col-auto">
        <select class="form-select" name="city" id="citySelect" onchange="updateDistricts()">
          <option value="">전체 시/도</option>
          <c:forEach items="${cities}" var="cityItem">
            <option value="${cityItem}" <c:if test="${cityItem == city}">selected</c:if>>${cityItem}</option>
          </c:forEach>
        </select>
      </div>
      <div class="col-auto">
        <select class="form-select" name="district" id="districtSelect">
          <option value="">전체 구/군</option>
          <c:forEach items="${districts}" var="districtItem">
            <option value="${districtItem}" <c:if test="${districtItem == district}">selected</c:if>>${districtItem}</option>
          </c:forEach>
        </select>
      </div>
      <div class="col-auto">
        <select class="form-select" name="size">
          <option value="10" <c:if test="${size == 10}">selected</c:if>>10개씩</option>
          <option value="20" <c:if test="${size == 20}">selected</c:if>>20개씩</option>
          <option value="50" <c:if test="${size == 50}">selected</c:if>>50개씩</option>
        </select>
      </div>
      <div class="col-auto">
        <button type="submit" class="btn btn-primary">
          <i class="bi bi-search"></i> 검색
        </button>
      </div>
      <div class="col-auto ms-auto">
        <small class="text-muted">
          총 ${totalElements}개 지역
        </small>
      </div>
    </div>
  </div>

  <div class="card">
    <div class="card-header">
      <div class="d-flex justify-content-between align-items-center">
        <h5 class="mb-0"><i class="bi bi-table"></i> 지역별 통계</h5>
      </div>
    </div>
    <div class="card-body p-0">
      <div class="table-responsive">
        <table class="table table-hover mb-0">
          <thead class="table-dark">
          <tr>
            <th width="5%">순위</th>
            <th width="15%">지역</th>
            <th width="15%">총 업체 수</th>
            <th width="15%">법인등록번호</th>
            <th width="15%">행정구역코드</th>
            <th width="20%">완성도</th>
            <th width="15%">상세 보기</th>
          </tr>
          </thead>
          <tbody>
          <c:forEach items="${regionStats}" var="stat" varStatus="status">
            <tr>
              <td>
                <strong>${status.index + 1 + (currentPage * size)}</strong>
              </td>
              <td style="text-align: left;">
                <div class="d-flex align-items-center">
                  <strong>${stat.city}</strong> ${stat.district}
                </div>
              </td>
              <td>
                <span class="badge bg-primary fs-6">${stat.totalCount}</span>
              </td>
              <td>
                <c:choose>
                  <c:when test="${not empty stat.validCorpRegNoCount}">
                    ${stat.validCorpRegNoCount}
                    <small class="text-muted">(<fmt:formatNumber value="${stat.validCorpRegNoCount * 100.0 / stat.totalCount}" pattern="0.0"/>%)</small>
                  </c:when>
                  <c:otherwise>
                    -
                  </c:otherwise>
                </c:choose>
              </td>
              <td>
                <c:choose>
                  <c:when test="${not empty stat.validRegionCdCount}">
                    ${stat.validRegionCdCount}
                    <small class="text-muted">(<fmt:formatNumber value="${stat.validRegionCdCount * 100.0 / stat.totalCount}" pattern="0.0"/>%)</small>
                  </c:when>
                  <c:otherwise>
                    -
                  </c:otherwise>
                </c:choose>
              </td>
              <td>
                <c:set var="completionRate" value="${stat.completionRate}" />
                <div class="progress" style="height: 25px;">
                  <div class="progress-bar ${completionRate >= 80 ? 'completion-high' : completionRate >= 50 ? 'completion-medium' : 'completion-low'}"
                       role="progressbar"
                       style="width: ${completionRate}%"
                       aria-valuenow="${completionRate}"
                       aria-valuemin="0"
                       aria-valuemax="100">
                    <strong><fmt:formatNumber value="${completionRate}" pattern="0.0"/>%</strong>
                  </div>
                </div>
              </td>
              <td>
                <button class="btn btn-sm btn-outline-primary"
                        onclick="viewDetails('${stat.city}', '${stat.district}')">
                  <i class="bi bi-eye"></i> 상세 보기
                </button>
              </td>
            </tr>
          </c:forEach>
          </tbody>
        </table>
      </div>

      <c:if test="${totalPages > 1}">
        <div class="card-footer">
          <nav aria-label="Page navigation">
            <ul class="pagination justify-content-center mb-0">
              <c:if test="${hasPrevious}">
                <li class="page-item">
                  <a class="page-link" href="?page=${currentPage - 1}&size=${size}&city=${city}&district=${district}">
                    <i class="bi bi-chevron-left"></i> 이전
                  </a>
                </li>
              </c:if>

              <c:forEach var="i" begin="0" end="${totalPages - 1}">
                <c:if test="${i >= currentPage - 2 && i <= currentPage + 2}">
                  <li class="page-item ${i == currentPage ? 'active' : ''}">
                    <a class="page-link" href="?page=${i}&size=${size}&city=${city}&district=${district}">${i + 1}</a>
                  </li>
                </c:if>
              </c:forEach>

              <c:if test="${hasNext}">
                <li class="page-item">
                  <a class="page-link" href="?page=${currentPage + 1}&size=${size}&city=${city}&district=${district}">
                    다음 <i class="bi bi-chevron-right"></i>
                  </a>
                </li>
              </c:if>
            </ul>
          </nav>
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

<div class="modal fade" id="detailModal" tabindex="-1" aria-labelledby="detailModalLabel" aria-hidden="true">
  <div class="modal-dialog modal-xl">
    <div class="modal-content">
      <div class="modal-header">
        <h5 class="modal-title" id="detailModalLabel">
          <i class="bi bi-building"></i> <span id="modalRegionTitle">상세 법인 목록</span>
        </h5>
        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close" onclick="cleanupModalBackdrop()"></button>
      </div>
      <div class="modal-body">
        <div id="detailContent">
          <div class="text-center py-4">
            <div class="spinner-border text-primary" role="status">
              <span class="visually-hidden">Loading...</span>
            </div>
            <p class="mt-3">데이터를 불러오는 중...</p>
          </div>
        </div>
      </div>
      <div class="modal-footer">
        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal" onclick="cleanupModalBackdrop()">닫기</button>
        <button type="button" class="btn btn-success" onclick="exportToExcel()">
          <i class="bi bi-download"></i> Excel 다운로드
        </button>
      </div>
    </div>
  </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
<script>
  let currentCity = '';
  let currentDistrict = '';
  let currentPage = 0;
  const pageSize = 18;

  function viewDetails(city, district, page = 0) {
    currentCity = city;
    currentDistrict = district;
    currentPage = page;

    document.getElementById('modalRegionTitle').textContent = city + ' ' + district + ' - 상세 법인 목록';

    document.getElementById('detailContent').innerHTML = '<div class="text-center py-4">' +
            '<div class="spinner-border text-primary" role="status">' +
            '<span class="visually-hidden">Loading...</span>' +
            '</div>' +
            '<p class="mt-3">데이터를 불러오는 중...</p>' +
            '</div>';

    const modalElement = document.getElementById('detailModal');
    const modal = new bootstrap.Modal(modalElement);

    modalElement.addEventListener('hidden.bs.modal', function () {
      const backdrops = document.querySelectorAll('.modal-backdrop');
      backdrops.forEach(backdrop => backdrop.remove());

      document.body.classList.remove('modal-open');

      document.body.style.overflow = '';
      document.body.style.paddingRight = '';

      if (window.location.hash) {
        history.replaceState(null, null, window.location.pathname + window.location.search);
      }
    });

    modal.show();

    fetchRegionDetails(city, district, page);
  }

  function fetchRegionDetails(city, district, page = 0) {
    const params = new URLSearchParams();
    params.append('city', city);
    params.append('district', district);
    params.append('page', page);
    params.append('size', pageSize);

    const queryString = params.toString();
    const url = '/api/v1/region-stats/details?' + queryString;

    fetch(url)
            .then(response => {
              if (!response.ok) {
                throw new Error('Network response was not ok');
              }
              return response.json();
            })
            .then(result => {
              if (result.success && result.data && result.data.content && result.data.content.length > 0) {
                displayRegionDetails(result.data.content, result.data.totalPages);
              } else {
                document.getElementById('detailContent').innerHTML = '<div class="text-center py-4">' +
                        '<i class="bi bi-inbox display-1 text-muted"></i>' +
                        '<h5 class="mt-3 text-muted">등록된 법인이 없습니다</h5>' +
                        '<p class="text-muted">' + city + ' ' + district + ' 지역에 등록된 법인이 없습니다.</p>' +
                        '</div>';
              }
            })
            .catch(error => {
              document.getElementById('detailContent').innerHTML = '<div class="alert alert-danger" role="alert">' +
                      '<i class="bi bi-exclamation-triangle-fill"></i> 데이터를 불러오는 중 오류가 발생했습니다.' +
                      '<br><small>' + error.message + '</small>' +
                      '</div>';
            });
  }

  function displayRegionDetails(corpList, totalPages = 1) {
    let html = '<div class="table-responsive">' +
            '<table class="table table-striped table-hover">' +
            '<thead>' +
            '<tr>' +
            '<th>번호</th>' +
            '<th>법인명</th>' +
            '<th>법인등록번호</th>' +
            '<th>사업자등록번호</th>' +
            '<th>대표자명</th>' +
            '<th>설립일자</th>' +
            '<th>주소</th>' +
            '<th>상태</th>' +
            '</tr>' +
            '</thead>' +
            '<tbody>';

    corpList.forEach(function(corp, index) {
      const bizNm = corp.bizNm || '-';
      const corpRegNo = corp.corpRegNo || '-';
      const bizNo = corp.bizNo || '-';

      const representativeName = corp.sellerId || '-';
      let establishmentDate = '-';
      if (corp.createDate) {
        const dateObj = new Date(corp.createDate);
        establishmentDate = dateObj.getFullYear() + '-' +
                String(dateObj.getMonth() + 1).padStart(2, '0') + '-' +
                String(dateObj.getDate()).padStart(2, '0');
      }
      const address = corp.roadNmAddr || corp.jibunAddr || ((corp.siNm && corp.sggNm) ? (corp.siNm + ' ' + corp.sggNm) : '-');
      const operationStatus = '정상운영';
      const statusClass = 'bg-success';

      html += '<tr>' +
              '<td>' + (currentPage * pageSize + index + 1) + '</td>' +
              '<td>' + bizNm + '</td>' +
              '<td>' + corpRegNo + '</td>' +
              '<td>' + bizNo + '</td>' +
              '<td>' + representativeName + '</td>' +
              '<td>' + establishmentDate + '</td>' +
              '<td>' + address + '</td>' +
              '<td>' +
              '<span class="badge ' + statusClass + '">' +
              operationStatus +
              '</span>' +
              '</td>' +
              '</tr>';
    });

    html += '</tbody>' +
            '</table>' +
            '</div>';

    if (totalPages > 1) {
      html += '<div class="mt-3 d-flex justify-content-between align-items-center">' +
              '<span class="badge bg-info">' + (currentPage + 1) + '/' + totalPages + ' 페이지</span>' +
              '<nav aria-label="Page navigation">' +
              '<ul class="pagination pagination-sm mb-0">';

      if (currentPage > 0) {
        html += '<li class="page-item">' +
                '<a class="page-link" href="#" onclick="changePage(' + (currentPage - 1) + ')">이전</a>' +
                '</li>';
      } else {
        html += '<li class="page-item disabled">' +
                '<span class="page-link">이전</span>' +
                '</li>';
      }

      for (let i = 0; i < totalPages; i++) {
        if (i === currentPage) {
          html += '<li class="page-item active">' +
                  '<span class="page-link">' + (i + 1) + '</span>' +
                  '</li>';
        } else {
          html += '<li class="page-item">' +
                  '<a class="page-link" href="#" onclick="changePage(' + i + ')">' + (i + 1) + '</a>' +
                  '</li>';
        }
      }

      if (currentPage < totalPages - 1) {
        html += '<li class="page-item">' +
                '<a class="page-link" href="#" onclick="changePage(' + (currentPage + 1) + ')">다음</a>' +
                '</li>';
      } else {
        html += '<li class="page-item disabled">' +
                '<span class="page-link">다음</span>' +
                '</li>';
      }

      html += '</ul>' +
              '</nav>' +
              '</div>';
    }

    document.getElementById('detailContent').innerHTML = html;
  }

  function exportToExcel() {
    if (!currentCity || !currentDistrict) {
      alert('지역을 선택해주세요.');
      return;
    }

    const params = new URLSearchParams();
    params.append('city', currentCity);
    params.append('district', currentDistrict);

    window.location.href = '/api/v1/region-stats/export?' + params.toString();
  }

  function changePage(page) {
    if (currentCity && currentDistrict) {
      viewDetails(currentCity, currentDistrict, page);
    }
  }

  function updateDistricts() {
    const citySelect = document.getElementById('citySelect');
    const districtSelect = document.getElementById('districtSelect');
    const selectedCity = citySelect.value;

    if (!selectedCity) {
      districtSelect.innerHTML = '<option value="">전체 구/군</option>';
      return;
    }

    fetch('/api/v1/region-stats/districts?city=' + encodeURIComponent(selectedCity))
            .then(response => response.json())
            .then(result => {
              if (result.success) {
                let options = '<option value="">전체 구/군</option>';
                result.data.forEach(function(district) {
                  options += '<option value="' + district + '">' + district + '</option>';
                });
                districtSelect.innerHTML = options;
              }
            })
            .catch(error => {});
  }

  document.addEventListener('DOMContentLoaded', function() {
    cleanupModalBackdrop();

    document.querySelectorAll("tbody tr").forEach((row) => {
      row.addEventListener("click", function (e) {
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

  function cleanupModalBackdrop() {
    const backdrops = document.querySelectorAll('.modal-backdrop');
    backdrops.forEach(backdrop => backdrop.remove());

    document.body.classList.remove('modal-open');

    document.body.style.overflow = '';
    document.body.style.paddingRight = '';

    if (window.location.hash) {
      history.replaceState(null, null, window.location.pathname + window.location.search);
    }
  }
</script>
</body>
</html>