<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<c:set var="pageTitle" value="${component == 'all' ? '전체 컴포넌트' : component} 헬스 상태"/>
<c:set var="pageJS" value="${['health.js']}" />

<!DOCTYPE html>
<html>
<head>
  <%@ include file="../common/head.jsp" %>
</head>
<body>
<%@ include file="../common/navigation.jsp" %>

<div class="container-fluid mt-4">
  <div class="d-flex justify-content-between align-items-center mb-4">
    <h2>
      <i class="bi bi-gear"></i> ${component == 'all' ? '전체 컴포넌트' : component} 헬스 상태
    </h2>
    <div>
      <c:if test="${component != 'all'}">
        <a href="/health/component" class="btn btn-outline-info me-2">
          <i class="bi bi-list"></i> 전체 컴포넌트
        </a>
      </c:if>
      <a href="/health" class="btn btn-outline-secondary">
        <i class="bi bi-arrow-left"></i> 대시보드
      </a>
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
                  <c:set var="avgResponseTime" value="${avgResponseTime + check.responseTime}"/>
                </c:forEach>
                <c:if test="${healthChecks.numberOfElements > 0}">
                  <c:set var="avgResponseTime" value="${avgResponseTime / healthChecks.numberOfElements}"/>
                </c:if>
                <fmt:formatNumber value="${avgResponseTime}" pattern="#.##"/>ms
              </div>
            </div>
            <div class="icon-align">
              <i class="bi bi-clock stats-icon"></i>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
  <c:if test="${component == 'all'}">
    <div class="row mb-4">
      <div class="col-12">
        <div class="card">
          <div class="card-header">
            <h6 class="mb-0"><i class="bi bi-funnel"></i> 컴포넌트 필터</h6>
          </div>
          <div class="card-body">
            <div class="d-flex flex-wrap gap-2">
              <a href="/health/component" class="btn btn-outline-primary ${component == 'all' ? 'active' : ''}">
                <i class="bi bi-list"></i> 전체
              </a>
              <c:forEach var="comp" items="${components}">
                <a href="/health/component/${comp}" class="btn btn-outline-secondary">
                  <i class="bi bi-gear"></i> ${comp}
                </a>
              </c:forEach>
            </div>
          </div>
        </div>
      </div>
    </div>
  </c:if>
  <div class="row">
    <div class="col-12">
      <div class="card health-card">
        <div class="card-header">
          <h5 class="mb-0">
            <i class="bi bi-clock-history"></i> 체크 이력
            <c:if test="${component != 'all'}">
              <span class="badge bg-secondary ms-2">${component}</span>
            </c:if>
          </h5>
        </div>
        <div class="card-body">
          <div class="table-responsive">
            <table class="table table-hover">
              <thead>
              <tr>
                <c:if test="${component == 'all'}">
                  <th>컴포넌트</th>
                </c:if>
                <th>상태</th>
                <th>메시지</th>
                <th>응답시간</th>
                <th>체크 타입</th>
                <th>체크시간</th>
                <th>만료시간</th>
              </tr>
              </thead>
              <tbody>
              <c:choose>
                <c:when test="${empty healthChecks.content}">
                  <tr>
                    <td colspan="${component == 'all' ? '7' : '6'}" class="text-center text-muted">
                      <i class="bi bi-info-circle"></i> 체크 이력이 없습니다.
                    </td>
                  </tr>
                </c:when>
                <c:otherwise>
                  <c:forEach var="check" items="${healthChecks.content}">
                    <tr class="${check.healthy ? 'table-success' : 'table-danger'}">
                      <c:if test="${component == 'all'}">
                        <td>
                          <span class="badge bg-secondary">${check.component}</span>
                        </td>
                      </c:if>
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
                    </tr>
                  </c:forEach>
                </c:otherwise>
              </c:choose>
              </tbody>
            </table>
          </div>
          <div class="d-flex justify-content-between align-items-center mb-3">
            <div class="pagination-info">
                <span class="text-muted">
                  <c:choose>
                    <c:when test="${size == 0}">
                      총 <strong>${totalElements}</strong>개 항목 전체 표시
                    </c:when>
                    <c:otherwise>
                      총 <strong>${totalElements}</strong>개 항목 중
                      <strong>${(currentPage * size) + 1}</strong>-<strong>${(currentPage * size) + healthChecks.numberOfElements}</strong>개 표시
                      (페이지 ${currentPage + 1}/${totalPages})
                    </c:otherwise>
                  </c:choose>
                </span>
            </div>
            <div class="page-size-selector">
              <label for="pageSize" class="form-label me-2">페이지 크기:</label>
              <select id="pageSize" class="form-select form-select-sm" style="width: auto;" onchange="changePageSize(this.value)">
                <option value="10" ${size == 10 ? 'selected' : ''}>10개</option>
                <option value="20" ${size == 20 || (size == null || size == 0) ? 'selected' : ''}>20개</option>
                <option value="50" ${size == 50 ? 'selected' : ''}>50개</option>
                <option value="100" ${size == 100 ? 'selected' : ''}>100개</option>
                <option value="0" ${size == 0 ? 'selected' : ''}>전체</option>
              </select>
            </div>
          </div>
          <c:if test="${totalPages > 1 && size != 0}">
            <nav aria-label="페이지 네비게이션">
              <ul class="pagination justify-content-center">
                <li class="page-item ${currentPage == 0 ? 'disabled' : ''}">
                  <a class="page-link" href="?page=0&size=${size}" title="첫 페이지">
                    <i class="bi bi-chevron-double-left"></i>
                  </a>
                </li>
                <li class="page-item ${currentPage == 0 ? 'disabled' : ''}">
                  <a class="page-link" href="?page=${currentPage - 1}&size=${size}" title="이전 페이지">
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
                    <a class="page-link" href="?page=${pageNum}&size=${size}">${pageNum + 1}</a>
                  </li>
                </c:forEach>
                <c:if test="${endPage < totalPages - 1}">
                  <li class="page-item disabled">
                    <span class="page-link">...</span>
                  </li>
                </c:if>
                <li class="page-item ${currentPage == totalPages - 1 ? 'disabled' : ''}">
                  <a class="page-link" href="?page=${currentPage + 1}&size=${size}" title="다음 페이지">
                    <i class="bi bi-chevron-right"></i>
                  </a>
                </li>
                <li class="page-item ${currentPage == totalPages - 1 ? 'disabled' : ''}">
                  <a class="page-link" href="?page=${totalPages - 1}&size=${size}" title="마지막 페이지">
                    <i class="bi bi-chevron-double-right"></i>
                  </a>
                </li>
              </ul>
            </nav>
          </c:if>
        </div>
      </div>
    </div>
  </div>
</div>
<%@ include file="../common/footer.jsp" %>
<%@ include file="../common/scripts.jsp" %>
<script>
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
    const currentSize = '${size}';
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