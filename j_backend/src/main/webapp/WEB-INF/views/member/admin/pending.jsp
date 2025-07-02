<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>승인 대기 회원 - Antock System</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body>
<div class="container mt-4">
    <div class="d-flex justify-content-between align-items-center mb-4">
        <h2>승인 대기 회원</h2>
        <nav>
            <a href="/members/admin/list" class="btn btn-outline-primary">전체 회원</a>
            <a href="/members/profile" class="btn btn-outline-secondary">내 프로필</a>
        </nav>
    </div>

    <c:if test="${not empty successMessage}">
        <div class="alert alert-success alert-dismissible fade show" role="alert">
                ${successMessage}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
    </c:if>

    <c:if test="${not empty errorMessage}">
        <div class="alert alert-danger alert-dismissible fade show" role="alert">
                ${errorMessage}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
    </c:if>

    <div class="card">
        <div class="card-body">
            <c:choose>
                <c:when test="${not empty pendingMembers.content}">
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
                            <c:forEach var="member" items="${pendingMembers.content}">
                                <tr>
                                    <td>${member.id}</td>
                                    <td>${member.username}</td>
                                    <td>${member.nickname}</td>
                                    <td>${member.email}</td>
                                    <td>
                                        <fmt:formatDate value="${member.createDate}" pattern="yyyy-MM-dd HH:mm" />
                                    </td>
                                    <td>
                                        <span class="badge bg-warning">${member.status.description}</span>
                                    </td>
                                    <td>
                                        <form method="post" action="/members/admin/${member.id}/approve" class="d-inline">
                                            <button type="submit" class="btn btn-sm btn-success"
                                                    onclick="return confirm('이 회원을 승인하시겠습니까?')">
                                                승인
                                            </button>
                                        </form>
                                        <form method="post" action="/members/admin/${member.id}/reject" class="d-inline">
                                            <button type="submit" class="btn btn-sm btn-danger"
                                                    onclick="return confirm('이 회원을 거부하시겠습니까?')">
                                                거부
                                            </button>
                                        </form>
                                    </td>
                                </tr>
                            </c:forEach>
                            </tbody>
                        </table>
                    </div>

                    <!-- 페이징 -->
                    <c:if test="${pendingMembers.totalPages > 1}">
                        <nav aria-label="Page navigation">
                            <ul class="pagination justify-content-center">
                                <c:if test="${pendingMembers.hasPrevious()}">
                                    <li class="page-item">
                                        <a class="page-link" href="?page=${pendingMembers.number - 1}">이전</a>
                                    </li>
                                </c:if>

                                <c:forEach var="i" begin="0" end="${pendingMembers.totalPages - 1}">
                                    <li class="page-item ${i == pendingMembers.number ? 'active' : ''}">
                                        <a class="page-link" href="?page=${i}">${i + 1}</a>
                                    </li>
                                </c:forEach>

                                <c:if test="${pendingMembers.hasNext()}">
                                    <li class="page-item">
                                        <a class="page-link" href="?page=${pendingMembers.number + 1}">다음</a>
                                    </li>
                                </c:if>
                            </ul>
                        </nav>
                    </c:if>
                </c:when>
                <c:otherwise>
                    <div class="text-center py-5">
                        <p class="text-muted">승인 대기중인 회원이 없습니다.</p>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>