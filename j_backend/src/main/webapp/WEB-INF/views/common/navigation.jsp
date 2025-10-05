<%@ page language="java" contentType="text/html; charset=UTF-8"
pageEncoding="UTF-8"%> <%@ taglib prefix="c"
uri="http://java.sun.com/jsp/jstl/core" %> <%@ taglib prefix="sec"
uri="http://www.springframework.org/security/tags" %>

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
      <ul class="navbar-nav ms-auto">
        <sec:authorize access="isAuthenticated()">
          <sec:authorize access="hasRole('ADMIN')">
            <li class="nav-item">
              <a class="nav-link" href="/web/files">
                <i class="bi bi-files"></i> 파일 관리
              </a>
            </li>
            <li class="nav-item">
              <a class="nav-link" href="/corp/list">
                <i class="bi bi-building"></i> 법인 관리
              </a>
            </li>
            <li class="nav-item dropdown">
              <a
                class="nav-link dropdown-toggle"
                href="javascript:void(0)"
                id="healthDropdown"
                role="button"
                data-bs-toggle="dropdown"
                aria-expanded="false"
              >
                <i class="bi bi-heart-pulse"></i> 시스템 관리
              </a>
              <ul
                class="dropdown-menu dropdown-menu-end"
                aria-labelledby="healthDropdown"
              >
                <li>
                  <a class="dropdown-item" href="/health/status">
                    <i class="bi bi-speedometer2"></i> 시스템 상태
                  </a>
                </li>
                <li>
                  <a class="dropdown-item" href="/health">
                    <i class="bi bi-speedometer2"></i> 대시보드
                  </a>
                </li>
                <li>
                  <a class="dropdown-item" href="/health/component">
                    <i class="bi bi-gear"></i> 컴포넌트 목록
                  </a>
                </li>
                <li>
                  <a class="dropdown-item" href="/health/metrics">
                    <i class="bi bi-graph-up"></i> 헬스체크
                  </a>
                </li>
                <li>
                  <a class="dropdown-item" href="/health/metrics/realtime">
                    <i class="bi bi-activity"></i> 실시간 메트릭
                  </a>
                </li>
                <li>
                  <a class="dropdown-item" href="/health/history">
                    <i class="bi bi-clock-history"></i> 헬스 이력
                  </a>
                </li>
              </ul>
            </li>
            <li class="nav-item">
              <a class="nav-link" href="/members/admin/list">
                <i class="bi bi-people"></i> 회원 관리
              </a>
            </li>
            <li class="nav-item">
              <a class="nav-link" href="/members/admin/pending">
                <i class="bi bi-clock"></i> 승인 대기
              </a>
            </li>
          </sec:authorize>

          <sec:authorize access="hasRole('MANAGER') and !hasRole('ADMIN')">
            <li class="nav-item">
              <a class="nav-link" href="/web/files">
                <i class="bi bi-files"></i> 파일 관리
              </a>
            </li>
            <li class="nav-item">
              <a class="nav-link" href="/corp/list">
                <i class="bi bi-building"></i> 법인 관리
              </a>
            </li>
            <li class="nav-item">
              <a class="nav-link" href="/members/admin/pending">
                <i class="bi bi-clock"></i> 승인 대기
              </a>
            </li>
          </sec:authorize>

          <li class="nav-item">
            <a class="nav-link" href="/members/profile">
              <i class="bi bi-person-circle"></i> 내 프로필
            </a>
          </li>
          <li class="nav-item">
            <a class="nav-link" href="/members/logout">
              <i class="bi bi-box-arrow-right"></i> 로그아웃
            </a>
          </li>
        </sec:authorize>
        <sec:authorize access="!isAuthenticated()">
          <li class="nav-item">
            <a class="nav-link" href="/members/join">
              <i class="bi bi-person-plus"></i> 회원가입
            </a>
          </li>
          <li class="nav-item">
            <a class="nav-link" href="/members/login">
              <i class="bi bi-box-arrow-in-right"></i> 로그인
            </a>
          </li>
        </sec:authorize>
      </ul>
    </div>
  </div>
</nav>
