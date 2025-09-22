<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="ko">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <title>시스템 상태</title>
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
    .status-card {
      min-height: 200px;
      display: flex;
      align-items: center;
      justify-content: center;
      flex-direction: column;
    }
    .pulse {
      animation: pulse 2s infinite;
    }
    @keyframes pulse {
      0% {
        transform: scale(1);
      }
      50% {
        transform: scale(1.05);
      }
      100% {
        transform: scale(1);
      }
    }
  </style>
</head>
<body>
<div class="container-fluid">
  <div class="row justify-content-center">
    <div class="col-md-8 col-lg-6">
      <div class="card status-card ${systemHealth.healthy ? 'border-success' : 'border-danger'}">
        <div class="card-body text-center">
          <c:choose>
            <c:when test="${systemHealth.healthy}">
              <i class="fas fa-check-circle fa-5x status-up pulse"></i>
              <h1 class="mt-3 status-up">시스템 정상</h1>
              <p class="text-muted">모든 서비스가 정상적으로 작동하고 있습니다.</p>
            </c:when>
            <c:otherwise>
              <i class="fas fa-exclamation-triangle fa-5x status-down pulse"></i>
              <h1 class="mt-3 status-down">시스템 장애</h1>
              <p class="text-muted">일부 서비스에 문제가 발생했습니다.</p>
            </c:otherwise>
          </c:choose>
          <div class="mt-4">
            <h3>${systemHealth.overallStatus.code}</h3>
            <p class="text-muted">${systemHealth.overallStatusDescription}</p>
          </div>
          <div class="row mt-4">
            <div class="col-4">
              <h4>${systemHealth.totalComponents}</h4>
              <small class="text-muted">총 컴포넌트</small>
            </div>
            <div class="col-4">
              <h4 class="status-up">${systemHealth.healthyComponents}</h4>
              <small class="text-muted">정상</small>
            </div>
            <div class="col-4">
              <h4 class="status-down">${systemHealth.unhealthyComponents}</h4>
              <small class="text-muted">장애</small>
            </div>
          </div>
          <div class="mt-4">
            <div class="progress" style="height: 20px">
              <div class="progress-bar bg-${systemHealth.healthy ? 'success' : 'danger'}" role="progressbar" style="width: ${systemHealth.healthPercentage}%">
                <fmt:formatNumber value="${systemHealth.healthPercentage}" pattern="#.##"/>%
              </div>
            </div>
          </div>
          <div class="mt-4">
            <small class="text-muted">마지막 체크: ${systemHealth.checkedAt}</small>
          </div>
        </div>
      </div>
    </div>
  </div>
</div>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/js/bootstrap.bundle.min.js"></script>
<script>
  setInterval(function () {
    location.reload();
  }, 30000);
  document.addEventListener("DOMContentLoaded", function () {
    const card = document.querySelector(".status-card");
    card.style.opacity = "0";
    card.style.transform = "scale(0.8)";
    card.style.transition = "all 0.5s ease";
    setTimeout(() => {
      card.style.opacity = "1";
      card.style.transform = "scale(1)";
    }, 100);
  });
</script>
</body>
</html>