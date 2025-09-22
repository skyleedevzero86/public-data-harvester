<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="ko">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <title>시스템 헬스 대시보드</title>
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css" rel="stylesheet" />
  <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css" rel="stylesheet" />
  <style>
    .health-card {
      transition: transform 0.2s;
    }
    .health-card:hover {
      transform: translateY(-2px);
    }
    .status-up {
      color: #28a745;
    }
    .status-down {
      color: #dc3545;
    }
    .status-unknown {
      color: #ffc107;
    }
    .component-status {
      display: flex;
      align-items: center;
      gap: 8px;
    }
    .refresh-btn {
      position: fixed;
      bottom: 20px;
      right: 20px;
      z-index: 1000;
    }
  </style>
</head>
<body>
<div class="container-fluid">
  <div class="row mb-4">
    <div class="col-12">
      <div class="d-flex justify-content-between align-items-center">
        <h1><i class="fas fa-heartbeat"></i> 시스템 헬스 대시보드</h1>
        <div>
          <a href="/health/check" class="btn btn-primary">
            <i class="fas fa-sync-alt"></i> 수동 체크
          </a>
          <a href="/health/history" class="btn btn-outline-secondary">
            <i class="fas fa-history"></i> 이력 보기
          </a>
        </div>
      </div>
    </div>
  </div>
  <div class="row mb-4">
    <div class="col-12">
      <div class="card health-card">
        <div class="card-header">
          <h5 class="mb-0">
            <i class="fas fa-server"></i> 전체 시스템 상태
          </h5>
        </div>
        <div class="card-body">
          <div class="row">
            <div class="col-md-3">
              <div class="text-center">
                <h2 class="status-${systemHealth.overallStatus.code.toLowerCase()}">
                  <i class="fas fa-${systemHealth.healthy ? 'check-circle' : 'exclamation-triangle'}"></i>
                  ${systemHealth.overallStatus.code}
                </h2>
                <p class="text-muted">
                  ${systemHealth.overallStatusDescription}
                </p>
              </div>
            </div>
            <div class="col-md-3">
              <div class="text-center">
                <h3>${systemHealth.totalComponents}</h3>
                <p class="text-muted">총 컴포넌트</p>
              </div>
            </div>
            <div class="col-md-3">
              <div class="text-center">
                <h3 class="status-up">${systemHealth.healthyComponents}</h3>
                <p class="text-muted">정상</p>
              </div>
            </div>
            <div class="col-md-3">
              <div class="text-center">
                <h3 class="status-down">
                  ${systemHealth.unhealthyComponents}
                </h3>
                <p class="text-muted">장애</p>
              </div>
            </div>
          </div>
          <div class="row mt-3">
            <div class="col-12">
              <div class="progress" style="height: 20px">
                <div class="progress-bar bg-success" role="progressbar" style="width: ${systemHealth.healthPercentage}%">
                  <fmt:formatNumber value="${systemHealth.healthPercentage}" pattern="#.##"/>%
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
          <h5 class="mb-0"><i class="fas fa-cogs"></i> 컴포넌트별 상태</h5>
        </div>
        <div class="card-body">
          <div class="row">
            <c:forEach var="component" items="${systemHealth.components}">
              <div class="col-md-6 col-lg-4 mb-3">
                <div class="card border-${component.healthy ? 'success' : 'danger'}">
                  <div class="card-body">
                    <div class="component-status">
                      <i class="fas fa-${component.healthy ? 'check-circle' : 'times-circle'} status-${component.status.code.toLowerCase()}"></i>
                      <strong>${component.component}</strong>
                    </div>
                    <p class="text-muted mb-1">${component.message}</p>
                    <small class="text-muted">
                      응답시간: ${component.responseTime}ms |
                      <fmt:formatDate value="${component.checkedAtAsDate}" pattern="HH:mm:ss"/>
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
            <i class="fas fa-clock"></i> 최근 체크 이력 (24시간)
          </h5>
        </div>
        <div class="card-body">
          <div class="table-responsive">
            <table class="table table-hover">
              <thead>
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
                  <td><i class="fas fa-cog"></i> ${check.component}</td>
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
<button class="btn btn-primary refresh-btn" onclick="location.reload()">
  <i class="fas fa-sync-alt"></i>
</button>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/js/bootstrap.bundle.min.js"></script>
<script>
  setInterval(function () {
    location.reload();
  }, 300000);
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