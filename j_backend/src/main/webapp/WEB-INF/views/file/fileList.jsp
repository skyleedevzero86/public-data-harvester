<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<c:set var="pageTitle" value="파일 관리" />

<!DOCTYPE html>
<html>
<head>
    <%@ include file="../common/head.jsp" %>
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

<%@ include file="../common/footer.jsp" %>

<%@ include file="../common/scripts.jsp" %>
</body>
</html>
