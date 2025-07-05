<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>승인 대기 회원 - Antock System</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.7.2/font/bootstrap-icons.css" rel="stylesheet">
</head>
<body>

<nav class="navbar navbar-expand-lg navbar-dark bg-dark">
    <div class="container">
        <a class="navbar-brand" href="/">
            <i class="bi bi-shield-check"></i> Antock System
        </a>
        <div class="navbar-nav ms-auto">
            <a class="nav-link" href="/members/profile">
                <i class="bi bi-person-circle"></i> 내 프로필
            </a>
            <a class="nav-link" href="/members/admin/list">
                <i class="bi bi-people"></i> 전체 회원
            </a>
            <a class="nav-link" href="/members/logout">
                <i class="bi bi-box-arrow-right"></i> 로그아웃
            </a>
        </div>
    </div>
</nav>

<div class="container mt-4">
    <div class="d-flex justify-content-between align-items-center mb-4">
        <h2><i class="bi bi-clock"></i> 승인 대기 회원</h2>
        <nav>
            <a href="/members/admin/list" class="btn btn-outline-primary">
                <i class="bi bi-people"></i> 전체 회원
            </a>
            <a href="/members/profile" class="btn btn-outline-secondary">
                <i class="bi bi-person-circle"></i> 내 프로필
            </a>
        </nav>
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
            <h5 class="mb-0"><i class="bi bi-clock-fill"></i> 승인 대기 중인 회원 목록</h5>
        </div>
        <div class="card-body">
            <c:choose>
                <c:when test="${not empty pendingMemberViewList}">
                    <div class="table-responsive">
                        <table class="table table-hover">
                            <thead class="table-dark">
                            <tr>
                                <th>ID</th>
                                <th>사용자명</th>
                                <th>닉네임</th>
                                <th>이메일</th>
                                <th>가입일</th>
                                <th>상태</th>
                                <th>작업</th>
                            </tr>
                            </thead>
                            <tbody>
                            <c:forEach var="memberView" items="${pendingMemberViewList}">
                                <c:set var="member" value="${memberView.member}" />
                                <tr>
                                    <td><strong>${member.id}</strong></td>
                                    <td>
                                        <div class="d-flex align-items-center">
                                            <strong>${member.username}</strong>
                                            <c:if test="${member.role == 'ADMIN'}">
                                                <i class="bi bi-shield-fill text-danger ms-1" title="시스템 관리자"></i>
                                            </c:if>
                                        </div>
                                    </td>
                                    <td>${member.nickname}</td>
                                    <td>
                                        <small class="text-muted">${member.email}</small>
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
                                        <span class="badge bg-warning text-dark">${member.status.description}</span>
                                    </td>
                                    <td>
                                        <div class="btn-group btn-group-sm" role="group">
                                            <form method="post" action="/members/admin/${member.id}/approve" class="d-inline">
                                                <button type="submit" class="btn btn-success btn-sm"
                                                        onclick="return confirm('이 회원을 승인하시겠습니까?')" title="승인">
                                                    <i class="bi bi-check"></i> 승인
                                                </button>
                                            </form>
                                            <form method="post" action="/members/admin/${member.id}/reject" class="d-inline">
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

                    <c:if test="${pendingMembers.totalPages > 1}">
                        <nav aria-label="Page navigation" class="mt-3">
                            <ul class="pagination justify-content-center">
                                <c:if test="${pendingMembers.hasPrevious()}">
                                    <li class="page-item">
                                        <a class="page-link" href="?page=${pendingMembers.number - 1}&size=${pendingMembers.size}">
                                            <i class="bi bi-chevron-left"></i> 이전
                                        </a>
                                    </li>
                                </c:if>

                                <c:forEach var="i" begin="0" end="${pendingMembers.totalPages - 1}">
                                    <c:if test="${i >= pendingMembers.number - 2 && i <= pendingMembers.number + 2}">
                                        <li class="page-item ${i == pendingMembers.number ? 'active' : ''}">
                                            <a class="page-link" href="?page=${i}&size=${pendingMembers.size}">${i + 1}</a>
                                        </li>
                                    </c:if>
                                </c:forEach>

                                <c:if test="${pendingMembers.hasNext()}">
                                    <li class="page-item">
                                        <a class="page-link" href="?page=${pendingMembers.number + 1}&size=${pendingMembers.size}">
                                            다음 <i class="bi bi-chevron-right"></i>
                                        </a>
                                    </li>
                                </c:if>
                            </ul>
                        </nav>
                    </c:if>
                </c:when>
                <c:otherwise>
                    <div class="text-center py-5">
                        <i class="bi bi-clock fs-1 text-muted"></i>
                        <p class="text-muted mt-3">승인 대기중인 회원이 없습니다.</p>
                        <a href="/members/admin/list" class="btn btn-primary">
                            <i class="bi bi-people"></i> 전체 회원 보기
                        </a>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/js/bootstrap.bundle.min.js"></script>
<script>

    document.querySelectorAll('tbody tr').forEach(row => {
        row.addEventListener('click', function(e) {

            if (e.target.closest('button') || e.target.closest('a') || e.target.closest('form')) {
                return;
            }

            document.querySelectorAll('tbody tr').forEach(r => r.classList.remove('table-active'));
            this.classList.add('table-active');
        });
    });
</script>
</body>
</html>