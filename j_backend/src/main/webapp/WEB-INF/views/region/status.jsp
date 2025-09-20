<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="_csrf" content="${_csrf.token}"/>
    <meta name="_csrf_header" content="${_csrf.headerName}"/>
    <title>지역별 상세 통계 - 통신판매자사업관리시스템</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.7.2/font/bootstrap-icons.css" rel="stylesheet">
    <style>
        .quick-menu {
            position: fixed;
            top: 50%;
            right: 20px;
            transform: translateY(-50%);
            z-index: 1000;
            background: white;
            border: 1px solid #dee2e6;
            border-radius: 10px;
            box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
            padding: 15px;
            max-height: 200px;
            overflow-y: auto;
            display: none;
            min-width: 150px;
        }
        .quick-menu-item {
            display: block;
            padding: 8px 12px;
            margin: 2px 0;
            text-decoration: none;
            color: #495057;
            border-radius: 5px;
            transition: background-color 0.2s;
            font-size: 14px;
            cursor: pointer;
        }
        .quick-menu-item:hover {
            background-color: #f8f9fa;
            color: #0d6efd;
        }
        .quick-menu-header {
            font-weight: bold;
            color: #495057;
            margin-bottom: 10px;
            padding-bottom: 5px;
            border-bottom: 1px solid #dee2e6;
        }
        .pagination-wrapper {
            display: flex;
            justify-content: center;
            align-items: center;
            margin: 20px 0;
        }
        .page-info {
            margin: 0 20px;
            color: #6c757d;
        }
        .stats-card {
            transition: transform 0.2s;
        }
        .stats-card:hover {
            transform: translateY(-2px);
        }
        .completion-rate {
            font-size: 1.2em;
            font-weight: bold;
        }
        .completion-rate.high {
            color: #28a745;
        }
        .completion-rate.medium {
            color: #ffc107;
        }
        .completion-rate.low {
            color: #dc3545;
        }
        .page-size-selector {
            display: flex;
            align-items: center;
            gap: 10px;
            margin-bottom: 20px;
        }
        .jump-to-page {
            display: flex;
            align-items: center;
            gap: 10px;
            margin-top: 10px;
        }
        .jump-input {
            width: 80px;
        }
        .modal-header {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            border-radius: 15px 15px 0 0;
            padding: 1rem 1.5rem;
        }
        .modal-content {
            border-radius: 15px;
            border: none;
            max-height: 80vh;
            overflow: hidden;
        }
        .modal-body {
            padding: 1.5rem;
            max-height: 60vh;
            overflow-y: auto;
        }
        .detail-table th {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            border: none;
            font-weight: 600;
            padding: 0.75rem;
            font-size: 0.9rem;
        }
        .detail-table td {
            padding: 0.75rem;
            vertical-align: middle;
            border-color: #e9ecef;
            font-size: 0.9rem;
        }
        .detail-table tbody tr:hover {
            background-color: #f8f9fa;
        }
        .loading-spinner {
            display: flex;
            justify-content: center;
            align-items: center;
            height: 200px;
        }
        .modal-dialog {
            max-width: 90vw;
            width: 1200px;
        }
        .table-responsive {
            max-height: 400px;
            overflow-y: auto;
        }
    </style>
</head>
<body>
<div class="container-fluid">
    <div class="row">
        <div class="col-12">
            <div class="d-flex justify-content-between align-items-center mb-4">
                <h2><i class="bi bi-graph-up"></i> 지역별 상세 통계</h2>
                <div class="d-flex gap-2">
                    <button class="btn btn-outline-primary" onclick="exportToExcel()">
                        <i class="bi bi-download"></i> Excel 다운로드
                    </button>
                    <button class="btn btn-outline-secondary" onclick="refreshData()">
                        <i class="bi bi-arrow-clockwise"></i> 새로고침
                    </button>
                </div>
            </div>
            <div class="row mb-4">
                <div class="col-md-3">
                    <div class="card stats-card">
                        <div class="card-body text-center">
                            <h5 class="card-title text-primary">총 지역 수</h5>
                            <h3 class="text-primary">${totalElements}</h3>
                        </div>
                    </div>
                </div>
                <div class="col-md-3">
                    <div class="card stats-card">
                        <div class="card-body text-center">
                            <h5 class="card-title text-success">현재 페이지</h5>
                            <h3 class="text-success">${currentPage + 1}</h3>
                        </div>
                    </div>
                </div>
                <div class="col-md-3">
                    <div class="card stats-card">
                        <div class="card-body text-center">
                            <h5 class="card-title text-info">총 페이지</h5>
                            <h3 class="text-info">${totalPages}</h3>
                        </div>
                    </div>
                </div>
                <div class="col-md-3">
                    <div class="card stats-card">
                        <div class="card-body text-center">
                            <h5 class="card-title text-warning">페이지당 항목</h5>
                            <h3 class="text-warning">${size}</h3>
                        </div>
                    </div>
                </div>
            </div>
            <div class="page-size-selector">
                <label for="pageSizeSelect">페이지당 항목 수:</label>
                <select class="form-select" id="pageSizeSelect" style="width: auto;" onchange="changePageSize()">
                    <option value="10" ${size == 10 ? 'selected' : ''}>10개</option>
                    <option value="20" ${size == 20 ? 'selected' : ''}>20개</option>
                    <option value="50" ${size == 50 ? 'selected' : ''}>50개</option>
                    <option value="100" ${size == 100 ? 'selected' : ''}>100개</option>
                </select>
            </div>
            <div class="card">
                <div class="card-header">
                    <div class="row align-items-center">
                        <div class="col-md-6">
                            <h5 class="mb-0">지역별 완성도 통계</h5>
                        </div>
                        <div class="col-md-6">
                            <div class="d-flex gap-2">
                                <select class="form-select" id="cityFilter" onchange="filterByCity()">
                                    <option value="">전체 시/도</option>
                                    <c:forEach var="city" items="${cities}">
                                        <option value="${city}" ${city == param.city ? 'selected' : ''}>${city}</option>
                                    </c:forEach>
                                </select>
                                <select class="form-select" id="districtFilter" onchange="filterByDistrict()">
                                    <option value="">전체 구/군</option>
                                    <c:forEach var="district" items="${districts}">
                                        <option value="${district}" ${district == param.district ? 'selected' : ''}>${district}</option>
                                    </c:forEach>
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
                                <th>총 법인 수</th>
                                <th>유효한 법인등록번호</th>
                                <th>유효한 지역코드</th>
                                <th>완성도</th>
                                <th>상세보기</th>
                            </tr>
                            </thead>
                            <tbody>
                            <c:choose>
                                <c:when test="${not empty error}">
                                    <tr>
                                        <td colspan="7" class="text-center text-danger">
                                            <i class="bi bi-exclamation-triangle"></i> ${error}
                                        </td>
                                    </tr>
                                </c:when>
                                <c:when test="${empty regionStats}">
                                    <tr>
                                        <td colspan="7" class="text-center text-muted">
                                            <i class="bi bi-info-circle"></i> 데이터가 없습니다.
                                        </td>
                                    </tr>
                                </c:when>
                                <c:otherwise>
                                    <c:forEach var="stat" items="${regionStats}" varStatus="status">
                                        <tr>
                                            <td>
                                                <span class="badge bg-primary">${(currentPage * size) + status.index + 1}</span>
                                            </td>
                                            <td>
                                                <strong>${stat.city}</strong><br>
                                                <small class="text-muted">${stat.district}</small>
                                            </td>
                                            <td>
                                                <span class="fw-bold">${stat.formattedCount}</span>
                                            </td>
                                            <td>
                                                <span class="text-success">${stat.validCorpRegNoCount}</span>
                                            </td>
                                            <td>
                                                <span class="text-info">${stat.validRegionCdCount}</span>
                                            </td>
                                            <td>
                                                <div class="completion-rate ${stat.completionRate >= 80 ? 'high' : stat.completionRate >= 60 ? 'medium' : 'low'}">
                                                        ${stat.completionRate}%
                                                </div>
                                                <div class="progress mt-1" style="height: 6px;">
                                                    <div class="progress-bar ${stat.completionRate >= 80 ? 'bg-success' : stat.completionRate >= 60 ? 'bg-warning' : 'bg-danger'}"
                                                         style="width: ${stat.completionRate}%"></div>
                                                </div>
                                            </td>
                                            <td>
                                                <button class="btn btn-sm btn-outline-primary" onclick="viewDetails('${stat.city}', '${stat.district}')">
                                                    <i class="bi bi-eye"></i> 보기
                                                </button>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </c:otherwise>
                            </c:choose>
                            </tbody>
                        </table>
                    </div>
                    <div class="pagination-wrapper">
                        <nav aria-label="페이지 네비게이션">
                            <ul class="pagination">
                                <li class="page-item ${isFirst ? 'disabled' : ''}">
                                    <a class="page-link" href="?page=0&size=${size}&city=${param.city}&district=${param.district}">
                                        <i class="bi bi-chevron-double-left"></i>
                                    </a>
                                </li>
                                <li class="page-item ${hasPrevious ? '' : 'disabled'}">
                                    <a class="page-link" href="?page=${currentPage - 1}&size=${size}&city=${param.city}&district=${param.district}">
                                        <i class="bi bi-chevron-left"></i>
                                    </a>
                                </li>
                                <c:set var="startPage" value="${currentPage - 2}" />
                                <c:if test="${startPage < 0}">
                                    <c:set var="startPage" value="0" />
                                </c:if>
                                <c:set var="endPage" value="${currentPage + 2}" />
                                <c:if test="${endPage > totalPages - 1}">
                                    <c:set var="endPage" value="${totalPages - 1}" />
                                </c:if>
                                <c:forEach begin="${startPage}" end="${endPage}" var="pageNum">
                                    <li class="page-item ${pageNum == currentPage ? 'active' : ''}">
                                        <a class="page-link" href="?page=${pageNum}&size=${size}&city=${param.city}&district=${param.district}">
                                                ${pageNum + 1}
                                        </a>
                                    </li>
                                </c:forEach>
                                <li class="page-item ${hasNext ? '' : 'disabled'}">
                                    <a class="page-link" href="?page=${currentPage + 1}&size=${size}&city=${param.city}&district=${param.district}">
                                        <i class="bi bi-chevron-right"></i>
                                    </a>
                                </li>
                                <li class="page-item ${isLast ? 'disabled' : ''}">
                                    <a class="page-link" href="?page=${totalPages - 1}&size=${size}&city=${param.city}&district=${param.district}">
                                        <i class="bi bi-chevron-double-right"></i>
                                    </a>
                                </li>
                            </ul>
                        </nav>
                    </div>
                    <div class="page-info">
                        <c:set var="rangeStart" value="${(currentPage * size) + 1}" />
                        <c:set var="rangeEnd" value="${(currentPage + 1) * size}" />
                        <c:if test="${rangeEnd > totalElements}">
                            <c:set var="rangeEnd" value="${totalElements}" />
                        </c:if>
                        <span>총 ${totalElements}개 항목 중 ${rangeStart}-${rangeEnd}번째 표시</span>
                    </div>
                    <div class="jump-to-page">
                        <label for="jumpPageInput">페이지로 이동:</label>
                        <input type="number" class="form-control jump-input" id="jumpPageInput"
                               min="1" max="${totalPages}" value="${currentPage + 1}">
                        <button class="btn btn-sm btn-outline-primary" onclick="jumpToPage()">
                            <i class="bi bi-arrow-right"></i> 이동
                        </button>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>
<div class="quick-menu" id="quickMenu">
    <div class="quick-menu-header">퀵 메뉴</div>
    <a href="#" class="quick-menu-item" onclick="scrollToTop()">
        <i class="bi bi-arrow-up"></i> 맨 위로
    </a>
</div>
<div class="modal fade" id="detailModal" tabindex="-1" aria-labelledby="detailModalLabel" aria-hidden="true">
    <div class="modal-dialog modal-lg">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title" id="detailModalLabel">지역별 상세 법인 목록</h5>
                <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="Close"></button>
            </div>
            <div class="modal-body">
                <div id="detailContent">
                    <div class="loading-spinner">
                        <div class="spinner-border text-primary" role="status">
                            <span class="visually-hidden">로딩중...</span>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/js/bootstrap.bundle.min.js"></script>
<script>
    let quickMenuVisible = false;
    let lastScrollTop = 0;
    const urlParams = new URLSearchParams(window.location.search);
    const currentCity = urlParams.get('city') || '';
    const currentDistrict = urlParams.get('district') || '';
    const currentSize = urlParams.get('size') || '${size}';
    const totalPages = ${totalPages};
    window.addEventListener('scroll', function() {
        const scrollTop = window.pageYOffset || document.documentElement.scrollTop;
        const windowHeight = window.innerHeight;
        const documentHeight = document.documentElement.scrollHeight;
        const scrollPercent = (scrollTop / (documentHeight - windowHeight)) * 100;
        if (scrollPercent > 10 && !quickMenuVisible) {
            document.getElementById('quickMenu').style.display = 'block';
            quickMenuVisible = true;
        } else if (scrollPercent <= 10 && quickMenuVisible) {
            document.getElementById('quickMenu').style.display = 'none';
            quickMenuVisible = false;
        }
        lastScrollTop = scrollTop;
    });
    function scrollToTop() {
        window.scrollTo({ top: 0, behavior: 'smooth' });
    }
    function changePageSize() {
        const newSize = document.getElementById('pageSizeSelect').value;
        window.location.href = '?page=0&size=' + newSize + '&city=' + currentCity + '&district=' + currentDistrict;
    }
    function jumpToPage() {
        const pageInput = document.getElementById('jumpPageInput');
        if (pageInput) {
            const page = parseInt(pageInput.value);
            if (!isNaN(page) && page >= 1 && page <= totalPages) {
                window.location.href = '?page=' + (page - 1) + '&size=' + currentSize + '&city=' + currentCity + '&district=' + currentDistrict;
            }
        }
    }
    function filterByCity() {
        const cityFilter = document.getElementById('cityFilter');
        const districtFilter = document.getElementById('districtFilter');
        if (cityFilter && districtFilter) {
            const city = cityFilter.value;
            const district = districtFilter.value;
            window.location.href = '?page=0&size=' + currentSize + '&city=' + city + '&district=' + district;
        }
    }
    function filterByDistrict() {
        const cityFilter = document.getElementById('cityFilter');
        const districtFilter = document.getElementById('districtFilter');
        if (cityFilter && districtFilter) {
            const city = cityFilter.value;
            const district = districtFilter.value;
            window.location.href = '?page=0&size=' + currentSize + '&city=' + city + '&district=' + district;
        }
    }
    function viewDetails(city, district) {
        const modal = new bootstrap.Modal(document.getElementById('detailModal'));
        const detailContent = document.getElementById('detailContent');
        detailContent.innerHTML = `
                <div class="loading-spinner">
                    <div class="spinner-border text-primary" role="status">
                        <span class="visually-hidden">로딩중...</span>
                    </div>
                </div>
            `;
        document.getElementById('detailModalLabel').textContent = `${city} ${district} - 상세 법인 목록`;
        modal.show();
        const encodedCity = encodeURIComponent(city);
        const encodedDistrict = encodeURIComponent(district);
        fetch(`/api/v1/region-stats/details?city=${encodedCity}&district=${encodedDistrict}`)
            .then(response => {
                if (!response.ok) {
                    throw new Error('HTTP error! status: ' + response.status);
                }
                return response.json();
            })
            .then(data => {
                if (data && data.success && data.data) {
                    const corps = data.data;
                    let tableHtml = `
                            <div class="d-flex justify-content-between align-items-center mb-3">
                                <h6 class="mb-0">${city} ${district} - 총 ${corps.length}개 법인</h6>
                                <small class="text-muted">데이터 로드 완료</small>
                            </div>
                            <div class="table-responsive">
                                <table class="table table-hover detail-table">
                                    <thead>
                                        <tr>
                                            <th>ID</th>
                                            <th>법인명</th>
                                            <th>사업자번호</th>
                                            <th>법인등록번호</th>
                                            <th>지역코드</th>
                                            <th>판매자ID</th>
                                            <th>등록자</th>
                                            <th>등록일시</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                        `;
                    if (!corps || corps.length === 0) {
                        tableHtml += `
                                <tr>
                                    <td colspan="8" class="text-center text-muted">
                                        <i class="bi bi-info-circle"></i> 해당 지역에 등록된 법인이 없습니다.
                                    </td>
                                </tr>
                            `;
                    } else {
                        corps.forEach((corp, index) => {
                            const id = corp.id || '-';
                            const bizNm = corp.bizNm || '-';
                            const bizNo = corp.bizNo || '-';
                            const corpRegNo = corp.corpRegNo || '-';
                            const regionCd = corp.regionCd || '-';
                            const sellerId = corp.sellerId || '-';
                            const username = corp.username || '-';
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
                                } catch (e) {}
                            }
                            const rowHtml = `
                                    <tr>
                                        <td>${id}</td>
                                        <td>${bizNm}</td>
                                        <td>${bizNo}</td>
                                        <td>${corpRegNo}</td>
                                        <td>${regionCd}</td>
                                        <td>${sellerId}</td>
                                        <td>${username}</td>
                                        <td>${formattedDate}</td>
                                    </tr>
                                `;
                            tableHtml += rowHtml;
                        });
                    }
                    tableHtml += `
                                    </tbody>
                                </table>
                            </div>
                        `;
                    detailContent.innerHTML = tableHtml;
                } else {
                    detailContent.innerHTML = `
                            <div class="alert alert-danger" role="alert">
                                <i class="bi bi-exclamation-triangle"></i> 데이터를 불러오는 중 오류가 발생했습니다.
                                <br><small>${data.message || '알 수 없는 오류'}</small>
                            </div>
                        `;
                }
            })
            .catch(error => {
                detailContent.innerHTML = `
                        <div class="alert alert-danger" role="alert">
                            <i class="bi bi-exclamation-triangle"></i> 데이터를 불러오는 중 오류가 발생했습니다.
                            <br><small>${error.message || '알 수 없는 오류'}</small>
                        </div>
                    `;
            });
    }
    function exportToExcel() {
        const cityFilter = document.getElementById('cityFilter');
        const districtFilter = document.getElementById('districtFilter');
        const city = cityFilter ? cityFilter.value : '';
        const district = districtFilter ? districtFilter.value : '';
        let url = '/api/v1/region-stats/export';
        const params = new URLSearchParams();
        if (city) params.append('city', city);
        if (district) params.append('district', district);
        if (params.toString()) {
            url += '?' + params.toString();
        }
        const link = document.createElement('a');
        link.href = url;
        link.download = '';
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
    }
    function refreshData() {
        window.location.reload();
    }
    function fetchDistricts(city) {
        const districtFilter = document.getElementById('districtFilter');
        if (!districtFilter) return;
        if (!city) {
            districtFilter.innerHTML = '<option value="">전체 구/군</option>';
            return;
        }
        const encodedCity = encodeURIComponent(city);
        fetch('/api/v1/region-stats/districts?city=' + encodedCity)
            .then(response => {
                if (!response.ok) {
                    throw new Error('HTTP error! status: ' + response.status);
                }
                return response.json();
            })
            .then(data => {
                districtFilter.innerHTML = '<option value="">전체 구/군</option>';
                if (data && data.length > 0) {
                    data.forEach(district => {
                        const option = document.createElement('option');
                        option.value = district;
                        option.textContent = district;
                        districtFilter.appendChild(option);
                    });
                }
            })
            .catch(error => {
                districtFilter.innerHTML = '<option value="">전체 구/군</option>';
            });
    }
    document.addEventListener('DOMContentLoaded', function() {
        const cityFilter = document.getElementById('cityFilter');
        const districtFilter = document.getElementById('districtFilter');
        const jumpPageInput = document.getElementById('jumpPageInput');
        if (cityFilter) {
            cityFilter.addEventListener('change', function() {
                if (this.value) {
                    fetchDistricts(this.value);
                } else if (districtFilter) {
                    districtFilter.innerHTML = '<option value="">전체 구/군</option>';
                }
            });
        }
        if (jumpPageInput) {
            jumpPageInput.addEventListener('keypress', function(e) {
                if (e.key === 'Enter') {
                    jumpToPage();
                }
            });
        }
    });
</script>
</body>
</html>