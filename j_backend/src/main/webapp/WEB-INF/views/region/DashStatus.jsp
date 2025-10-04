<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8"%> <%@ taglib prefix="c"
                                           uri="http://java.sun.com/jsp/jstl/core"%> <%@ taglib prefix="fmt"
                                                                                                uri="http://java.sun.com/jsp/jstl/fmt"%>

<c:set var="pageTitle" value="지역별 상세 통계" />
<c:set var="pageCSS" value="${['region.css']}" />
<c:set var="pageJS" value="${['region.js']}" />

<!DOCTYPE html>
<html lang="ko">
<head>
  <%@ include file="../common/head.jsp" %>
</head>
<body>
<%@ include file="../common/navigation.jsp" %>

<div class="container mt-4">
  <div class="d-flex justify-content-between align-items-center mb-4">
    <h2><i class="bi bi-geo-alt"></i> 지역별 상세 통계</h2>
    <div class="dropdown custom-dropdown">
      <button
              class="btn btn-outline-primary dropdown-toggle"
              type="button"
              id="regionDropdown"
              data-bs-toggle="dropdown"
      >
        <i class="bi bi-geo-alt"></i> 지역별 보기
      </button>
      <ul class="dropdown-menu">
        <li><h6 class="dropdown-header">시/도별</h6></li>
        <li>
          <a class="dropdown-item" href="/region/status?siNm=서울특별시"
          >서울특별시</a
          >
        </li>
        <li>
          <a class="dropdown-item" href="/region/status?siNm=부산광역시"
          >부산광역시</a
          >
        </li>
        <li>
          <a class="dropdown-item" href="/region/status?siNm=대구광역시"
          >대구광역시</a
          >
        </li>
        <li>
          <a class="dropdown-item" href="/region/status?siNm=인천광역시"
          >인천광역시</a
          >
        </li>
        <li>
          <a class="dropdown-item" href="/region/status?siNm=광주광역시"
          >광주광역시</a
          >
        </li>
        <li>
          <a class="dropdown-item" href="/region/status?siNm=대전광역시"
          >대전광역시</a
          >
        </li>
        <li>
          <a class="dropdown-item" href="/region/status?siNm=울산광역시"
          >울산광역시</a
          >
        </li>
        <li>
          <a class="dropdown-item" href="/region/status?siNm=세종특별자치시"
          >세종특별자치시</a
          >
        </li>
        <li>
          <a class="dropdown-item" href="/region/status?siNm=경기도"
          >경기도</a
          >
        </li>
        <li>
          <a class="dropdown-item" href="/region/status?siNm=강원특별자치도"
          >강원특별자치도</a
          >
        </li>
        <li>
          <a class="dropdown-item" href="/region/status?siNm=충청북도"
          >충청북도</a
          >
        </li>
        <li>
          <a class="dropdown-item" href="/region/status?siNm=충청남도"
          >충청남도</a
          >
        </li>
        <li>
          <a class="dropdown-item" href="/region/status?siNm=전북특별자치도"
          >전북특별자치도</a
          >
        </li>
        <li>
          <a class="dropdown-item" href="/region/status?siNm=전라남도"
          >전라남도</a
          >
        </li>
        <li>
          <a class="dropdown-item" href="/region/status?siNm=경상북도"
          >경상북도</a
          >
        </li>
        <li>
          <a class="dropdown-item" href="/region/status?siNm=경상남도"
          >경상남도</a
          >
        </li>
        <li>
          <a class="dropdown-item" href="/region/status?siNm=제주특별자치도"
          >제주특별자치도</a
          >
        </li>
        <li><hr class="dropdown-divider" /></li>
        <li>
          <a class="dropdown-item" href="/region/status">전체 보기</a>
        </li>
      </ul>
    </div>
  </div>

  <c:if test="${not empty siNm}">
    <div class="alert alert-info">
      <i class="bi bi-info-circle"></i> <strong>${siNm}</strong> 지역의 상세
      통계를 표시하고 있습니다.
    </div>
  </c:if>

  <div class="row mb-4">
    <div class="col-md-3">
      <div class="card text-center">
        <div class="card-body">
          <h5 class="card-title text-primary">
            <i class="bi bi-geo-alt"></i> 총 지역 수
          </h5>
          <h2 class="text-primary">${totalRegions}</h2>
        </div>
      </div>
    </div>
    <div class="col-md-3">
      <div class="card text-center">
        <div class="card-body">
          <h5 class="card-title text-success">
            <i class="bi bi-building"></i> 총 법인 수
          </h5>
          <h2 class="text-success">
            <fmt:formatNumber value="${totalCorps}" pattern="#,###" />
          </h2>
        </div>
      </div>
    </div>
    <div class="col-md-3">
      <div class="card text-center">
        <div class="card-body">
          <h5 class="card-title text-info">
            <i class="bi bi-calculator"></i> 평균 법인/지역
          </h5>
          <h2 class="text-info">
            <fmt:formatNumber
                    value="${averageCorpsPerRegion}"
                    pattern="#.#"
            />
          </h2>
        </div>
      </div>
    </div>
    <div class="col-md-3">
      <div class="card text-center">
        <div class="card-body">
          <h5 class="card-title text-warning">
            <i class="bi bi-star"></i> 상위 지역
          </h5>
          <h2 class="text-warning">${topRegions}</h2>
        </div>
      </div>
    </div>
  </div>

  <div class="card">
    <div class="card-header">
      <div class="d-flex justify-content-between align-items-center">
        <h5 class="mb-0">
          <i class="bi bi-table"></i> 지역별 상세 현황
          <c:if test="${not empty siNm}"> - ${siNm} </c:if>
        </h5>
        <div class="input-group" style="width: 300px">
          <input
                  type="text"
                  class="form-control"
                  placeholder="지역명 검색..."
                  id="regionSearch"
          />
          <button
                  class="btn btn-outline-secondary"
                  type="button"
                  onclick="searchRegions()"
          >
            <i class="bi bi-search"></i>
          </button>
        </div>
      </div>
    </div>
    <div class="card-body p-0">
      <div class="table-responsive">
        <table class="table table-hover mb-0">
          <thead class="table-dark">
          <tr>
            <th>순위</th>
            <th>시/도</th>
            <th>구/군</th>
            <th>법인 수</th>
            <th>비율</th>
            <th>상세</th>
          </tr>
          </thead>
          <tbody>
          <c:forEach
                  var="region"
                  items="${regionStats}"
                  varStatus="status"
          >
            <tr>
              <td>
                      <span
                              class="badge <c:choose> <c:when test='${status.index < 5}'>bg-warning</c:when> <c:when test='${status.index < 10}'>bg-info</c:when> <c:otherwise>bg-secondary</c:otherwise> </c:choose>"
                      >
                          ${status.index + 1}
                      </span>
              </td>
              <td><i class="bi bi-geo-alt"></i> ${region.siNm}</td>
              <td>
                <i class="bi bi-geo"></i>
                <c:choose>
                  <c:when test="${not empty region.sggNm}">
                    ${region.sggNm}
                  </c:when>
                  <c:otherwise>
                    <span class="text-muted">전체</span>
                  </c:otherwise>
                </c:choose>
              </td>
              <td>
                      <span class="fw-bold">
                        <fmt:formatNumber
                                value="${region.totalCount}"
                                pattern="#,###"
                        />
                      </span>
              </td>
              <td>
                <div class="progress" style="height: 20px">
                  <div
                          class="progress-bar bg-primary"
                          role="progressbar"
                          style="width: ${region.percentage}%"
                  >
                    <fmt:formatNumber
                            value="${region.percentage}"
                            pattern="#.##"
                    />%
                  </div>
                </div>
              </td>
              <td>
                <a
                        href="/corp/list?siNm=${region.siNm}<c:if test='${not empty region.sggNm}'>&sggNm=${region.sggNm}</c:if>"
                        class="btn btn-sm btn-outline-primary"
                >
                  <i class="bi bi-eye"></i> 상세
                </a>
              </td>
            </tr>
          </c:forEach>
          </tbody>
        </table>
      </div>
    </div>
  </div>

  <c:if test="${totalPages > 1}">
    <nav aria-label="페이지 네비게이션" class="mt-4">
      <ul class="pagination justify-content-center">
        <li class="page-item ${currentPage == 0 ? 'disabled' : ''}">
          <a
                  class="page-link"
                  href="?page=${currentPage - 1}&size=${size}<c:if test='${not empty siNm}'>&siNm=${siNm}</c:if>"
          >
            이전
          </a>
        </li>
        <c:forEach begin="0" end="${totalPages - 1}" var="pageNum">
          <li class="page-item ${currentPage == pageNum ? 'active' : ''}">
            <a
                    class="page-link"
                    href="?page=${pageNum}&size=${size}<c:if test='${not empty siNm}'>&siNm=${siNm}</c:if>"
            >
                ${pageNum + 1}
            </a>
          </li>
        </c:forEach>
        <li
                class="page-item ${currentPage == totalPages - 1 ? 'disabled' : ''}"
        >
          <a
                  class="page-link"
                  href="?page=${currentPage + 1}&size=${size}<c:if test='${not empty siNm}'>&siNm=${siNm}</c:if>"
          >
            다음
          </a>
        </li>
      </ul>
    </nav>
  </c:if>
</div>

<%@ include file="../common/footer.jsp" %> <%@ include
        file="../common/scripts.jsp" %>
<script>
  function searchRegions() {
    const searchTerm = document
            .getElementById("regionSearch")
            .value.toLowerCase();
    const rows = document.querySelectorAll("tbody tr");

    rows.forEach((row) => {
      const siNm = row.cells[1].textContent.toLowerCase();
      const sggNm = row.cells[2].textContent.toLowerCase();

      if (siNm.includes(searchTerm) || sggNm.includes(searchTerm)) {
        row.style.display = "";
      } else {
        row.style.display = "none";
      }
    });
  }

  document
          .getElementById("regionSearch")
          .addEventListener("keypress", function (e) {
            if (e.key === "Enter") {
              searchRegions();
            }
          });

  function cleanupModalBackdrop() {
    const backdrops = document.querySelectorAll(".modal-backdrop");
    backdrops.forEach((backdrop) => backdrop.remove());

    document.body.classList.remove("modal-open");
    document.body.style.overflow = "";
    document.body.style.paddingRight = "";
  }

  document.addEventListener("DOMContentLoaded", function () {
    cleanupModalBackdrop();

    document.querySelectorAll("tbody tr").forEach((row) => {
      row.addEventListener("click", function (e) {
        if (
                e.target.closest("button") ||
                e.target.closest("a") ||
                e.target.closest("form")
        ) {
          return;
        }

        document
                .querySelectorAll("tbody tr")
                .forEach((r) => r.classList.remove("table-active"));
        this.classList.add("table-active");
      });
    });
  });
</script>
</body>
</html>