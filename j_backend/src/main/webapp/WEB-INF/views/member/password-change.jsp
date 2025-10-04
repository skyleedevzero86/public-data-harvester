<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>

<c:set var="pageTitle" value="비밀번호 변경" />
<c:set var="pageCSS" value="${['member.css']}" />
<c:set var="pageJS" value="${['member.js']}" />

<%@ include file="../common/header.jsp" %>
<body>
<%@ include file="../common/navigation.jsp" %>

<div class="container-fluid mt-4">
    <div class="d-flex justify-content-between align-items-center mb-4">
        <h2><i class="bi bi-key"></i> 비밀번호 변경</h2>
    </div>

    <div class="row justify-content-center">
        <div class="col-md-6">
            <div class="card">
                <div class="card-header">
                    <div class="d-flex justify-content-between align-items-center">
                        <h5 class="mb-0"><i class="bi bi-table"></i> 비밀번호 변경</h5>
                    </div>
                </div>
                <div class="card-body">
                    <c:if test="${param.debug == 'true'}">
                        <div class="alert alert-info alert-dismissible fade show" role="alert">
                            <i class="bi bi-info-circle"></i>
                            <strong>디버그 정보:</strong><br>
                            - isPasswordChangeRequired: ${isPasswordChangeRequired}<br>
                            - isPasswordChangeRecommended: ${isPasswordChangeRecommended}<br>
                            - todayChangeCount: ${todayChangeCount}<br>
                            - passwordChangeRequest: ${passwordChangeRequest != null ? 'exists' : 'null'}
                            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
                        </div>
                    </c:if>

                    <c:if test="${isPasswordChangeRequired}">
                        <div class="alert alert-danger alert-dismissible fade show" role="alert">
                            <i class="bi bi-exclamation-triangle"></i>
                            <strong>비밀번호 변경 필요</strong><br>
                            90일이 지난 비밀번호입니다. 보안을 위해 새 비밀번호로 변경해주세요.
                            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
                        </div>
                    </c:if>

                    <c:if test="${isPasswordChangeRecommended && !isPasswordChangeRequired}">
                        <div class="alert alert-warning alert-dismissible fade show" role="alert">
                            <i class="bi bi-info-circle"></i>
                            <strong>비밀번호 변경 권장</strong><br>
                            80일이 지난 비밀번호입니다. 보안을 위해 새 비밀번호로 변경을 권장합니다.
                            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
                        </div>
                    </c:if>

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

                    <div class="alert ${todayChangeCount >= 3 ? 'alert-danger' : 'alert-info'} alert-dismissible fade show" role="alert">
                        <i class="bi bi-info-circle"></i>
                        <small>
                            오늘 비밀번호 변경 횟수: <strong>${todayChangeCount}/3</strong>
                            <br>하루 최대 3회까지 변경 가능합니다.
                            <c:if test="${todayChangeCount >= 3}">
                                <br><strong>⚠️ 오늘은 더 이상 변경할 수 없습니다.</strong>
                            </c:if>
                        </small>
                        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
                    </div>

                    <form:form modelAttribute="passwordChangeRequest" method="post" id="passwordChangeForm" novalidate="true">
                        <div class="mb-3">
                            <label for="oldPassword" class="form-label">현재 비밀번호 <span class="text-danger">*</span></label>
                            <div class="input-group">
                                <form:password path="oldPassword" class="form-control ${not empty bindingResult && bindingResult.hasFieldErrors('oldPassword') ? 'is-invalid' : ''}"
                                               id="oldPassword" placeholder="현재 비밀번호를 입력하세요"
                                               disabled="${todayChangeCount >= 3}" />
                                <button type="button" class="btn btn-outline-secondary"
                                        onclick="togglePassword('oldPassword')"
                                    ${todayChangeCount >= 3 ? 'disabled' : ''}>
                                    <i class="bi bi-eye" id="oldPasswordIcon"></i>
                                </button>
                            </div>
                            <form:errors path="oldPassword" class="invalid-feedback d-block" />
                        </div>

                        <div class="mb-3">
                            <label for="newPassword" class="form-label">새 비밀번호 <span class="text-danger">*</span></label>
                            <div class="input-group">
                                <form:password path="newPassword" class="form-control ${not empty bindingResult && bindingResult.hasFieldErrors('newPassword') ? 'is-invalid' : ''}"
                                               id="newPassword" placeholder="새 비밀번호를 입력하세요"
                                               disabled="${todayChangeCount >= 3}" />
                                <button type="button" class="btn btn-outline-secondary"
                                        onclick="togglePassword('newPassword')"
                                    ${todayChangeCount >= 3 ? 'disabled' : ''}>
                                    <i class="bi bi-eye" id="newPasswordIcon"></i>
                                </button>
                            </div>
                            <div class="form-text">
                                8-20자, 영문 대/소문자, 숫자, 특수문자 포함 필요
                            </div>
                            <form:errors path="newPassword" class="invalid-feedback d-block" />
                        </div>

                        <div class="mb-3">
                            <label for="newPasswordConfirm" class="form-label">새 비밀번호 확인 <span class="text-danger">*</span></label>
                            <div class="input-group">
                                <form:password path="newPasswordConfirm" class="form-control ${not empty bindingResult && bindingResult.hasFieldErrors('newPasswordConfirm') ? 'is-invalid' : ''}"
                                               id="newPasswordConfirm" placeholder="새 비밀번호를 다시 입력하세요"
                                               disabled="${todayChangeCount >= 3}" />
                                <button type="button" class="btn btn-outline-secondary"
                                        onclick="togglePassword('newPasswordConfirm')"
                                    ${todayChangeCount >= 3 ? 'disabled' : ''}>
                                    <i class="bi bi-eye" id="newPasswordConfirmIcon"></i>
                                </button>
                            </div>
                            <form:errors path="newPasswordConfirm" class="invalid-feedback d-block" />
                        </div>

                        <div class="mb-3">
                            <label class="form-label">비밀번호 강도</label>
                            <div class="progress" style="height: 10px;">
                                <div class="progress-bar" role="progressbar" id="passwordStrength" style="width: 0%"></div>
                            </div>
                            <small class="form-text" id="passwordStrengthText">비밀번호를 입력하세요</small>
                        </div>

                        <div class="alert alert-info alert-dismissible fade show" role="alert">
                            <i class="bi bi-info-circle"></i>
                            <small>
                                <strong>비밀번호 정책:</strong><br>
                                • 길이: 8자 이상 20자 이하<br>
                                • 포함 필수: 영문 대문자, 소문자, 숫자, 특수문자<br>
                                • 사용 가능한 특수문자: !@#$%^&*(),.?":{}<>_+-=[]\;'`~<br>
                                • 최근 5개 비밀번호는 재사용 불가<br>
                                • 하루 최대 3회까지 변경 가능
                            </small>
                            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
                        </div>

                        <div class="d-grid gap-2">
                            <button type="submit" class="btn btn-primary"
                                ${todayChangeCount >= 3 ? 'disabled' : ''}
                                    id="submitButton">
                                <i class="bi bi-key"></i>
                                <c:choose>
                                    <c:when test="${todayChangeCount >= 3}">변경 불가 (일일 한도 초과)</c:when>
                                    <c:otherwise>비밀번호 변경</c:otherwise>
                                </c:choose>
                            </button>
                            <a href="/members/profile" class="btn btn-secondary">
                                <i class="bi bi-arrow-left"></i> 프로필로 돌아가기
                            </a>
                        </div>
                    </form:form>
                </div>
            </div>
        </div>
    </div>
</div>

<%@ include file="../common/footer.jsp" %>
<%@ include file="../common/scripts.jsp" %>
</body>
</html>

