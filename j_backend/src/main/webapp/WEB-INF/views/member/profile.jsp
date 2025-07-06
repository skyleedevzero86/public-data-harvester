<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>내 프로필 - Antock System</title>
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
            <c:if test="${member.role == 'ADMIN' || member.role == 'MANAGER'}">
                <a class="nav-link" href="/members/admin/list">
                    <i class="bi bi-people"></i> 회원 관리
                </a>
                <a class="nav-link" href="/members/admin/pending">
                    <i class="bi bi-clock"></i> 승인 대기
                </a>
            </c:if>
            <a class="nav-link" href="/members/logout">
                <i class="bi bi-box-arrow-right"></i> 로그아웃
            </a>
        </div>
    </div>
</nav>

<div class="container mt-4">
    <div class="row">
        <div class="col-md-8">
            <div class="card">
                <div class="card-header">
                    <h4><i class="bi bi-person-circle"></i> 내 프로필</h4>
                </div>
                <div class="card-body">
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

                    <form:form modelAttribute="memberUpdateRequest" method="post" action="/members/profile">
                        <div class="row">
                            <div class="col-md-6">
                                <div class="mb-3">
                                    <label class="form-label">사용자명</label>
                                    <input type="text" class="form-control" value="${member.username}" readonly>
                                    <div class="form-text">사용자명은 변경할 수 없습니다.</div>
                                </div>
                            </div>
                            <div class="col-md-6">
                                <div class="mb-3">
                                    <label class="form-label">권한</label>
                                    <input type="text" class="form-control" value="${member.role.description}" readonly>
                                </div>
                            </div>
                        </div>

                        <div class="row">
                            <div class="col-md-6">
                                <div class="mb-3">
                                    <label for="nickname" class="form-label">닉네임 <span class="text-danger">*</span></label>
                                    <form:input path="nickname" class="form-control" id="nickname" />
                                    <form:errors path="nickname" class="text-danger" />
                                </div>
                            </div>
                            <div class="col-md-6">
                                <div class="mb-3">
                                    <label for="email" class="form-label">이메일 <span class="text-danger">*</span></label>
                                    <form:input path="email" type="email" class="form-control" id="email" />
                                    <form:errors path="email" class="text-danger" />
                                </div>
                            </div>
                        </div>

                        <div class="row">
                            <div class="col-md-6">
                                <div class="mb-3">
                                    <label class="form-label">가입일</label>
                                    <input type="text" class="form-control"
                                           value="<c:choose><c:when test='${not empty createDateFormatted}'><fmt:formatDate value='${createDateFormatted}' pattern='yyyy-MM-dd HH:mm' /></c:when><c:otherwise>없음</c:otherwise></c:choose>" readonly>
                                </div>
                            </div>
                            <div class="col-md-6">
                                <div class="mb-3">
                                    <label class="form-label">최근 로그인</label>
                                    <input type="text" class="form-control"
                                           value="<c:choose><c:when test='${not empty lastLoginAtFormatted}'><fmt:formatDate value='${lastLoginAtFormatted}' pattern='yyyy-MM-dd HH:mm' /></c:when><c:otherwise>없음</c:otherwise></c:choose>" readonly>
                                </div>
                            </div>
                        </div>

                        <div class="row">
                            <div class="col-md-6">
                                <div class="mb-3">
                                    <label class="form-label">승인일</label>
                                    <input type="text" class="form-control"
                                           value="<c:choose><c:when test='${not empty approvedAtFormatted}'><fmt:formatDate value='${approvedAtFormatted}' pattern='yyyy-MM-dd HH:mm' /></c:when><c:otherwise>없음</c:otherwise></c:choose>" readonly>
                                </div>
                            </div>
                        </div>

                        <div class="mb-3">
                            <label class="form-label">계정 상태</label>
                            <div>
                                <span class="badge fs-6
                                    <c:choose>
                                        <c:when test='${member.status == "APPROVED"}'>bg-success</c:when>
                                        <c:when test='${member.status == "PENDING"}'>bg-warning</c:when>
                                        <c:when test='${member.status == "SUSPENDED"}'>bg-danger</c:when>
                                        <c:otherwise>bg-secondary</c:otherwise>
                                    </c:choose>
                                ">
                                    <i class="bi bi-circle-fill me-1"></i>
                                    ${member.status.description}
                                </span>
                            </div>
                        </div>

                        <div class="d-grid gap-2">
                            <button type="submit" class="btn btn-primary">
                                <i class="bi bi-save"></i> 프로필 수정
                            </button>
                        </div>
                    </form:form>

                    <div class="d-grid gap-2 mt-3">
                        <form id="withdrawForm" action="/members/withdraw" method="post" style="margin: 0;">
                            <button type="button" class="btn btn-danger w-100" onclick="confirmWithdraw()">
                                <i class="bi bi-person-x"></i> 회원 탈퇴
                            </button>
                        </form>
                    </div>
                </div>
            </div>
        </div>

        <div class="col-md-4">
            <div class="card">
                <div class="card-header">
                    <h5><i class="bi bi-key"></i> API 정보</h5>
                </div>
                <div class="card-body">
                    <div class="mb-3">
                        <label class="form-label">API Key</label>
                        <div class="input-group">
                            <input type="text" class="form-control font-monospace"
                                   value="${not empty member.apiKey ? member.apiKey : 'API Key 없음'}"
                                   readonly id="apiKey">
                            <button class="btn btn-outline-secondary" type="button" onclick="copyApiKey()">
                                <i class="bi bi-clipboard"></i>
                            </button>
                        </div>
                    </div>

                    <div class="alert alert-info">
                        <i class="bi bi-info-circle"></i>
                        <small>
                            API Key는 외부 서비스 연동 시 사용됩니다. 타인에게 노출하지 마세요.
                        </small>
                    </div>
                </div>
            </div>

            <div class="card mt-3">
                <div class="card-header">
                    <h5><i class="bi bi-shield-lock"></i> 비밀번호 보안</h5>
                </div>
                <div class="card-body">
                    <c:set var="now" value="<%= new java.util.Date() %>" />
                    <c:choose>
                        <c:when test="${member.passwordChangedAt == null}">
                            <div class="d-flex justify-content-between mb-2">
                                <span>가입일 기준:</span>
                                <span>
                                   <c:choose>
                                       <c:when test="${not empty createDateFormatted}">
                                           <fmt:formatDate value="${createDateFormatted}" pattern="yyyy-MM-dd HH:mm" />
                                       </c:when>
                                       <c:otherwise>없음</c:otherwise>
                                   </c:choose>
                               </span>
                            </div>

                            <c:if test="${not empty createDateFormatted}">
                                <c:set var="daysSinceJoin" value="${(now.time - createDateFormatted.time) / (1000 * 60 * 60 * 24)}" />
                                <c:choose>
                                    <c:when test="${daysSinceJoin >= 90}">
                                        <div class="alert alert-danger">
                                            <i class="bi bi-exclamation-triangle"></i>
                                            <strong>변경 필요</strong><br>
                                            <small>가입한지 90일이 지났습니다. 비밀번호를 변경해주세요.</small>
                                        </div>
                                    </c:when>
                                    <c:when test="${daysSinceJoin >= 80}">
                                        <div class="alert alert-warning">
                                            <i class="bi bi-info-circle"></i>
                                            <strong>변경 권장</strong><br>
                                            <small>가입한지 80일이 지났습니다. 비밀번호 변경을 권장합니다.</small>
                                        </div>
                                    </c:when>
                                    <c:otherwise>
                                        <div class="alert alert-info">
                                            <i class="bi bi-info-circle"></i>
                                            <strong>초기 상태</strong><br>
                                            <small>아직 비밀번호를 변경하지 않았습니다. (가입 ${Math.round(daysSinceJoin)}일 경과)</small>
                                        </div>
                                    </c:otherwise>
                                </c:choose>
                            </c:if>
                        </c:when>
                        <c:otherwise>
                            <div class="d-flex justify-content-between mb-2">
                                <span>마지막 변경:</span>
                                <span>
                                   <c:choose>
                                       <c:when test="${not empty passwordChangedAtFormatted}">
                                           <fmt:formatDate value="${passwordChangedAtFormatted}" pattern="yyyy-MM-dd HH:mm" />
                                       </c:when>
                                       <c:otherwise>없음</c:otherwise>
                                   </c:choose>
                               </span>
                            </div>

                            <c:if test="${not empty passwordChangedAtFormatted}">
                                <c:set var="daysSinceChange" value="${(now.time - passwordChangedAtFormatted.time) / (1000 * 60 * 60 * 24)}" />
                                <c:choose>
                                    <c:when test="${daysSinceChange >= 90}">
                                        <div class="alert alert-danger">
                                            <i class="bi bi-exclamation-triangle"></i>
                                            <strong>변경 필요</strong><br>
                                            <small>마지막 변경한지 90일이 지났습니다. 비밀번호를 변경해주세요.</small>
                                        </div>
                                    </c:when>
                                    <c:when test="${daysSinceChange >= 80}">
                                        <div class="alert alert-warning">
                                            <i class="bi bi-info-circle"></i>
                                            <strong>변경 권장</strong><br>
                                            <small>마지막 변경한지 80일이 지났습니다. 비밀번호 변경을 권장합니다.</small>
                                        </div>
                                    </c:when>
                                    <c:otherwise>
                                        <div class="alert alert-success">
                                            <i class="bi bi-check-circle"></i>
                                            <strong>안전</strong><br>
                                            <small>비밀번호가 안전합니다. (${Math.round(daysSinceChange)}일 전 변경)</small>
                                        </div>
                                    </c:otherwise>
                                </c:choose>
                            </c:if>
                        </c:otherwise>
                    </c:choose>

                    <div class="d-grid">
                        <a href="/members/password/change" class="btn btn-outline-primary">
                            <i class="bi bi-key"></i> 비밀번호 변경
                        </a>
                    </div>
                </div>
            </div>

            <div class="card mt-3">
                <div class="card-header">
                    <h5><i class="bi bi-graph-up"></i> 계정 통계</h5>
                </div>
                <div class="card-body">
                    <div class="d-flex justify-content-between mb-2">
                        <span>회원 ID:</span>
                        <strong>#${member.id}</strong>
                    </div>
                    <div class="d-flex justify-content-between mb-2">
                        <span>가입일:</span>
                        <span>
                           <c:choose>
                               <c:when test="${not empty createDateFormatted}">
                                   <fmt:formatDate value="${createDateFormatted}" pattern="yyyy-MM-dd" />
                               </c:when>
                               <c:otherwise>없음</c:otherwise>
                           </c:choose>
                       </span>
                    </div>
                    <div class="d-flex justify-content-between">
                        <span>활동 기간:</span>
                        <span>
                           <c:if test="${not empty createDateFormatted}">
                               <c:set var="daysSinceJoin" value="${(now.time - createDateFormatted.time) / (1000 * 60 * 60 * 24)}" />
                               ${Math.round(daysSinceJoin)}일
                           </c:if>
                           <c:if test="${empty createDateFormatted}">
                               알 수 없음
                           </c:if>
                       </span>
                    </div>
                    <c:if test="${not empty passwordChangedAtFormatted}">
                        <div class="d-flex justify-content-between mt-2">
                            <span>비밀번호 변경:</span>
                            <span><fmt:formatDate value="${passwordChangedAtFormatted}" pattern="yyyy-MM-dd" /></span>
                        </div>
                    </c:if>
                </div>
            </div>
        </div>
    </div>
</div>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/js/bootstrap.bundle.min.js"></script>
<script>
    function copyApiKey() {
        const apiKeyInput = document.getElementById('apiKey');
        apiKeyInput.select();
        apiKeyInput.setSelectionRange(0, 99999);

        navigator.clipboard.writeText(apiKeyInput.value).then(function() {
            const button = event.target.closest('button');
            const originalHtml = button.innerHTML;
            button.innerHTML = '<i class="bi bi-check"></i>';
            button.classList.add('btn-success');
            button.classList.remove('btn-outline-secondary');

            setTimeout(() => {
                button.innerHTML = originalHtml;
                button.classList.remove('btn-success');
                button.classList.add('btn-outline-secondary');
            }, 1000);
        }).catch(function() {
            alert('API Key가 클립보드에 복사되었습니다.');
        });
    }

    function confirmWithdraw() {
        if (confirm('정말로 탈퇴하시겠습니까?\n\n탈퇴 시:\n- 계정 복구가 불가능합니다\n- 즉시 로그아웃됩니다')) {
            document.getElementById('withdrawForm').submit();
        }
    }
</script>
</body>
</html>