<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="_csrf" content="${_csrf.token}"/>
    <meta name="_csrf_header" content="${_csrf.headerName}"/>
    <title>법인 정보 상세 - Antock System</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.7.2/font/bootstrap-icons.css" rel="stylesheet">

    <style>
        .detail-container {
            background: white;
            border-radius: 15px;
            box-shadow: 0 4px 20px rgba(0,0,0,0.1);
            overflow: hidden;
        }

        .detail-header {
            background: linear-gradient(135deg, #007bff, #0056b3);
            color: white;
            padding: 30px;
            text-align: center;
        }

        .detail-header h3 {
            margin: 0;
            font-weight: 600;
        }

        .detail-header .subtitle {
            opacity: 0.9;
            margin-top: 5px;
        }

        .detail-body {
            padding: 30px;
        }

        .info-section {
            margin-bottom: 30px;
        }

        .info-section h5 {
            color: #495057;
            font-weight: 600;
            margin-bottom: 20px;
            padding-bottom: 10px;
            border-bottom: 2px solid #e9ecef;
        }

        .info-row {
            display: flex;
            padding: 12px 0;
            border-bottom: 1px solid #f8f9fa;
        }

        .info-row:last-child {
            border-bottom: none;
        }

        .info-label {
            width: 150px;
            font-weight: 600;
            color: #6c757d;
            flex-shrink: 0;
        }

        .info-value {
            flex: 1;
            color: #212529;
        }

        .info-value.highlight {
            font-weight: 600;
            color: #007bff;
        }

        .bizno-display {
            font-family: 'Courier New', monospace;
            font-size: 1.1em;
            color: #28a745;
            font-weight: bold;
        }

        .address-display {
            background: #f8f9fa;
            padding: 10px 15px;
            border-radius: 6px;
            border-left: 4px solid #007bff;
        }

        .action-buttons {
            text-align: center;
            padding: 20px 0;
            border-top: 1px solid #e9ecef;
            margin-top: 20px;
        }

        .btn-back {
            background: linear-gradient(45deg, #6c757d, #495057);
            border: none;
            padding: 12px 30px;
            font-weight: 600;
            margin-right: 10px;
        }

        .btn-edit {
            background: linear-gradient(45deg, #ffc107, #e0a800);
            border: none;
            padding: 12px 30px;
            font-weight: 600;
            color: #212529;
        }

        .copy-button {
            background: none;
            border: none;
            color: #6c757d;
            padding: 2px 6px;
            border-radius: 3px;
            font-size: 0.8em;
            margin-left: 10px;
            cursor: pointer;
        }

        .copy-button:hover {
            background: #e9ecef;
            color: #495057;
        }

        .copy-success {
            color: #28a745 !important;
        }

        .badge-status {
            font-size: 0.9em;
            padding: 6px 12px;
        }

        .region-info {
            background: linear-gradient(45deg, #17a2b8, #138496);
            color: white;
            padding: 15px;
            border-radius: 8px;
            text-align: center;
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

</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/js/bootstrap.bundle.min.js"></script>
<script>

    function copyToClipboard(text, button) {
        navigator.clipboard.writeText(text).then(function() {

            const icon = button.querySelector('i');
            const originalClass = icon.className;

            icon.className = 'bi bi-check';
            button.classList.add('copy-success');

            setTimeout(() => {
                icon.className = originalClass;
                button.classList.remove('copy-success');
            }, 1000);

        }).catch(function(err) {
            console.error('클립보드 복사 실패:', err);
            alert('복사에 실패했습니다.');
        });
    }

    function goBack() {
        if (document.referrer && document.referrer.includes('/corp/search')) {
            window.history.back();
        } else {
            window.location.href = '/corp/search';
        }
    }

    function editCorp(corpId) {
        alert('법인 수정 기능은 개발 중입니다.');
    }

    function searchSimilar() {
        const corpName = '${corp.bizNm}';
        const city = '${corp.siNm}';
        const district = '${corp.sggNm}';

        const searchUrl = '/corp/search?city=' + encodeURIComponent(city) +
            '&district=' + encodeURIComponent(district);

        window.location.href = searchUrl;
    }

    function exportCorpInfo() {
        const corpInfo = {
            id: '${corp.id}',
            bizNm: '${corp.bizNm}',
            bizNo: '${corp.bizNo}',
            corpRegNo: '${corp.corpRegNo}',
            sellerId: '${corp.sellerId}',
            address: '${corp.fullAddress}',
            regionCd: '${corp.regionCd}',
            username: '${corp.username}'
        };

        const dataStr = JSON.stringify(corpInfo, null, 2);
        const dataBlob = new Blob([dataStr], {type: 'application/json'});

        const link = document.createElement('a');
        link.href = URL.createObjectURL(dataBlob);
        link.download = '법인정보_' + '${corp.bizNm}' + '_' + new Date().getTime() + '.json';
        link.click();
    }

    document.addEventListener('keydown', function(e) {

        if (e.key === 'Escape') {
            goBack();
        }

        if (e.ctrlKey && e.key === 'c' && !window.getSelection().toString()) {
            e.preventDefault();
            copyToClipboard('${corp.bizNm}', document.querySelector('.copy-button'));
        }
    });

    document.addEventListener('DOMContentLoaded', function() {
        const container = document.querySelector('.detail-container');
        container.style.opacity = '0';
        container.style.transform = 'translateY(20px)';

        setTimeout(() => {
            container.style.transition = 'all 0.5s ease';
            container.style.opacity = '1';
            container.style.transform = 'translateY(0)';
        }, 100);
    });

</script>

</body>
</html>