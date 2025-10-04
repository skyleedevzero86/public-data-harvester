<%@ page contentType="text/html;charset=UTF-8" language="java" %> <%@ taglib
prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="pageTitle" value="CoSeller 수동 등록" />

<html>
  <head>
    <%@ include file="../common/head.jsp" %>
  </head>
  <body>
  <%@ include file="../common/navigation.jsp" %>
    <h2>CoSeller 수동 등록</h2>
    <form id="cosellerForm" onsubmit="return false;">
      <label for="city">시/도:</label>
      <select name="city" id="city">
        <c:forEach var="city" items="${cities}">
          <option value="${city}">${city.value}</option>
        </c:forEach>
      </select>
      <label for="district">구/군:</label>
      <select name="district" id="district">
        <c:forEach var="district" items="${districts}">
          <option value="${district}">${district.value}</option>
        </c:forEach>
      </select>
      <button type="button" onclick="submitCoseller()">등록</button>
    </form>

    <c:if test="${not empty resultMsg}">
      <div style="margin-top: 20px; color: blue">${resultMsg}</div>
    </c:if>

    <h3>등록 이력 (샘플)</h3>
    <table border="1">
      <tr>
        <th>등록자</th>
        <th>시/도</th>
        <th>구/군</th>
        <th>등록일시</th>
        <th>결과</th>
      </tr>
      <tr>
        <td>admin</td>
        <td>서울특별시</td>
        <td>강서구</td>
        <td>2024-07-07 12:34:56</td>
        <td>성공(100건)</td>
      </tr>
      <tr>
        <td>user1</td>
        <td>서울특별시</td>
        <td>강남구</td>
        <td>2024-07-06 09:12:00</td>
        <td>실패(중복 3건)</td>
      </tr>
    </table>

    <%@ include file="../common/footer.jsp" %>
  </body>

  <%@ include file="../common/scripts.jsp" %>
</html>
