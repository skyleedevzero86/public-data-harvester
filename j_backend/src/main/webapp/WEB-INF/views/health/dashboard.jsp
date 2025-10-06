<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<c:set var="pageTitle" value="시스템 헬스 대시보드" />
<c:set var="pageCSS" value="${['health-paged.css']}" />
<c:set var="pageJS" value="${['health-paged.js']}" />

<!DOCTYPE html>
<html lang="ko">
<head>
  <%@ include file="../common/head.jsp" %>
</head>
<body>
<%@ include file="../common/navigation.jsp" %>

<div class="container-fluid mt-4">
  <div class="d-flex justify-content-between align-items-center mb-4">
    <h2><i class="bi bi-heart-pulse"></i> 시스템 헬스 대시보드</h2>
    <div>
      <a href="/health/history" class="btn btn-outline-secondary">
        <i class="bi bi-clock-history"></i> 이력 보기
      </a>
    </div>
  </div>

  <div class="filter-controls">
    <div>
      <label for="groupBy">그룹핑:</label>
      <select id="groupBy" onchange="changeGroupBy()">
        <option value="component" ${currentGroupBy == 'component' ? 'selected' : ''}>컴포넌트별</option>
        <option value="status" ${currentGroupBy == 'status' ? 'selected' : ''}>상태별</option>
      </select>
    </div>
    <div>
      <label for="pageSize">페이지 크기:</label>
      <select id="pageSize" onchange="changePageSize()">
        <option value="5" ${currentSize == 5 ? 'selected' : ''}>5개</option>
        <option value="10" ${currentSize == 10 ? 'selected' : ''}>10개</option>
        <option value="20" ${currentSize == 20 ? 'selected' : ''}>20개</option>
        <option value="50" ${currentSize == 50 ? 'selected' : ''}>50개</option>
      </select>
    </div>
    <div>
      <button class="btn btn-outline-primary" onclick="refreshPage()">
        <i class="bi bi-arrow-clockwise"></i> 새로고침
      </button>
    </div>
  </div>

  <div class="row mb-4">
    <div class="col-xl-3 col-md-6 col-sm-6">
      <div class="card bg-primary text-white mb-4">
        <div class="card-body">
          <div class="d-flex justify-content-between align-items-center">
            <div>
              <div class="text-white-50 small">전체 상태</div>
              <div class="fs-4 fw-bold status-${systemHealth.overallStatus.code.toLowerCase()}">
                <i class="bi bi-${systemHealth.healthy ? 'check-circle' : 'exclamation-triangle'}"></i>
                ${systemHealth.overallStatus.code}
              </div>
            </div>
            <div class="icon-align">
              <i class="bi bi-server stats-icon"></i>
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
              <div class="text-white-50 small">총 컴포넌트</div>
              <div class="fs-4 fw-bold">
                ${systemHealth.totalComponents}
              </div>
            </div>
            <div class="icon-align">
              <i class="bi bi-gear-fill stats-icon"></i>
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
                ${systemHealth.healthyComponents}
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
                ${systemHealth.unhealthyComponents}
              </div>
            </div>
            <div class="icon-align">
              <i class="bi bi-x-circle-fill stats-icon"></i>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>

  <c:if test="${not empty componentGroups}">
    <div class="row mb-4">
      <div class="col-12">
        <div class="card health-card">
          <div class="card-header">
            <h5 class="mb-0"><i class="bi bi-collection"></i> 컴포넌트 그룹 정보</h5>
          </div>
          <div class="card-body">
            <div class="component-groups">
              <c:forEach var="group" items="${componentGroups}">
                <div class="group-card">
                  <div class="group-header">
                    <div>
                      <strong>${group.key}</strong>
                      <span class="group-status ${group.value.groupStatus.code}">
                          ${group.value.groupStatus.code}
                      </span>
                    </div>
                    <div class="group-stats">
                      <span>총 ${group.value.totalCount}개</span>
                      <span>정상 ${group.value.healthyCount}개</span>
                      <span>장애 ${group.value.unhealthyCount}개</span>
                    </div>
                  </div>
                  <div class="text-muted">${group.value.groupStatusDescription}</div>
                </div>
              </c:forEach>
            </div>
          </div>
        </div>
      </div>
    </div>
  </c:if>

  <div class="row mb-4">
    <div class="col-12">
      <div class="card health-card">
        <div class="card-header">
          <h5 class="mb-0"><i class="bi bi-gear"></i> 컴포넌트별 상태</h5>
        </div>
        <div class="card-body">
          <c:choose>
            <c:when test="${not empty systemHealth.components}">
              <div class="component-grid">
                <c:forEach var="component" items="${systemHealth.components}">
                  <div class="component-card ${component.healthy ? 'healthy' : 'unhealthy'}">
                    <div class="component-header">
                      <div class="component-name">${component.component}</div>
                      <div class="component-status ${component.status.code}">
                          ${component.status.code}
                      </div>
                    </div>
                    <div class="component-details">
                      <p class="mb-1">${component.message}</p>
                      <small>
                        응답시간: ${component.responseTime}ms |
                        <fmt:formatDate value="${component.checkedAtAsDate}" pattern="HH:mm:ss"/>
                      </small>
                    </div>
                  </div>
                </c:forEach>
              </div>
            </c:when>
            <c:otherwise>
              <div class="no-components">
                <i class="bi bi-inbox"></i>
                <p>표시할 컴포넌트가 없습니다.</p>
              </div>
            </c:otherwise>
          </c:choose>
        </div>
      </div>
    </div>
  </div>

  <c:if test="${pagination.totalPages > 1}">
    <div class="pagination-controls">
      <button class="btn btn-outline-primary" onclick="goToPage(0)" ${pagination.pageNumber == 0 ? 'disabled' : ''}>
        <i class="bi bi-chevron-double-left"></i> 첫 페이지
      </button>
      <button class="btn btn-outline-primary" onclick="goToPage(${pagination.previousPage})" ${!pagination.hasPrevious ? 'disabled' : ''}>
        <i class="bi bi-chevron-left"></i> 이전
      </button>
      <span class="mx-3">
            페이지 ${pagination.pageNumber + 1} / ${pagination.totalPages}
            (총 ${pagination.totalElements}개)
          </span>
      <button class="btn btn-outline-primary" onclick="goToPage(${pagination.nextPage})" ${!pagination.hasNext ? 'disabled' : ''}>
        다음 <i class="bi bi-chevron-right"></i>
      </button>
      <button class="btn btn-outline-primary" onclick="goToPage(${pagination.totalPages - 1})" ${pagination.pageNumber >= pagination.totalPages - 1 ? 'disabled' : ''}>
        마지막 페이지 <i class="bi bi-chevron-double-right"></i>
      </button>
    </div>
  </c:if>

  <div class="pagination-info">
    <small>
      현재 페이지: ${pagination.pageNumber + 1}/${pagination.totalPages} |
      총 ${pagination.totalElements}개 중 ${pagination.numberOfElements}개 표시
    </small>
  </div>

  <div class="row">
    <div class="col-12">
      <div class="card health-card">
        <div class="card-header">
          <h5 class="mb-0">
            <i class="bi bi-clock-history"></i> 최근 체크 이력 (24시간)
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
                <th>체크시간</th>
              </tr>
              </thead>
              <tbody>
              <c:forEach var="check" items="${recentChecks}">
                <tr>
                  <td><i class="bi bi-gear"></i> ${check.component}</td>
                  <td>
                    <span class="badge bg-${check.healthy ? 'success' : 'danger'}">
                        ${check.status.code}
                    </span>
                  </td>
                  <td>${check.message}</td>
                  <td>${check.responseTime}ms</td>
                  <td>
                    <fmt:formatDate value="${check.checkedAtAsDate}" pattern="MM-dd HH:mm:ss"/>
                  </td>
                </tr>
              </c:forEach>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  </div>
</div>

<button class="btn btn-primary refresh-btn" onclick="refreshPage()">
  <i class="bi bi-arrow-clockwise"></i>
</button>

<%@ include file="../common/footer.jsp" %>
<%@ include file="../common/scripts.jsp" %>

<script>
  let isLoading = false;

  function goToPage(page) {
    if (isLoading) {
      return;
    }

    isLoading = true;
    const url = new URL(window.location);
    url.searchParams.set('page', page);
    window.location.href = url.toString();
  }

  function changePageSize() {
    if (isLoading) {
      return;
    }

    isLoading = true;
    const pageSize = document.getElementById('pageSize').value;
    const url = new URL(window.location);
    url.searchParams.set('size', pageSize);
    url.searchParams.set('page', '0');
    window.location.href = url.toString();
  }

  function changeGroupBy() {
    if (isLoading) {
      return;
    }

    isLoading = true;
    const groupBy = document.getElementById('groupBy').value;
    const url = new URL(window.location);
    url.searchParams.set('groupBy', groupBy);
    url.searchParams.set('page', '0');
    window.location.href = url.toString();
  }

  function refreshPage() {
    if (isLoading) {
      return;
    }

    isLoading = true;
    window.location.reload();
  }
</script>
</body>
</html>