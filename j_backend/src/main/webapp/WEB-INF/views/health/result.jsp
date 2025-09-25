<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8"%> <%@ taglib prefix="c"
                                           uri="http://java.sun.com/jsp/jstl/core" %> <%@ taglib prefix="fmt"
                                                                                                 uri="http://java.sun.com/jsp/jstl/fmt" %>

<c:set var="pageTitle" value="헬스 체크 결과" />
<c:set var="pageCSS" value="${['health.css']}" />

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
      <a class="nav-link" href="/health">
        <i class="bi bi-speedometer2"></i> 헬스 대시보드
      </a>
      <a class="nav-link" href="/health/status">
        <i class="bi bi-heart-pulse"></i> 상태 조회
      </a>
      <a class="nav-link" href="/health/metrics">
        <i class="bi bi-graph-up"></i> 메트릭
      </a>
      <a class="nav-link" href="/health/history">
        <i class="bi bi-clock-history"></i> 이력 조회
      </a>
      <a class="nav-link" href="/members/logout">
        <i class="bi bi-box-arrow-right"></i> 로그아웃
      </a>
    </div>
  </div>
</nav>

<div class="container-fluid mt-4">
  <div class="d-flex justify-content-between align-items-center mb-4">
    <h2><i class="bi bi-check-circle"></i> 헬스 체크 결과</h2>
    <div>
      <a href="/health/check" class="btn btn-primary">
        <i class="bi bi-arrow-clockwise"></i> 다시 체크
      </a>
      <a href="/health" class="btn btn-outline-secondary">
        <i class="bi bi-arrow-left"></i> 대시보드
      </a>
    </div>
  </div>

  <div class="row mb-4">
    <div class="col-12">
      <div class="card">
        <div class="card-header">
          <h5 class="mb-0">
            <i class="bi bi-clipboard-data"></i> 체크 결과 요약
          </h5>
        </div>
        <div class="card-body">
          <div class="row">
            <div class="col-md-3">
              <div class="text-center">
                <div
                        class="status-indicator status-${overallStatus.healthy ? 'up' : 'down'} mb-2"
                ></div>
                <h6 class="text-muted">전체 상태</h6>
                <h4
                        class="${overallStatus.healthy ? 'text-success' : 'text-danger'}"
                >
                  ${overallStatus.status}
                </h4>
              </div>
            </div>
            <div class="col-md-3">
              <div class="text-center">
                <i
                        class="bi bi-gear-fill text-primary mb-2"
                        style="font-size: 2rem"
                ></i>
                <h6 class="text-muted">총 컴포넌트</h6>
                <h4>${totalComponents}</h4>
              </div>
            </div>
            <div class="col-md-3">
              <div class="text-center">
                <i
                        class="bi bi-check-circle-fill text-success mb-2"
                        style="font-size: 2rem"
                ></i>
                <h6 class="text-muted">정상</h6>
                <h4 class="text-success">${healthyComponents}</h4>
              </div>
            </div>
            <div class="col-md-3">
              <div class="text-center">
                <i
                        class="bi bi-x-circle-fill text-danger mb-2"
                        style="font-size: 2rem"
                ></i>
                <h6 class="text-muted">장애</h6>
                <h4 class="text-danger">${unhealthyComponents}</h4>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>

  <div class="row">
    <div class="col-12">
      <div class="card">
        <div class="card-header">
          <h5 class="mb-0">
            <i class="bi bi-list-check"></i> 컴포넌트별 상세 결과
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
                <th>체크 시간</th>
                <th>상세</th>
              </tr>
              </thead>
              <tbody>
              <c:forEach var="result" items="${checkResults}">
                <tr
                        class="${result.healthy ? 'table-success' : 'table-danger'}"
                >
                  <td><i class="bi bi-gear"></i> ${result.component}</td>
                  <td>
                          <span
                                  class="badge bg-${result.healthy ? 'success' : 'danger'}"
                          >
                            <i
                                    class="bi bi-${result.healthy ? 'check-circle' : 'x-circle'}"
                            ></i>
                            ${result.status}
                          </span>
                  </td>
                  <td>
                    <c:choose>
                      <c:when test="${not empty result.message}">
                        ${result.message}
                      </c:when>
                      <c:otherwise>
                        <span class="text-muted">메시지 없음</span>
                      </c:otherwise>
                    </c:choose>
                  </td>
                  <td>
                          <span
                                  class="badge bg-${result.responseTime < 1000 ? 'success' : result.responseTime < 3000 ? 'warning' : 'danger'}"
                          >
                            ${result.responseTime}ms
                          </span>
                  </td>
                  <td>
                    <fmt:formatDate
                            value="${result.checkTime}"
                            pattern="MM-dd HH:mm:ss"
                    />
                  </td>
                  <td>
                    <c:if test="${not empty result.details}">
                      <button
                              class="btn btn-sm btn-outline-info"
                              data-bs-toggle="modal"
                              data-bs-target="#detailModal"
                              onclick="showDetails('${result.component}', '${result.details}')"
                      >
                        <i class="bi bi-info-circle"></i> 상세
                      </button>
                    </c:if>
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

  <div class="row mt-4">
    <div class="col-12">
      <div class="card">
        <div class="card-header">
          <h5 class="mb-0">
            <i class="bi bi-info-circle"></i> 체크 실행 정보
          </h5>
        </div>
        <div class="card-body">
          <div class="row">
            <div class="col-md-4">
              <strong>체크 시작 시간:</strong><br />
              <fmt:formatDate
                      value="${checkStartTime}"
                      pattern="yyyy-MM-dd HH:mm:ss"
              />
            </div>
            <div class="col-md-4">
              <strong>체크 완료 시간:</strong><br />
              <fmt:formatDate
                      value="${checkEndTime}"
                      pattern="yyyy-MM-dd HH:mm:ss"
              />
            </div>
            <div class="col-md-4">
              <strong>총 소요 시간:</strong><br />
              ${totalDuration}ms
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</div>

<div class="modal fade" id="detailModal" tabindex="-1">
  <div class="modal-dialog modal-lg">
    <div class="modal-content">
      <div class="modal-header">
        <h5 class="modal-title" id="detailModalTitle">
          컴포넌트 상세 정보
        </h5>
        <button
                type="button"
                class="btn-close"
                data-bs-dismiss="modal"
        ></button>
      </div>
      <div class="modal-body">
        <pre id="detailModalBody"></pre>
      </div>
      <div class="modal-footer">
        <button
                type="button"
                class="btn btn-secondary"
                data-bs-dismiss="modal"
        >
          닫기
        </button>
      </div>
    </div>
  </div>
</div>

<%@ include file="../common/footer.jsp" %> <%@ include
        file="../common/scripts.jsp" %>
<script>
  function showDetails(component, details) {
    document.getElementById("detailModalTitle").textContent =
            component + " 상세 정보";
    document.getElementById("detailModalBody").textContent = details;
  }

  function refreshResults() {
    location.reload();
  }

  setInterval(refreshResults, 60000);
</script>
</body>
</html>