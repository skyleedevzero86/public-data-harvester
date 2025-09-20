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
            color: #212529 !important;
            background-color: #ffffff !important;
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

    <c:if test="${not empty successMessage}">
        <div class="alert alert-success alert-dismissible fade show" role="alert">
            <i class="bi bi-check-circle"></i> ${successMessage}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
    </c:if>

    <c:if test="${not empty errorMessage}">
        <div class="alert alert-danger alert-dismissible fade show" role="alert">
            <i class="bi bi-exclamation-triangle"></i> ${errorMessage}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
    </c:if>

    <div class="filter-container">
        <div class="row align-items-center">
            <div class="col-auto">
                <h6 class="mb-0"><i class="bi bi-filter"></i> 필터</h6>
            </div>
            <div class="col-auto">
                <select class="form-select ${not empty param.city ? 'filter-active' : ''}" id="cityFilter" onchange="filterByCity()">
                    <option value="">전체 시/도</option>
                    <c:forEach var="city" items="${cities}">
                        <option value="${city}" ${city == param.city ? 'selected' : ''}>${city}</option>
                    </c:forEach>
                </select>
            </div>
            <div class="col-auto">
                <select class="form-select ${not empty param.district ? 'filter-active' : ''}" id="districtFilter" onchange="filterByDistrict()">
                    <option value="">전체 구/군</option>
                    <c:forEach var="district" items="${districts}">
                        <option value="${district}" ${district == param.district ? 'selected' : ''}>${district}</option>
                    </c:forEach>
                </select>
            </div>
            <div class="col-auto">
                <c:if test="${not empty param.city or not empty param.district}">
                    <button type="button" class="btn btn-outline-secondary btn-sm" onclick="clearFilters()">
                        <i class="bi bi-x-lg"></i> 필터 초기화
                    </button>
                </c:if>
            </div>
            <div class="col-auto ms-auto">
                <small class="text-muted">
                    총 ${totalElements}개 지역
                    <c:if test="${not empty param.city or not empty param.district}">
                        (필터링됨)
                    </c:if>
                </small>
            </div>
        </div>
    </div>

    <div class="row mb-4 justify-content-end">
        <div class="col-xl-2 col-md-4 col-sm-6">
            <div class="card bg-primary text-white mb-4">
                <div class="card-body">
                    <div class="d-flex justify-content-between align-items-center">
                        <div>
                            <div class="text-white-50 small">총 지역 수</div>
                            <div class="fs-4 fw-bold">${totalElements}</div>
                        </div>
                        <div class="icon-align">
                            <i class="bi bi-geo-alt-fill stats-icon"></i>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <div class="col-xl-2 col-md-4 col-sm-6">
            <div class="card bg-success text-white mb-4">
                <div class="card-body">
                    <div class="d-flex justify-content-between align-items-center">
                        <div>
                            <div class="text-white-50 small">현재 페이지</div>
                            <div class="fs-4 fw-bold">${currentPage + 1}</div>
                        </div>
                        <div class="icon-align">
                            <i class="bi bi-file-earmark-text-fill stats-icon"></i>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <div class="col-xl-2 col-md-4 col-sm-6">
            <div class="card bg-info text-white mb-4">
                <div class="card-body">
                    <div class="d-flex justify-content-between align-items-center">
                        <div>
                            <div class="text-white-50 small">총 페이지</div>
                            <div class="fs-4 fw-bold">${totalPages}</div>
                        </div>
                        <div class="icon-align">
                            <i class="bi bi-collection-fill stats-icon"></i>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <div class="col-xl-2 col-md-4 col-sm-6">
            <div class="card bg-warning text-white mb-4">
                <div class="card-body">
                    <div class="d-flex justify-content-between align-items-center">
                        <div>
                            <div class="text-white-50 small">페이지당 항목</div>
                            <div class="fs-4 fw-bold">${size}</div>
                        </div>
                        <div class="icon-align">
                            <i class="bi bi-list-ol stats-icon"></i>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <div class="card">
        <div class="card-header">
            <div class="d-flex justify-content-between align-items-center">
                <h5 class="mb-0"><i class="bi bi-table"></i> 지역별 완성도 통계</h5>
                <div class="d-flex gap-2">
                    <select class="form-select" id="pageSizeSelect" style="width: auto;" onchange="changePageSize()">
                        <option value="10" ${size == 10 ? 'selected' : ''}>10개</option>
                        <option value="20" ${size == 20 ? 'selected' : ''}>20개</option>
                        <option value="50" ${size == 50 ? 'selected' : ''}>50개</option>
                        <option value="100" ${size == 100 ? 'selected' : ''}>100개</option>
                    </select>
                </div>
            </div>
        </div>
        <div class="card-body p-0">
            <c:choose>
                <c:when test="${not empty error}">
                    <div class="text-center py-5">
                        <i class="bi bi-exclamation-triangle fs-1 text-danger"></i>
                        <p class="text-danger mt-3">${error}</p>
                    </div>
                </c:when>
                <c:when test="${empty regionStats}">
                    <div class="text-center py-5">
                        <i class="bi bi-info-circle fs-1 text-muted"></i>
                        <p class="text-muted mt-3">데이터가 없습니다.</p>
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="table-responsive">
                        <table class="table table-hover mb-0">
                            <thead class="table-dark">
                            <tr>
                                <th width="8%">순위</th>
                                <th width="20%">지역</th>
                                <th width="12%">총 법인 수</th>
                                <th width="12%">유효한 법인등록번호</th>
                                <th width="12%">유효한 지역코드</th>
                                <th width="12%">완성도</th>
                                <th width="12%">상세보기</th>
                            </tr>
                            </thead>
                            <tbody>
                            <c:forEach var="stat" items="${regionStats}" varStatus="status">
                                <tr>
                                    <td>
                                        <span class="badge bg-primary">${(currentPage * size) + status.index + 1}</span>
                                    </td>
                                    <td style="text-align: left;">
                                        <div class="d-flex align-items-center">
                                            <strong>${stat.city}</strong>
                                        </div>
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
                                        <div class="btn-group btn-group-sm" role="group">
                                            <button class="btn btn-outline-primary btn-sm" onclick="viewDetails('${stat.city}', '${stat.district}')">
                                                <i class="bi bi-eye"></i> 보기
                                            </button>
                                        </div>
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
                                    <c:if test="${currentPage > 0}">
                                        <li class="page-item">
                                            <a class="page-link" href="?page=0&size=${size}&city=${param.city}&district=${param.district}">
                                                <i class="bi bi-chevron-double-left"></i>
                                            </a>
                                        </li>
                                        <li class="page-item">
                                            <a class="page-link" href="?page=${currentPage - 1}&size=${size}&city=${param.city}&district=${param.district}">
                                                <i class="bi bi-chevron-left"></i>
                                            </a>
                                        </li>
                                    </c:if>

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

                                    <c:if test="${currentPage < totalPages - 1}">
                                        <li class="page-item">
                                            <a class="page-link" href="?page=${currentPage + 1}&size=${size}&city=${param.city}&district=${param.district}">
                                                <i class="bi bi-chevron-right"></i>
                                            </a>
                                        </li>
                                        <li class="page-item">
                                            <a class="page-link" href="?page=${totalPages - 1}&size=${size}&city=${param.city}&district=${param.district}">
                                                <i class="bi bi-chevron-double-right"></i>
                                            </a>
                                        </li>
                                    </c:if>
                                </ul>
                            </nav>
                        </div>
                    </c:if>
                </c:otherwise>
            </c:choose>
        </div>
    </div>
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
    const urlParams = new URLSearchParams(window.location.search);
    const currentCity = urlParams.get('city') || '';
    const currentDistrict = urlParams.get('district') || '';
    const currentSize = urlParams.get('size') || '${size}';
    const totalPages = ${totalPages};

    function changePageSize() {
        const newSize = document.getElementById('pageSizeSelect').value;
        window.location.href = '?page=0&size=' + newSize + '&city=' + currentCity + '&district=' + currentDistrict;
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

    function clearFilters() {
        const url = new URL(window.location);
        url.searchParams.delete('city');
        url.searchParams.delete('district');
        url.searchParams.delete('page');
        window.location.href = url.toString();
    }

    function viewDetails(city, district) {
        const modal = new bootstrap.Modal(document.getElementById('detailModal'));
        const detailContent = document.getElementById('detailContent');
        detailContent.innerHTML = '<div class="loading-spinner"><div class="spinner-border text-primary" role="status"><span class="visually-hidden">로딩중...</span></div></div>';
        document.getElementById('detailModalLabel').textContent = city + ' ' + district + ' - 상세 법인 목록';
        modal.show();
        
        const encodedCity = encodeURIComponent(city);
        const encodedDistrict = encodeURIComponent(district);
        const url = '/api/v1/region-stats/details?city=' + encodedCity + '&district=' + encodedDistrict;
        
        fetch(url)
            .then(response => {
                if (!response.ok) {
                    throw new Error('HTTP error! status: ' + response.status);
                }
                return response.json();
            })
            .then(data => {
                if (data && data.success && data.data) {
                    const corps = data.data;
                    let tableHtml = '<div class="d-flex justify-content-between align-items-center mb-3">' +
                            '<h6 class="mb-0">' + city + ' ' + district + ' - 총 ' + corps.length + '개 법인</h6>' +
                            '<small class="text-muted">데이터 로드 완료</small>' +
                        '</div>' +
                        '<div class="table-responsive">' +
                            '<table class="table table-hover detail-table">' +
                                '<thead>' +
                                    '<tr>' +
                                        '<th>ID</th>' +
                                        '<th>법인명</th>' +
                                        '<th>사업자번호</th>' +
                                        '<th>법인등록번호</th>' +
                                        '<th>지역코드</th>' +
                                        '<th>판매자ID</th>' +
                                        '<th>등록자</th>' +
                                        '<th>등록일시</th>' +
                                    '</tr>' +
                                '</thead>' +
                                '<tbody>';
                    if (!corps || corps.length === 0) {
                        tableHtml += '<tr>' +
                                '<td colspan="8" class="text-center text-muted">' +
                                    '<i class="bi bi-info-circle"></i> 해당 지역에 등록된 법인이 없습니다.' +
                                '</td>' +
                            '</tr>';
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
                                } catch (e) {
                                    console.error('Date parsing error:', e);
                                }
                            }
                            const rowHtml = '<tr>' +
                                    '<td>' + id + '</td>' +
                                    '<td>' + bizNm + '</td>' +
                                    '<td>' + bizNo + '</td>' +
                                    '<td>' + corpRegNo + '</td>' +
                                    '<td>' + regionCd + '</td>' +
                                    '<td>' + sellerId + '</td>' +
                                    '<td>' + username + '</td>' +
                                    '<td>' + formattedDate + '</td>' +
                                '</tr>';
                            tableHtml += rowHtml;
                        });
                    }
                    tableHtml += '</tbody>' +
                            '</table>' +
                        '</div>';
                    detailContent.innerHTML = tableHtml;
                } else {
                    detailContent.innerHTML = '<div class="alert alert-danger" role="alert">' +
                            '<i class="bi bi-exclamation-triangle"></i> 데이터를 불러오는 중 오류가 발생했습니다.' +
                            '<br><small>' + (data ? data.message : 'API 응답이 올바르지 않습니다') + '</small>' +
                        '</div>';
                }
            })
            .catch(error => {
                detailContent.innerHTML = '<div class="alert alert-danger" role="alert">' +
                        '<i class="bi bi-exclamation-triangle"></i> 데이터를 불러오는 중 오류가 발생했습니다.' +
                        '<br><small>' + (error.message || '알 수 없는 오류') + '</small>' +
                    '</div>';
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

        if (cityFilter && cityFilter.value) {
            cityFilter.classList.add('filter-active');
        }

        if (districtFilter && districtFilter.value) {
            districtFilter.classList.add('filter-active');
        }

        if (cityFilter) {
            cityFilter.addEventListener('change', function() {
                if (this.value) {
                    fetchDistricts(this.value);
                } else if (districtFilter) {
                    districtFilter.innerHTML = '<option value="">전체 구/군</option>';
                }
            });
        }

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
</script>
</body>
</html>