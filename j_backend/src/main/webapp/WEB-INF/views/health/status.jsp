<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<c:set var="pageTitle" value="시스템 상태" />
<c:set var="pageCSS" value="${['health.css']}" />
<c:set var="pageJS" value="${['health.js']}" />

<%@ include file="../common/header.jsp" %>
<%@ include file="../common/navigation.jsp" %>
<%@ include file="../common/scripts.jsp" %>

<div class="container mt-4">
    <div class="d-flex justify-content-between align-items-center mb-4">
        <h2><i class="bi bi-heart-pulse"></i> 시스템 상태</h2>
        <div>
            <button id="refreshButton" class="btn btn-outline-primary">
                <i class="bi bi-arrow-clockwise"></i> 새로고침
            </button>
        </div>
    </div>

    <c:if test="${not empty message}">
        <div class="alert alert-info alert-dismissible fade show" role="alert">
            <i class="bi bi-info-circle"></i> ${message}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
    </c:if>

    <div class="row mb-4">
        <div class="col-12">
            <div class="card">
                <div class="card-body text-center">
                    <h5 class="card-title">전체 시스템 상태</h5>
                    <div class="status-card">
                        <c:choose>
                            <c:when test="${systemHealth.overallStatus.toString() == 'UP'}">
                                <i class="bi bi-check-circle-fill status-up stats-icon"></i>
                                <h3 class="status-up">시스템 정상</h3>
                                <p class="text-muted">모든 서비스가 정상적으로 작동 중입니다.</p>
                            </c:when>
                            <c:when test="${systemHealth.overallStatus.toString() == 'DOWN'}">
                                <i class="bi bi-x-circle-fill status-down stats-icon"></i>
                                <h3 class="status-down">시스템 오류</h3>
                                <p class="text-muted">일부 서비스에 문제가 발생했습니다.</p>
                            </c:when>
                            <c:when test="${systemHealth.overallStatus.toString() == 'WARNING'}">
                                <i class="bi bi-exclamation-triangle-fill status-warning stats-icon"></i>
                                <h3 class="status-warning">시스템 경고</h3>
                                <p class="text-muted">일부 서비스에 주의가 필요합니다.</p>
                            </c:when>
                            <c:otherwise>
                                <i class="bi bi-question-circle-fill status-unknown stats-icon"></i>
                                <h3 class="status-unknown">상태 불명</h3>
                                <p class="text-muted">시스템 상태를 확인할 수 없습니다.</p>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <c:if test="${not empty systemHealth}">
        <div class="row mb-4">
            <div class="col-12">
                <div class="card">
                    <div class="card-header">
                        <h5 class="mb-0"><i class="bi bi-info-circle"></i> 시스템 상태 상세</h5>
                    </div>
                    <div class="card-body">
                        <div class="row">
                            <div class="col-md-3 mb-3">
                                <div class="text-center">
                                    <h6 class="text-muted">전체 상태</h6>
                                    <c:choose>
                                        <c:when test="${systemHealth.overallStatus.toString() == 'UP'}">
                                            <span class="badge bg-success fs-6">${systemHealth.overallStatusDescription}</span>
                                        </c:when>
                                        <c:when test="${systemHealth.overallStatus.toString() == 'DOWN'}">
                                            <span class="badge bg-danger fs-6">${systemHealth.overallStatusDescription}</span>
                                        </c:when>
                                        <c:when test="${systemHealth.overallStatus.toString() == 'WARNING'}">
                                            <span class="badge bg-warning fs-6">${systemHealth.overallStatusDescription}</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="badge bg-secondary fs-6">${systemHealth.overallStatusDescription}</span>
                                        </c:otherwise>
                                    </c:choose>
                                </div>
                            </div>
                            <div class="col-md-3 mb-3">
                                <div class="text-center">
                                    <h6 class="text-muted">총 컴포넌트</h6>
                                    <h4 class="mb-0">${systemHealth.totalComponents}</h4>
                                </div>
                            </div>
                            <div class="col-md-3 mb-3">
                                <div class="text-center">
                                    <h6 class="text-muted">정상 컴포넌트</h6>
                                    <h4 class="mb-0 text-success">${systemHealth.healthyComponents}</h4>
                                </div>
                            </div>
                            <div class="col-md-3 mb-3">
                                <div class="text-center">
                                    <h6 class="text-muted">헬스 비율</h6>
                                    <h4 class="mb-0">
                                        <fmt:formatNumber value="${systemHealth.healthPercentage}" pattern="0.0"/>%
                                    </h4>
                                </div>
                            </div>
                        </div>
                        <c:if test="${not empty systemHealth.checkedAt}">
                            <div class="row">
                                <div class="col-12 text-center">
                                    <small class="text-muted">
                                        마지막 확인: ${fn:substring(systemHealth.checkedAt, 0, 19)}
                                    </small>
                                </div>
                            </div>
                        </c:if>
                    </div>
                </div>
            </div>
        </div>
    </c:if>

    <c:if test="${not empty systemHealth.components}">
        <div class="row">
            <div class="col-12">
                <div class="card">
                    <div class="card-header">
                        <h5 class="mb-0"><i class="bi bi-gear"></i> 컴포넌트 상태</h5>
                    </div>
                    <div class="card-body">
                        <div class="row" id="componentSummary">
                            <c:forEach var="component" items="${systemHealth.components}" varStatus="status">
                                <c:if test="${status.index < 6}">
                                    <div class="col-md-4 col-lg-2 mb-3">
                                        <div class="component-summary-card text-center">
                                            <div class="component-icon mb-2">
                                                <c:choose>
                                                    <c:when test="${component.status.toString() == 'UP'}">
                                                        <i class="bi bi-check-circle-fill text-success fs-4"></i>
                                                    </c:when>
                                                    <c:when test="${component.status.toString() == 'DOWN'}">
                                                        <i class="bi bi-x-circle-fill text-danger fs-4"></i>
                                                    </c:when>
                                                    <c:when test="${component.status.toString() == 'WARNING'}">
                                                        <i class="bi bi-exclamation-triangle-fill text-warning fs-4"></i>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <i class="bi bi-question-circle-fill text-secondary fs-4"></i>
                                                    </c:otherwise>
                                                </c:choose>
                                            </div>
                                            <h6 class="component-name mb-1">${component.component}</h6>
                                            <c:if test="${not empty component.responseTime}">
                                                <small class="text-muted">${component.responseTime}ms</small>
                                            </c:if>
                                        </div>
                                    </div>
                                </c:if>
                            </c:forEach>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </c:if>

    <c:if test="${not empty metrics}">
        <div class="row mt-4">
            <div class="col-12">
                <div class="card">
                    <div class="card-header">
                        <h5 class="mb-0"><i class="bi bi-graph-up"></i> 시스템 메트릭스</h5>
                    </div>
                    <div class="card-body">
                        <div class="row">
                            <c:if test="${not empty metrics.cpu}">
                                <div class="col-md-3 mb-3">
                                    <div class="metrics-card">
                                        <h6>CPU 사용률</h6>
                                        <div class="metrics-value" id="cpuUsage"><fmt:formatNumber value="${metrics.cpu}" pattern="0.0"/>%</div>
                                        <div class="metrics-label">현재 사용률</div>
                                    </div>
                                </div>
                            </c:if>
                            <c:if test="${not empty metrics.memory}">
                                <div class="col-md-3 mb-3">
                                    <div class="metrics-card">
                                        <h6>메모리 사용률</h6>
                                        <div class="metrics-value" id="memoryUsage"><fmt:formatNumber value="${metrics.memory}" pattern="0.0"/>%</div>
                                        <div class="metrics-label">현재 사용률</div>
                                    </div>
                                </div>
                            </c:if>
                            <c:if test="${not empty metrics.disk}">
                                <div class="col-md-3 mb-3">
                                    <div class="metrics-card">
                                        <h6>디스크 사용률</h6>
                                        <div class="metrics-value" id="diskUsage"><fmt:formatNumber value="${metrics.disk}" pattern="0.0"/>%</div>
                                        <div class="metrics-label">현재 사용률</div>
                                    </div>
                                </div>
                            </c:if>
                            <c:if test="${not empty metrics.averageResponseTime}">
                                <div class="col-md-3 mb-3">
                                    <div class="metrics-card">
                                        <h6>평균 응답 시간</h6>
                                        <div class="metrics-value" id="responseTime">
                                            <fmt:formatNumber value="${metrics.averageResponseTime}" pattern="0.0"/>ms
                                        </div>
                                        <div class="metrics-label">평균 응답 시간</div>
                                    </div>
                                </div>
                            </c:if>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </c:if>

    <div class="row mt-4">
        <div class="col-12">
            <div class="card">
                <div class="card-header">
                    <h5 class="mb-0"><i class="bi bi-activity"></i> 실시간 메트릭스</h5>
                </div>
                <div class="card-body">
                    <div id="realtimeMetrics">
                        <div class="row">
                            <div class="col-md-4">
                                <div class="card">
                                    <div class="card-header">
                                        <h6 class="mb-0">응답 시간</h6>
                                    </div>
                                    <div class="card-body">
                                        <canvas id="responseTimeChart" width="400" height="200"></canvas>
                                    </div>
                                </div>
                            </div>
                            <div class="col-md-4">
                                <div class="card">
                                    <div class="card-header">
                                        <h6 class="mb-0">가용성</h6>
                                    </div>
                                    <div class="card-body">
                                        <canvas id="availabilityChart" width="400" height="200"></canvas>
                                    </div>
                                </div>
                            </div>
                            <div class="col-md-4">
                                <div class="card">
                                    <div class="card-header">
                                        <h6 class="mb-0">성공률</h6>
                                    </div>
                                    <div class="card-body">
                                        <canvas id="successRateChart" width="400" height="200"></canvas>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<%@ include file="../common/footer.jsp" %>
</body>
</html>