<%@ page language="java" contentType="text/html; charset=UTF-8"
pageEncoding="UTF-8"%> <%@ taglib uri="http://java.sun.com/jsp/jstl/core"
prefix="c" %> <%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<c:set var="pageTitle" value="파일 상세 정보" />

<!DOCTYPE html>
<html>
  <head>
    <%@ include file="../common/head.jsp" %>
  </head>
  <body>
  <%@ include file="../common/navigation.jsp" %>
    <div class="container">
      <h2>파일 상세 정보</h2>

      <c:if test="${not empty error}">
        <p class="error">${error}</p>
      </c:if>

      <c:if test="${not empty file}">
        <div class="detail-item">
          <span class="detail-label">파일 ID:</span>
          <span class="detail-value">${file.id}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">원본 파일명:</span>
          <span class="detail-value">${file.originalFileName}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">저장된 파일명:</span>
          <span class="detail-value">${file.storedFileName}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">파일 크기:</span>
          <span class="detail-value"
            ><fmt:formatNumber
              value="${file.fileSizeInMB}"
              maxFractionDigits="2"
            />
            MB</span
          >
        </div>
        <div class="detail-item">
          <span class="detail-label">콘텐츠 타입:</span>
          <span class="detail-value">${file.contentType}</span>
        </div>
        <div class="detail-item">
          <span class="detail-label">업로드 시간:</span>
          <span class="detail-value"
            ><fmt:formatDate
              value="${file.uploadTimeAsDate}"
              pattern="yyyy-MM-dd HH:mm:ss"
          /></span>
        </div>
        <div class="detail-item">
          <span class="detail-label">수정 시간:</span>
          <span class="detail-value"
            ><fmt:formatDate
              value="${file.lastModifiedTimeAsDate}"
              pattern="yyyy-MM-dd HH:mm:ss"
          /></span>
        </div>
        <div class="detail-item">
          <span class="detail-label">설명:</span>
          <span class="detail-value">${file.description}</span>
        </div>

        <div class="button-group">
          <a
            href="${pageContext.request.contextPath}/web/files"
            class="back-btn"
            >파일 목록으로</a
          >
          <c:if test="${not empty file.downloadUrl}">
            <a href="${file.downloadUrl}" class="download-btn" target="_blank"
              >파일 다운로드</a
            >
          </c:if>
          <c:if test="${empty file.downloadUrl}">
            <a
              href="${pageContext.request.contextPath}/web/files/download/${file.id}"
              class="download-btn"
              >파일 다운로드</a
            >
          </c:if>
        </div>
      </c:if>
    </div>

    <%@ include file="../common/footer.jsp" %> <%@ include
    file="../common/scripts.jsp" %>
  </body>
</html>
