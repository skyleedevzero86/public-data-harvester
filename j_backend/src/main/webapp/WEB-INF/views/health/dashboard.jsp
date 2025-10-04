<%@ page language="java" contentType="text/html; charset=UTF-8"
pageEncoding="UTF-8"%> <%@ taglib prefix="c"
uri="http://java.sun.com/jsp/jstl/core" %> <%@ taglib prefix="fmt"
uri="http://java.sun.com/jsp/jstl/fmt" %>

<c:set var="pageTitle" value="시스템 헬스 대시보드" />
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
        <h2><i class="bi bi-heart-pulse"></i> 시스템 헬스 대시보드</h2>
        <div>
          <a href="/health/check" class="btn btn-primary">
            <i class="bi bi-arrow-clockwise"></i> 수동 체크
          </a>
          <a href="/health/history" class="btn btn-outline-secondary">
            <i class="bi bi-clock-history"></i> 이력 보기
          </a>
        </div>
      </div>
      <div class="row mb-4">
        <div class="col-xl-3 col-md-6 col-sm-6">
          <div class="card bg-primary text-white mb-4">
            <div class="card-body">
              <div class="d-flex justify-content-between align-items-center">
                <div>
                  <div class="text-white-50 small">전체 상태</div>
                  <div
                    class="fs-4 fw-bold status-${systemHealth.overallStatus.code.toLowerCase()}"
                  >
                    <i
                      class="bi bi-${systemHealth.healthy ? 'check-circle' : 'exclamation-triangle'}"
                    ></i>
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

      <div class="row mb-4">
        <div class="col-12">
          <div class="card health-card">
            <div class="card-header">
              <h5 class="mb-0">
                <i class="bi bi-speedometer2"></i> 시스템 상태 개요
              </h5>
            </div>
            <div class="card-body">
              <div class="row">
                <div class="col-md-6">
                  <h6 class="text-muted">전체 상태 설명</h6>
                  <p class="mb-3">${systemHealth.overallStatusDescription}</p>
                </div>
                <div class="col-md-6">
                  <h6 class="text-muted">헬스 비율</h6>
                  <div class="progress" style="height: 25px">
                    <div
                      class="progress-bar bg-success"
                      role="progressbar"
                      style="width: ${systemHealth.healthPercentage}%"
                    >
                      <fmt:formatNumber
                        value="${systemHealth.healthPercentage}"
                        pattern="#.##"
                      />%
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
      <div class="row mb-4">
        <div class="col-12">
          <div class="card health-card">
            <div class="card-header">
              <h5 class="mb-0"><i class="bi bi-gear"></i> 컴포넌트별 상태</h5>
            </div>
            <div class="card-body">
              <div class="row">
                <c:forEach var="component" items="${systemHealth.components}">
                  <div class="col-md-6 col-lg-4 mb-3">
                    <div
                      class="card border-${component.healthy ? 'success' : 'danger'}"
                    >
                      <div class="card-body">
                        <div class="component-status">
                          <i
                            class="bi bi-${component.healthy ? 'check-circle' : 'x-circle'} status-${component.status.code.toLowerCase()}"
                          ></i>
                          <strong>${component.component}</strong>
                        </div>
                        <p class="text-muted mb-1">${component.message}</p>
                        <small class="text-muted">
                          응답시간: ${component.responseTime}ms |
                          <fmt:formatDate
                            value="${component.checkedAtAsDate}"
                            pattern="HH:mm:ss"
                          />
                        </small>
                      </div>
                    </div>
                  </div>
                </c:forEach>
              </div>
            </div>
          </div>
        </div>
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
                          <span
                            class="badge bg-${check.healthy ? 'success' : 'danger'}"
                          >
                            ${check.status.code}
                          </span>
                        </td>
                        <td>${check.message}</td>
                        <td>${check.responseTime}ms</td>
                        <td>
                          <fmt:formatDate
                            value="${check.checkedAtAsDate}"
                            pattern="MM-dd HH:mm:ss"
                          />
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
    <button class="btn btn-primary refresh-btn" onclick="location.reload()">
      <i class="bi bi-arrow-clockwise"></i>
    </button>

    <%@ include file="../common/footer.jsp" %> <%@ include
    file="../common/scripts.jsp" %>
    <script>
      setInterval(function () {
        location.reload();
      }, 300000);
    </script>
  </body>
</html>
