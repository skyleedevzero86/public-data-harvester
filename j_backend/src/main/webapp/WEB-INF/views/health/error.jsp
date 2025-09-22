<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
  <title>오류 발생</title>
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css" rel="stylesheet" />
  <link href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0/css/all.min.css" rel="stylesheet" />
  <style>
    .error-card {
      min-height: 300px;
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
      <div class="card error-card border-danger">
        <div class="card-body text-center">
          <i class="fas fa-exclamation-triangle fa-5x text-danger pulse"></i>
          <h1 class="mt-3 text-danger">오류 발생</h1>
          <p class="text-muted">요청을 처리하는 중 오류가 발생했습니다.</p>
          <c:if test="${not empty error}">
            <div class="alert alert-danger mt-4">
              <i class="fas fa-bug"></i> ${error}
            </div>
          </c:if>
          <div class="mt-4">
            <a href="/health" class="btn btn-primary">
              <i class="fas fa-home"></i> 대시보드로 돌아가기
            </a>
            <button onclick="history.back()" class="btn btn-outline-secondary">
              <i class="fas fa-arrow-left"></i> 이전 페이지
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</div>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/js/bootstrap.bundle.min.js"></script>
<script>
  document.addEventListener("DOMContentLoaded", function () {
    const card = document.querySelector(".error-card");
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