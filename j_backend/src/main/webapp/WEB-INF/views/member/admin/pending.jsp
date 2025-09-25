<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<c:set var="pageTitle" value="승인 대기 회원" />
<c:set var="pageCSS" value="${['admin.css']}" />
<c:set var="pageJS" value="${['admin.js']}" />

<%@ include file="../../common/header.jsp" %>
<body>
<nav class="navbar navbar-expand-lg navbar-dark bg-dark">
  <div class="container">
    <a class="navbar-brand" href="/">
      <i class="bi bi-shield-check"></i> 통신판매사업자관리 시스템
    </a>
    <div class="navbar-nav ms-auto">
      <a class="nav-link" href="/members/profile">
        <i class="bi bi-person-circle"></i> 내 프로필
      </a>
      <a class="nav-link" href="/members/admin/list">
        <i class="bi bi-people"></i> 전체 회원
      </a>
      <a class="nav-link" href="/members/admin/pending">
        <i class="bi bi-clock"></i> 승인 대기
      </a>
      <a class="nav-link" href="/members/logout">
        <i class="bi bi-box-arrow-right"></i> 로그아웃
      </a>
    </div>
  </div>
</nav>

<div class="container-fluid mt-4">
  <div class="d-flex justify-content-between align-items-center mb-4">
    <h2><i class="bi bi-clock"></i> 승인 대기 회원</h2>
    <div>
      <a href="/members/admin/list" class="btn btn-outline-primary">
        <i class="bi bi-people"></i> 전체 회원
      </a>
      <a href="/members/profile" class="btn btn-outline-secondary">
        <i class="bi bi-person-circle"></i> 내 프로필
      </a>
    </div>
  </div>

  <c:if test="${not empty successMessage}">
    <div class="alert alert-success alert-dismissible fade show" role="alert">
      <i class="bi bi-check-circle"></i> ${successMessage}
      <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    </div>
  </c:if>

  <c:if test="${not empty errorMessage}">
    <div class="alert alert-danger alert-dismissible fade show" role="alert">
      <i class="bi bi-exclamation-triangle"></i> ${errorMessage}
      <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    </div>
  </c:if>

  <div class="card">
    <div class="card-header">
      <div class="d-flex justify-content-between align-items-center">
        <h5 class="mb-0"><i class="bi bi-table"></i> 승인 대기 중인 회원 목록</h5>
      </div>
    </div>
    <div class="card-body p-0">
      <c:choose>
        <c:when test="${not empty pendingMemberViewList}">
          <div class="table-responsive">
            <table class="table table-hover mb-0">
              <thead class="table-dark">
              <tr>
                <th width="10%">ID</th>
                <th width="20%">사용자명</th>
                <th width="20%">닉네임</th>
                <th width="25%">이메일</th>
                <th width="15%">가입일</th>
                <th width="10%">작업</th>
              </tr>
              </thead>
              <tbody>
              <c:forEach var="memberView" items="${pendingMemberViewList}">
                <tr>
                  <td>
                    <strong>${memberView.member.id}</strong>
                  </td>
                  <td style="text-align: left;">
                    <div class="d-flex align-items-center">
                      <strong>${memberView.member.username}</strong>
                      <c:if test="${memberView.member.role == 'ADMIN'}">
                        <i class="bi bi-shield-fill text-danger ms-1" title="시스템 관리자"></i>
                      </c:if>
                    </div>
                  </td>
                  <td style="text-align: left;">${memberView.member.nickname}</td>
                  <td style="text-align: center;">
                    <small class="text-muted">${memberView.member.email}</small>
                  </td>
                  <td>
                    <small>
                      <c:choose>
                        <c:when test="${not empty memberView.createDateFormatted}">
                          <fmt:formatDate value="${memberView.createDateFormatted}" pattern="yyyy-MM-dd HH:mm" />
                        </c:when>
                        <c:otherwise>없음</c:otherwise>
                      </c:choose>
                    </small>
                  </td>
                  <td>
                    <div class="btn-group btn-group-sm" role="group">
                      <form method="post" action="/members/admin/${memberView.member.id}/approve" class="d-inline">
                        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                        <button type="submit" class="btn btn-success btn-sm"
                                onclick="return confirm('이 회원을 승인하시겠습니까?')" title="승인">
                          <i class="bi bi-check"></i> 승인
                        </button>
                      </form>
                      <form method="post" action="/members/admin/${memberView.member.id}/reject" class="d-inline">
                        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                        <button type="submit" class="btn btn-danger btn-sm"
                                onclick="return confirm('이 회원을 거부하시겠습니까?')" title="거부">
                          <i class="bi bi-x"></i> 거부
                        </button>
                      </form>
                    </div>
                  </td>
                </tr>
              </c:forEach>
              </tbody>
            </table>
          </div>

          <c:if test="${members.totalPages > 1}">
            <div class="card-footer">
              <nav aria-label="Page navigation">
                <ul class="pagination justify-content-center mb-0">
                  <c:if test="${members.hasPrevious()}">
                    <li class="page-item">
                      <a class="page-link" href="?page=${members.number - 1}&size=${members.size}">
                        <i class="bi bi-chevron-left"></i> 이전
                      </a>
                    </li>
                  </c:if>

                  <c:forEach var="i" begin="0" end="${members.totalPages - 1}">
                    <c:if test="${i >= members.number - 2 && i <= members.number + 2}">
                      <li class="page-item ${i == members.number ? 'active' : ''}">
                        <a class="page-link" href="?page=${i}&size=${members.size}">${i + 1}</a>
                      </li>
                    </c:if>
                  </c:forEach>

                  <c:if test="${members.hasNext()}">
                    <li class="page-item">
                      <a class="page-link" href="?page=${members.number + 1}&size=${members.size}">
                        다음 <i class="bi bi-chevron-right"></i>
                      </a>
                    </li>
                  </c:if>
                </ul>
              </nav>
            </div>
          </c:if>
        </c:when>
        <c:otherwise>
          <div class="text-center py-5">
            <i class="bi bi-inbox fs-1 text-muted"></i>
            <h4 class="mt-3 text-muted">승인 대기 중인 회원이 없습니다</h4>
            <p class="text-muted">새로운 회원이 가입하면 여기에 표시됩니다.</p>
          </div>
        </c:otherwise>
      </c:choose>
    </div>
  </div>
</div>

<!-- Footer -->
<%@ include file="../../common/footer.jsp" %>
<%@ include file="../../common/scripts.jsp" %>
</body>
</html>

