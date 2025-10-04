<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/js/bootstrap.bundle.min.js"></script>
<script src="/static/js/common.js"></script>
<script src="/static/js/modal.js"></script>
<script src="/static/js/utils.js"></script>
<script src="/static/js/init.js"></script>

<script>
  document.addEventListener("DOMContentLoaded", function () {
    if (typeof bootstrap !== "undefined" && bootstrap.Dropdown) {
      const dropdownElementList = [].slice.call(document.querySelectorAll(".dropdown-toggle"));
      const dropdownList = dropdownElementList.map(function (dropdownToggleEl) {
        return new bootstrap.Dropdown(dropdownToggleEl);
      });
    } else {
      const dropdownToggles = document.querySelectorAll(".dropdown-toggle");
      dropdownToggles.forEach((toggle) => {
        toggle.addEventListener("click", function (e) {
          e.preventDefault();
          e.stopPropagation();
          const dropdownMenu = this.nextElementSibling;
          if (dropdownMenu && dropdownMenu.classList.contains("dropdown-menu")) {
            const isOpen = dropdownMenu.classList.contains("show");
            document.querySelectorAll(".dropdown-menu.show").forEach((menu) => {
              if (menu !== dropdownMenu) {
                menu.classList.remove("show");
              }
            });
            if (isOpen) {
              dropdownMenu.classList.remove("show");
            } else {
              dropdownMenu.classList.add("show");
            }
          }
        });
      });
      document.addEventListener("click", function (e) {
        if (!e.target.closest(".dropdown")) {
          document.querySelectorAll(".dropdown-menu.show").forEach((menu) => {
            menu.classList.remove("show");
          });
        }
      });
    }
  });
</script>

<c:if test="${not empty pageJS}">
  <c:forEach var="js" items="${pageJS}">
    <script src="/static/js/${js}"></script>
  </c:forEach>
</c:if>

<c:if test="${not empty includeCKEditor}">
  <script src="https://cdn.ckeditor.com/ckeditor5/35.0.1/classic/ckeditor.js"></script>
</c:if>