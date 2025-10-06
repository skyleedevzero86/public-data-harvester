<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<c:set var="pageTitle" value="헬스 체크 이력" />
<c:set var="pageCSS" value="${['health.css']}" />
<c:set var="pageJS" value="${['health.js']}" />

<!DOCTYPE html>
<html lang="ko">
<head>
    <%@ include file="../common/head.jsp" %>
</head>
<body>
<%@ include file="../common/navigation.jsp" %>

<div class="container-fluid mt-4">
    <div class="d-flex justify-content-between align-items-center mb-4">
        <h2><i class="bi bi-clock-history"></i> 헬스 체크 이력</h2>
    </div>

    <div class="card mb-4">
        <div class="card-header">
            <h5 class="mb-0"><i class="bi bi-funnel"></i> 검색 필터</h5>
        </div>
        <div class="card-body">
            <form method="get" action="/health/history">
                <div class="row">
                    <div class="col-md-3">
                        <label for="component" class="form-label">컴포넌트</label>
                        <select class="form-select" id="component" name="component">
                            <option value="">전체</option>
                            <c:forEach var="comp" items="${availableComponents}">
                                <option value="${comp}" ${param.component == comp ? 'selected' : ''}>${comp}</option>
                            </c:forEach>
                        </select>
                    </div>
                    <div class="col-md-2">
                        <label for="status" class="form-label">상태</label>
                        <select class="form-select" id="status" name="status">
                            <option value="">전체</option>
                            <option value="UP" ${param.status == 'UP' ? 'selected' : ''}>정상</option>
                            <option value="DOWN" ${param.status == 'DOWN' ? 'selected' : ''}>장애</option>
                            <option value="UNKNOWN" ${param.status == 'UNKNOWN' ? 'selected' : ''}>알 수 없음</option>
                        </select>
                    </div>
                    <div class="col-md-3">
                        <label for="fromDate" class="form-label">시작 시간</label>
                        <input type="datetime-local" class="form-control" id="fromDate" name="fromDate" value="${param.fromDate}">
                    </div>
                    <div class="col-md-3">
                        <label for="toDate" class="form-label">종료 시간</label>
                        <input type="datetime-local" class="form-control" id="toDate" name="toDate" value="${param.toDate}">
                    </div>
                    <div class="col-md-1">
                        <label class="form-label">&nbsp;</label>
                        <div>
                            <button type="submit" class="btn btn-primary w-100">
                                <i class="bi bi-search"></i>
                            </button>
                        </div>
                    </div>
                    <div class="col-md-1">
                        <label class="form-label">&nbsp;</label>
                        <div>
                            <button type="button" class="btn btn-secondary w-100" onclick="resetFilters()">
                                <i class="bi bi-arrow-clockwise"></i>
                            </button>
                        </div>
                    </div>
                </div>
            </form>
        </div>
    </div>

    <div class="row mb-4">
        <div class="col-xl-3 col-md-6">
            <div class="card bg-primary text-white mb-4">
                <div class="card-body">
                    <div class="d-flex justify-content-between align-items-center">
                        <div>
                            <div class="text-white-50 small">총 체크 수</div>
                            <div class="fs-4 fw-bold">${totalChecks}</div>
                        </div>
                        <div class="icon-align">
                            <i class="bi bi-list-check stats-icon"></i>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <div class="col-xl-3 col-md-6">
            <div class="card bg-success text-white mb-4">
                <div class="card-body">
                    <div class="d-flex justify-content-between align-items-center">
                        <div>
                            <div class="text-white-50 small">성공</div>
                            <div class="fs-4 fw-bold">${successfulChecks}</div>
                        </div>
                        <div class="icon-align">
                            <i class="bi bi-check-circle-fill stats-icon"></i>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <div class="col-xl-3 col-md-6">
            <div class="card bg-danger text-white mb-4">
                <div class="card-body">
                    <div class="d-flex justify-content-between align-items-center">
                        <div>
                            <div class="text-white-50 small">실패</div>
                            <div class="fs-4 fw-bold">${failedChecks}</div>
                        </div>
                        <div class="icon-align">
                            <i class="bi bi-x-circle-fill stats-icon"></i>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <div class="col-xl-3 col-md-6">
            <div class="card bg-info text-white mb-4">
                <div class="card-body">
                    <div class="d-flex justify-content-between align-items-center">
                        <div>
                            <div class="text-white-50 small">성공률</div>
                            <div class="fs-4 fw-bold">
                                <fmt:formatNumber value="${successRate}" pattern="#.##"/>%
                            </div>
                        </div>
                        <div class="icon-align">
                            <i class="bi bi-percent stats-icon"></i>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <div class="card health-card">
        <div class="card-header">
            <h5 class="mb-0">
                <i class="bi bi-clock-history"></i> 체크 이력
                <c:if test="${not empty param.component}">
                    - ${param.component}
                </c:if>
            </h5>
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
                                <fmt:formatDate value="${check.checkedAtAsDate}" pattern="MM-dd HH:mm:ss" />
                            </td>
                            <td>
                                <fmt:formatDate value="${check.expiresAtAsDate}" pattern="MM-dd HH:mm:ss" />
                                <c:if test="${check.expired}">
                                    <span class="badge bg-warning">만료</span>
                                </c:if>
                            </td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </div>

            <div class="d-flex justify-content-between align-items-center mb-3">
                <div class="pagination-info">
                    <span class="text-muted">
                        <c:choose>
                            <c:when test="${param.size == '0'}">
                                총 <strong>${totalElements}</strong>개 항목 전체 표시
                            </c:when>
                            <c:otherwise>
                                총 <strong>${totalElements}</strong>개 항목 중
                                <strong>${(currentPage * (param.size != null ? param.size : 20)) + 1}</strong>-<strong>${(currentPage * (param.size != null ? param.size : 20)) + healthChecks.numberOfElements}</strong>개 표시
                                (페이지 ${currentPage + 1}/${totalPages})
                            </c:otherwise>
                        </c:choose>
                    </span>
                </div>
                <div class="page-size-selector">
                    <label for="pageSize" class="form-label me-2">페이지 크기:</label>
                    <select id="pageSize" class="form-select form-select-sm" style="width: auto;" onchange="changePageSize(this.value)">
                        <option value="10" ${param.size == '10' ? 'selected' : ''}>10개</option>
                        <option value="20" ${param.size == '20' || empty param.size ? 'selected' : ''}>20개</option>
                        <option value="50" ${param.size == '50' ? 'selected' : ''}>50개</option>
                        <option value="100" ${param.size == '100' ? 'selected' : ''}>100개</option>
                        <option value="0" ${param.size == '0' ? 'selected' : ''}>전체</option>
                    </select>
                </div>
            </div>

            <c:if test="${totalPages > 1 && param.size != '0'}">
                <nav aria-label="페이지 네비게이션">
                    <ul class="pagination justify-content-center">
                        <li class="page-item ${currentPage == 0 ? 'disabled' : ''}">
                            <a class="page-link" href="?page=0&size=${param.size != null ? param.size : '20'}${not empty param.component ? '&component='.concat(param.component) : ''}${not empty param.status ? '&status='.concat(param.status) : ''}${not empty param.fromDate ? '&fromDate='.concat(param.fromDate) : ''}${not empty param.toDate ? '&toDate='.concat(param.toDate) : ''}" title="첫 페이지">
                                <i class="bi bi-chevron-double-left"></i>
                            </a>
                        </li>

                        <li class="page-item ${currentPage == 0 ? 'disabled' : ''}">
                            <a class="page-link" href="?page=${currentPage - 1}&size=${param.size != null ? param.size : '20'}${not empty param.component ? '&component='.concat(param.component) : ''}${not empty param.status ? '&status='.concat(param.status) : ''}${not empty param.fromDate ? '&fromDate='.concat(param.fromDate) : ''}${not empty param.toDate ? '&toDate='.concat(param.toDate) : ''}" title="이전 페이지">
                                <i class="bi bi-chevron-left"></i>
                            </a>
                        </li>

                        <c:set var="startPage" value="${Math.max(0, currentPage - 2)}" />
                        <c:set var="endPage" value="${Math.min(totalPages - 1, currentPage + 2)}" />

                        <c:if test="${startPage > 0}">
                            <li class="page-item disabled">
                                <span class="page-link">...</span>
                            </li>
                        </c:if>

                        <c:forEach begin="${startPage}" end="${endPage}" var="pageNum">
                            <li class="page-item ${currentPage == pageNum ? 'active' : ''}">
                                <a class="page-link" href="?page=${pageNum}&size=${param.size != null ? param.size : '20'}${not empty param.component ? '&component='.concat(param.component) : ''}${not empty param.status ? '&status='.concat(param.status) : ''}${not empty param.fromDate ? '&fromDate='.concat(param.fromDate) : ''}${not empty param.toDate ? '&toDate='.concat(param.toDate) : ''}">${pageNum + 1}</a>
                            </li>
                        </c:forEach>

                        <c:if test="${endPage < totalPages - 1}">
                            <li class="page-item disabled">
                                <span class="page-link">...</span>
                            </li>
                        </c:if>

                        <li class="page-item ${currentPage == totalPages - 1 ? 'disabled' : ''}">
                            <a class="page-link" href="?page=${currentPage + 1}&size=${param.size != null ? param.size : '20'}${not empty param.component ? '&component='.concat(param.component) : ''}${not empty param.status ? '&status='.concat(param.status) : ''}${not empty param.fromDate ? '&fromDate='.concat(param.fromDate) : ''}${not empty param.toDate ? '&toDate='.concat(param.toDate) : ''}" title="다음 페이지">
                                <i class="bi bi-chevron-right"></i>
                            </a>
                        </li>

                        <li class="page-item ${currentPage == totalPages - 1 ? 'disabled' : ''}">
                            <a class="page-link" href="?page=${totalPages - 1}&size=${param.size != null ? param.size : '20'}${not empty param.component ? '&component='.concat(param.component) : ''}${not empty param.status ? '&status='.concat(param.status) : ''}${not empty param.fromDate ? '&fromDate='.concat(param.fromDate) : ''}${not empty param.toDate ? '&toDate='.concat(param.toDate) : ''}" title="마지막 페이지">
                                <i class="bi bi-chevron-double-right"></i>
                            </a>
                        </li>
                    </ul>
                </nav>
            </c:if>
        </div>
    </div>
</div>

<%@ include file="../common/footer.jsp" %>
<%@ include file="../common/scripts.jsp" %>
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

    function resetFilters() {
        document.getElementById('component').value = '';
        document.getElementById('status').value = '';
        document.getElementById('fromDate').value = '';
        document.getElementById('toDate').value = '';
        document.querySelector('form').submit();
    }

    function changePageSize(newSize) {
        const url = new URL(window.location);
        if (newSize === '0') {
            url.searchParams.delete('size');
        } else {
            url.searchParams.set('size', newSize);
        }
        url.searchParams.set('page', '0');
        window.location.href = url.toString();
    }

    document.addEventListener('DOMContentLoaded', function() {
        const currentSize = '${param.size != null ? param.size : "20"}';
        const pageSizeSelect = document.getElementById('pageSize');
        if (pageSizeSelect) {
            if (currentSize === '' || currentSize === 'null' || currentSize === '0') {
                pageSizeSelect.value = '20';
            } else {
                pageSizeSelect.value = currentSize;
            }
        }
    });
</script>
</body>
</html>