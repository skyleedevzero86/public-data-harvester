<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="ko">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <title>헬스 체크 결과</title>
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
    .result-card {
      transition: transform 0.2s;
    }
    .result-card:hover {
      transform: translateY(-2px);
    }
  </style>
</head>
<body>
<div class="container-fluid">
  <div class="row mb-4">
    <div class="col-12">
      <div class="d-flex justify-content-between align-items-center">
        <h1><i class="fas fa-clipboard-check"></i> 헬스 체크 결과</h1>
        <div>
          <a href="/health" class="btn btn-outline-secondary">
            <i class="fas fa-arrow-left"></i> 대시보드
          </a>
          <a href="/health/check" class="btn btn-primary">
            <i class="fas fa-sync-alt"></i> 다시 체크
          </a>
        </div>
      </div>
    </div>
  </div>
  <c:if test="${not empty message}">
    <div class="row mb-4">
      <div class="col-12">
        <div class="alert alert-success alert-dismissible fade show" role="alert">
          <i class="fas fa-check-circle"></i> ${message}
          <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
      </div>
    </div>
  </c:if>
  <c:if test="${not empty systemHealth}">
    <div class="row mb-4">
      <div class="col-12">
        <div class="card result-card">
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
                  <h3 class="status-up">
                      ${systemHealth.healthyComponents}
                  </h3>
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
                  <div class="progress-bar bg-${systemHealth.healthy ? 'success' : 'danger'}" role="progressbar" style="width: ${systemHealth.healthPercentage}%">
                    <fmt:formatNumber value="${systemHealth.healthPercentage}" pattern="#.##"/>%
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    <div class="row">
      <div class="col-12">
        <div class="card result-card">
          <div class="card-header">
            <h5 class="mb-0">
              <i class="fas fa-cogs"></i> 컴포넌트별 상태
            </h5>
          </div>
          <div class="card-body">
            <div class="row">
              <c:forEach var="component" items="${systemHealth.components}">
                <div class="col-md-6 col-lg-4 mb-3">
                  <div class="card border-${component.healthy ? 'success' : 'danger'}">
                    <div class="card-body">
                      <div class="d-flex align-items-center mb-2">
                        <i class="fas fa-${component.healthy ? 'check-circle' : 'times-circle'} status-${component.status.code.toLowerCase()} me-2"></i>
                        <strong>${component.component}</strong>
                      </div>
                      <p class="text-muted mb-1">${component.message}</p>
                      <small class="text-muted">
                        응답시간: ${component.responseTime}ms |
                        <fmt:formatDate value="${component.checkedAt}" pattern="HH:mm:ss"/>
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
  </c:if>
</div>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/js/bootstrap.bundle.min.js"></script>
<script>
  document.addEventListener("DOMContentLoaded", function () {
    const cards = document.querySelectorAll(".result-card");
    cards.forEach((card, index) => {
      setTimeout(() => {
        card.style.opacity = "0";
        card.style.transform = "translateY(20px)";
        card.style.transition = "all 0.5s ease";
        setTimeout(() => {
          card.style.opacity = "1";
          card.style.transform = "translateY(0)";
        }, 100);
      }, index * 200);
    });
  });
</script>
</body>
</html>