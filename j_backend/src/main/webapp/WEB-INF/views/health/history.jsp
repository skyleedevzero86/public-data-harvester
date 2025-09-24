<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>헬스 체크 이력 - 통신판매자사업관리시스템</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.7.2/font/bootstrap-icons.css" rel="stylesheet">
    <style>
        .status-up { color: #28a745; }
        .status-down { color: #dc3545; }
        .status-unknown { color: #ffc107; }
        .health-card {
            transition: transform 0.2s;
        }
        .health-card:hover {
            transform: translateY(-2px);
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
        .icon-align {
            display: flex;
            align-items: center;
            justify-content: center;
        }
        .stats-icon {
            font-size: 2.5rem;
            opacity: 0.8;
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
        <h2><i class="bi bi-clock-history"></i> 헬스 체크 이력</h2>
        <div>
            <a href="/health" class="btn btn-outline-secondary">
                <i class="bi bi-arrow-left"></i> 대시보드
            </a>
            <a href="/health/check" class="btn btn-primary">
                <i class="bi bi-arrow-clockwise"></i> 수동 체크
            </a>
        </div>
    </div>
    <div class="filter-container">
        <div class="row align-items-center">
            <div class="col-auto">
                <h6 class="mb-0"><i class="bi bi-funnel"></i> 필터</h6>
            </div>
            <div class="col-auto">
                <form method="get" action="/health/history" class="d-flex align-items-center gap-3">
                    <div>
                        <label for="fromDate" class="form-label small">시작 날짜</label>
                        <input type="datetime-local" class="form-control form-control-sm" id="fromDate" name="fromDate">
                    </div>
                    <div>
                        <label for="toDate" class="form-label small">종료 날짜</label>
                        <input type="datetime-local" class="form-control form-control-sm" id="toDate" name="toDate">
                    </div>
                    <div>
                        <label for="size" class="form-label small">페이지 크기</label>
                        <select class="form-select form-select-sm" id="size" name="size">
                            <option value="10" ${param.size == '10' ? 'selected' : ''}>10개</option>
                            <option value="20" ${param.size == '20' ? 'selected' : ''}>20개</option>
                            <option value="50" ${param.size == '50' ? 'selected' : ''}>50개</option>
                            <option value="100" ${param.size == '100' ? 'selected' : ''}>100개</option>
                        </select>
                    </div>
                    <div class="d-flex gap-2">
                        <button type="submit" class="btn btn-primary btn-sm">
                            <i class="bi bi-search"></i> 검색
                        </button>
                        <a href="/health/history" class="btn btn-outline-secondary btn-sm">
                            <i class="bi bi-arrow-clockwise"></i> 초기화
                        </a>
                    </div>
                </form>
            </div>
        </div>
    </div>
    <div class="row mb-4">
        <div class="col-xl-3 col-md-6 col-sm-6">
            <div class="card bg-primary text-white mb-4">
                <div class="card-body">
                    <div class="d-flex justify-content-between align-items-center">
                        <div>
                            <div class="text-white-50 small">총 체크 수</div>
                            <div class="fs-4 fw-bold">${totalElements}</div>
                        </div>
                        <div class="icon-align">
                            <i class="bi bi-list-check stats-icon"></i>
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
                            <div class="text-white-50 small">정상</div>
                            <div class="fs-4 fw-bold">
                                <c:set var="upCount" value="0" />
                                <c:forEach var="check" items="${healthChecks.content}">
                                    <c:if test="${check.healthy}">
                                        <c:set var="upCount" value="${upCount + 1}" />
                                    </c:if>
                                </c:forEach>
                                ${upCount}
                            </div>
                        </div>
                        <div class="icon-align">
                            <i class="bi bi-check-circle-fill stats-icon"></i>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <div class="col-xl-3 col-md-6 col-sm-6">
            <div class="card bg-danger text-white mb-4">
                <div class="card-body">
                    <div class="d-flex justify-content-between align-items-center">
                        <div>
                            <div class="text-white-50 small">장애</div>
                            <div class="fs-4 fw-bold">
                                <c:set var="downCount" value="0" />
                                <c:forEach var="check" items="${healthChecks.content}">
                                    <c:if test="${!check.healthy}">
                                        <c:set var="downCount" value="${downCount + 1}" />
                                    </c:if>
                                </c:forEach>
                                ${downCount}
                            </div>
                        </div>
                        <div class="icon-align">
                            <i class="bi bi-x-circle-fill stats-icon"></i>
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
                            <div class="text-white-50 small">평균 응답시간</div>
                            <div class="fs-4 fw-bold">
                                <c:set var="avgResponseTime" value="0" />
                                <c:forEach var="check" items="${healthChecks.content}">
                                    <c:set var="avgResponseTime" value="${avgResponseTime + check.responseTime}" />
                                </c:forEach>
                                <c:if test="${healthChecks.numberOfElements > 0}">
                                    <c:set var="avgResponseTime" value="${avgResponseTime / healthChecks.numberOfElements}" />
                                </c:if>
                                <fmt:formatNumber value="${avgResponseTime}" pattern="#.##"/>ms
                            </div>
                        </div>
                        <div class="icon-align">
                            <i class="bi bi-speedometer2 stats-icon"></i>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
    <div class="row">
        <div class="col-12">
            <div class="card health-card">
                <div class="card-header">
                    <h5 class="mb-0"><i class="bi bi-list"></i> 체크 이력</h5>
                </div>
                <div class="card-body p-0">
                    <div class="table-responsive">
                        <table class="table table-hover mb-0">
                            <thead class="table-dark">
                                <tr>
                                    <th>컴포넌트</th>
                                    <th>상태</th>
                                    <th>메시지</th>
                                    <th>응답시간</th>
                                    <th>체크 타입</th>
                                    <th>체크시간</th>
                                    <th>만료시간</th>
                                    <th>액션</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="check" items="${healthChecks.content}">
                                    <tr class="${check.healthy ? 'table-success' : 'table-danger'}">
                                        <td>
                                            <i class="bi bi-gear"></i> ${check.component}
                                        </td>
                                        <td>
                                            <span class="badge bg-${check.healthy ? 'success' : 'danger'}">
                                                <i class="bi bi-${check.healthy ? 'check-circle' : 'x-circle'}"></i>
                                                ${check.status.code}
                                            </span>
                                        </td>
                                        <td>${check.message}</td>
                                        <td>
                                            <span class="badge bg-${check.responseTime < 1000 ? 'success' : check.responseTime < 3000 ? 'warning' : 'danger'}">
                                                ${check.responseTime}ms
                                            </span>
                                        </td>
                                        <td>
                                            <span class="badge bg-info">${check.checkType}</span>
                                        </td>
                                        <td>
                                            <fmt:formatDate value="${check.checkedAtAsDate}" pattern="MM-dd HH:mm:ss"/>
                                        </td>
                                        <td>
                                            <fmt:formatDate value="${check.expiresAtAsDate}" pattern="MM-dd HH:mm:ss"/>
                                            <c:if test="${check.expired}">
                                                <span class="badge bg-warning">만료</span>
                                            </c:if>
                                        </td>
                                        <td>
                                            <a href="/health/component/${check.component}" class="btn btn-sm btn-outline-primary">
                                                <i class="bi bi-eye"></i> 상세
                                            </a>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </div>
                    <c:if test="${totalPages > 1}">
                        <div class="card-footer">
                            <nav aria-label="페이지 네비게이션">
                                <ul class="pagination justify-content-center mb-0">
                                    <li class="page-item ${currentPage == 0 ? 'disabled' : ''}">
                                        <a class="page-link" href="?page=${currentPage - 1}&size=${param.size}&fromDate=${param.fromDate}&toDate=${param.toDate}">
                                            <i class="bi bi-chevron-left"></i> 이전
                                        </a>
                                    </li>
                                    <c:forEach begin="0" end="${totalPages - 1}" var="pageNum">
                                        <li class="page-item ${currentPage == pageNum ? 'active' : ''}">
                                            <a class="page-link" href="?page=${pageNum}&size=${param.size}&fromDate=${param.fromDate}&toDate=${param.toDate}">${pageNum + 1}</a>
                                        </li>
                                    </c:forEach>
                                    <li class="page-item ${currentPage == totalPages - 1 ? 'disabled' : ''}">
                                        <a class="page-link" href="?page=${currentPage + 1}&size=${param.size}&fromDate=${param.fromDate}&toDate=${param.toDate}">
                                            다음 <i class="bi bi-chevron-right"></i>
                                        </a>
                                    </li>
                                </ul>
                            </nav>
                        </div>
                    </c:if>
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
    document.addEventListener('DOMContentLoaded', function() {
        const cards = document.querySelectorAll('.health-card');
        cards.forEach((card, index) => {
            setTimeout(() => {
                card.style.opacity = '0';
                card.style.transform = 'translateY(20px)';
                card.style.transition = 'all 0.5s ease';
                setTimeout(() => {
                    card.style.opacity = '1';
                    card.style.transform = 'translateY(0)';
                }, 100);
            }, index * 100);
        });
    });
    
    document.getElementById('fromDate').addEventListener('change', function() {
        if (this.value && !document.getElementById('toDate').value) {
            const fromDate = new Date(this.value);
            fromDate.setHours(23, 59, 59);
            document.getElementById('toDate').value = fromDate.toISOString().slice(0, 16);
        }
    });
</script>
</body>
</html>