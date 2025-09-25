<%@ page language="java" contentType="text/html; charset=UTF-8"
pageEncoding="UTF-8"%> <%@ taglib prefix="c"
uri="http://java.sun.com/jsp/jstl/core" %>

<nav class="navbar navbar-expand-lg navbar-dark bg-dark">
  <div class="container">
    <a class="navbar-brand" href="/">
      <i class="bi bi-shield-check"></i> 통신판매사업자관리 시스템
    </a>

    <button
      class="navbar-toggler"
      type="button"
      data-bs-toggle="collapse"
      data-bs-target="#navbarNav"
    >
      <span class="navbar-toggler-icon"></span>
    </button>

    <div class="collapse navbar-collapse" id="navbarNav">
      <ul class="navbar-nav me-auto">
        <li class="nav-item">
          <a class="nav-link" href="/corp/list">
            <i class="bi bi-building"></i> 법인 관리
          </a>
        </li>
        <li class="nav-item">
          <a class="nav-link" href="/web/files">
            <i class="bi bi-files"></i> 파일 관리
          </a>
        </li>
        <li class="nav-item">
          <a class="nav-link" href="/health/status">
            <i class="bi bi-heart-pulse"></i> 시스템 상태
          </a>
        </li>
      </ul>

      <ul class="navbar-nav">
        <c:choose>
          <c:when test="${not empty sessionScope.member}">
            <li class="nav-item">
              <a class="nav-link" href="/members/profile">
                <i class="bi bi-person-circle"></i> 내 프로필
              </a>
            </li>
            <c:if test="${sessionScope.member.role == 'ADMIN'}">
              <li class="nav-item">
                <a class="nav-link" href="/members/admin/pending">
                  <i class="bi bi-clock"></i> 승인 대기
                </a>
              </li>
              <li class="nav-item">
                <a class="nav-link" href="/members/admin/list">
                  <i class="bi bi-people"></i> 회원 관리
                </a>
              </li>
            </c:if>
            <li class="nav-item">
              <a class="nav-link" href="/members/logout">
                <i class="bi bi-box-arrow-right"></i> 로그아웃
              </a>
            </li>
          </c:when>
          <c:otherwise>
            <li class="nav-item">
              <a class="nav-link" href="/members/login">
                <i class="bi bi-box-arrow-in-right"></i> 로그인
              </a>
            </li>
            <li class="nav-item">
              <a class="nav-link" href="/members/join">
                <i class="bi bi-person-plus"></i> 회원가입
              </a>
            </li>
          </c:otherwise>
        </c:choose>
      </ul>
    </div>
  </div>
</nav>
