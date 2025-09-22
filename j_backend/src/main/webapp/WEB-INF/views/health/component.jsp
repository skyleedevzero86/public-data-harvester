<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="ko">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <title>${component} 헬스 상태</title>
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css" rel="stylesheet" />
  <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css" rel="stylesheet" />
  <style>
    .status-up {
      color: #28a745;
    }
    .status-down {
      color: #dc3545;
    }
    .status-unknown {
      color: #ffc107;
    }
    .health-card {
      transition: transform 0.2s;
    }
    .health-card:hover {
      transform: translateY(-2px);
    }
  </style>
</head>
<body>
<div class="container-fluid">
  <div class="row mb-4">
    <div class="col-12">
      <div class="d-flex justify-content-between align-items-center">
        <h1><i class="fas fa-cog"></i> ${component} 헬스 상태</h1>
        <div>
          <a href="/health" class="btn btn-outline-secondary">
            <i class="fas fa-arrow-left"></i> 대시보드
          </a>
          <a href="/health/check" class="btn btn-primary">
            <i class="fas fa-sync-alt"></i> 수동 체크
          </a>
        </div>
      </div>
    </div>
  </div>
  <div class="row mb-4">
    <div class="col-md-3">
      <div class="card health-card">
        <div class="card-body text-center">
          <h3>${totalElements}</h3>
          <p class="text-muted">총 체크 수</p>
        </div>
      </div>
    </div>
    <div class="col-md-3">
      <div class="card health-card">
        <div class="card-body text-center">
          <h3 class="status-up">
            <c:set var="upCount" value="0" />
            <c:forEach var="check" items="${healthChecks.content}">
              <c:if test="${check.healthy}">
                <c:set var="upCount" value="${upCount + 1}" />
              </c:if>
            </c:forEach>
            ${upCount}
          </h3>
          <p class="text-muted">정상</p>
        </div>
      </div>
    </div>
    <div class="col-md-3">
      <div class="card health-card">
        <div class="card-body text-center">
          <h3 class="status-down">
            <c:set var="downCount" value="0" />
            <c:forEach var="check" items="${healthChecks.content}">
              <c:if test="${!check.healthy}">
                <c:set var="downCount" value="${downCount + 1}" />
              </c:if>
            </c:forEach>
            ${downCount}
          </h3>
          <p class="text-muted">장애</p>
        </div>
      </div>
    </div>
    <div class="col-md-3">
      <div class="card health-card">
        <div class="card-body text-center">
          <h3>
            <c:set var="avgResponseTime" value="0" />
            <c:forEach var="check" items="${healthChecks.content}">
              <c:set var="avgResponseTime" value="${avgResponseTime + check.responseTime}" />
            </c:forEach>
            <c:if test="${healthChecks.numberOfElements > 0}">
              <c:set var="avgResponseTime" value="${avgResponseTime / healthChecks.numberOfElements}" />
            </c:if>
            <fmt:formatNumber value="${avgResponseTime}" pattern="#.##" />ms
          </h3>
          <p class="text-muted">평균 응답시간</p>
        </div>
      </div>
    </div>
  </div>
  <div class="row">
    <div class="col-12">
      <div class="card health-card">
        <div class="card-header">
          <h5 class="mb-0"><i class="fas fa-history"></i> 체크 이력</h5>
        </div>
        <div class="card-body">
          <div class="table-responsive">
            <table class="table table-hover">
              <thead>
              <tr>
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
                          <span class="badge bg-${check.healthy ? 'success' : 'danger'}">
                            <i class="fas fa-${check.healthy ? 'check-circle' : 'times-circle'}"></i>
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
          <c:if test="${totalPages > 1}">
            <nav aria-label="페이지 네비게이션">
              <ul class="pagination justify-content-center">
                <li class="page-item ${currentPage == 0 ? 'disabled' : ''}">
                  <a class="page-link" href="?page=${currentPage - 1}&size=20">이전</a>
                </li>
                <c:forEach begin="0" end="${totalPages - 1}" var="pageNum">
                  <li class="page-item ${currentPage == pageNum ? 'active' : ''}">
                    <a class="page-link" href="?page=${pageNum}&size=20">${pageNum + 1}</a>
                  </li>
                </c:forEach>
                <li class="page-item ${currentPage == totalPages - 1 ? 'disabled' : ''}">
                  <a class="page-link" href="?page=${currentPage + 1}&size=20">다음</a>
                </li>
              </ul>
            </nav>
          </c:if>
        </div>
      </div>
    </div>
  </div>
</div>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/js/bootstrap.bundle.min.js"></script>
<script>
  document.addEventListener("DOMContentLoaded", function () {
    const cards = document.querySelectorAll(".health-card");
    cards.forEach((card, index) => {
      setTimeout(() => {
        card.style.opacity = "0";
        card.style.transform = "translateY(20px)";
        card.style.transition = "all 0.5s ease";
        setTimeout(() => {
          card.style.opacity = "1";
          card.style.transform = "translateY(0)";
        }, 100);
      }, index * 100);
    });
  });
</script>
</body>
</html>