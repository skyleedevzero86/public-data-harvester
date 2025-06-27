<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>파일 상세 정보</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 20px; background-color: #f4f7f6; }
        .container { max-width: 700px; margin: 30px auto; padding: 25px; border: 1px solid #e0e0e0; border-radius: 10px; background-color: #ffffff; box-shadow: 0 4px 8px rgba(0,0,0,0.05); }
        h2 { text-align: center; color: #333; margin-bottom: 25px; border-bottom: 2px solid #f0f0f0; padding-bottom: 10px; }
        .detail-item { display: flex; align-items: center; margin-bottom: 15px; padding-bottom: 10px; border-bottom: 1px dashed #f0f0f0; }
        .detail-item:last-child { border-bottom: none; }
        .detail-label { font-weight: bold; color: #555; width: 150px; flex-shrink: 0; }
        .detail-value { flex-grow: 1; color: #333; word-wrap: break-word; }
        .button-group { text-align: center; margin-top: 30px; }
        .button-group a { display: inline-block; padding: 12px 25px; margin: 0 10px; border-radius: 5px; text-decoration: none; font-size: 16px; transition: background-color 0.3s ease; }
        .back-btn { background-color: #6c757d; color: white; }
        .back-btn:hover { background-color: #5a6268; }
        .download-btn { background-color: #007bff; color: white; }
        .download-btn:hover { background-color: #0056b3; }
        .error { color: red; text-align: center; margin-top: 20px; font-weight: bold; }
    </style>
</head>
<body>
<div class="container">
    <h2>파일 상세 정보</h2>

    <c:if test="${not empty error}">
        <p class="error">${error}</p>
    </c:if>

    <c:if test="${empty file && empty error}">
        <p class="error">파일 정보를 찾을 수 없습니다.</p>
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
            <span class="detail-value"><fmt:formatNumber value="${file.fileSize / (1024 * 1024)}" maxFractionDigits="2" /> MB</span>
        </div>
        <div class="detail-item">
            <span class="detail-label">콘텐츠 타입:</span>
            <span class="detail-value">${file.contentType}</span>
        </div>
        <div class="detail-item">
            <span class="detail-label">업로드 시간:</span>
            <span class="detail-value"><fmt:formatDateTime value="${file.uploadTime}" pattern="yyyy-MM-dd HH:mm:ss"/></span>
        </div>
        <div class="detail-item">
            <span class="detail-label">설명:</span>
            <span class="detail-value">${file.description}</span>
        </div>

        <div class="button-group">
            <a href="${pageContext.request.contextPath}/files" class="back-btn">파일 목록으로</a>
            <c:if test="${not empty file.downloadUrl}">
                <a href="${file.downloadUrl}" class="download-btn" target="_blank">파일 다운로드</a>
            </c:if>
            <c:if test="${empty file.downloadUrl}">
                <a href="${pageContext.request.contextPath}/files/download/${file.id}" class="download-btn">파일 다운로드 (서버 경유)</a>
            </c:if>
        </div>
    </c:if>
</div>
</body>
</html>