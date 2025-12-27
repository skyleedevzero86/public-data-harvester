<%@ page language="java" contentType="text/html; charset=UTF-8"
pageEncoding="UTF-8"%> <%@ taglib prefix="c"
uri="http://java.sun.com/jsp/jstl/core" %>

<script
  src="https://cdn.jsdelivr.net/npm/chart.js@4.4.0/dist/chart.umd.js?v=20241205"
  crossorigin="anonymous"
  onload="console.log('Chart.js 로드 성공'); window.dispatchEvent(new Event('chartjs-loaded'));"
  onerror="console.error('Chart.js 로드 실패, 폴백 시도'); this.onerror=null; this.src='https://cdnjs.cloudflare.com/ajax/libs/Chart.js/4.4.0/chart.umd.js?v=20241205'; this.onload=function(){console.log('Chart.js 폴백 로드 성공'); window.dispatchEvent(new Event('chartjs-loaded'));};"
></script>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/js/bootstrap.bundle.min.js"></script>

<script src="/static/js/chart-init.js"></script>
<script src="/static/js/common.js"></script>
<script src="/static/js/modal.js"></script>
<script src="/static/js/utils.js"></script>
<script src="/static/js/init.js"></script>
<script src="/static/js/dropdown.js"></script>

<c:if test="${not empty pageJS}">
  <c:forEach var="js" items="${pageJS}">
    <script src="/static/js/${js}"></script>
  </c:forEach>
</c:if>

<c:if test="${not empty includeCKEditor}">
  <script
    src="https://cdn.ckeditor.com/ckeditor5/35.0.1/classic/ckeditor.js"
  ></script>
</c:if>
