<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>

<c:set var="pageTitle" value="CoSeller 수동 등록" />
<c:set var="pageCSS" value="${['corp.css']}" />
<c:set var="pageJS" value="${['utils.js']}" />

<%@ include file="../common/header.jsp" %>
<%@ include file="../common/navigation.jsp" %>

<div class="container-fluid mt-4">
  <div class="d-flex justify-content-between align-items-center mb-4">
    <h2><i class="bi bi-cloud-upload"></i> CoSeller 수동 등록</h2>
  </div>

  <c:if test="${not empty resultMsg}">
    <div class="alert alert-info alert-dismissible fade show" role="alert">
      <i class="bi bi-info-circle"></i> ${resultMsg}
      <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    </div>
  </c:if>

  <div class="search-container">
    <h5 class="search-title"><i class="bi bi-geo-alt"></i> 지역 선택</h5>
    <form id="cosellerForm" class="search-form" onsubmit="return false;">
      <div class="row g-3">
        <div class="col-md-6">
          <label for="city" class="form-label">시/도</label>
          <select class="form-select" name="city" id="city">
            <c:forEach var="city" items="${cities}">
              <option value="${city}">${city.value}</option>
            </c:forEach>
          </select>
        </div>
        <div class="col-md-6">
          <label for="district" class="form-label">구/군</label>
          <select class="form-select" name="district" id="district">
            <c:forEach var="district" items="${districts}">
              <option value="${district}">${district.value}</option>
            </c:forEach>
          </select>
        </div>
      </div>
      <div class="search-buttons">
        <sec:authorize access="hasAnyRole('ADMIN','MANAGER')">
          <button type="button" class="btn btn-primary" onclick="submitCoseller()">
            <i class="bi bi-check-circle"></i> 등록
          </button>
        </sec:authorize>
        <sec:authorize access="!hasAnyRole('ADMIN','MANAGER')">
          <p class="text-muted mb-0">등록은 관리자 또는 매니저만 할 수 있습니다.</p>
        </sec:authorize>
      </div>
    </form>
  </div>

  <div class="results-container">
    <div class="results-header">
      <h5 class="mb-0"><i class="bi bi-list-ul"></i> 등록 이력</h5>
    </div>
    <div class="table-responsive">
      <table class="table table-hover mb-0">
        <thead>
          <tr>
            <th>등록자</th>
            <th>시/도</th>
            <th>구/군</th>
            <th>등록일시</th>
            <th>결과</th>
          </tr>
        </thead>
        <tbody>
          <c:choose>
            <c:when test="${empty histories}">
              <tr>
                <td colspan="5" class="text-muted">등록 이력이 없습니다.</td>
              </tr>
            </c:when>
            <c:otherwise>
              <c:forEach var="history" items="${histories}">
                <tr>
                  <td><c:out value="${history.username}" /></td>
                  <td><c:out value="${history.city}" /></td>
                  <td><c:out value="${history.district}" /></td>
                  <td><c:out value="${history.registeredAt}" /></td>
                  <td>
                    <span class="badge ${history.success ? 'bg-success' : 'bg-danger'}">
                      <c:out value="${history.result}" />
                    </span>
                  </td>
                </tr>
              </c:forEach>
            </c:otherwise>
          </c:choose>
        </tbody>
      </table>
    </div>
  </div>
</div>

<%@ include file="../common/footer.jsp" %>
</body>
</html>
