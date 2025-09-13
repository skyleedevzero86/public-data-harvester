<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="_csrf" content="${_csrf.token}"/>
    <meta name="_csrf_header" content="${_csrf.headerName}"/>
    <title>파일 정보 수정 - 통신판매자사업관리시스템</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.7.2/font/bootstrap-icons.css" rel="stylesheet">

    <style>
        body {
            margin: 0;
            padding: 0;
            min-height: 100vh;
            display: flex;
            flex-direction: column;
        }

        .main-content {
            flex: 1;
        }

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

        .edit-form-container {
            background: #fff;
            border: 1px solid #dee2e6;
            border-radius: 8px;
            padding: 30px;
            margin-bottom: 20px;
            box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
        }

        .edit-form-title {
            font-weight: bold;
            font-size: 1.3rem;
            margin-bottom: 25px;
            display: flex;
            align-items: center;
            color: #495057;
            border-bottom: 2px solid #007bff;
            padding-bottom: 15px;
        }

        .edit-form-title i {
            margin-right: 10px;
            color: #007bff;
        }

        .form-group {
            margin-bottom: 20px;
        }

        .form-group label {
            font-weight: bold;
            color: #495057;
            margin-bottom: 8px;
            display: block;
        }

        .form-control {
            border: 1px solid #ced4da;
            border-radius: 6px;
            padding: 12px 15px;
            font-size: 14px;
            transition: border-color 0.3s ease;
        }

        .form-control:focus {
            border-color: #007bff;
            box-shadow: 0 0 0 0.2rem rgba(0, 123, 255, 0.25);
        }

        .form-control[readonly] {
            background-color: #f8f9fa;
            color: #6c757d;
        }

        .btn-save {
            background: linear-gradient(45deg, #28a745, #20c997);
            border: none;
            color: white;
            padding: 12px 30px;
            border-radius: 6px;
            font-weight: 600;
            font-size: 1rem;
            display: inline-flex;
            align-items: center;
            gap: 8px;
            transition: all 0.3s ease;
            width: 100%;
            justify-content: center;
        }

        .btn-save:hover {
            background: linear-gradient(45deg, #218838, #1ea085);
            color: white;
            transform: translateY(-2px);
            box-shadow: 0 4px 12px rgba(40, 167, 69, 0.3);
        }

        .btn-cancel {
            background: #6c757d;
            border: 1px solid #6c757d;
            color: white;
            padding: 12px 30px;
            border-radius: 6px;
            font-weight: 600;
            font-size: 1rem;
            display: inline-flex;
            align-items: center;
            gap: 8px;
            transition: all 0.3s ease;
            text-decoration: none;
            width: 100%;
            justify-content: center;
        }

        .btn-cancel:hover {
            background: #5a6268;
            color: white;
            text-decoration: none;
            transform: translateY(-2px);
            box-shadow: 0 4px 12px rgba(108, 117, 125, 0.3);
        }

        .file-info-card {
            background: #f8f9fa;
            border: 1px solid #dee2e6;
            border-radius: 8px;
            padding: 20px;
            margin-bottom: 25px;
        }

        .file-info-title {
            font-weight: bold;
            color: #495057;
            margin-bottom: 15px;
            display: flex;
            align-items: center;
        }

        .file-info-title i {
            margin-right: 8px;
            color: #17a2b8;
        }

        .file-info-item {
            display: flex;
            justify-content: space-between;
            align-items: center;
            padding: 8px 0;
            border-bottom: 1px solid #e9ecef;
        }

        .file-info-item:last-child {
            border-bottom: none;
        }

        .file-info-label {
            font-weight: 600;
            color: #495057;
        }

        .file-info-value {
            color: #6c757d;
            font-size: 0.9rem;
        }

        .footer {
            background-color: #343a40;
            color: white;
            padding: 40px 0 20px 0;
            margin-top: auto;
            width: 100%;
        }

        .footer-logo {
            margin-bottom: 30px;
        }

        .footer-logo .main-title {
            font-size: 1.8rem;
            font-weight: bold;
            margin-bottom: 5px;
        }

        .footer-logo .sub-title {
            font-size: 1rem;
            color: #adb5bd;
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

        .container-fluid {
            padding-left: 15px;
            padding-right: 15px;
        }

        .row {
            margin-left: -15px;
            margin-right: -15px;
        }

        .col-md-6, .col-md-8, .col-md-4 {
            padding-left: 15px;
            padding-right: 15px;
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
            <a class="nav-link" href="/web/files">
                <i class="bi bi-files"></i> 파일 관리
            </a>
            <a class="nav-link" href="/members/logout">
                <i class="bi bi-box-arrow-right"></i> 로그아웃
            </a>
        </div>
    </div>
</nav>

<div class="main-content">
    <div class="container-fluid mt-4">
        <div class="d-flex justify-content-between align-items-center mb-4">
            <h2><i class="bi bi-pencil-square"></i> 파일 정보 수정</h2>
            <div>
                <a href="${pageContext.request.contextPath}/web/files" class="btn btn-secondary">
                    <i class="bi bi-arrow-left"></i> 목록으로 돌아가기
                </a>
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

        <div class="row">
            <div class="col-md-8">
                <div class="edit-form-container">
                    <div class="edit-form-title">
                        <i class="bi bi-file-earmark-text"></i> 파일 정보 수정
                    </div>

                    <form action="${pageContext.request.contextPath}/web/files/update/${file.id}" method="post">
                        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>

                        <div class="form-group">
                            <label for="originalFileName">
                                <i class="bi bi-file-earmark"></i> 원본 파일명
                            </label>
                            <input type="text" class="form-control" id="originalFileName" name="originalFileName"
                                   value="${file.originalFileName}" readonly>
                            <small class="text-muted">파일명은 수정할 수 없습니다.</small>
                        </div>

                        <div class="form-group">
                            <label for="description">
                                <i class="bi bi-card-text"></i> 파일 설명
                            </label>
                            <input type="text" class="form-control" id="description" name="description"
                                   value="${file.description}" placeholder="파일에 대한 설명을 입력하세요">
                            <small class="text-muted">파일의 용도나 내용에 대한 설명을 입력해주세요.</small>
                        </div>

                        <div class="form-group">
                            <button type="submit" class="btn btn-save">
                                <i class="bi bi-check-circle"></i> 저장하기
                            </button>
                        </div>
                    </form>
                </div>
            </div>

            <div class="col-md-4">
                <div class="file-info-card">
                    <div class="file-info-title">
                        <i class="bi bi-info-circle"></i> 현재 파일 정보
                    </div>

                    <div class="file-info-item">
                        <span class="file-info-label">파일 ID:</span>
                        <span class="file-info-value">${file.id}</span>
                    </div>

                    <div class="file-info-item">
                        <span class="file-info-label">파일 크기:</span>
                        <span class="file-info-value">
                            <fmt:formatNumber value="${file.fileSizeInMB}" maxFractionDigits="2"/> MB
                        </span>
                    </div>

                    <div class="file-info-item">
                        <span class="file-info-label">콘텐츠 타입:</span>
                        <span class="file-info-value">${file.contentType}</span>
                    </div>

                    <div class="file-info-item">
                        <span class="file-info-label">업로드 시간:</span>
                        <span class="file-info-value">
                            <fmt:formatDate value="${file.uploadTimeAsDate}" pattern="yyyy-MM-dd HH:mm"/>
                        </span>
                    </div>

                    <div class="file-info-item">
                        <span class="file-info-label">수정 시간:</span>
                        <span class="file-info-value">
                            <fmt:formatDate value="${file.lastModifiedTimeAsDate}" pattern="yyyy-MM-dd HH:mm"/>
                        </span>
                    </div>
                </div>

                <div class="d-grid gap-2">
                    <a href="${pageContext.request.contextPath}/web/files/download/${file.id}" class="btn btn-info">
                        <i class="bi bi-download"></i> 파일 다운로드
                    </a>
                    <a href="${pageContext.request.contextPath}/web/files" class="btn btn-cancel">
                        <i class="bi bi-arrow-left"></i> 목록으로 돌아가기
                    </a>
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
</body>
</html>