<%@ page language="java" contentType="text/html; charset=UTF-8"
pageEncoding="UTF-8"%>

<c:set var="pageTitle" value="파일 업로드" />

<!DOCTYPE html>
<html>
  <head>
    <%@ include file="../common/head.jsp" %>
  </head>
  <body>
    <div class="container">
      <h2>파일 업로드</h2>

      <form
        action="${pageContext.request.contextPath}/web/files/upload"
        method="post"
        enctype="multipart/form-data"
      >
        <div>
          <label for="file">파일 선택:</label>
          <input type="file" id="file" name="file" required />
        </div>
        <div>
          <label for="description">파일 설명:</label>
          <input
            type="text"
            id="description"
            name="description"
            placeholder="파일에 대한 설명을 입력하세요"
          />
        </div>
        <div>
          <input type="submit" value="업로드" />
        </div>
      </form>

      <div class="button-group">
        <a href="${pageContext.request.contextPath}/web/files"
          >파일 목록 보기</a
        >
        <a href="/api/v1/files/template" class="btn btn-info" download
          >CSV 양식 다운로드</a
        >
      </div>
    </div>

    <%@ include file="../common/footer.jsp" %> <%@ include
    file="../common/scripts.jsp" %>
  </body>
</html>
