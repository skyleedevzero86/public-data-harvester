<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<c:set var="pageTitle" value="시스템 상태" />
<c:set var="pageCSS" value="${['health.css']}" />
<c:set var="pageJS" value="${['health.js']}" />

<%@ include file="../common/header.jsp" %>
<%@ include file="../common/navigation.jsp" %>

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
                            <c:when test="${overallStatus == 'UP'}">
                                <i class="bi bi-check-circle-fill status-up stats-icon pulse"></i>
                                <h3 class="status-up">시스템 정상</h3>
                                <p class="text-muted">모든 서비스가 정상적으로 작동 중입니다.</p>
                            </c:when>
                            <c:when test="${overallStatus == 'DOWN'}">
                                <i class="bi bi-x-circle-fill status-down stats-icon"></i>
                                <h3 class="status-down">시스템 오류</h3>
                                <p class="text-muted">일부 서비스에 문제가 발생했습니다.</p>
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

    <div class="row">
        <c:forEach var="component" items="${components}">
            <div class="col-md-6 col-lg-4 mb-4">
                <div class="card h-100">
                    <div class="card-body">
                        <div class="d-flex justify-content-between align-items-center mb-3">
                            <h6 class="card-title mb-0">${component.name}</h6>
                            <c:choose>
                                <c:when test="${component.status == 'UP'}">
                                    <span class="badge bg-success">정상</span>
                                </c:when>
                                <c:when test="${component.status == 'DOWN'}">
                                    <span class="badge bg-danger">오류</span>
                                </c:when>
                                <c:otherwise>
                                    <span class="badge bg-warning">불명</span>
                                </c:otherwise>
                            </c:choose>
                        </div>

                        <div class="component-details">
                            <c:if test="${not empty component.responseTime}">
                                <div class="mb-2">
                                    <small class="text-muted">응답 시간:</small>
                                    <span class="response-time ${component.responseTime < 500 ? 'fast' : component.responseTime < 1000 ? 'medium' : 'slow'}">
                                        ${component.responseTime}ms
                                    </span>
                                </div>
                            </c:if>

                            <c:if test="${not empty component.lastChecked}">
                                <div class="mb-2">
                                    <small class="text-muted">마지막 확인:</small>
                                    <small>${component.lastChecked}</small>
                                </div>
                            </c:if>

                            <c:if test="${not empty component.details}">
                                <div class="mb-2">
                                    <small class="text-muted">상세 정보:</small>
                                    <small>${component.details}</small>
                                </div>
                            </c:if>
                        </div>
                    </div>
                </div>
            </div>
        </c:forEach>
    </div>

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
                                        <div class="metrics-value" id="cpuUsage">${metrics.cpu}%</div>
                                        <div class="metrics-label">현재 사용률</div>
                                    </div>
                                </div>
                            </c:if>

                            <c:if test="${not empty metrics.memory}">
                                <div class="col-md-3 mb-3">
                                    <div class="metrics-card">
                                        <h6>메모리 사용률</h6>
                                        <div class="metrics-value" id="memoryUsage">${metrics.memory}%</div>
                                        <div class="metrics-label">현재 사용률</div>
                                    </div>
                                </div>
                            </c:if>

                            <c:if test="${not empty metrics.disk}">
                                <div class="col-md-3 mb-3">
                                    <div class="metrics-card">
                                        <h6>디스크 사용률</h6>
                                        <div class="metrics-value" id="diskUsage">${metrics.disk}%</div>
                                        <div class="metrics-label">현재 사용률</div>
                                    </div>
                                </div>
                            </c:if>

                            <c:if test="${not empty metrics.responseTime}">
                                <div class="col-md-3 mb-3">
                                    <div class="metrics-card">
                                        <h6>평균 응답 시간</h6>
                                        <div class="metrics-value" id="responseTime">${metrics.responseTime}ms</div>
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
                            <div class="col-md-6">
                                <canvas id="cpuChart" width="400" height="200"></canvas>
                            </div>
                            <div class="col-md-6">
                                <canvas id="memoryChart" width="400" height="200"></canvas>
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