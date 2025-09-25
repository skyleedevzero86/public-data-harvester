<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8"%> <%@ taglib prefix="c"
                                           uri="http://java.sun.com/jsp/jstl/core" %>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/js/bootstrap.bundle.min.js"></script>

<script src="/static/js/common.js"></script>
<script src="/static/js/modal.js"></script>
<script src="/static/js/utils.js"></script>
<script src="/static/js/init.js"></script>

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