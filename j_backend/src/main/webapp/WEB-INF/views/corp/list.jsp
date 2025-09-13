<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="_csrf" content="${_csrf.token}"/>
    <meta name="_csrf_header" content="${_csrf.headerName}"/>
    <title>법인 목록 - 통신판매사업자관리 시스템</title>
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

        .btn-create {
            background: linear-gradient(45deg, #28a745, #20c997);
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

        .alert-container {
            margin-bottom: 25px;
        }

        .table-responsive {
            overflow: visible !important;
        }

        .btn-group-sm .btn {
            margin-right: 2px;
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
        <h2><i class="bi bi-building"></i> 법인 목록</h2>
        <div>
            <a href="/corp/create" class="btn btn-success btn-create">
                <i class="bi bi-plus-circle"></i> 신규등록
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

        <form method="GET" action="/corp/list" class="search-form">
            <div class="row g-3">
                <div class="col-md-6">
                    <label for="bizNm" class="form-label">법인명</label>
                    <input type="text" class="form-control" id="bizNm" name="bizNm"
                           value="${form.bizNm}" placeholder="법인명을 입력하세요">
                </div>
                <div class="col-md-6">
                    <label for="bizNo" class="form-label">사업자번호</label>
                    <input type="text" class="form-control" id="bizNo" name="bizNo"
                           value="${form.bizNo}" placeholder="000-00-00000">
                </div>
                <div class="col-md-6">
                    <label for="corpRegNo" class="form-label">법인등록번호</label>
                    <input type="text" class="form-control" id="corpRegNo" name="corpRegNo"
                           value="${form.corpRegNo}" placeholder="법인등록번호를 입력하세요">
                </div>
                <div class="col-md-6">
                    <label for="siNm" class="form-label">시/도</label>
                    <input type="text" class="form-control" id="siNm" name="siNm"
                           value="${form.siNm}" placeholder="시/도를 입력하세요">
                </div>
                <div class="col-md-6">
                    <label for="sggNm" class="form-label">구/군</label>
                    <input type="text" class="form-control" id="sggNm" name="sggNm"
                           value="${form.sggNm}" placeholder="구/군을 입력하세요">
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
                        <i class="bi bi-list-ul"></i> 법인 목록
                    </h5>
                    <span class="badge bg-light text-dark fs-6">
                        총 ${corpList.totalElements}건
                    </span>
                </div>
            </div>

            <c:choose>
                <c:when test="${not empty corpList.content}">
                    <div class="table-responsive">
                        <table class="table table-hover mb-0">
                            <thead>
                            <tr>
                                <th width="5%">No</th>
                                <th width="20%">법인명</th>
                                <th width="12%">사업자번호</th>
                                <th width="15%">법인등록번호</th>
                                <th width="15%">시/도</th>
                                <th width="15%">구/군</th>
                                <th width="10%">등록자</th>
                                <th width="8%">관리</th>
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
                                        <span class="bizno-format">${corp.bizNo}</span>
                                    </td>
                                    <td>
                                        <small class="text-muted">${corp.corpRegNo}</small>
                                    </td>
                                    <td>
                                        <span class="address-info">${corp.siNm}</span>
                                    </td>
                                    <td>
                                        <span class="address-info">${corp.sggNm}</span>
                                    </td>
                                    <td>
                                        <span class="badge bg-info">
                                            <c:choose>
                                                <c:when test="${isAdmin}">${corp.username}</c:when>
                                                <c:otherwise>본인</c:otherwise>
                                            </c:choose>
                                        </span>
                                    </td>
                                    <td>
                                        <div class="btn-group" role="group">
                                            <a href="/corp/modify/${corp.id}" class="btn btn-outline-warning btn-sm">
                                                <i class="bi bi-pencil"></i> 수정
                                            </a>
                                            <button type="button" class="btn btn-outline-danger btn-sm"
                                                    onclick="deleteCorp(${corp.id}, '${corp.bizNm}')">
                                                <i class="bi bi-trash"></i> 삭제
                                            </button>
                                        </div>
                                    </td>
                                </tr>
                            </c:forEach>
                            </tbody>
                        </table>
                    </div>

                    <c:if test="${corpList != null && corpList.totalPages > 1}">
                        <div class="pagination-container">
                            <nav aria-label="법인 목록 페이징">
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
                        <i class="bi bi-building"></i>
                        <h5>등록된 법인 정보가 없습니다</h5>
                        <p class="text-muted">첫 번째 법인을 등록해보세요.</p>
                        <a href="/corp/create" class="btn btn-primary btn-create">
                            <i class="bi bi-plus-circle"></i> 첫 번째 법인 등록하기
                        </a>
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

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/js/bootstrap.min.js"></script>
<script>
    function resetForm() {
        document.getElementById('bizNm').value = '';
        document.getElementById('bizNo').value = '';
        document.getElementById('corpRegNo').value = '';
        document.getElementById('siNm').value = '';
        document.getElementById('sggNm').value = '';

        window.location.href = '/corp/list';
    }

    function viewDetail(corpId) {
        window.location.href = '/corp/detail/' + corpId;
    }

    function deleteCorp(corpId, bizNm) {
        if (confirm(`'${bizNm}' 법인을 삭제하시겠습니까?`)) {
            const form = document.createElement('form');
            form.method = 'POST';
            form.action = `/corp/delete/${corpId}`;

            const csrfToken = document.querySelector('meta[name="_csrf"]').getAttribute('content');
            const csrfHeader = document.querySelector('meta[name="_csrf_header"]').getAttribute('content');

            const csrfInput = document.createElement('input');
            csrfInput.type = 'hidden';
            csrfInput.name = '_csrf';
            csrfInput.value = csrfToken;
            form.appendChild(csrfInput);

            document.body.appendChild(form);
            form.submit();
        }
    }

    function goToPage(pageNum) {
        const params = new URLSearchParams(window.location.search);
        params.set('page', pageNum);
        if (!params.has('size')) params.set('size', '20');
        if (!params.has('sort')) params.set('sort', 'id,desc');
        window.location.search = params.toString();
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

    document.querySelectorAll('.search-form input').forEach(input => {
        input.addEventListener('keypress', function(e) {
            if (e.key === 'Enter') {
                e.preventDefault();
                document.querySelector('.search-form').submit();
            }
        });
    });
</script>

</body>
</html>