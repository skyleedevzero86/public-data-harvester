<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>파일 정보 수정</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 20px; }
        .container { max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #ccc; border-radius: 8px; }
        h2 { text-align: center; color: #333; }
        form div { margin-bottom: 15px; }
        label { display: block; margin-bottom: 5px; font-weight: bold; }
        input[type="text"] { width: calc(100% - 22px); padding: 10px; border: 1px solid #ddd; border-radius: 4px; }
        input[type="submit"] { background-color: #007bff; color: white; padding: 10px 15px; border: none; border-radius: 4px; cursor: pointer; font-size: 16px; width: 100%; }
        input[type="submit"]:hover { background-color: #0056b3; }
        .message { color: green; text-align: center; margin-top: 10px; }
        .error { color: red; text-align: center; margin-top: 10px; }
        .button-group { text-align: center; margin-top: 20px; }
        .button-group a { display: inline-block; padding: 10px 20px; background-color: #6c757d; color: white; text-decoration: none; border-radius: 5px; }
        .button-group a:hover { background-color: #5a6268; }
    </style>
</head>
<body>
<div class="container">
    <h2>파일 정보 수정</h2>

    <form action="${pageContext.request.contextPath}/web/files/update/${file.id}" method="post">
        <div>
            <label for="originalFileName">원본 파일명:</label>
            <input type="text" id="originalFileName" name="originalFileName" value="${file.originalFileName}" readonly>
        </div>
        <div>
            <label for="description">파일 설명:</label>
            <input type="text" id="description" name="description" value="${file.description}">
        </div>
        <div>
            <input type="submit" value="저장">
        </div>
    </form>

    <div class="button-group">
        <a href="${pageContext.request.contextPath}/web/files">목록으로 돌아가기</a>
    </div>
</div>
</body>
</html>