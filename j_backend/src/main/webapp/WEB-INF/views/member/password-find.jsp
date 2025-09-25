<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<c:set var="pageTitle" value="비밀번호 찾기" />
<c:set var="pageCSS" value="${['member.css']}" />
<c:set var="pageJS" value="${['member.js']}" />

<%@ include file="../common/header.jsp" %>
  <body>
    <nav class="navbar navbar-expand-lg navbar-dark bg-dark">
      <div class="container">
        <a class="navbar-brand" href="/">
          <i class="bi bi-shield-check"></i> 통신판매사업자관리 시스템
        </a>
        <div class="navbar-nav ms-auto">
          <a class="nav-link" href="/members/login">
            <i class="bi bi-box-arrow-in-right"></i> 로그인
          </a>
          <a class="nav-link" href="/members/join">
            <i class="bi bi-person-plus"></i> 회원가입
          </a>
        </div>
      </div>
    </nav>

    <div class="container-fluid mt-4">
      <div class="d-flex justify-content-between align-items-center mb-4">
        <h2><i class="bi bi-key"></i> 비밀번호 찾기</h2>
      </div>

      <div class="row justify-content-center">
        <div class="col-md-8 col-lg-6">
          <div class="password-find-container">
            <div class="password-find-icon">
              <i class="bi bi-search"></i>
            </div>
            <h1 class="password-find-title">비밀번호 찾기</h1>
            <p class="password-find-subtitle">
              가입 시 사용한 이메일로 비밀번호 재설정 링크를 보내드립니다
            </p>

            <div class="step-indicator">
              <div class="step active">
                <div class="step-number">1</div>
                <div class="step-text">정보 입력</div>
              </div>
              <i class="bi bi-arrow-right step-arrow"></i>
              <div class="step">
                <div class="step-number">2</div>
                <div class="step-text">이메일 확인</div>
              </div>
              <i class="bi bi-arrow-right step-arrow"></i>
              <div class="step">
                <div class="step-number">3</div>
                <div class="step-text">비밀번호 재설정</div>
              </div>
            </div>

            <c:if test="${not empty errorMessage}">
              <div class="alert alert-danger" role="alert">
                <i class="bi bi-exclamation-triangle me-2"></i>
                ${errorMessage}
              </div>
            </c:if>

            <c:if test="${not empty successMessage}">
              <div class="alert alert-success" role="alert">
                <i class="bi bi-check-circle me-2"></i>
                ${successMessage}
              </div>
            </c:if>

            <div class="info-box">
              <h6><i class="bi bi-info-circle me-2"></i>비밀번호 찾기 안내</h6>
              <ul>
                <li>가입 시 사용한 사용자명과 이메일 주소를 입력해주세요</li>
                <li>입력하신 이메일로 비밀번호 재설정 링크를 보내드립니다</li>
                <li>링크는 30분간 유효하며, 한 번만 사용할 수 있습니다</li>
                <li>이메일이 오지 않는다면 스팸함을 확인해주세요</li>
              </ul>
            </div>

            <form:form
              method="post"
              action="/members/password/find"
              modelAttribute="passwordFindRequest"
            >
              <div class="mb-3">
                <label for="username" class="form-label">사용자명</label>
                <form:input
                  type="text"
                  class="form-control"
                  id="username"
                  path="username"
                  placeholder="사용자명을 입력하세요"
                  required="true"
                />
                <div class="form-text">
                  가입 시 사용한 사용자명을 입력해주세요
                </div>
                <form:errors path="username" cssClass="text-danger" />
              </div>

              <div class="mb-3">
                <label for="email" class="form-label">이메일 주소</label>
                <form:input
                  type="email"
                  class="form-control"
                  id="email"
                  path="email"
                  placeholder="이메일 주소를 입력하세요"
                  required="true"
                />
                <div class="form-text">
                  가입 시 사용한 이메일 주소를 입력해주세요
                </div>
                <form:errors path="email" cssClass="text-danger" />
              </div>

              <div class="d-grid gap-2">
                <button type="submit" class="btn btn-primary btn-lg">
                  <i class="bi bi-envelope me-2"></i>
                  비밀번호 재설정 링크 보내기
                </button>
              </div>
            </form:form>

            <div class="text-center mt-4">
              <a href="/members/login" class="text-decoration-none me-3">
                <i class="bi bi-arrow-left me-1"></i>
                로그인 페이지로 돌아가기
              </a>
              <a href="/members/join" class="text-decoration-none">
                <i class="bi bi-person-plus me-1"></i>
                회원가입
              </a>
            </div>
          </div>
        </div>
      </div>
    </div>

    <%@ include file="../common/footer.jsp" %>
    <%@ include file="../common/scripts.jsp" %>
  </body>
</html>


