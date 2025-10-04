<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>

<c:set var="pageTitle" value="메인" />
<c:set var="pageCSS" value="${['common.css']}" />
<c:set var="pageJS" value="${['common.js']}" />

<%@ include file="../common/header.jsp" %>
<body>
<%@ include file="../common/navigation.jsp" %>

<div class="container-fluid mt-4">
    <div class="d-flex justify-content-between align-items-center mb-4">
        <h2><i class="bi bi-house"></i>  통신판매사업자관리 시스템 현황</h2>
    </div>

    <div class="row mb-4">
        <div class="col-xl-3 col-md-6 col-sm-6">
            <div class="card bg-primary text-white mb-4">
                <div class="card-body">
                    <div class="d-flex justify-content-between align-items-center">
                        <div>
                            <div class="text-white-50 small">총 등록 업체</div>
                            <div class="fs-4 fw-bold">${stats.total}</div>
                        </div>
                        <div class="icon-align">
                            <i class="bi bi-building stats-icon"></i>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <div class="col-xl-3 col-md-6 col-sm-6">
            <div class="card bg-success text-white mb-4">
                <div class="card-body">
                    <div class="d-flex justify-content-between align-items-center">
                        <div>
                            <div class="text-white-50 small">유효 법인등록번호</div>
                            <div class="fs-4 fw-bold">${stats.validCorpRegNo}</div>
                        </div>
                        <div class="icon-align">
                            <i class="bi bi-check-circle-fill stats-icon"></i>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <div class="col-xl-3 col-md-6 col-sm-6">
            <div class="card bg-info text-white mb-4">
                <div class="card-body">
                    <div class="d-flex justify-content-between align-items-center">
                        <div>
                            <div class="text-white-50 small">유효 행정구역코드</div>
                            <div class="fs-4 fw-bold">${stats.validRegionCd}</div>
                        </div>
                        <div class="icon-align">
                            <i class="bi bi-geo-alt-fill stats-icon"></i>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <div class="col-xl-3 col-md-6 col-sm-6">
            <div class="card bg-warning text-white mb-4">
                <div class="card-body">
                    <div class="d-flex justify-content-between align-items-center">
                        <div>
                            <div class="text-white-50 small">데이터 수집 성공률</div>
                            <div class="fs-4 fw-bold">${stats.successRate}%</div>
                        </div>
                        <div class="icon-align">
                            <i class="bi bi-graph-up-arrow stats-icon"></i>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <div class="row mt-4">
        <div class="col-md-6">
            <div class="card">
                <div class="card-header">
                    <div class="d-flex justify-content-between align-items-center">
                        <h5 class="mb-0">
                            <i class="bi bi-download"></i> 데이터 수집
                        </h5>
                    </div>
                </div>
                <div class="card-body">
                    <div class="row">
                        <div class="col-md-6">
                            <div class="mb-3">
                                <label for="citySelect" class="form-label">시/도 선택</label>
                                <select class="form-select" id="citySelect">
                                    <option value="city">시/도 선택</option>
                                    <option value="seoul">서울특별시</option>
                                    <option value="busan">부산광역시</option>
                                    <option value="daegu">대구광역시</option>
                                    <option value="incheon">인천광역시</option>
                                    <option value="gwangju">광주광역시</option>
                                    <option value="daejeon">대전광역시</option>
                                    <option value="ulsan">울산광역시</option>
                                    <option value="gyeonggi">경기도</option>
                                    <option value="gangwon">강원특별자치도</option>
                                    <option value="chungbuk">충청북도</option>
                                    <option value="chungnam">충청남도</option>
                                    <option value="jeonbuk">전라북도</option>
                                    <option value="jeonnam">전라남도</option>
                                    <option value="gyeongbuk">경상북도</option>
                                    <option value="gyeongnam">경상남도</option>
                                    <option value="jeju">제주특별자치도</option>
                                </select>
                            </div>
                        </div>
                        <div class="col-md-6">
                            <div class="mb-3">
                                <label for="districtSelect" class="form-label">구/군 선택</label>
                                <select class="form-select" id="districtSelect">
                                    <option>구/군 선택</option>
                                </select>
                            </div>
                        </div>
                    </div>
                    <sec:authorize access="hasAnyRole('MANAGER', 'ADMIN')">
                        <div class="text-end">
                            <button type="button" class="btn btn-primary btn-lg" id="collectDataBtn">
                                <i class="bi bi-download"></i> 데이터 수집 시작
                            </button>
                        </div>
                    </sec:authorize>
                    <div class="mt-3">
                        <div class="alert alert-info">
                            <i class="bi bi-info-circle"></i>
                            <strong>데이터 수집 안내:</strong>
                            <ul class="mb-0 mt-2">
                                <li>시/도와 구/군을 선택한 후 "데이터 수집 시작" 버튼을 클릭하세요.</li>
                                <li>MinIO에 저장된 해당 지역의 CSV 파일을 읽어서 데이터베이스에 저장합니다.</li>
                                <li>원본 파일명 형식: <code>시도명_구군명.csv</code></li>
                            </ul>
                        </div>
                    </div>

                    <div class="d-flex justify-content-end flex-wrap gap-2 mt-3">
                        <a href="/region/status" class="action-btn">
                            <i class="bi bi-map"></i> 지역별 현황 보기
                        </a>
                        <sec:authorize access="hasAnyRole('MANAGER', 'ADMIN')">
                            <a href="/corp/search" class="action-btn">
                                <i class="bi bi-search"></i> 법인 정보 검색
                            </a>
                            <button type="button" class="action-btn" onclick="openFileUploadModal()">
                                <i class="bi bi-cloud-upload"></i> 파일 업로드
                            </button>
                        </sec:authorize>
                    </div>
                </div>
            </div>
        </div>

        <div class="col-md-6">
            <div class="card">
                <div class="card-header">
                    <div class="d-flex justify-content-between align-items-center">
                        <h5 class="mb-0"><i class="bi bi-activity"></i> 최근 활동</h5>
                    </div>
                </div>
                <div class="card-body">
                    <ul class="activity-list">
                        <c:choose>
                            <c:when test="${not empty recentActivities and recentActivities.size() > 0}">
                                <c:forEach var="activity" items="${recentActivities}">
                                    <li class="activity-item">
                                        <span class="dot ${activity.type}"></span>
                                        <span class="activity-msg">${activity.message}</span>
                                        <span class="activity-time">${activity.timeAgo}</span>
                                    </li>
                                </c:forEach>
                            </c:when>
                            <c:otherwise>
                                <li class="activity-item">
                                    <span class="dot lightblue"></span>
                                    <span class="activity-msg">시스템이 정상적으로 실행 중입니다</span>
                                    <span class="time">방금 전</span>
                                </li>
                                <li class="activity-item">
                                    <span class="dot green"></span>
                                    <span class="activity-msg">데이터베이스 연결 확인됨</span>
                                    <span class="time">1분 전</span>
                                </li>
                                <li class="activity-item">
                                    <span class="dot yellow"></span>
                                    <span class="activity-msg">새로운 데이터 수집 대기 중</span>
                                    <span class="time">5분 전</span>
                                </li>
                            </c:otherwise>
                        </c:choose>
                    </ul>
                </div>
            </div>
        </div>
    </div>

    <div class="row mt-4">
        <div class="col-12">
            <div class="card">
                <div class="card-header">
                    <div class="d-flex justify-content-between align-items-center">
                        <h5 class="mb-0">
                            <i class="bi bi-bar-chart"></i> 지역별 통계 현황
                        </h5>
                    </div>
                </div>
                <div class="card-body">
                    <c:choose>
                        <c:when test="${not empty regionStats and regionStats.size() > 0}">
                            <div class="table-responsive">
                                <table class="table table-hover">
                                    <thead class="table-dark">
                                        <tr>
                                            <th>지역명</th>
                                            <th>총 업체 수</th>
                                            <th>법인등록번호</th>
                                            <th>행정구역코드</th>
                                            <th>완성도</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <c:forEach var="regionStat" items="${regionStats}">
                                            <tr>
                                                <td>
                                                    <strong>${regionStat.city} ${regionStat.district}</strong>
                                                </td>
                                                <td>
                                                    <span class="badge bg-primary fs-6">
                                                        <fmt:formatNumber value="${regionStat.totalCount}" pattern="#,##0"/>
                                                    </span>
                                                </td>
                                                <td>
                                                    <fmt:formatNumber value="${regionStat.validCorpRegNoCount}" pattern="#,##0"/>
                                                    <span class="text-muted">
                                                        (<fmt:formatNumber value="${regionStat.validCorpRegNoCount * 100.0 / regionStat.totalCount}" pattern="0.0"/>%)
                                                    </span>
                                                </td>
                                                <td>
                                                    <fmt:formatNumber value="${regionStat.validRegionCdCount}" pattern="#,##0"/>
                                                    <span class="text-muted">
                                                        (<fmt:formatNumber value="${regionStat.validRegionCdCount * 100.0 / regionStat.totalCount}" pattern="0.0"/>%)
                                                    </span>
                                                </td>
                                                <td>
                                                    <div class="progress" style="height: 12px">
                                                        <div class="progress-bar bg-primary" role="progressbar" 
                                                             data-width="${regionStat.completionRate}"
                                                             aria-valuenow="${regionStat.completionRate}" 
                                                             aria-valuemin="0" aria-valuemax="100"></div>
                                                    </div>
                                                    <span class="text-primary fw-bold">${regionStat.completionRate}%</span>
                                                </td>
                                            </tr>
                                        </c:forEach>
                                    </tbody>
                                </table>
                            </div>
                            <div class="text-center mt-4">
                                <a href="/region/detail" class="btn btn-view-all-stats">
                                    <i class="bi bi-bar-chart"></i> 전체 통계 보기
                                </a>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <div class="text-center py-5">
                                <i class="bi bi-bar-chart fs-1 text-muted"></i>
                                <h5 class="text-muted mt-3">등록된 지역 통계가 없습니다</h5>
                                <p class="text-muted">데이터 수집을 통해 지역별 통계를 확인할 수 있습니다.</p>
                                <a href="/region/detail" class="btn btn-primary">
                                    <i class="bi bi-map"></i> 지역별 현황 보기
                                </a>
                            </div>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>
        </div>
</div>

<div id="fileUploadModal" class="file-upload-modal">
    <div class="file-upload-content">
        <div class="file-upload-header">
            <h3><i class="bi bi-cloud-upload"></i> 파일 업로드</h3>
            <button type="button" class="file-upload-close" onclick="closeFileUploadModal()">
                &times;
            </button>
        </div>
        <div class="file-upload-body">
            <form class="file-upload-form" action="${pageContext.request.contextPath}/web/files/upload" 
                  method="post" enctype="multipart/form-data">
                <div class="form-group">
                    <label for="file">파일 선택:</label>
                    <input type="file" id="file" name="file" required />
                </div>
                <div class="form-group">
                    <label for="description">파일 설명:</label>
                    <input type="text" id="description" name="description" 
                           placeholder="파일에 대한 설명을 입력하세요" />
                </div>
                <div class="form-group">
                    <input type="submit" value="업로드" />
                </div>
            </form>

            <div class="file-upload-buttons">
                <a href="/web/files" class="file-upload-btn primary" onclick="closeFileUploadModal()">
                    <i class="bi bi-list"></i> 파일 목록 보기
                </a>
                <a href="/api/v1/files/template" class="file-upload-btn info" download>
                    <i class="bi bi-download"></i> CSV 양식 다운로드
                </a>
            </div>
        </div>
    </div>
</div>

<%@ include file="../common/footer.jsp" %>
<%@ include file="../common/scripts.jsp" %>
</body>
</html>


