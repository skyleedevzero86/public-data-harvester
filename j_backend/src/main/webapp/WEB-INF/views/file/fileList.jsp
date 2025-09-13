<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="_csrf" content="${_csrf.token}"/>
    <meta name="_csrf_header" content="${_csrf.headerName}"/>
    <title>파일 관리 - 통신판매자사업관리시스템</title>
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
            text-align: center;
        }

        .table th:nth-child(4), .table td:nth-child(4) {
            text-align: center;
        }

        .table th:nth-child(5), .table td:nth-child(5) {
            text-align: center;
        }

        .table th:nth-child(6), .table td:nth-child(6) {
            text-align: center;
        }

        .table th:nth-child(7), .table td:nth-child(7) {
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

        .file-upload-modal {
            display: none;
            position: fixed;
            z-index: 1050;
            left: 0;
            top: 0;
            width: 100%;
            height: 100%;
            background-color: rgba(0, 0, 0, 0.5);
        }

        .file-upload-modal.show {
            display: block;
        }

        .file-upload-content {
            background-color: #fefefe;
            margin: 5% auto;
            padding: 0;
            border: none;
            border-radius: 10px;
            width: 90%;
            max-width: 600px;
            box-shadow: 0 4px 20px rgba(0, 0, 0, 0.3);
            animation: modalSlideIn 0.3s ease-out;
        }

        @keyframes modalSlideIn {
            from {
                transform: translateY(-50px);
                opacity: 0;
            }
            to {
                transform: translateY(0);
                opacity: 1;
            }
        }

        .file-upload-header {
            background: linear-gradient(45deg, #28a745, #20c997);
            color: white;
            padding: 20px;
            border-radius: 10px 10px 0 0;
            display: flex;
            justify-content: space-between;
            align-items: center;
        }

        .file-upload-header h3 {
            margin: 0;
            font-size: 1.3rem;
            font-weight: 600;
        }

        .file-upload-close {
            color: white;
            font-size: 28px;
            font-weight: bold;
            cursor: pointer;
            background: none;
            border: none;
            padding: 0;
            width: 30px;
            height: 30px;
            display: flex;
            align-items: center;
            justify-content: center;
            border-radius: 50%;
            transition: background-color 0.3s;
        }

        .file-upload-close:hover {
            background-color: rgba(255, 255, 255, 0.2);
        }

        .file-upload-body {
            padding: 30px;
        }

        .file-upload-form {
            margin-bottom: 20px;
        }

        .file-upload-form .form-group {
            margin-bottom: 15px;
        }

        .file-upload-form label {
            display: block;
            margin-bottom: 5px;
            font-weight: bold;
            color: #495057;
        }

        .file-upload-form input[type="file"],
        .file-upload-form input[type="text"] {
            width: 100%;
            padding: 10px;
            border: 1px solid #ced4da;
            border-radius: 6px;
            font-size: 14px;
        }

        .file-upload-form input[type="submit"] {
            background: #28a745;
            border: none;
            color: white;
            padding: 12px 24px;
            border-radius: 6px;
            font-weight: bold;
            cursor: pointer;
            font-size: 16px;
            width: 100%;
            transition: background-color 0.3s;
        }

        .file-upload-form input[type="submit"]:hover {
            background: #218838;
        }

        .file-upload-buttons {
            display: flex;
            gap: 10px;
            margin-top: 20px;
        }

        .file-upload-btn {
            flex: 1;
            padding: 12px 20px;
            border: none;
            border-radius: 6px;
            font-weight: bold;
            text-decoration: none;
            text-align: center;
            transition: all 0.3s ease;
            cursor: pointer;
        }

        .file-upload-btn.primary {
            background: #007bff;
            color: white;
        }

        .file-upload-btn.primary:hover {
            background: #0056b3;
            color: white;
        }

        .file-upload-btn.info {
            background: #17a2b8;
            color: white;
        }

        .file-upload-btn.info:hover {
            background: #138496;
            color: white;
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
            <a class="nav-link" href="/members/admin/pending">
                <i class="bi bi-clock"></i> 승인 대기
                <c:if test="${pendingCount > 0}">
                    <span class="badge bg-warning text-dark ms-1">${pendingCount}</span>
                </c:if>
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
        <h2><i class="bi bi-files"></i> 파일 관리</h2>
        <div>
            <button type="button" class="btn btn-success" onclick="openFileUploadModal()">
                <i class="bi bi-upload"></i> 새 파일 업로드
            </button>
        </div>
    </div>

    <c:if test="${not empty message}">
        <div class="alert alert-success alert-dismissible fade show" role="alert">
            <i class="bi bi-check-circle"></i> ${message}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
    </c:if>

    <c:if test="${not empty error}">
        <div class="alert alert-danger alert-dismissible fade show" role="alert">
            <i class="bi bi-exclamation-triangle"></i> ${error}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
    </c:if>

    <div class="filter-container">
        <div class="row align-items-center">
            <div class="col-auto">
                <h6 class="mb-0"><i class="bi bi-search"></i> 검색</h6>
            </div>
            <div class="col-auto">
                <form action="${pageContext.request.contextPath}/web/files" method="get" class="d-flex">
                    <input type="text" name="keyword" placeholder="파일명 또는 설명으로 검색"
                           value="${keyword}" class="form-control me-2" style="min-width: 250px;">
                    <button type="submit" class="btn btn-primary">
                        <i class="bi bi-search"></i> 검색
                    </button>
                </form>
            </div>
            <div class="col-auto ms-auto">
                <small class="text-muted">
                    총 ${files.size()}개 파일
                    <c:if test="${not empty keyword}">
                        (검색 결과)
                    </c:if>
                </small>
            </div>
        </div>
    </div>

    <div class="row mb-4">
        <div class="col-xl-3 col-md-6 col-sm-6">
            <div class="card bg-primary text-white mb-4">
                <div class="card-body">
                    <div class="d-flex justify-content-between align-items-center">
                        <div>
                            <div class="text-white-50 small">전체 파일</div>
                            <div class="fs-4 fw-bold">${files.size()}</div>
                        </div>
                        <div class="icon-align">
                            <i class="bi bi-files stats-icon"></i>
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
                            <div class="text-white-50 small">이미지 파일</div>
                            <div class="fs-4 fw-bold">
                                <c:set var="imageCount" value="0"/>
                                <c:forEach var="file" items="${files}">
                                    <c:if test="${file.contentType.startsWith('image/')}">
                                        <c:set var="imageCount" value="${imageCount + 1}"/>
                                    </c:if>
                                </c:forEach>
                                ${imageCount}
                            </div>
                        </div>
                        <div class="icon-align">
                            <i class="bi bi-image stats-icon"></i>
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
                            <div class="text-white-50 small">문서 파일</div>
                            <div class="fs-4 fw-bold">
                                <c:set var="documentCount" value="0"/>
                                <c:forEach var="file" items="${files}">
                                    <c:if test="${file.contentType.startsWith('application/') || file.contentType.startsWith('text/')}">
                                        <c:set var="documentCount" value="${documentCount + 1}"/>
                                    </c:if>
                                </c:forEach>
                                ${documentCount}
                            </div>
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
                            <div class="text-white-50 small">기타 파일</div>
                            <div class="fs-4 fw-bold">
                                <c:set var="otherCount" value="0"/>
                                <c:forEach var="file" items="${files}">
                                    <c:if test="${!file.contentType.startsWith('image/') && !file.contentType.startsWith('application/') && !file.contentType.startsWith('text/')}">
                                        <c:set var="otherCount" value="${otherCount + 1}"/>
                                    </c:if>
                                </c:forEach>
                                ${otherCount}
                            </div>
                        </div>
                        <div class="icon-align">
                            <i class="bi bi-file-earmark stats-icon"></i>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <div class="card">
        <div class="card-header">
            <div class="d-flex justify-content-between align-items-center">
                <h5 class="mb-0"><i class="bi bi-table"></i> 파일 목록</h5>
            </div>
        </div>
        <div class="card-body p-0">
            <c:choose>
                <c:when test="${not empty files}">
                    <div class="table-responsive">
                        <table class="table table-hover mb-0">
                            <thead class="table-dark">
                            <tr>
                                <th width="5%">ID</th>
                                <th width="20%">원본 파일명</th>
                                <th width="10%">파일 크기</th>
                                <th width="12%">콘텐츠 타입</th>
                                <th width="12%">업로드 시간</th>
                                <th width="12%">수정 시간</th>
                                <th width="15%">설명</th>
                                <th width="7%">다운로드</th>
                                <th width="7%">관리</th>
                            </tr>
                            </thead>
                            <tbody>
                            <c:forEach var="file" items="${files}">
                                <tr>
                                    <td>
                                        <strong>${file.id}</strong>
                                    </td>
                                    <td style="text-align: left;">
                                        <div class="d-flex align-items-center">
                                            <i class="bi bi-file-earmark me-2"></i>
                                            <strong>${file.originalFileName}</strong>
                                        </div>
                                    </td>
                                    <td>
                                            <span class="badge bg-secondary">
                                                <fmt:formatNumber value="${file.fileSizeInMB}" maxFractionDigits="2"/> MB
                                            </span>
                                    </td>
                                    <td>
                                            <span class="badge bg-info">
                                                    ${file.contentType}
                                            </span>
                                    </td>
                                    <td>
                                        <small>
                                            <fmt:formatDate value="${file.uploadTimeAsDate}" pattern="yyyy-MM-dd HH:mm"/>
                                        </small>
                                    </td>
                                    <td>
                                        <small>
                                            <fmt:formatDate value="${file.lastModifiedTimeAsDate}" pattern="yyyy-MM-dd HH:mm"/>
                                        </small>
                                    </td>
                                    <td style="text-align: left;">
                                        <c:choose>
                                            <c:when test="${not empty file.description}">
                                                <small class="text-muted">${file.description}</small>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="text-muted">설명 없음</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td>
                                        <c:if test="${not empty file.downloadUrl}">
                                            <a href="${file.downloadUrl}" class="btn btn-info btn-sm" target="_blank">
                                                <i class="bi bi-download"></i>
                                            </a>
                                        </c:if>
                                        <c:if test="${empty file.downloadUrl}">
                                            <a href="${pageContext.request.contextPath}/web/files/download/${file.id}" class="btn btn-info btn-sm">
                                                <i class="bi bi-download"></i>
                                            </a>
                                        </c:if>
                                    </td>
                                    <td>
                                        <div class="btn-group btn-group-sm" role="group">
                                            <div class="dropdown custom-dropdown">
                                                <button class="btn btn-outline-primary btn-sm dropdown-toggle"
                                                        type="button" data-bs-toggle="dropdown">
                                                    <i class="bi bi-gear"></i>
                                                </button>
                                                <ul class="dropdown-menu">
                                                    <li><h6 class="dropdown-header"><i class="bi bi-pencil"></i> 파일 관리</h6></li>
                                                    <li>
                                                        <a href="${pageContext.request.contextPath}/web/files/edit/${file.id}" class="dropdown-item">
                                                            <i class="bi bi-pencil"></i> 수정
                                                        </a>
                                                    </li>
                                                    <li><hr class="dropdown-divider"></li>
                                                    <li>
                                                        <form action="${pageContext.request.contextPath}/web/files/delete/${file.id}" method="post" class="d-inline">
                                                            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                                                            <button type="submit" class="dropdown-item text-danger border-0 bg-transparent"
                                                                    onclick="return confirm('정말로 이 파일을 삭제하시겠습니까?')">
                                                                <i class="bi bi-trash"></i> 삭제
                                                            </button>
                                                        </form>
                                                    </li>
                                                </ul>
                                            </div>
                                        </div>
                                    </td>
                                </tr>
                            </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="text-center py-5">
                        <i class="bi bi-files fs-1 text-muted"></i>
                        <c:choose>
                            <c:when test="${not empty keyword}">
                                <p class="text-muted mt-3">검색 조건에 맞는 파일이 없습니다.</p>
                                <a href="${pageContext.request.contextPath}/web/files" class="btn btn-primary">
                                    <i class="bi bi-x-lg"></i> 검색 초기화
                                </a>
                            </c:when>
                            <c:otherwise>
                                <p class="text-muted mt-3">업로드된 파일이 없습니다.</p>
                                <button type="button" class="btn btn-primary" onclick="openFileUploadModal()">
                                    <i class="bi bi-upload"></i> 첫 번째 파일 업로드하기
                                </button>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>
    </div>
</div>

<div id="fileUploadModal" class="file-upload-modal">
    <div class="file-upload-content">
        <div class="file-upload-header">
            <h3><i class="bi bi-cloud-upload"></i> 파일 업로드</h3>
            <button type="button" class="file-upload-close" onclick="closeFileUploadModal()">&times;</button>
        </div>
        <div class="file-upload-body">
            <form class="file-upload-form" action="${pageContext.request.contextPath}/web/files/upload" method="post" enctype="multipart/form-data">
                <div class="form-group">
                    <label for="file">파일 선택:</label>
                    <input type="file" id="file" name="file" required>
                </div>
                <div class="form-group">
                    <label for="description">파일 설명:</label>
                    <input type="text" id="description" name="description" placeholder="파일에 대한 설명을 입력하세요">
                </div>
                <div class="form-group">
                    <input type="submit" value="업로드">
                </div>
            </form>

            <div class="file-upload-buttons">
                <a href="/api/v1/files/template" class="file-upload-btn info" download>
                    <i class="bi bi-download"></i> CSV 양식 다운로드
                </a>
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
    function openFileUploadModal() {
        document.getElementById('fileUploadModal').classList.add('show');
        document.body.style.overflow = 'hidden';
    }

    function closeFileUploadModal() {
        document.getElementById('fileUploadModal').classList.remove('show');
        document.body.style.overflow = '';
    }

    document.getElementById('fileUploadModal').addEventListener('click', function(e) {
        if (e.target === this) {
            closeFileUploadModal();
        }
    });

    document.addEventListener('keydown', function(e) {
        if (e.key === 'Escape') {
            closeFileUploadModal();
        }
    });

    document.addEventListener('DOMContentLoaded', function() {
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