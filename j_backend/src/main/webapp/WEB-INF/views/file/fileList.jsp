<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>파일 목록</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 20px; }
        .container { max-width: 1200px; margin: 0 auto; padding: 20px; border: 1px solid #ccc; border-radius: 8px; }
        h2 { text-align: center; color: #333; }
        .message { color: green; text-align: center; margin-top: 10px; }
        .error { color: red; text-align: center; margin-top: 10px; }
        .search-form { display: flex; justify-content: center; margin-bottom: 20px; }
        .search-form input[type="text"] { flex-grow: 1; padding: 10px; border: 1px solid #ddd; border-radius: 4px; margin-right: 10px; max-width: 300px; }
        .search-form input[type="submit"] { background-color: #007bff; color: white; padding: 10px 15px; border: none; border-radius: 4px; cursor: pointer; }
        .search-form input[type="submit"]:hover { background-color: #0056b3; }
        table { width: 100%; border-collapse: collapse; margin-top: 20px; }
        th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }
        th { background-color: #f2f2f2; }
        .actions a { margin-right: 10px; text-decoration: none; padding: 5px 10px; border-radius: 3px; }
        .actions .download-btn { background-color: #17a2b8; color: white; }
        .actions .edit-btn { background-color: #ffc107; color: black; }
        .actions .delete-form button { background-color: #dc3545; color: white; border: none; padding: 5px 10px; border-radius: 3px; cursor: pointer; }
        .actions .delete-form button:hover { background-color: #c82333; }
        .button-group { text-align: center; margin-top: 20px; }
        .button-group a { display: inline-block; padding: 10px 20px; background-color: #28a745; color: white; text-decoration: none; border-radius: 5px; }
        .button-group a:hover { background-color: #218838; }
    </style>
</head>
<body>
<div class="container">
    <h2>업로드된 파일 목록</h2>

    <c:if test="${not empty message}">
        <p class="message">${message}</p>
    </c:if>
    <c:if test="${not empty error}">
        <p class="error">${error}</p>
    </c:if>

    <div class="search-form">
        <form action="${pageContext.request.contextPath}/web/files" method="get">
            <input type="text" name="keyword" placeholder="파일명 또는 설명으로 검색" value="${keyword}">
            <input type="submit" value="검색">
        </form>
    </div>

    <table>
        <thead>
        <tr>
            <th>ID</th>
            <th>원본 파일명</th>
            <th>파일 크기</th>
            <th>콘텐츠 타입</th>
            <th>업로드 시간</th>
            <th>수정 시간</th>
            <th>설명</th>
            <th>다운로드</th>
            <th>관리</th>
        </tr>
        </thead>
        <tbody>
        <c:forEach var="file" items="${files}">
            <tr>
                <td>${file.id}</td>
                <td>${file.originalFileName}</td>
                <td><fmt:formatNumber value="${file.fileSizeInMB}" maxFractionDigits="2" /> MB</td>
                <td>${file.contentType}</td>
                <td><fmt:formatDate value="${file.uploadTimeAsDate}" pattern="yyyy-MM-dd HH:mm:ss"/></td>
                <td><fmt:formatDate value="${file.lastModifiedTimeAsDate}" pattern="yyyy-MM-dd HH:mm:ss"/></td>
                <td>${file.description}</td>
                <td>
                    <c:if test="${not empty file.downloadUrl}">
                        <a href="${file.downloadUrl}" class="download-btn" target="_blank">다운로드</a>
                    </c:if>
                    <c:if test="${empty file.downloadUrl}">
                        <a href="${pageContext.request.contextPath}/web/files/download/${file.id}" class="download-btn">다운로드</a>
                    </c:if>
                </td>
                <td class="actions">
                    <a href="${pageContext.request.contextPath}/web/files/edit/${file.id}" class="edit-btn">수정</a>
                    <form action="${pageContext.request.contextPath}/web/files/delete/${file.id}" method="post" style="display:inline;" class="delete-form">
                        <button type="submit" onclick="return confirm('정말로 삭제하시겠습니까?');">삭제</button>
                    </form>
                </td>
            </tr>
        </c:forEach>
        <c:if test="${empty files}">
            <tr>
                <td colspan="9" style="text-align: center;">파일이 없습니다.</td>
            </tr>
        </c:if>
        </tbody>
    </table>

    <div class="button-group">
        <a href="${pageContext.request.contextPath}/web/files/upload">새 파일 업로드</a>
    </div>
</div>
</body>
</html>