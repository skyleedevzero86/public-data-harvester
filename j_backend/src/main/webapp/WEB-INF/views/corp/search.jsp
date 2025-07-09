<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="_csrf" content="${_csrf.token}"/>
    <meta name="_csrf_header" content="${_csrf.headerName}"/>
    <title>법인 정보 검색 - Antock System</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.7.2/font/bootstrap-icons.css" rel="stylesheet">

    <style>
        .search-container {
            background: #f8f9fa;
            padding: 25px;
            border-radius: 10px;
            margin-bottom: 25px;
            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
        }

        .search-title {
            color: #495057;
            margin-bottom: 20px;
            font-weight: 600;
        }

        .search-form .form-label {
            font-weight: 500;
            color: #495057;
        }

        .search-form .form-control {
            border-radius: 6px;
            border: 1px solid #dee2e6;
        }

        .search-form .form-control:focus {
            border-color: #0d6efd;
            box-shadow: 0 0 0 0.2rem rgba(13, 110, 253, 0.25);
        }

        .search-buttons {
            display: flex;
            gap: 10px;
            justify-content: center;
            margin-top: 20px;
        }

        .results-container {
            background: white;
            border-radius: 10px;
            box-shadow: 0 2px 10px rgba(0,0,0,0.1);
        }

        .results-header {
            background: #007bff;
            color: white;
            padding: 15px 20px;
            border-radius: 10px 10px 0 0;
            margin: 0;
        }

        .results-summary {
            background: #e3f2fd;
            padding: 15px 20px;
            border-bottom: 1px solid #dee2e6;
            margin: 0;
        }

        .table th, .table td {
            vertical-align: middle;
            padding: 12px 8px;
        }

        .table th {
            background-color: #f8f9fa;
            font-weight: 600;
            color: #495057;
            border-bottom: 2px solid #dee2e6;
        }

        .table-hover tbody tr:hover {
            background-color: #f5f5f5;
        }

        .corp-name {
            font-weight: 600;
            color: #0d6efd;
            cursor: pointer;
        }

        .corp-name:hover {
            text-decoration: underline;
        }

        .bizno-format {
            font-family: 'Courier New', monospace;
            color: #6c757d;
        }

        .address-info {
            color: #6c757d;
            font-size: 0.9em;
        }

        .no-results {
            text-align: center;
            padding: 60px 20px;
            color: #6c757d;
        }

        .no-results i {
            font-size: 4rem;
            margin-bottom: 20px;
            opacity: 0.5;
        }

        .pagination-container {
            padding: 20px;
            background: #f8f9fa;
            border-radius: 0 0 10px 10px;
        }

        .btn-search {
            background: linear-gradient(45deg, #007bff, #0056b3);
            border: none;
            padding: 10px 30px;
            font-weight: 600;
        }

        .btn-reset {
            background: linear-gradient(45deg, #6c757d, #495057);
            border: none;
            padding: 10px 30px;
            font-weight: 600;
        }

        .stats-card {
            background: linear-gradient(45deg, #28a745, #20c997);
            color: white;
            padding: 15px;
            border-radius: 8px;
            text-align: center;
            margin-bottom: 15px;
        }

        .stats-number {
            font-size: 1.5rem;
            font-weight: bold;
        }

        .stats-label {
            font-size: 0.9rem;
            opacity: 0.9;
        }
    </style>
</head>
<body>

<nav class="navbar navbar-expand-lg navbar-dark bg-dark">
    <div class="container">
        <a class="navbar-brand" href="/">
            <i class="bi bi-shield-check"></i> Antock System
        </a>
        <div class="navbar-nav ms-auto">
            <a class="nav-link" href="/members/profile">
                <i class="bi bi-person-circle"></i> 내 프로필
            </a>
            <a class="nav-link" href="/members/admin/pending">
                <i class="bi bi-clock"></i> 승인 대기
            </a>
            <a class="nav-link" href="/members/logout">
                <i class="bi bi-box-arrow-right"></i> 로그아웃
            </a>
        </div>
    </div>
</nav>

<div class="container-fluid mt-4">

    <div class="d-flex justify-content-between align-items-center mb-4">
        <h2><i class="bi bi-building"></i> 법인 정보 검색</h2>
        <div>
            <a href="/api/v1/corp/export?${pageContext.request.queryString}" class="btn btn-success">
                <i class="bi bi-file-earmark-excel"></i> Excel 다운로드
            </a>
        </div>
    </div>

    <c:if test="${not empty message}">
        <div class="alert alert-info alert-dismissible fade show" role="alert">
            <i class="bi bi-info-circle"></i> ${message}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
    </c:if>

    <div class="search-container">
        <h5 class="search-title">
            <i class="bi bi-search"></i> 검색 조건
        </h5>

        <form method="GET" action="/corp/search" class="search-form">
            <div class="row g-3">
                <div class="col-md-6">
                    <label for="bizNm" class="form-label">법인명</label>
                    <input type="text" class="form-control" id="bizNm" name="bizNm"
                           value="${searchRequest.bizNm}" placeholder="법인명을 입력하세요">
                </div>
                <div class="col-md-6">
                    <label for="bizNo" class="form-label">사업자번호</label>
                    <input type="text" class="form-control" id="bizNo" name="bizNo"
                           value="${searchRequest.bizNo}" placeholder="000-00-00000">
                </div>
                <div class="col-md-6">
                    <label for="sellerId" class="form-label">판매자ID</label>
                    <input type="text" class="form-control" id="sellerId" name="sellerId"
                           value="${searchRequest.sellerId}" placeholder="판매자ID를 입력하세요">
                </div>
                <div class="col-md-6">
                    <label for="corpRegNo" class="form-label">법인등록번호</label>
                    <input type="text" class="form-control" id="corpRegNo" name="corpRegNo"
                           value="${searchRequest.corpRegNo}" placeholder="법인등록번호를 입력하세요">
                </div>
                <div class="col-md-6">
                    <label for="city" class="form-label">시/도</label>
                    <select class="form-select" id="city" name="city" onchange="loadDistricts()">
                        <option value="">전체</option>
                        <c:forEach var="cityOption" items="${cities}">
                            <option value="${cityOption}" ${searchRequest.city == cityOption ? 'selected' : ''}>
                                    ${cityOption}
                            </option>
                        </c:forEach>
                    </select>
                </div>
                <div class="col-md-6">
                    <label for="district" class="form-label">구/군</label>
                    <select class="form-select" id="district" name="district">
                        <option value="">전체</option>
                        <c:forEach var="districtOption" items="${districts}">
                            <option value="${districtOption}" ${searchRequest.district == districtOption ? 'selected' : ''}>
                                    ${districtOption}
                            </option>
                        </c:forEach>
                    </select>
                </div>
            </div>

            <div class="search-buttons">
                <button type="submit" class="btn btn-primary btn-search">
                    <i class="bi bi-search"></i> 검색
                </button>
                <button type="button" class="btn btn-secondary btn-reset" onclick="resetForm()">
                    <i class="bi bi-arrow-clockwise"></i> 초기화
                </button>
            </div>
        </form>
    </div>

    <c:if test="${corpList != null}">
        <div class="results-container">

            <div class="results-header">
                <div class="d-flex justify-content-between align-items-center">
                    <h5 class="mb-0">
                        <i class="bi bi-list-ul"></i> 검색 결과
                    </h5>
                    <span class="badge bg-light text-dark fs-6">
                        총 ${corpList.totalElements}건
                    </span>
                </div>
            </div>

            <c:if test="${statistics != null && statistics.totalCount > 0}">
                <div class="results-summary">
                    <div class="row">
                        <div class="col-md-3">
                            <div class="stats-card">
                                <div class="stats-number">${statistics.totalCount}</div>
                                <div class="stats-label">검색된 법인 수</div>
                            </div>
                        </div>
                        <div class="col-md-3">
                            <div class="stats-card">
                                <div class="stats-number">${corpList.totalPages}</div>
                                <div class="stats-label">총 페이지 수</div>
                            </div>
                        </div>
                        <div class="col-md-3">
                            <div class="stats-card">
                                <div class="stats-number">${corpList.number + 1}</div>
                                <div class="stats-label">현재 페이지</div>
                            </div>
                        </div>
                        <div class="col-md-3">
                            <div class="stats-card">
                                <div class="stats-number">${corpList.numberOfElements}</div>
                                <div class="stats-label">현재 페이지 결과</div>
                            </div>
                        </div>
                    </div>
                </div>
            </c:if>

            <c:choose>
                <c:when test="${not empty corpList.content}">
                    <div class="table-responsive">
                        <table class="table table-hover mb-0">
                            <thead>
                            <tr>
                                <th width="5%">No</th>
                                <th width="20%">법인명</th>
                                <th width="12%">사업자번호</th>
                                <th width="15%">판매자ID</th>
                                <th width="15%">법인등록번호</th>
                                <th width="15%">주소</th>
                                <th width="10%">등록자</th>
                                <th width="8%">상세</th>
                            </tr>
                            </thead>
                            <tbody>
                            <c:forEach var="corp" items="${corpList.content}" varStatus="status">
                                <tr>
                                    <td>
                                            ${corpList.totalElements - (corpList.number * corpList.size) - status.index}
                                    </td>
                                    <td>
                                            <span class="corp-name" onclick="viewDetail(${corp.id})">
                                                    ${corp.bizNm}
                                            </span>
                                    </td>
                                    <td>
                                        <span class="bizno-format">${corp.formattedBizNo}</span>
                                    </td>
                                    <td>
                                        <small class="text-muted">${corp.sellerId}</small>
                                    </td>
                                    <td>
                                        <small class="text-muted">${corp.corpRegNo}</small>
                                    </td>
                                    <td>
                                        <span class="address-info">${corp.fullAddress}</span>
                                    </td>
                                    <td>
                                        <span class="badge bg-info">${corp.username}</span>
                                    </td>
                                    <td>
                                        <button type="button" class="btn btn-outline-primary btn-sm"
                                                onclick="viewDetail(${corp.id})">
                                            <i class="bi bi-eye"></i>
                                        </button>
                                    </td>
                                </tr>
                            </c:forEach>
                            </tbody>
                        </table>
                    </div>

                    <c:if test="${corpList != null && corpList.totalPages > 1}">
                        <div class="pagination-container">
                            <nav aria-label="검색 결과 페이징">
                                <ul class="pagination justify-content-center mb-0">
                                    <c:if test="${corpList.hasPrevious()}">
                                        <li class="page-item">
                                            <a class="page-link" href="javascript:void(0)" onclick="goToPage(${corpList.number - 1})">
                                                <i class="bi bi-chevron-left"></i> 이전
                                            </a>
                                        </li>
                                    </c:if>
                                    <c:forEach var="i" begin="0" end="${corpList.totalPages - 1}">
                                        <c:if test="${i >= corpList.number - 2 && i <= corpList.number + 2}">
                                            <li class="page-item ${i == corpList.number ? 'active' : ''}">
                                                <a class="page-link" href="javascript:void(0)" onclick="goToPage(${i})">${i + 1}</a>
                                            </li>
                                        </c:if>
                                    </c:forEach>
                                    <c:if test="${corpList.hasNext()}">
                                        <li class="page-item">
                                            <a class="page-link" href="javascript:void(0)" onclick="goToPage(${corpList.number + 1})">
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
                    <div class="no-results">
                        <i class="bi bi-search"></i>
                        <h5>검색 결과가 없습니다</h5>
                        <p class="text-muted">검색 조건을 변경하여 다시 시도해보세요.</p>
                        <button type="button" class="btn btn-primary" onclick="resetForm()">
                            <i class="bi bi-arrow-clockwise"></i> 검색 조건 초기화
                        </button>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>
    </c:if>

    <c:if test="${corpList == null}">
        <div class="results-container">
            <div class="no-results">
                <i class="bi bi-building"></i>
                <h5>법인 정보를 검색해보세요</h5>
                <p class="text-muted">위의 검색 조건을 입력하고 검색 버튼을 클릭하세요.</p>
            </div>
        </div>
    </c:if>

</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/js/bootstrap.bundle.min.js"></script>
<script>
    function loadDistricts() {
        const citySelect = document.getElementById('city');
        const districtSelect = document.getElementById('district');
        const selectedCity = citySelect.value;

        districtSelect.innerHTML = '<option value="">전체</option>';

        if (selectedCity) {
            fetch('/corp/districts/' + encodeURIComponent(selectedCity))
                .then(response => response.json())
                .then(districts => {
                    districts.forEach(district => {
                        const option = document.createElement('option');
                        option.value = district;
                        option.textContent = district;
                        districtSelect.appendChild(option);
                    });
                })
                .catch(error => {
                    console.error('구/군 목록 로딩 실패:', error);
                });
        }
    }

    function resetForm() {
        document.getElementById('bizNm').value = '';
        document.getElementById('bizNo').value = '';
        document.getElementById('sellerId').value = '';
        document.getElementById('corpRegNo').value = '';
        document.getElementById('city').value = '';
        document.getElementById('district').value = '';

        window.location.href = '/corp/search';
    }

    // 상세 페이지로 이동
    function viewDetail(corpId) {
        window.location.href = '/corp/detail/' + corpId;
    }

    function buildPageUrl(pageNum) {
        const params = new URLSearchParams(window.location.search);
        params.set('page', pageNum);

        if (!params.has('size')) params.set('size', '20');
        if (!params.has('sort')) params.set('sort', 'id,desc');
        return params.toString();
    }

    document.getElementById('bizNo').addEventListener('input', function(e) {
        let value = e.target.value.replace(/[^0-9]/g, '');
        if (value.length <= 10) {
            if (value.length > 5) {
                value = value.substring(0, 3) + '-' + value.substring(3, 5) + '-' + value.substring(5);
            } else if (value.length > 3) {
                value = value.substring(0, 3) + '-' + value.substring(3);
            }
            e.target.value = value;
        }
    });

    document.addEventListener('DOMContentLoaded', function() {
        const city = document.getElementById('city').value;
        if (city) {
            loadDistricts();

            setTimeout(() => {
                const selectedDistrict = '${searchRequest.district}';
                if (selectedDistrict) {
                    document.getElementById('district').value = selectedDistrict;
                }
            }, 500);
        }
    });

    document.querySelectorAll('.search-form input').forEach(input => {
        input.addEventListener('keypress', function(e) {
            if (e.key === 'Enter') {
                e.preventDefault();
                document.querySelector('.search-form').submit();
            }
        });
    });

    function goToPage(pageNum) {
        const params = new URLSearchParams(window.location.search);
        params.set('page', pageNum);
        if (!params.has('size')) params.set('size', '20');
        if (!params.has('sort')) params.set('sort', 'id,desc');
        window.location.search = params.toString();
    }

</script>

</body>
</html>