<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<c:set var="pageTitle" value="파일 정보 수정" />
<c:set var="pageCSS" value="${['file.css']}" />
<c:set var="pageJS" value="${['file.js']}" />

<%@ include file="../common/header.jsp" %>
<%@ include file="../common/navigation.jsp" %>

<div class="main-content">
    <div class="container-fluid mt-4">
        <div class="d-flex justify-content-between align-items-center mb-4">
            <h2><i class="bi bi-pencil-square"></i> 파일 정보 수정</h2>
            <div>
            <a
              href="${pageContext.request.contextPath}/web/files"
              class="btn btn-secondary"
            >
                    <i class="bi bi-arrow-left"></i> 목록으로 돌아가기
                </a>
            </div>
        </div>

        <c:if test="${not empty message}">
          <div
            class="alert alert-success alert-dismissible fade show"
            role="alert"
          >
                <i class="bi bi-check-circle"></i> ${message}
            <button
              type="button"
              class="btn-close"
              data-bs-dismiss="alert"
            ></button>
            </div>
        </c:if>

        <c:if test="${not empty error}">
          <div
            class="alert alert-danger alert-dismissible fade show"
            role="alert"
          >
                <i class="bi bi-exclamation-triangle"></i> ${error}
            <button
              type="button"
              class="btn-close"
              data-bs-dismiss="alert"
            ></button>
            </div>
        </c:if>

        <div class="row">
            <div class="col-md-8">
                <div class="edit-form-container">
                    <div class="edit-form-title">
                        <i class="bi bi-file-earmark-text"></i> 파일 정보 수정
                    </div>

              <form
                action="${pageContext.request.contextPath}/web/files/update/${file.id}"
                method="post"
              >
                <input
                  type="hidden"
                  name="${_csrf.parameterName}"
                  value="${_csrf.token}"
                />

                        <div class="form-group">
                            <label for="originalFileName">
                                <i class="bi bi-file-earmark"></i> 원본 파일명
                            </label>
                  <input
                    type="text"
                    class="form-control"
                    id="originalFileName"
                    name="originalFileName"
                    value="${file.originalFileName}"
                    readonly
                  />
                            <small class="text-muted">파일명은 수정할 수 없습니다.</small>
                        </div>

                        <div class="form-group">
                            <label for="description">
                                <i class="bi bi-card-text"></i> 파일 설명
                            </label>
                  <input
                    type="text"
                    class="form-control"
                    id="description"
                    name="description"
                    value="${file.description}"
                    placeholder="파일에 대한 설명을 입력하세요"
                  />
                  <small class="text-muted"
                    >파일의 용도나 내용에 대한 설명을 입력해주세요.</small
                  >
                        </div>

                        <div class="form-group">
                            <button type="submit" class="btn btn-save">
                                <i class="bi bi-check-circle"></i> 저장하기
                            </button>
                        </div>
                    </form>
                </div>
            </div>

            <div class="col-md-4">
                <div class="file-info-card">
                    <div class="file-info-title">
                        <i class="bi bi-info-circle"></i> 현재 파일 정보
                    </div>

                    <div class="file-info-item">
                        <span class="file-info-label">파일 ID:</span>
                        <span class="file-info-value">${file.id}</span>
                    </div>

                    <div class="file-info-item">
                        <span class="file-info-label">파일 크기:</span>
                        <span class="file-info-value">
                  <fmt:formatNumber
                    value="${file.fileSizeInMB}"
                    maxFractionDigits="2"
                  />
                  MB
                        </span>
                    </div>

                    <div class="file-info-item">
                        <span class="file-info-label">콘텐츠 타입:</span>
                        <span class="file-info-value">${file.contentType}</span>
                    </div>

                    <div class="file-info-item">
                        <span class="file-info-label">업로드 시간:</span>
                        <span class="file-info-value">
                  <fmt:formatDate
                    value="${file.uploadTimeAsDate}"
                    pattern="yyyy-MM-dd HH:mm"
                  />
                        </span>
                    </div>

                    <div class="file-info-item">
                        <span class="file-info-label">수정 시간:</span>
                        <span class="file-info-value">
                  <fmt:formatDate
                    value="${file.lastModifiedTimeAsDate}"
                    pattern="yyyy-MM-dd HH:mm"
                  />
                        </span>
                    </div>
                </div>

                <div class="d-grid gap-2">
              <a
                href="${pageContext.request.contextPath}/web/files/download/${file.id}"
                class="btn btn-info"
              >
                        <i class="bi bi-download"></i> 파일 다운로드
                    </a>
              <a
                href="${pageContext.request.contextPath}/web/files"
                class="btn btn-cancel"
              >
                        <i class="bi bi-arrow-left"></i> 목록으로 돌아가기
                    </a>
                </div>
            </div>
        </div>
    </div>
</div>

<%@ include file="../common/footer.jsp" %>
</body>
</html>
