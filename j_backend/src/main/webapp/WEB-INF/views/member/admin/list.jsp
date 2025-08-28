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
            min-width: 200px !important;
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

        .custom-dropdown .dropdown-menu { 
            min-width: 220px !important; 
        }

        .table th, .table td {
            vertical-align: middle;
            text-align: center;
        }
        
        .table th:first-child, .table td:first-child {
            text-align: center;
        }
        
        .table th:nth-child(2), .table td:nth-child(2) {
            text-align: left;
        }
        
        .table th:nth-child(3), .table td:nth-child(3) {
            text-align: left;
        }
        
        .table th:nth-child(4), .table td:nth-child(4) {
            text-align: left;
        }
        
        .table th:last-child, .table td:last-child {
            min-width: 50px;
            text-align: center;
        }
        
        .table th:nth-last-child(2), .table td:nth-last-child(2) {
            min-width: 250px;
            text-align: center;
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
            color: #dc3545;
            font-weight: bold;
            font-size: 0.9em;
        }

        .badge {
            font-size: 0.8em;
        }

        .filter-container {
            background: #f8f9fa;
            border: 1px solid #dee2e6;
            border-radius: 8px;
            padding: 15px;
            margin-bottom: 20px;
        }

        .filter-active {
            border-color: #007bff !important;
            box-shadow: 0 0 0 0.2rem rgba(0, 123, 255, 0.25) !important;
        }

        .status-cell {
            white-space: nowrap;
        }

        .btn-group .dropdown-menu {
            border: 1px solid #dee2e6;
            box-shadow: 0 4px 6px rgba(0, 0, 0, 0.1);
        }

        .page-link:hover {
            background-color: #e9ecef;
        }

        .icon-align {
            display: flex;
            align-items: center;
            justify-content: center;
        }

        .stats-icon {
            font-size: 2.5rem;
            opacity: 0.8;
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
                <c:if test="${pendingCount > 0}">
                    <span class="badge bg-warning text-dark ms-1">${pendingCount}</span>
                </c:if>
            </a>
            <a class="nav-link" href="/members/logout">
                <i class="bi bi-box-arrow-right"></i> 로그아웃
            </a>
        </div>
    </div>
</nav>

<div class="container-fluid mt-4">
    <div class="d-flex justify-content-between align-items-center mb-4">
        <h2><i class="bi bi-people"></i> 회원 관리</h2>
        <div>
            <a href="/members/admin/pending" class="btn btn-warning">
                <i class="bi bi-clock"></i> 승인 대기 회원
                <c:if test="${pendingCount > 0}">
                    <span class="badge bg-light text-dark ms-1">${pendingCount}</span>
                </c:if>
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

    <div class="filter-container">
        <div class="row align-items-center">
            <div class="col-auto">
                <h6 class="mb-0"><i class="bi bi-filter"></i> 필터</h6>
            </div>
            <div class="col-auto">
                <select class="form-select ${not empty selectedStatus ? 'filter-active' : ''}"
                        onchange="filterByStatus(this.value)" id="statusFilter">
                    <option value="">전체 상태</option>
                    <option value="APPROVED" ${selectedStatus == 'APPROVED' ? 'selected' : ''}>승인됨</option>
                    <option value="PENDING" ${selectedStatus == 'PENDING' ? 'selected' : ''}>승인 대기</option>
                    <option value="SUSPENDED" ${selectedStatus == 'SUSPENDED' ? 'selected' : ''}>정지됨</option>
                    <option value="REJECTED" ${selectedStatus == 'REJECTED' ? 'selected' : ''}>거부됨</option>
                    <option value="WITHDRAWN" ${selectedStatus == 'WITHDRAWN' ? 'selected' : ''}>탈퇴됨</option>
                </select>
            </div>
            <div class="col-auto">
                <select class="form-select ${not empty selectedRole ? 'filter-active' : ''}"
                        onchange="filterByRole(this.value)" id="roleFilter">
                    <option value="">전체 권한</option>
                    <option value="USER" ${selectedRole == 'USER' ? 'selected' : ''}>일반 사용자</option>
                    <option value="MANAGER" ${selectedRole == 'MANAGER' ? 'selected' : ''}>관리자</option>
                    <option value="ADMIN" ${selectedRole == 'ADMIN' ? 'selected' : ''}>시스템 관리자</option>
                </select>
            </div>
            <div class="col-auto">
                <c:if test="${not empty selectedStatus or not empty selectedRole}">
                    <button type="button" class="btn btn-outline-secondary btn-sm" onclick="clearFilters()">
                        <i class="bi bi-x-lg"></i> 필터 초기화
                    </button>
                </c:if>
            </div>
            <div class="col-auto ms-auto">
                <small class="text-muted">
                    총 ${members.totalElements}명
                    <c:if test="${not empty selectedStatus or not empty selectedRole}">
                        (필터링됨)
                    </c:if>
                </small>
            </div>
        </div>
    </div>

    <div class="row mb-4">
        <div class="col-xl-2 col-md-4 col-sm-6">
            <div class="card bg-primary text-white mb-4">
                <div class="card-body">
                    <div class="d-flex justify-content-between align-items-center">
                        <div>
                            <div class="text-white-50 small">전체 회원</div>
                            <div class="fs-4 fw-bold">${members.totalElements}</div>
                        </div>
                        <div class="icon-align">
                            <i class="bi bi-people-fill stats-icon"></i>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <div class="col-xl-2 col-md-4 col-sm-6">
            <div class="card bg-success text-white mb-4">
                <div class="card-body">
                    <div class="d-flex justify-content-between align-items-center">
                        <div>
                            <div class="text-white-50 small">승인된 회원</div>
                            <div class="fs-4 fw-bold">${approvedCount}</div>
                        </div>
                        <div class="icon-align">
                            <i class="bi bi-check-circle-fill stats-icon"></i>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <div class="col-xl-2 col-md-4 col-sm-6">
            <div class="card bg-warning text-white mb-4">
                <div class="card-body">
                    <div class="d-flex justify-content-between align-items-center">
                        <div>
                            <div class="text-white-50 small">승인 대기</div>
                            <div class="fs-4 fw-bold">${pendingCount}</div>
                        </div>
                        <div class="icon-align">
                            <i class="bi bi-clock-fill stats-icon"></i>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <div class="col-xl-2 col-md-4 col-sm-6">
            <div class="card bg-danger text-white mb-4">
                <div class="card-body">
                    <div class="d-flex justify-content-between align-items-center">
                        <div>
                            <div class="text-white-50 small">정지된 회원</div>
                            <div class="fs-4 fw-bold">${suspendedCount}</div>
                        </div>
                        <div class="icon-align">
                            <i class="bi bi-x-circle-fill stats-icon"></i>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <div class="col-xl-2 col-md-4 col-sm-6">
            <div class="card bg-secondary text-white mb-4">
                <div class="card-body">
                    <div class="d-flex justify-content-between align-items-center">
                        <div>
                            <div class="text-white-50 small">탈퇴 회원</div>
                            <div class="fs-4 fw-bold">${withdrawnCount}</div>
                        </div>
                        <div class="icon-align">
                            <i class="bi bi-person-dash-fill stats-icon"></i>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <div class="col-xl-2 col-md-4 col-sm-6">
            <div class="card bg-dark text-white mb-4">
                <div class="card-body">
                    <div class="d-flex justify-content-between align-items-center">
                        <div>
                            <div class="text-white-50 small">거부 회원</div>
                            <div class="fs-4 fw-bold">${rejectedCount}</div>
                        </div>
                        <div class="icon-align">
                            <i class="bi bi-person-x-fill stats-icon"></i>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <div class="card">
        <div class="card-header">
            <div class="d-flex justify-content-between align-items-center">
                <h5 class="mb-0"><i class="bi bi-table"></i> 회원 목록</h5>
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
                                <th width="4%">삭제</th>
                            </tr>
                            </thead>
                            <tbody>
                            <c:forEach var="memberView" items="${memberViewList}">
                                <c:set var="member" value="${memberView.member}" />
                                <tr class="${member.status == 'SUSPENDED' ? 'table-danger' : ''}">
                                    <td>
                                        <strong>${member.id}</strong>
                                    </td>
                                    <td style="text-align: left;">
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
                                    <td style="text-align: left;">${member.nickname}</td>
                                    <td style="text-align: left;">
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
                                                <c:when test='${member.status == "WITHDRAWN"}'>bg-dark</c:when>
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
                                    <td>
                                        <div class="btn-group btn-group-sm" role="group">
                                            <div class="dropdown custom-dropdown">
                                                <button class="btn btn-outline-primary btn-sm dropdown-toggle"
                                                        type="button" data-bs-toggle="dropdown">
                                                    <i class="bi bi-gear"></i>
                                                </button>
                                                <ul class="dropdown-menu">
                                                    <li><h6 class="dropdown-header"><i class="bi bi-person-gear"></i> 권한 변경</h6></li>
                                                    <li>
                                                        <form method="post" action="/members/admin/${member.id}/role?role=USER" class="d-inline">
                                                            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                                                            <button type="submit" class="dropdown-item border-0 bg-transparent"
                                                                    onclick="return confirm('해당 회원의 권한을 \'일반 사용자\'로 변경하시겠습니까?')">
                                                                <i class="bi bi-person"></i> 일반 사용자
                                                            </button>
                                                        </form>
                                                    </li>
                                                    <li>
                                                        <form method="post" action="/members/admin/${member.id}/role?role=MANAGER" class="d-inline">
                                                            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                                                            <button type="submit" class="dropdown-item border-0 bg-transparent"
                                                                    onclick="return confirm('해당 회원의 권한을 \'관리자\'로 변경하시겠습니까?')">
                                                                <i class="bi bi-person-badge"></i> 관리자
                                                            </button>
                                                        </form>
                                                    </li>

                                                    <li><hr class="dropdown-divider"></li>
                                                    <li><h6 class="dropdown-header"><i class="bi bi-gear"></i> 상태 관리</h6></li>

                                                    <!-- 승인 버튼 -->
                                                    <c:if test="${member.status == 'PENDING' || member.status == 'REJECTED'}">
                                                        <li>
                                                            <form method="post" action="/members/admin/${member.id}/approve" class="d-inline">
                                                                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                                                                <button type="submit" class="dropdown-item text-success border-0 bg-transparent"
                                                                        onclick="return confirm('이 회원을 승인하시겠습니까?')">
                                                                    <i class="bi bi-check-circle"></i> 승인
                                                                </button>
                                                            </form>
                                                        </li>
                                                    </c:if>

                                                    <!-- 정지 버튼 -->
                                                    <c:if test="${member.status == 'APPROVED'}">
                                                        <li>
                                                            <form method="post" action="/members/admin/${member.id}/suspend" class="d-inline">
                                                                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                                                                <button type="submit" class="dropdown-item text-warning border-0 bg-transparent"
                                                                        onclick="return confirm('해당 회원의 계정을 정지하시겠습니까?\n\n• 즉시 로그인이 불가능해집니다\n• 관리자가 직접 해제할 때까지 정지됩니다')">
                                                                    <i class="bi bi-pause-circle"></i> 계정 정지
                                                                </button>
                                                            </form>
                                                        </li>
                                                    </c:if>

                                                    <!-- 정지 해제 버튼 -->
                                                    <c:if test="${member.status == 'SUSPENDED' || member.loginFailCount >= 5}">
                                                        <li>
                                                            <form method="post" action="/members/admin/${member.id}/unlock" class="d-inline">
                                                                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                                                                <button type="submit" class="dropdown-item text-info border-0 bg-transparent"
                                                                        onclick="return confirm('이 회원의 계정 정지를 해제하시겠습니까?\n\n• 로그인 실패 횟수가 초기화됩니다\n• 계정 잠금이 해제됩니다\n• 상태가 승인됨으로 변경됩니다')">
                                                                    <i class="bi bi-unlock"></i> 정지 해제
                                                                </button>
                                                            </form>
                                                        </li>
                                                    </c:if>

                                                    <!-- 거부 버튼 -->
                                                    <c:if test="${member.status == 'PENDING'}">
                                                        <li>
                                                            <form method="post" action="/members/admin/${member.id}/reject" class="d-inline">
                                                                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                                                                <button type="submit" class="dropdown-item text-danger border-0 bg-transparent"
                                                                        onclick="return confirm('이 회원을 거부하시겠습니까?')">
                                                                    <i class="bi bi-x-circle"></i> 거부
                                                                </button>
                                                            </form>
                                                        </li>
                                                    </c:if>

                                                    <!-- 승인 대기로 되돌리기 버튼 -->
                                                    <c:if test="${member.status == 'REJECTED' || member.status == 'SUSPENDED' || member.status == 'WITHDRAWN'}">
                                                        <li>
                                                            <form method="post" action="/members/admin/${member.id}/reset-to-pending" class="d-inline">
                                                                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                                                                <button type="submit" class="dropdown-item text-primary border-0 bg-transparent"
                                                                        onclick="return confirm('이 회원을 승인 대기 상태로 되돌리시겠습니까?\n\n• 상태가 승인 대기로 변경됩니다\n• 다시 승인 과정을 거쳐야 합니다')">
                                                                    <i class="bi bi-arrow-clockwise"></i> 승인 대기로 되돌리기
                                                                </button>
                                                            </form>
                                                        </li>
                                                    </c:if>
                                                </ul>
                                            </div>
                                        </div>
                                    </td>
                                    <td>
                                        <c:if test="${member.role != 'ADMIN' && member.username != pageContext.request.userPrincipal.name}">
                                            <c:if test="${member.status != 'WITHDRAWN'}">
                                                <form action="/members/admin/${member.id}/delete" method="post" style="display:inline;" onsubmit="return confirm('정말로 삭제(탈퇴) 처리하시겠습니까?');">
                                                    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                                                    <button type="submit" class="btn btn-danger btn-sm" title="탈퇴 처리">
                                                        <i class="bi bi-person-x"></i>
                                                    </button>
                                                </form>
                                            </c:if>
                                        </c:if>
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
                                            <a class="page-link" href="?page=${members.number - 1}&size=${members.size}<c:if test='${not empty selectedStatus}'>&status=${selectedStatus}</c:if><c:if test='${not empty selectedRole}'>&role=${selectedRole}</c:if>">
                                                <i class="bi bi-chevron-left"></i> 이전
                                            </a>
                                        </li>
                                    </c:if>

                                    <c:forEach var="i" begin="0" end="${members.totalPages - 1}">
                                        <c:if test="${i >= members.number - 2 && i <= members.number + 2}">
                                            <li class="page-item ${i == members.number ? 'active' : ''}">
                                                <a class="page-link" href="?page=${i}&size=${members.size}<c:if test='${not empty selectedStatus}'>&status=${selectedStatus}</c:if><c:if test='${not empty selectedRole}'>&role=${selectedRole}</c:if>">${i + 1}</a>
                                            </li>
                                        </c:if>
                                    </c:forEach>

                                    <c:if test="${members.hasNext()}">
                                        <li class="page-item">
                                            <a class="page-link" href="?page=${members.number + 1}&size=${members.size}<c:if test='${not empty selectedStatus}'>&status=${selectedStatus}</c:if><c:if test='${not empty selectedRole}'>&role=${selectedRole}</c:if>">
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
                        <c:choose>
                            <c:when test="${not empty selectedStatus or not empty selectedRole}">
                                <p class="text-muted mt-3">필터 조건에 맞는 회원이 없습니다.</p>
                                <button type="button" class="btn btn-primary" onclick="clearFilters()">
                                    <i class="bi bi-x-lg"></i> 필터 초기화
                                </button>
                            </c:when>
                            <c:otherwise>
                                <p class="text-muted mt-3">등록된 회원이 없습니다.</p>
                                <a href="/members/join" class="btn btn-primary">
                                    <i class="bi bi-person-plus"></i> 첫 번째 회원 추가하기
                                </a>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </c:otherwise>
            </c:choose>
        </div>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/js/bootstrap.bundle.min.js"></script>
<script>
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

    function clearFilters() {
        const url = new URL(window.location);
        url.searchParams.delete('status');
        url.searchParams.delete('role');
        url.searchParams.delete('page');
        window.location.href = url.toString();
    }

    document.addEventListener('DOMContentLoaded', function() {
        const statusFilter = document.getElementById('statusFilter');
        const roleFilter = document.getElementById('roleFilter');
        
        if (statusFilter.value) {
            statusFilter.classList.add('filter-active');
        }
        
        if (roleFilter.value) {
            roleFilter.classList.add('filter-active');
        }

        document.querySelectorAll("tbody tr").forEach((row) => {
            row.addEventListener("click", function (e) {
                if (
                    e.target.closest("button") ||
                    e.target.closest("a") ||
                    e.target.closest("form")
                ) {
                    return;
                }

                document
                    .querySelectorAll("tbody tr")
                    .forEach((r) => r.classList.remove("table-active"));
                this.classList.add("table-active");
            });
        });
    });
</script>
</body>
</html>