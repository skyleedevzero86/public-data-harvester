<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>

<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>지역별 통계 - Antock</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.7.2/font/bootstrap-icons.css" rel="stylesheet">

    <style>
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
            text-align: center;
        }

        .table th:nth-child(5), .table td:nth-child(5) {
            text-align: center;
        }

        .table th:last-child, .table td:last-child {
            text-align: center;
        }

        .table-responsive {
            overflow: visible !important;
        }

        .stats-card {
            background: white;
            border-radius: 8px;
            padding: 20px;
            margin-bottom: 20px;
            box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
            border: 1px solid #dee2e6;
        }

        .stats-number {
            font-size: 2.5rem;
            font-weight: 700;
            color: #007bff;
            margin-bottom: 10px;
        }

        .stats-label {
            color: #6c757d;
            font-size: 1.1rem;
            font-weight: 500;
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

        .page-link:hover {
            background-color: #e9ecef;
        }

        .badge {
            font-size: 0.8em;
        }

        .progress {
            border-radius: 10px;
            overflow: hidden;
        }

        .progress-bar {
            transition: width 0.6s ease;
        }

        .loading-spinner {
            position: fixed;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            background: rgba(255, 255, 255, 0.8);
            display: none;
            justify-content: center;
            align-items: center;
            z-index: 9999;
        }

        .spinner-border {
            width: 3rem;
            height: 3rem;
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
        <h2><i class="bi bi-geo-alt"></i> 지역별 통계</h2>
    </div>

    <div class="filter-container">
        <form id="filterForm" method="GET" action="/region/status">
            <div class="row align-items-center">
                <div class="col-auto">
                    <h6 class="mb-0"><i class="bi bi-filter"></i> 필터</h6>
                </div>
                <div class="col-auto">
                    <select id="city" name="city" class="form-select">
                        <option value="">전체 시/도</option>
                        <c:forEach items="${cities}" var="city">
                            <option value="${city.value}" ${selectedCity == city.value ? 'selected' : ''}>
                                    ${city.value}
                            </option>
                        </c:forEach>
                    </select>
                </div>
                <div class="col-auto">
                    <select id="district" name="district" class="form-select">
                        <option value="">전체 구/군</option>
                        <c:forEach items="${districts}" var="district">
                            <option value="${district.value}" ${selectedDistrict == district.value ? 'selected' : ''}>
                                    ${district.value}
                            </option>
                        </c:forEach>
                    </select>
                </div>
                <div class="col-auto">
                    <select id="size" name="size" class="form-select">
                        <option value="10" ${param.size == '10' ? 'selected' : ''}>10개</option>
                        <option value="20" ${param.size == '20' ? 'selected' : ''}>20개</option>
                        <option value="50" ${param.size == '50' ? 'selected' : ''}>50개</option>
                    </select>
                </div>
                <div class="col-auto">
                    <button type="submit" class="btn btn-primary btn-sm">
                        <i class="bi bi-search"></i> 검색
                    </button>
                </div>
                <div class="col-auto">
                    <button type="button" class="btn btn-outline-secondary btn-sm" onclick="resetForm()">
                        <i class="bi bi-x-lg"></i> 초기화
                    </button>
                </div>
                <div class="col-auto ms-auto">
                    <small class="text-muted">
                        총 ${totalElements}개
                        <c:if test="${not empty selectedCity or not empty selectedDistrict}">
                            (필터링됨)
                        </c:if>
                    </small>
                </div>
            </div>
        </form>
    </div>

    <div class="row mb-4">
        <div class="col-xl-3 col-md-6 col-sm-6">
            <div class="card bg-primary text-white mb-4">
                <div class="card-body">
                    <div class="d-flex justify-content-between align-items-center">
                        <div>
                            <div class="text-white-50 small">전체 법인 수</div>
                            <div class="fs-4 fw-bold">
                                <fmt:formatNumber value="${totalElements}" pattern="#,###"/>
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
            <div class="card bg-success text-white mb-4">
                <div class="card-body">
                    <div class="d-flex justify-content-between align-items-center">
                        <div>
                            <div class="text-white-50 small">전체 페이지</div>
                            <div class="fs-4 fw-bold">${totalPages}</div>
                        </div>
                        <div class="icon-align">
                            <i class="bi bi-file-text stats-icon"></i>
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
                            <div class="text-white-50 small">현재 페이지</div>
                            <div class="fs-4 fw-bold">${currentPage + 1}</div>
                        </div>
                        <div class="icon-align">
                            <i class="bi bi-arrow-right-circle stats-icon"></i>
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
                            <div class="text-white-50 small">현재 표시</div>
                            <div class="fs-4 fw-bold">${regionStats.size()}</div>
                        </div>
                        <div class="icon-align">
                            <i class="bi bi-eye stats-icon"></i>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <div class="card">
        <div class="card-header">
            <div class="d-flex justify-content-between align-items-center">
                <h5 class="mb-0"><i class="bi bi-table"></i> 지역별 통계 데이터</h5>
            </div>
        </div>
        <div class="card-body p-0">
            <c:choose>
                <c:when test="${not empty regionStats}">
                    <div class="table-responsive">
                        <table class="table table-hover mb-0">
                            <thead class="table-dark">
                            <tr>
                                <th width="8%">순위</th>
                                <th width="20%">시/도</th>
                                <th width="20%">구/군</th>
                                <th width="15%">법인 수</th>
                                <th width="25%">비율</th>
                                <th width="12%">상세보기</th>
                            </tr>
                            </thead>
                            <tbody>
                            <c:forEach items="${regionStats}" var="stat" varStatus="status">
                                <tr>
                                    <td>
                                        <span class="badge bg-primary">${(currentPage * size) + status.index + 1}</span>
                                    </td>
                                    <td style="text-align: left;">
                                        <strong>${stat.city}</strong>
                                    </td>
                                    <td style="text-align: left;">${stat.district}</td>
                                    <td>
                                        <span class="badge bg-success fs-6">
                                            <fmt:formatNumber value="${stat.totalCount}" pattern="#,###"/>
                                        </span>
                                    </td>
                                    <td>
                                        <div class="progress" style="height: 20px;">
                                            <c:set var="percentage" value="${(stat.totalCount / totalElements) * 100}"/>
                                            <div class="progress-bar bg-info"
                                                 role="progressbar"
                                                 style="width: ${percentage}%"
                                                 aria-valuenow="${percentage}"
                                                 aria-valuemin="0"
                                                 aria-valuemax="100">
                                                <fmt:formatNumber value="${percentage}" pattern="#.#"/>%
                                            </div>
                                        </div>
                                    </td>
                                    <td>
                                        <a href="/corp/search?city=${stat.city}&district=${stat.district}"
                                           class="btn btn-outline-primary btn-sm">
                                            <i class="bi bi-eye"></i> 상세보기
                                        </a>
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
                                            <a class="page-link" href="javascript:void(0);" onclick="goToPage(${currentPage - 1})">
                                                <i class="bi bi-chevron-left"></i> 이전
                                            </a>
                                        </li>
                                    </c:if>

                                    <c:forEach begin="0" end="${totalPages - 1}" var="pageNum">
                                        <c:if test="${pageNum >= currentPage - 2 && pageNum <= currentPage + 2}">
                                            <li class="page-item ${pageNum == currentPage ? 'active' : ''}">
                                                <a class="page-link" href="javascript:void(0);" onclick="goToPage(${pageNum})">${pageNum + 1}</a>
                                            </li>
                                        </c:if>
                                    </c:forEach>

                                    <c:if test="${currentPage < totalPages - 1}">
                                        <li class="page-item">
                                            <a class="page-link" href="javascript:void(0);" onclick="goToPage(${currentPage + 1})">
                                                다음 <i class="bi bi-chevron-right"></i>
                                            </a>
                                        </li>
                                    </c:if>
                                </ul>
                            </nav>
                        </div>
                    </c:if>
                </c:when>
                <c:otherwise>
                    <div class="text-center py-5">
                        <i class="bi bi-inbox fs-1 text-muted"></i>
                        <c:choose>
                            <c:when test="${not empty selectedCity or not empty selectedDistrict}">
                                <p class="text-muted mt-3">필터 조건에 맞는 지역 데이터가 없습니다.</p>
                                <button type="button" class="btn btn-primary" onclick="resetForm()">
                                    <i class="bi bi-x-lg"></i> 필터 초기화
                                </button>
                            </c:when>
                            <c:otherwise>
                                <p class="text-muted mt-3">등록된 지역 데이터가 없습니다.</p>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>
    </div>
</div>

<div id="loadingSpinner" class="loading-spinner">
    <div class="spinner-border text-primary" role="status">
        <span class="visually-hidden">Loading...</span>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/js/bootstrap.bundle.min.js"></script>
<script>
    function goToPage(pageNum) {
        if (pageNum < 0 || pageNum >= ${totalPages}) {
            return;
        }

        const urlParams = new URLSearchParams(window.location.search);
        urlParams.set('page', pageNum);

        if (!urlParams.has('size')) {
            urlParams.set('size', '10');
        }

        const newUrl = window.location.pathname + '?' + urlParams.toString();
        document.getElementById('loadingSpinner').style.display = 'block';
        window.location.href = newUrl;
    }

    function updateDistricts() {
        const citySelect = document.getElementById('city');
        const districtSelect = document.getElementById('district');
        const selectedCity = citySelect.value;

        districtSelect.innerHTML = '<option value="">전체 구/군</option>';

        if (selectedCity) {
            const cityDistricts = {
                '서울특별시': ['강남구', '강동구', '강북구', '강서구', '관악구', '광진구', '구로구', '금천구', '노원구', '도봉구', '동대문구', '동작구', '마포구', '서대문구', '서초구', '성동구', '성북구', '송파구', '양천구', '영등포구', '용산구', '은평구', '종로구', '중구', '중랑구'],
                '부산광역시': ['강서구', '금정구', '남구', '동구', '북구', '사상구', '사하구', '서구', '수영구', '연제구', '영도구', '중구', '해운대구', '부산진구', '동래구', '기장군'],
                '대구광역시': ['남구', '달서구', '달성군', '동구', '북구', '서구', '수성구', '중구'],
                '인천광역시': ['계양구', '남구', '남동구', '동구', '미추홀구', '부평구', '서구', '연수구', '중구', '강화군', '옹진군'],
                '광주광역시': ['광산구', '남구', '동구', '북구', '서구'],
                '대전광역시': ['대덕구', '동구', '서구', '유성구', '중구'],
                '울산광역시': ['남구', '동구', '북구', '울주군', '중구'],
                '세종특별자치시': ['세종특별자치시'],
                '경기도': ['가평군', '고양시', '과천시', '광명시', '광주시', '구리시', '군포시', '김포시', '남양주시', '동두천시', '부천시', '성남시', '수원시', '시흥시', '안산시', '안성시', '안양시', '양주시', '양평군', '여주시', '연천군', '오산시', '용인시', '의왕시', '의정부시', '이천시', '파주시', '평택시', '포천시', '하남시', '화성시'],
                '강원특별자치도': ['강릉시', '고성군', '동해시', '삼척시', '속초시', '양구군', '양양군', '영월군', '원주시', '인제군', '정선군', '철원군', '춘천시', '태백시', '평창군', '홍천군', '화천군', '횡성군'],
                '충청북도': ['괴산군', '단양군', '보은군', '영동군', '옥천군', '음성군', '제천시', '증평군', '진천군', '청주시', '충주시'],
                '충청남도': ['계룡시', '공주시', '금산군', '논산시', '당진시', '보령시', '부여군', '서산시', '서천군', '아산시', '예산군', '천안시', '청양군', '태안군', '홍성군'],
                '전라북도': ['고창군', '군산시', '김제시', '남원시', '무주군', '부안군', '순창군', '완주군', '익산시', '임실군', '장수군', '전주시', '정읍시', '진안군'],
                '전라남도': ['강진군', '고흥군', '곡성군', '광양시', '구례군', '나주시', '담양군', '목포시', '무안군', '보성군', '순천시', '신안군', '여수시', '영광군', '영암군', '완도군', '장성군', '장흥군', '진도군', '함평군', '해남군', '화순군'],
                '경상북도': ['경산시', '경주시', '고령군', '구미시', '군위군', '김천시', '문경시', '봉화군', '상주시', '성주군', '안동시', '영덕군', '영양군', '영주시', '영천시', '예천군', '울릉군', '울진군', '의성군', '청도군', '청송군', '칠곡군', '포항시'],
                '경상남도': ['거제시', '거창군', '고성군', '김해시', '남해군', '밀양시', '사천시', '산청군', '양산시', '의령군', '진주시', '창녕군', '창원시', '통영시', '하동군', '함안군', '함양군', '합천군'],
                '제주특별자치도': ['제주시', '서귀포시']
            };

            const districts = cityDistricts[selectedCity] || [];
            districts.forEach(district => {
                const option = document.createElement('option');
                option.value = district;
                option.textContent = district;
                districtSelect.appendChild(option);
            });
        }
    }

    function resetForm() {
        document.getElementById('city').value = '';
        document.getElementById('district').value = '';
        document.getElementById('size').value = '10';
        updateDistricts();
    }

    document.getElementById('filterForm').addEventListener('submit', function() {
        document.getElementById('loadingSpinner').style.display = 'block';
    });

    window.addEventListener('load', function() {
        document.getElementById('loadingSpinner').style.display = 'none';
    });

    document.getElementById('city').addEventListener('change', function() {
        updateDistricts();
        document.getElementById('district').value = '';
    });

    document.getElementById('size').addEventListener('change', function() {
        const urlParams = new URLSearchParams(window.location.search);
        urlParams.set('size', this.value);
        urlParams.set('page', '0');

        const newUrl = window.location.pathname + '?' + urlParams.toString();
        window.location.href = newUrl;
    });

    document.addEventListener('keydown', function(e) {
        if (e.key === 'ArrowLeft' && ${currentPage > 0}) {
            goToPage(${currentPage - 1});
        } else if (e.key === 'ArrowRight' && ${currentPage < totalPages - 1}) {
            goToPage(${currentPage + 1});
        }
    });

    document.addEventListener('DOMContentLoaded', function() {
        updateDistricts();
    });
</script>
</body>
</html>