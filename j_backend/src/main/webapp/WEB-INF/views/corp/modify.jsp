<%@ page contentType="text/html;charset=UTF-8" %>

<c:set var="pageTitle" value="법인 상세" />

<!DOCTYPE html>
<html>
  <head>
    <%@ include file="../common/head.jsp" %>
  </head>
  <body>
  <%@ include file="../common/navigation.jsp" %>
    <div class="container mt-4">
      <h2>법인 상세</h2>
      <table class="table">
        <tr>
          <th>법인명</th>
          <td>${corp.bizNm}</td>
        </tr>
        <tr>
          <th>사업자번호</th>
          <td>${corp.bizNo}</td>
        </tr>
        <tr>
          <th>법인등록번호</th>
          <td>${corp.corpRegNo}</td>
        </tr>
        <tr>
          <th>시/도</th>
          <td>${corp.siNm}</td>
        </tr>
        <tr>
          <th>구/군</th>
          <td>${corp.sggNm}</td>
        </tr>
        <tr>
          <th>등록자</th>
          <td>${isAdmin ? corp.username : '본인'}</td>
        </tr>
      </table>
      <a href="/corp/list" class="btn btn-secondary">목록</a>
      <a href="/corp/edit/${corp.id}" class="btn btn-warning">수정</a>
    </div>

    <%@ include file="../common/footer.jsp" %> <%@ include
    file="../common/scripts.jsp" %>
  </body>
</html>
