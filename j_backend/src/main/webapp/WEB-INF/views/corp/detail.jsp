<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<c:set var="pageTitle" value="법인 정보 상세" />
<c:set var="pageCSS" value="${['corp.css']}" />
<c:set var="pageJS" value="${['corp.js']}" />

<%@ include file="../common/header.jsp" %>
<%@ include file="../common/navigation.jsp" %>

<div class="container mt-4">

    <nav aria-label="breadcrumb" class="mb-4">
        <ol class="breadcrumb">
            <li class="breadcrumb-item"><a href="/">홈</a></li>
            <li class="breadcrumb-item"><a href="/corp/search">법인 검색</a></li>
            <li class="breadcrumb-item active" aria-current="page">법인 상세</li>
        </ol>
    </nav>

    <c:if test="${not empty errorMessage}">
        <div class="alert alert-danger alert-dismissible fade show" role="alert">
            <i class="bi bi-exclamation-triangle"></i> ${errorMessage}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
    </c:if>

    <c:if test="${not empty corp}">
        <div class="detail-container">

            <div class="detail-header">
                <h3>
                    <i class="bi bi-building"></i> ${corp.bizNm}
                </h3>
                <div class="subtitle">법인 상세 정보</div>
            </div>

            <div class="detail-body">

                <div class="info-section">
                    <h5><i class="bi bi-info-circle"></i> 기본 정보</h5>

                    <div class="info-row">
                        <div class="info-label">
                            <i class="bi bi-building"></i> 법인명
                        </div>
                        <div class="info-value highlight">
                                ${corp.bizNm}
                            <button class="copy-button" onclick="copyToClipboard('${corp.bizNm}', this)">
                                <i class="bi bi-clipboard"></i>
                            </button>
                        </div>
                    </div>

                    <div class="info-row">
                        <div class="info-label">
                            <i class="bi bi-credit-card"></i> 사업자번호
                        </div>
                        <div class="info-value">
                            <span class="bizno-display">${corp.formattedBizNo}</span>
                            <button class="copy-button" onclick="copyToClipboard('${corp.bizNo}', this)">
                                <i class="bi bi-clipboard"></i>
                            </button>
                        </div>
                    </div>

                    <div class="info-row">
                        <div class="info-label">
                            <i class="bi bi-file-text"></i> 법인등록번호
                        </div>
                        <div class="info-value">
                                ${corp.corpRegNo}
                            <button class="copy-button" onclick="copyToClipboard('${corp.corpRegNo}', this)">
                                <i class="bi bi-clipboard"></i>
                            </button>
                        </div>
                    </div>

                    <div class="info-row">
                        <div class="info-label">
                            <i class="bi bi-person-badge"></i> 판매자ID
                        </div>
                        <div class="info-value">
                                ${corp.sellerId}
                            <button class="copy-button" onclick="copyToClipboard('${corp.sellerId}', this)">
                                <i class="bi bi-clipboard"></i>
                            </button>
                        </div>
                    </div>
                </div>

                <div class="info-section">
                    <h5><i class="bi bi-geo-alt"></i> 위치 정보</h5>

                    <div class="info-row">
                        <div class="info-label">
                            <i class="bi bi-map"></i> 주소
                        </div>
                        <div class="info-value">
                            <div class="address-display">
                                <strong>${corp.fullAddress}</strong>
                                <button class="copy-button" onclick="copyToClipboard('${corp.fullAddress}', this)">
                                    <i class="bi bi-clipboard"></i>
                                </button>
                            </div>
                        </div>
                    </div>

                    <div class="info-row">
                        <div class="info-label">
                            <i class="bi bi-pin-map"></i> 시/도
                        </div>
                        <div class="info-value">
                                ${corp.siNm}
                        </div>
                    </div>

                    <div class="info-row">
                        <div class="info-label">
                            <i class="bi bi-pin"></i> 구/군
                        </div>
                        <div class="info-value">
                                ${corp.sggNm}
                        </div>
                    </div>

                    <div class="info-row">
                        <div class="info-label">
                            <i class="bi bi-hash"></i> 지역코드
                        </div>
                        <div class="info-value">
                                ${corp.regionCd}
                            <button class="copy-button" onclick="copyToClipboard('${corp.regionCd}', this)">
                                <i class="bi bi-clipboard"></i>
                            </button>
                        </div>
                    </div>
                </div>

                <div class="info-section">
                    <h5><i class="bi bi-gear"></i> 관리 정보</h5>

                    <div class="info-row">
                        <div class="info-label">
                            <i class="bi bi-person"></i> 등록자
                        </div>
                        <div class="info-value">
                            <span class="badge bg-info badge-status">${corp.username}</span>
                        </div>
                    </div>

                    <div class="info-row">
                        <div class="info-label">
                            <i class="bi bi-key"></i> 데이터 ID
                        </div>
                        <div class="info-value">
                                ${corp.id}
                        </div>
                    </div>
                </div>

                <div class="row mt-4">
                    <div class="col-md-12">
                        <div class="region-info">
                            <h6 class="mb-2">
                                <i class="bi bi-geo-alt-fill"></i> 위치 요약
                            </h6>
                            <div>
                                    ${corp.siNm} ${corp.sggNm} 지역에 등록된 법인입니다.
                            </div>
                        </div>
                    </div>
                </div>

                <div class="action-buttons">
                    <button type="button" class="btn btn-secondary btn-back" onclick="goBack()">
                        <i class="bi bi-arrow-left"></i> 목록으로
                    </button>

                    <c:if test="${hasEditPermission}">
                        <button type="button" class="btn btn-warning btn-edit" onclick="editCorp(${corp.id})">
                            <i class="bi bi-pencil"></i> 수정
                        </button>
                    </c:if>

                    <button type="button" class="btn btn-info" onclick="searchSimilar()">
                        <i class="bi bi-search"></i> 유사 법인 검색
                    </button>
                </div>

            </div>
        </div>

        <div class="mt-4">
            <div class="card">
                <div class="card-header">
                    <h6 class="mb-0">
                        <i class="bi bi-info-circle"></i> 추가 정보
                    </h6>
                </div>
                <div class="card-body">
                    <div class="row">
                        <div class="col-md-6">
                            <h6>같은 지역 법인 검색</h6>
                            <p class="text-muted">
                                    ${corp.siNm} ${corp.sggNm} 지역에 등록된 다른 법인들을 검색할 수 있습니다.
                            </p>
                            <a href="/corp/search?city=${corp.siNm}&district=${corp.sggNm}" class="btn btn-outline-primary btn-sm">
                                <i class="bi bi-search"></i> 같은 지역 법인 보기
                            </a>
                        </div>
                        <div class="col-md-6">
                            <h6>법인 정보 활용</h6>
                            <p class="text-muted">
                                이 법인의 사업자번호나 등록번호를 다른 시스템에서 활용할 수 있습니다.
                            </p>
                            <button type="button" class="btn btn-outline-success btn-sm" onclick="exportCorpInfo()">
                                <i class="bi bi-download"></i> 정보 내보내기
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </c:if>

<%@ include file="../common/footer.jsp" %>
</body>
</html>