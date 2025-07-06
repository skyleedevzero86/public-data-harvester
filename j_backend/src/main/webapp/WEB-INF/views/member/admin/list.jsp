<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="_csrf" content="${_csrf.token}"/>
    <meta name="_csrf_header" content="${_csrf.headerName}"/>
    <title>회원 관리 - Antock System</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.7.2/font/bootstrap-icons.css" rel="stylesheet">

    <style>
        .dropdown-menu {
            min-width: 180px !important;
            padding: 10px 0;
        }

        .dropdown-menu .dropdown-item {
            padding: 10px 20px;
            font-size: 1rem;
        }

        .dropdown-menu .dropdown-header {
            font-size: 1.05rem;
            font-weight: bold;
            padding: 10px 20px 5px 20px;
        }

        .custom-dropdown .dropdown-menu { min-width: 200px !important; }

        .table th, .table td {
            vertical-align: middle;
        }
        .table th:last-child, .table td:last-child {
            min-width: 220px;
        }

        .table-responsive {
            overflow: visible !important;
        }

        .btn-group-sm .btn {
            margin-right: 2px;
        }

        .locked-indicator {
            color: #dc3545;
            font-weight: bold;
        }

        .login-fail-count {
            color: #fd7e14;
            font-weight: bold;
        }
    </style>

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
        <h2><i class="bi bi-people-fill"></i> 전체 회원 관리</h2>
        <div>
            <a href="/members/admin/pending" class="btn btn-warning">
                <i class="bi bi-clock"></i> 승인 대기 회원
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

    <div class="row mb-4">
        <div class="col-xl-2 col-md-4">
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
        <div class="col-xl-2 col-md-4">
            <div class="card bg-success text-white mb-4">
                <div class="card-body">
                    <div class="d-flex justify-content-between align-items-center">
                        <div>
                            <div class="text-white-50 small">승인된 회원</div>
                            <div class="text-lg font-weight-bold">
                                <c:set var="approvedCount" value="0" />
                                <c:forEach var="memberView" items="${memberViewList}">
                                    <c:if test="${memberView.member.status == 'APPROVED'}">
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
        <div class="col-xl-2 col-md-4">
            <div class="card bg-warning text-white mb-4">
                <div class="card-body">
                    <div class="d-flex justify-content-between align-items-center">
                        <div>
                            <div class="text-white-50 small">승인 대기</div>
                            <div class="text-lg font-weight-bold">
                                <c:set var="pendingCount" value="0" />
                                <c:forEach var="memberView" items="${memberViewList}">
                                    <c:if test="${memberView.member.status == 'PENDING'}">
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
        <div class="col-xl-2 col-md-4">
            <div class="card bg-danger text-white mb-4">
                <div class="card-body">
                    <div class="d-flex justify-content-between align-items-center">
                        <div>
                            <div class="text-white-50 small">정지된 회원</div>
                            <div class="text-lg font-weight-bold">
                                <c:set var="suspendedCount" value="0" />
                                <c:forEach var="memberView" items="${memberViewList}">
                                    <c:if test="${memberView.member.status == 'SUSPENDED'}">
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
        <div class="col-xl-2 col-md-4">
            <div class="card bg-secondary text-white mb-4">
                <div class="card-body">
                    <div class="d-flex justify-content-between align-items-center">
                        <div>
                            <div class="text-white-50 small">탈퇴 회원</div>
                            <div class="text-lg font-weight-bold">${withdrawnCount}</div>
                        </div>
                        <i class="bi bi-person-dash fa-2x"></i>
                    </div>
                </div>
            </div>
        </div>
        <div class="col-xl-2 col-md-4">
            <div class="card bg-dark text-white mb-4">
                <div class="card-body">
                    <div class="d-flex justify-content-between align-items-center">
                        <div>
                            <div class="text-white-50 small">거부 회원</div>
                            <div class="text-lg font-weight-bold">${rejectedCount}</div>
                        </div>
                        <i class="bi bi-person-x fa-2x"></i>
                    </div>
                </div>
            </div>
        </div>
    </div>

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
                <c:when test="${not empty memberViewList}">
                    <div class="table-responsive">
                        <table class="table table-hover mb-0">
                            <thead class="table-dark">
                            <tr>
                                <th width="5%">ID</th>
                                <th width="12%">사용자명</th>
                                <th width="12%">닉네임</th>
                                <th width="15%">이메일</th>
                                <th width="8%">권한</th>
                                <th width="8%">상태</th>
                                <th width="8%">실패횟수</th>
                                <th width="8%">가입일</th>
                                <th width="8%">최근 로그인</th>
                                <th width="12%">작업</th>
                                <th width="4%"></th>
                            </tr>
                            </thead>
                            <tbody>
                            <c:forEach var="memberView" items="${memberViewList}">
                                <c:set var="member" value="${memberView.member}" />
                                <c:if test="${member.status != 'PENDING'}">
                                    <tr class="${member.status == 'SUSPENDED' ? 'table-danger' : ''}">
                                        <td>
                                            <strong>${member.id}</strong>
                                        </td>
                                        <td>
                                            <div class="d-flex align-items-center">
                                                <strong>${member.username}</strong>
                                                <c:if test="${member.role == 'ADMIN'}">
                                                    <i class="bi bi-shield-fill text-danger ms-1" title="시스템 관리자"></i>
                                                </c:if>
                                                <c:if test="${member.loginFailCount >= 5}">
                                                    <i class="bi bi-lock-fill locked-indicator ms-1" title="계정 잠금됨"></i>
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
                                        <td class="status-cell">
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
                                            <c:choose>
                                                <c:when test="${member.loginFailCount > 0}">
                                                    <span class="login-fail-count">${member.loginFailCount}/5</span>
                                                    <c:if test="${member.loginFailCount >= 5}">
                                                        <br><small class="text-danger">잠금됨</small>
                                                    </c:if>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="text-muted">0</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td>
                                            <small>
                                                <c:choose>
                                                    <c:when test="${not empty memberView.createDateFormatted}">
                                                        <fmt:formatDate value="${memberView.createDateFormatted}" pattern="yyyy-MM-dd" />
                                                    </c:when>
                                                    <c:otherwise>없음</c:otherwise>
                                                </c:choose>
                                            </small>
                                        </td>
                                        <td>
                                            <small>
                                                <c:choose>
                                                    <c:when test="${not empty memberView.lastLoginAtFormatted}">
                                                        <fmt:formatDate value="${memberView.lastLoginAtFormatted}" pattern="MM-dd HH:mm" />
                                                    </c:when>
                                                    <c:otherwise>
                                                        <span class="text-muted">없음</span>
                                                    </c:otherwise>
                                                </c:choose>
                                            </small>
                                        </td>
                                        <td style="min-width:220px;">
                                            <div class="btn-group btn-group-sm" role="group">
                                                <div class="dropdown custom-dropdown">
                                                    <button class="btn btn-outline-primary btn-sm dropdown-toggle"
                                                            type="button" data-bs-toggle="dropdown">
                                                        <i class="bi bi-gear"></i>
                                                    </button>
                                                    <ul class="dropdown-menu" style="min-width:200px;">
                                                        <li><h6 class="dropdown-header">권한 변경</h6></li>
                                                        <li>
                                                            <form method="post" action="/members/admin/${member.id}/role?role=USER" class="d-inline">
                                                                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                                                                <button type="submit" class="dropdown-item border-0 bg-transparent p-0"
                                                                        onclick="return confirm('해당 회원의 권한을 \'일반 사용자\'로 변경하시겠습니까?')"
                                                                        style="width: 100%; text-align: left; padding: 10px 20px !important;">
                                                                    <i class="bi bi-person"></i> 일반 사용자
                                                                </button>
                                                            </form>
                                                        </li>
                                                        <li>
                                                            <form method="post" action="/members/admin/${member.id}/role?role=MANAGER" class="d-inline">
                                                                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                                                                <button type="submit" class="dropdown-item border-0 bg-transparent p-0"
                                                                        onclick="return confirm('해당 회원의 권한을 \'관리자\'로 변경하시겠습니까?')"
                                                                        style="width: 100%; text-align: left; padding: 10px 20px !important;">
                                                                    <i class="bi bi-person-badge"></i> 관리자
                                                                </button>
                                                            </form>
                                                        </li>

                                                        <c:if test="${member.status == 'APPROVED' || member.status == 'SUSPENDED'}">
                                                            <li><hr class="dropdown-divider"></li>
                                                            <li><h6 class="dropdown-header">계정 관리</h6></li>

                                                            <c:if test="${member.status == 'APPROVED'}">
                                                                <li>
                                                                    <form method="post" action="/members/admin/${member.id}/suspend" class="d-inline">
                                                                        <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                                                                        <button type="submit" class="dropdown-item text-danger border-0 bg-transparent p-0"
                                                                                onclick="return confirm('해당 회원의 계정을 정지하시겠습니까?\n\n• 즉시 로그인이 불가능해집니다\n• 관리자가 직접 해제할 때까지 정지됩니다')"
                                                                                style="width: 100%; text-align: left; padding: 10px 20px !important;">
                                                                            <i class="bi bi-person-x"></i> 계정 정지
                                                                        </button>
                                                                    </form>
                                                                </li>
                                                            </c:if>
                                                        </c:if>
                                                    </ul>
                                                </div>

                                                <c:if test="${member.status == 'SUSPENDED' || member.loginFailCount >= 5}">
                                                    <form method="post" action="/members/admin/${member.id}/unlock" class="d-inline">
                                                        <button type="submit" class="btn btn-warning btn-sm"
                                                                onclick="return confirm('이 회원의 계정 정지를 해제하시겠습니까?\n\n• 로그인 실패 횟수가 초기화됩니다\n• 계정 잠금이 해제됩니다\n• 상태가 승인됨으로 변경됩니다')"
                                                                title="정지 해제">
                                                            <i class="bi bi-unlock"></i> 해제
                                                        </button>
                                                    </form>
                                                </c:if>
                                            </div>
                                        </td>
                                        <td>
                                            <c:if test="${member.role != 'ADMIN' && member.username != pageContext.request.userPrincipal.name}">
                                                <c:if test="${member.status != 'WITHDRAWN'}">
                                                    <form action="/members/admin/${member.id}/delete" method="post" style="display:inline;" onsubmit="return confirm('정말로 삭제(탈퇴) 처리하시겠습니까?');">
                                                        <button type="submit" class="btn btn-danger btn-sm">
                                                            <i class="bi bi-person-x"></i>
                                                        </button>
                                                    </form>
                                                </c:if>
                                            </c:if>
                                            <c:if test="${member.status == 'REJECTED'}">
                                                <button type="button" class="btn btn-success btn-sm" onclick="approveMember(${member.id}, this)">승인</button>
                                            </c:if>
                                        </td>
                                    </tr>
                                </c:if>
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

        console.log('changeRole 호출됨:', 'memberId =', memberId, 'role =', role);
        console.log('memberId 타입:', typeof memberId);

        if (!memberId || memberId === 'null' || memberId === 'undefined') {
            alert('회원 ID가 올바르지 않습니다. memberId: ' + memberId);
            return false;
        }

        if (confirm(`해당 회원의 권한을 '${role}'로 변경하시겠습니까?`)) {
            const form = document.createElement('form');
            form.method = 'POST';
            form.action = `/members/admin/${memberId}/role?role=${role}`;

            const csrfToken = document.querySelector('meta[name="_csrf"]');
            if (csrfToken) {
                const csrfInput = document.createElement('input');
                csrfInput.type = 'hidden';
                csrfInput.name = '_csrf';
                csrfInput.value = csrfToken.getAttribute('content');
                form.appendChild(csrfInput);
            }

            console.log('권한 변경 요청 URL:', form.action);

            document.body.appendChild(form);
            form.submit();
        }
        return false;
    }

    function filterByStatus(status) {
        const url = new URL(window.location);
        if (status) {
            url.searchParams.set('status', status);
        } else {
            url.searchParams.delete('status');
        }
        url.searchParams.delete('page');
        window.location.href = url.toString();
    }

    function filterByRole(role) {
        const url = new URL(window.location);
        if (role) {
            url.searchParams.set('role', role);
        } else {
            url.searchParams.delete('role');
        }
        url.searchParams.delete('page');
        window.location.href = url.toString();
    }

    document.querySelectorAll('tbody tr').forEach(row => {
        row.addEventListener('click', function(e) {
            if (e.target.closest('button') || e.target.closest('a') || e.target.closest('form')) {
                return;
            }

            document.querySelectorAll('tbody tr').forEach(r => r.classList.remove('table-active'));
            this.classList.add('table-active');
        });
    });

    function approveMember(memberId, btn) {
        if (!confirm('정말 승인하시겠습니까?')) return;
        fetch('/members/admin/' + memberId + '/approve', {
            method: 'POST',
            headers: {'X-Requested-With': 'XMLHttpRequest'}
        })
            .then(res => {
                if (res.ok) {
                    btn.disabled = true;
                    btn.innerText = '승인됨';
                    const row = btn.closest('tr');
                    row.querySelector('.status-cell').innerText = '승인됨';
                    location.reload();
                } else {
                    alert('승인 실패');
                }
            });
    }

    document.addEventListener('DOMContentLoaded', function() {
        const suspendedRows = document.querySelectorAll('tr.table-danger');
        if (suspendedRows.length > 0) {
            console.log(`정지된 회원 ${suspendedRows.length}명 발견`);
        }
    });
</script>
</body>
</html>