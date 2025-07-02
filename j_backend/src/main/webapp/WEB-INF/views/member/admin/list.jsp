<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>회원 관리 - Antock System</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.7.2/font/bootstrap-icons.css" rel="stylesheet">
</head>
<body>
<!-- 네비게이션 바 -->
<nav class="navbar navbar-expand-lg navbar-dark bg-dark">
    <div class="container">
        <a class="navbar-brand" href="/">
            <i class="bi bi-shield-check"></i> Antock System
        </a>
        <div class="navbar-nav ms-auto">
            <a class="nav-link" href="/members/profile">
                <i class="bi bi-person-circle"></i> 내 프로필
            </a>
            <a class="nav-link" href="/members/admin/pending">
                <i class="bi bi-clock"></i> 승인 대기
            </a>
            <a class="nav-link" href="/api/v1/members/logout">
                <i class="bi bi-box-arrow-right"></i> 로그아웃
            </a>
        </div>
    </div>
</nav>

<div class="container-fluid mt-4">
    <!-- 헤더 -->
    <div class="d-flex justify-content-between align-items-center mb-4">
        <h2><i class="bi bi-people-fill"></i> 전체 회원 관리</h2>
        <div>
            <a href="/members/admin/pending" class="btn btn-warning">
                <i class="bi bi-clock"></i> 승인 대기 회원
            </a>
        </div>
    </div>

    <!-- 알림 메시지 -->
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

    <!-- 통계 카드들 -->
    <div class="row mb-4">
        <div class="col-xl-3 col-md-6">
            <div class="card bg-primary text-white mb-4">
                <div class="card-body">
                    <div class="d-flex justify-content-between align-items-center">
                        <div>
                            <div class="text-white-50 small">전체 회원</div>
                            <div class="text-lg font-weight-bold">${members.totalElements}</div>
                        </div>
                        <i class="bi bi-people-fill fa-2x"></i>
                    </div>
                </div>
            </div>
        </div>
        <div class="col-xl-3 col-md-6">
            <div class="card bg-success text-white mb-4">
                <div class="card-body">
                    <div class="d-flex justify-content-between align-items-center">
                        <div>
                            <div class="text-white-50 small">승인된 회원</div>
                            <div class="text-lg font-weight-bold">
                                <c:set var="approvedCount" value="0" />
                                <c:forEach var="member" items="${members.content}">
                                    <c:if test="${member.status == 'APPROVED'}">
                                        <c:set var="approvedCount" value="${approvedCount + 1}" />
                                    </c:if>
                                </c:forEach>
                                ${approvedCount}
                            </div>
                        </div>
                        <i class="bi bi-check-circle-fill fa-2x"></i>
                    </div>
                </div>
            </div>
        </div>
        <div class="col-xl-3 col-md-6">
            <div class="card bg-warning text-white mb-4">
                <div class="card-body">
                    <div class="d-flex justify-content-between align-items-center">
                        <div>
                            <div class="text-white-50 small">승인 대기</div>
                            <div class="text-lg font-weight-bold">
                                <c:set var="pendingCount" value="0" />
                                <c:forEach var="member" items="${members.content}">
                                    <c:if test="${member.status == 'PENDING'}">
                                        <c:set var="pendingCount" value="${pendingCount + 1}" />
                                    </c:if>
                                </c:forEach>
                                ${pendingCount}
                            </div>
                        </div>
                        <i class="bi bi-clock-fill fa-2x"></i>
                    </div>
                </div>
            </div>
        </div>
        <div class="col-xl-3 col-md-6">
            <div class="card bg-danger text-white mb-4">
                <div class="card-body">
                    <div class="d-flex justify-content-between align-items-center">
                        <div>
                            <div class="text-white-50 small">정지된 회원</div>
                            <div class="text-lg font-weight-bold">
                                <c:set var="suspendedCount" value="0" />
                                <c:forEach var="member" items="${members.content}">
                                    <c:if test="${member.status == 'SUSPENDED'}">
                                        <c:set var="suspendedCount" value="${suspendedCount + 1}" />
                                    </c:if>
                                </c:forEach>
                                ${suspendedCount}
                            </div>
                        </div>
                        <i class="bi bi-x-circle-fill fa-2x"></i>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <!-- 회원 목록 테이블 -->
    <div class="card">
        <div class="card-header">
            <div class="d-flex justify-content-between align-items-center">
                <h5 class="mb-0"><i class="bi bi-table"></i> 회원 목록</h5>
                <div class="d-flex gap-2">
                    <!-- 검색 필터 -->
                    <select class="form-select form-select-sm" onchange="filterByStatus(this.value)">
                        <option value="">전체 상태</option>
                        <option value="APPROVED">승인됨</option>
                        <option value="PENDING">승인 대기</option>
                        <option value="SUSPENDED">정지됨</option>
                        <option value="REJECTED">거부됨</option>
                    </select>
                    <select class="form-select form-select-sm" onchange="filterByRole(this.value)">
                        <option value="">전체 권한</option>
                        <option value="USER">일반 사용자</option>
                        <option value="MANAGER">관리자</option>
                        <option value="ADMIN">시스템 관리자</option>
                    </select>
                </div>
            </div>
        </div>
        <div class="card-body p-0">
            <c:choose>
                <c:when test="${not empty members.content}">
                    <div class="table-responsive">
                        <table class="table table-hover mb-0">
                            <thead class="table-dark">
                            <tr>
                                <th width="5%">ID</th>
                                <th width="15%">사용자명</th>
                                <th width="15%">닉네임</th>
                                <th width="20%">이메일</th>
                                <th width="10%">권한</th>
                                <th width="10%">상태</th>
                                <th width="10%">가입일</th>
                                <th width="10%">최근 로그인</th>
                                <th width="15%">작업</th>
                            </tr>
                            </thead>
                            <tbody>
                            <c:forEach var="member" items="${members.content}">
                                <tr>
                                    <td>
                                        <strong>${member.id}</strong>
                                    </td>
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
                                                <span class="badge
                                                    <c:choose>
                                                        <c:when test='${member.role == "ADMIN"}'>bg-danger</c:when>
                                                        <c:when test='${member.role == "MANAGER"}'>bg-warning text-dark</c:when>
                                                        <c:otherwise>bg-info</c:otherwise>
                                                    </c:choose>
                                                ">${member.role.description}</span>
                                    </td>
                                    <td>
                                                <span class="badge
                                                    <c:choose>
                                                        <c:when test='${member.status == "APPROVED"}'>bg-success</c:when>
                                                        <c:when test='${member.status == "PENDING"}'>bg-warning text-dark</c:when>
                                                        <c:when test='${member.status == "SUSPENDED"}'>bg-danger</c:when>
                                                        <c:when test='${member.status == "REJECTED"}'>bg-secondary</c:when>
                                                        <c:otherwise>bg-light text-dark</c:otherwise>
                                                    </c:choose>
                                                ">${member.status.description}</span>
                                    </td>
                                    <td>
                                        <small>
                                            <fmt:formatDate value="${member.createDate}" pattern="yyyy-MM-dd" />
                                        </small>
                                    </td>
                                    <td>
                                        <small>
                                            <c:choose>
                                                <c:when test="${not empty member.lastLoginAt}">
                                                    <fmt:formatDate value="${member.lastLoginAt}" pattern="MM-dd HH:mm" />
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="text-muted">없음</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </small>
                                    </td>
                                    <td>
                                        <div class="btn-group btn-group-sm" role="group">
                                            <!-- 권한 변경 드롭다운 (ADMIN만 가능) -->
                                            <div class="dropdown">
                                                <button class="btn btn-outline-primary btn-sm dropdown-toggle"
                                                        type="button" data-bs-toggle="dropdown">
                                                    <i class="bi bi-gear"></i>
                                                </button>
                                                <ul class="dropdown-menu">
                                                    <li><h6 class="dropdown-header">권한 변경</h6></li>
                                                    <li>
                                                        <a class="dropdown-item" href="#"
                                                           onclick="changeRole(${member.id}, 'USER')">
                                                            <i class="bi bi-person"></i> 일반 사용자
                                                        </a>
                                                    </li>
                                                    <li>
                                                        <a class="dropdown-item" href="#"
                                                           onclick="changeRole(${member.id}, 'MANAGER')">
                                                            <i class="bi bi-person-badge"></i> 관리자
                                                        </a>
                                                    </li>
                                                    <c:if test="${member.status == 'APPROVED'}">
                                                        <li><hr class="dropdown-divider"></li>
                                                        <li>
                                                            <a class="dropdown-item text-danger" href="#"
                                                               onclick="suspendMember(${member.id})">
                                                                <i class="bi bi-person-x"></i> 계정 정지
                                                            </a>
                                                        </li>
                                                    </c:if>
                                                </ul>
                                            </div>

                                            <!-- 상태별 액션 버튼들 -->
                                            <c:if test="${member.status == 'PENDING'}">
                                                <form method="post" action="/members/admin/${member.id}/approve" class="d-inline">
                                                    <button type="submit" class="btn btn-success btn-sm"
                                                            onclick="return confirm('이 회원을 승인하시겠습니까?')" title="승인">
                                                        <i class="bi bi-check"></i>
                                                    </button>
                                                </form>
                                                <form method="post" action="/members/admin/${member.id}/reject" class="d-inline">
                                                    <button type="submit" class="btn btn-danger btn-sm"
                                                            onclick="return confirm('이 회원을 거부하시겠습니까?')" title="거부">
                                                        <i class="bi bi-x"></i>
                                                    </button>
                                                </form>
                                            </c:if>
                                        </div>
                                    </td>
                                </tr>
                            </c:forEach>
                            </tbody>
                        </table>
                    </div>

                    <!-- 페이징 -->
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
                        <i class="bi bi-people fs-1 text-muted"></i>
                        <p class="text-muted mt-3">등록된 회원이 없습니다.</p>
                        <a href="/members/join" class="btn btn-primary">
                            <i class="bi bi-person-plus"></i> 첫 번째 회원 추가하기
                        </a>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/js/bootstrap.bundle.min.js"></script>
<script>
    function changeRole(memberId, role) {
        if (confirm(`해당 회원의 권한을 '${role}'로 변경하시겠습니까?`)) {
            const form = document.createElement('form');
            form.method = 'POST';
            form.action = `/members/admin/${memberId}/role?role=${role}`;
            document.body.appendChild(form);
            form.submit();
        }
    }

    function suspendMember(memberId) {
        if (confirm('해당 회원의 계정을 정지하시겠습니까?')) {
            const form = document.createElement('form');
            form.method = 'POST';
            form.action = `/members/admin/${memberId}/suspend`;
            document.body.appendChild(form);
            form.submit();
        }
    }

    function filterByStatus(status) {
        const url = new URL(window.location);
        if (status) {
            url.searchParams.set('status', status);
        } else {
            url.searchParams.delete('status');
        }
        url.searchParams.delete('page'); // 필터 변경 시 첫 페이지로
        window.location.href = url.toString();
    }

    function filterByRole(role) {
        const url = new URL(window.location);
        if (role) {
            url.searchParams.set('role', role);
        } else {
            url.searchParams.delete('role');
        }
        url.searchParams.delete('page'); // 필터 변경 시 첫 페이지로
        window.location.href = url.toString();
    }

    // 테이블 행 클릭 시 상세 정보 표시 (선택사항)
    document.querySelectorAll('tbody tr').forEach(row => {
        row.addEventListener('click', function(e) {
            // 버튼이나 링크 클릭 시에는 동작하지 않음
            if (e.target.closest('button') || e.target.closest('a') || e.target.closest('form')) {
                return;
            }

            // 행이 선택되었음을 표시
            document.querySelectorAll('tbody tr').forEach(r => r.classList.remove('table-active'));
            this.classList.add('table-active');
        });
    });
</script>
</body>
</html>