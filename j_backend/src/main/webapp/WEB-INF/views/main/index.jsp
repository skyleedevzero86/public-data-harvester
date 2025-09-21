<%@ page language="java" contentType="text/html; charset=UTF-8"
pageEncoding="UTF-8"%> <%@ taglib prefix="c"
uri="http://java.sun.com/jsp/jstl/core" %> <%@ taglib prefix="fmt"
uri="http://java.sun.com/jsp/jstl/fmt" %> <%@ taglib prefix="sec"
uri="http://www.springframework.org/security/tags" %>
<!DOCTYPE html>
<html>
  <head>
    <meta charset="UTF-8" />
    <meta name="_csrf" content="${_csrf.token}" />
    <meta name="_csrf_header" content="${_csrf.headerName}" />
    <title>통신판매사업자관리 시스템 - 메인</title>
    <link
      href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css"
      rel="stylesheet"
    />
    <link
      href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.7.2/font/bootstrap-icons.css"
      rel="stylesheet"
    />

    <style>
      body {
        margin: 0;
        padding: 0;
        min-height: 100vh;
        display: flex;
        flex-direction: column;
      }

      .main-content {
        flex: 1;
      }

      .stats-card {
        background: #fff;
        border: 1px solid #dee2e6;
        border-radius: 8px;
        padding: 20px;
        margin-bottom: 20px;
        box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
      }

      .stats-value {
        font-size: 2.5rem;
        font-weight: bold;
        color: #495057;
      }

      .stats-label {
        color: #6c757d;
        font-size: 0.9rem;
        margin-top: 5px;
      }

      .btn-primary {
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        border: none;
        padding: 12px 30px;
        border-radius: 25px;
        font-weight: 600;
        transition: all 0.3s ease;
        box-shadow: 0 4px 15px rgba(102, 126, 234, 0.3);
      }

      .btn-primary:hover {
        transform: translateY(-2px);
        box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
      }

      .btn-primary:disabled {
        opacity: 0.6;
        transform: none;
        box-shadow: 0 4px 15px rgba(102, 126, 234, 0.3);
      }

      .form-select {
        border: 2px solid #e9ecef;
        border-radius: 10px;
        padding: 10px 15px;
        transition: all 0.3s ease;
      }

      .form-select:focus {
        border-color: #667eea;
        box-shadow: 0 0 0 0.2rem rgba(102, 126, 234, 0.25);
      }

      .alert-info {
        background: linear-gradient(135deg, #e3f2fd 0%, #bbdefb 100%);
        border: 1px solid #90caf9;
        border-radius: 15px;
        color: #1565c0;
      }

      .alert-info code {
        background-color: rgba(102, 126, 234, 0.1);
        color: #667eea;
        padding: 2px 6px;
        border-radius: 4px;
        font-size: 0.9em;
      }

      .btn-view-all-stats {
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        border: none;
        color: white;
        padding: 10px 25px;
        border-radius: 25px;
        text-decoration: none;
        transition: all 0.3s ease;
        box-shadow: 0 4px 15px rgba(102, 126, 234, 0.3);
      }

      .btn-view-all-stats:hover {
        transform: translateY(-2px);
        box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
        color: white;
      }

      .action-buttons {
        display: flex;
        gap: 10px;
        margin-top: 20px;
        flex-wrap: wrap;
      }

      .action-btn {
        background: #6c757d;
        border: 1px solid #6c757d;
        color: white;
        padding: 8px 16px;
        border-radius: 6px;
        font-size: 0.9rem;
        display: flex;
        align-items: center;
        gap: 5px;
        transition: all 0.3s ease;
        text-decoration: none;
      }

      .action-btn:hover {
        background: #5a6268;
        color: white;
        text-decoration: none;
      }

      .activity-list {
        list-style: none;
        padding: 0;
        margin: 0;
      }

      .activity-item {
        display: flex;
        align-items: center;
        padding: 10px 0;
        border-bottom: 1px solid #f0f0f0;
      }

      .activity-item:last-child {
        border-bottom: none;
      }

      .dot {
        width: 8px;
        height: 8px;
        border-radius: 50%;
        margin-right: 12px;
        flex-shrink: 0;
      }

      .dot.success {
        background-color: #28a745;
      }

      .dot.info {
        background-color: #17a2b8;
      }

      .dot.warning {
        background-color: #ffc107;
      }

      .dot.error {
        background-color: #dc3545;
      }

      .dot.lightblue {
        background-color: #87ceeb;
      }

      .dot.green {
        background-color: #28a745;
      }

      .dot.red {
        background-color: #dc3545;
      }

      .dot.yellow {
        background-color: #ffc107;
      }

      .dot.gray {
        background-color: #6c757d;
      }

      .activity-msg {
        flex: 1;
        color: #495057;
        font-size: 0.9rem;
      }

      .activity-time {
        color: #6c757d;
        font-size: 0.8rem;
        margin-left: 10px;
      }

      .footer {
        background-color: #343a40;
        color: white;
        padding: 40px 0 20px 0;
        margin-top: auto;
      }

      .footer-logo {
        margin-bottom: 30px;
      }

      .footer-logo .main-title {
        font-size: 1.8rem;
        font-weight: bold;
        margin-bottom: 5px;
      }

      .footer-logo .sub-title {
        font-size: 1rem;
        color: #adb5bd;
      }

      .footer-contact {
        margin-bottom: 25px;
      }

      .footer-contact .contact-title {
        font-size: 1.1rem;
        font-weight: bold;
        margin-bottom: 8px;
        color: #f8f9fa;
      }

      .footer-contact .contact-address {
        font-size: 0.9rem;
        color: #adb5bd;
        margin-bottom: 5px;
        line-height: 1.4;
      }

      .footer-contact .contact-phone {
        font-size: 0.9rem;
        color: #adb5bd;
      }

      .footer-contact .contact-email {
        font-size: 0.9rem;
        color: #adb5bd;
        margin-top: 5px;
      }

      .footer-copyright {
        border-top: 1px solid #495057;
        padding-top: 20px;
        text-align: center;
        font-size: 0.8rem;
        color: #adb5bd;
      }

      .file-upload-modal {
        display: none;
        position: fixed;
        z-index: 1050;
        left: 0;
        top: 0;
        width: 100%;
        height: 100%;
        background-color: rgba(0, 0, 0, 0.5);
      }

      .file-upload-modal.show {
        display: block;
      }

      .file-upload-content {
        background-color: #fefefe;
        margin: 5% auto;
        padding: 0;
        border: none;
        border-radius: 10px;
        width: 90%;
        max-width: 600px;
        box-shadow: 0 4px 20px rgba(0, 0, 0, 0.3);
        animation: modalSlideIn 0.3s ease-out;
      }

      @keyframes modalSlideIn {
        from {
          transform: translateY(-50px);
          opacity: 0;
        }
        to {
          transform: translateY(0);
          opacity: 1;
        }
      }

      .file-upload-header {
        background: linear-gradient(45deg, #28a745, #20c997);
        color: white;
        padding: 20px;
        border-radius: 10px 10px 0 0;
        display: flex;
        justify-content: space-between;
        align-items: center;
      }

      .file-upload-header h3 {
        margin: 0;
        font-size: 1.3rem;
        font-weight: 600;
      }

      .file-upload-close {
        color: white;
        font-size: 28px;
        font-weight: bold;
        cursor: pointer;
        background: none;
        border: none;
        padding: 0;
        width: 30px;
        height: 30px;
        display: flex;
        align-items: center;
        justify-content: center;
        border-radius: 50%;
        transition: background-color 0.3s;
      }

      .file-upload-close:hover {
        background-color: rgba(255, 255, 255, 0.2);
      }

      .file-upload-body {
        padding: 30px;
      }

      .file-upload-form {
        margin-bottom: 20px;
      }

      .file-upload-form .form-group {
        margin-bottom: 15px;
      }

      .file-upload-form label {
        display: block;
        margin-bottom: 5px;
        font-weight: bold;
        color: #495057;
      }

      .file-upload-form input[type="file"],
      .file-upload-form input[type="text"] {
        width: 100%;
        padding: 10px;
        border: 1px solid #ced4da;
        border-radius: 6px;
        font-size: 14px;
      }

      .file-upload-form input[type="submit"] {
        background: #28a745;
        border: none;
        color: white;
        padding: 12px 24px;
        border-radius: 6px;
        font-weight: bold;
        cursor: pointer;
        font-size: 16px;
        width: 100%;
        transition: background-color 0.3s;
      }

      .file-upload-form input[type="submit"]:hover {
        background: #218838;
      }

      .file-upload-buttons {
        display: flex;
        gap: 10px;
        margin-top: 20px;
      }

      .file-upload-btn {
        flex: 1;
        padding: 12px 20px;
        border: none;
        border-radius: 6px;
        font-weight: bold;
        text-decoration: none;
        text-align: center;
        transition: all 0.3s ease;
        cursor: pointer;
      }

      .file-upload-btn.primary {
        background: #007bff;
        color: white;
      }

      .file-upload-btn.primary:hover {
        background: #0056b3;
        color: white;
      }

      .file-upload-btn.info {
        background: #17a2b8;
        color: white;
      }

      .file-upload-btn.info:hover {
        background: #138496;
        color: white;
      }
    </style>
  </head>
  <body>
    <nav class="navbar navbar-expand-lg navbar-dark bg-dark">
      <div class="container">
        <a class="navbar-brand" href="/">
          <i class="bi bi-shield-check"></i> 통신판매사업자관리 시스템
        </a>
        <div class="navbar-nav ms-auto">
          <c:choose>
            <c:when test="${not empty pageContext.request.userPrincipal}">
              <a class="nav-link" href="/members/profile">
                <i class="bi bi-person-circle"></i> 내 프로필
              </a>
              <a class="nav-link" href="/members/logout">
                <i class="bi bi-box-arrow-right"></i> 로그아웃
              </a>
            </c:when>
            <c:otherwise>
              <a class="nav-link" href="/members/login">
                <i class="bi bi-box-arrow-in-right"></i> 로그인
              </a>
            </c:otherwise>
          </c:choose>
        </div>
      </div>
    </nav>

    <div class="main-content">
      <div class="container mt-4">
        <div
          class="d-flex justify-content-between align-items-center mb-4"
        ></div>

        <div class="row mb-4">
          <div class="col-xl-3 col-md-6 col-sm-6">
            <div class="stats-card">
              <div class="d-flex justify-content-between align-items-center">
                <div>
                  <div class="stats-label">총 등록 업체</div>
                  <div class="stats-value">${stats.total}</div>
                </div>
                <div>
                  <i
                    class="bi bi-building"
                    style="font-size: 2.5rem; opacity: 0.8"
                  ></i>
                </div>
              </div>
            </div>
          </div>
          <div class="col-xl-3 col-md-6 col-sm-6">
            <div class="stats-card">
              <div class="d-flex justify-content-between align-items-center">
                <div>
                  <div class="stats-label">유효 법인등록번호</div>
                  <div class="stats-value">${stats.validCorpRegNo}</div>
                </div>
                <div>
                  <i
                    class="bi bi-check-circle-fill"
                    style="font-size: 2.5rem; opacity: 0.8"
                  ></i>
                </div>
              </div>
            </div>
          </div>
          <div class="col-xl-3 col-md-6 col-sm-6">
            <div class="stats-card">
              <div class="d-flex justify-content-between align-items-center">
                <div>
                  <div class="stats-label">유효 행정구역코드</div>
                  <div class="stats-value">${stats.validRegionCd}</div>
                </div>
                <div>
                  <i
                    class="bi bi-geo-alt-fill"
                    style="font-size: 2.5rem; opacity: 0.8"
                  ></i>
                </div>
              </div>
            </div>
          </div>
          <div class="col-xl-3 col-md-6 col-sm-6">
            <div class="stats-card">
              <div class="d-flex justify-content-between align-items-center">
                <div>
                  <div class="stats-label">데이터 수집 성공률</div>
                  <div class="stats-value">${stats.successRate}%</div>
                </div>
                <div>
                  <i
                    class="bi bi-graph-up-arrow"
                    style="font-size: 2.5rem; opacity: 0.8"
                  ></i>
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
                      <label for="citySelect" class="form-label"
                        >시/도 선택</label
                      >
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
                      <label for="districtSelect" class="form-label"
                        >구/군 선택</label
                      >
                      <select class="form-select" id="districtSelect">
                        <option>구/군 선택</option>
                      </select>
                    </div>
                  </div>
                </div>
                <sec:authorize access="hasAnyRole('MANAGER', 'ADMIN')">
                  <div class="text-center">
                    <button
                      type="button"
                      class="btn btn-primary btn-lg"
                      id="collectDataBtn"
                    >
                      <i class="bi bi-download"></i> 데이터 수집 시작
                    </button>
                  </div>
                </sec:authorize>
                <div class="mt-3">
                  <div class="alert alert-info">
                    <i class="bi bi-info-circle"></i>
                    <strong>데이터 수집 안내:</strong>
                    <ul class="mb-0 mt-2">
                      <li>
                        시/도와 구/군을 선택한 후 "데이터 수집 시작" 버튼을
                        클릭하세요.
                      </li>
                      <li>
                        MinIO에 저장된 해당 지역의 CSV 파일을 읽어서
                        데이터베이스에 저장합니다.
                      </li>
                      <li>원본 파일명 형식: <code>시도명_구군명.csv</code></li>
                    </ul>
                  </div>
                </div>

                <div class="action-buttons">
                  <a href="/region/status" class="btn btn-primary">
                    <i class="bi bi-map"></i> 지역별 현황 보기
                  </a>
                  <sec:authorize access="hasAnyRole('MANAGER', 'ADMIN')">
                    <a href="/corp/search" class="action-btn">
                      <i class="bi bi-search"></i> 법인 정보 검색
                    </a>
                    <button
                      type="button"
                      class="action-btn"
                      onclick="openFileUploadModal()"
                    >
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
                    <c:when
                      test="${not empty recentActivities and recentActivities.size() > 0}"
                    >
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
                        <span class="activity-msg"
                          >시스템이 정상적으로 실행 중입니다</span
                        >
                        <span class="time">방금 전</span>
                      </li>
                      <li class="activity-item">
                        <span class="dot green"></span>
                        <span class="activity-msg"
                          >데이터베이스 연결 확인됨</span
                        >
                        <span class="time">1분 전</span>
                      </li>
                      <li class="activity-item">
                        <span class="dot yellow"></span>
                        <span class="activity-msg"
                          >새로운 데이터 수집 대기 중</span
                        >
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
                  <c:when
                    test="${not empty regionStats and regionStats.size() > 0}"
                  >
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
                                <strong
                                  >${regionStat.city}
                                  ${regionStat.district}</strong
                                >
                              </td>
                              <td>
                                <span class="badge bg-primary fs-6"
                                  ><fmt:formatNumber
                                    value="${regionStat.totalCount}"
                                    pattern="#,##0"
                                /></span>
                              </td>
                              <td>
                                <fmt:formatNumber
                                  value="${regionStat.validCorpRegNoCount}"
                                  pattern="#,##0"
                                /><span class="text-muted"
                                  >(<fmt:formatNumber
                                    value="${regionStat.validCorpRegNoCount * 100.0 / regionStat.totalCount}"
                                    pattern="0.0"
                                  />%)</span
                                >
                              </td>
                              <td>
                                <fmt:formatNumber
                                  value="${regionStat.validRegionCdCount}"
                                  pattern="#,##0"
                                /><span class="text-muted"
                                  >(<fmt:formatNumber
                                    value="${regionStat.validRegionCdCount * 100.0 / regionStat.totalCount}"
                                    pattern="0.0"
                                  />%)</span
                                >
                              </td>
                              <td>
                                <div class="progress" style="height: 12px">
                                  <div
                                    class="progress-bar bg-primary"
                                    role="progressbar"
                                    style="width: ${regionStat.completionRate}%"
                                    aria-valuenow="${regionStat.completionRate}"
                                    aria-valuemin="0"
                                    aria-valuemax="100"
                                  ></div>
                                </div>
                                <span class="text-primary fw-bold"
                                  >${regionStat.completionRate}%</span
                                >
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
                      <h5 class="text-muted mt-3">
                        등록된 지역 통계가 없습니다
                      </h5>
                      <p class="text-muted">
                        데이터 수집을 통해 지역별 통계를 확인할 수 있습니다.
                      </p>
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
      </div>
    </div>

    <div id="fileUploadModal" class="file-upload-modal">
      <div class="file-upload-content">
        <div class="file-upload-header">
          <h3><i class="bi bi-cloud-upload"></i> 파일 업로드</h3>
          <button
            type="button"
            class="file-upload-close"
            onclick="closeFileUploadModal()"
          >
            &times;
          </button>
        </div>
        <div class="file-upload-body">
          <form
            class="file-upload-form"
            action="${pageContext.request.contextPath}/web/files/upload"
            method="post"
            enctype="multipart/form-data"
          >
            <div class="form-group">
              <label for="file">파일 선택:</label>
              <input type="file" id="file" name="file" required />
            </div>
            <div class="form-group">
              <label for="description">파일 설명:</label>
              <input
                type="text"
                id="description"
                name="description"
                placeholder="파일에 대한 설명을 입력하세요"
              />
            </div>
            <div class="form-group">
              <input type="submit" value="업로드" />
            </div>
          </form>

          <div class="file-upload-buttons">
            <a
              href="/web/files"
              class="file-upload-btn primary"
              onclick="closeFileUploadModal()"
            >
              <i class="bi bi-list"></i> 파일 목록 보기
            </a>
            <a
              href="/api/v1/files/template"
              class="file-upload-btn info"
              download
            >
              <i class="bi bi-download"></i> CSV 양식 다운로드
            </a>
          </div>
        </div>
      </div>
    </div>

    <footer class="footer">
      <div class="container">
        <div class="row">
          <div class="col-md-6">
            <div class="footer-logo">
              <div class="main-title">public-data-harvester</div>
              <div class="sub-title">
                CHUNGJANG STREET FESTIVAL OF RECOLLECTION
              </div>
            </div>

            <div class="footer-contact">
              <div class="contact-title">통신판매사업자 정보 관리시스템</div>
              <div class="contact-address">대한민국 광주광역시 서구</div>
              <div class="contact-phone">TEL: 010-xxx-ㄱㄴㄷㄹ</div>
            </div>
          </div>

          <div class="col-md-6">
            <div class="footer-contact">
              <div class="contact-title">궁금하면 500원</div>
              <div class="contact-address">대한민국 광주광역시 서구</div>
              <div class="contact-phone">TEL: 010-ㄴㄷㄹ</div>
              <div class="contact-email">E-MAIL: 2025chungjang@gmail.com</div>
            </div>
          </div>
        </div>

        <div class="footer-copyright">
          ⓒ public-data-harvester. ALL RIGHT RESERVED.
        </div>
      </div>
    </footer>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/js/bootstrap.bundle.min.js"></script>
    <script>
      document.addEventListener("DOMContentLoaded", function () {
        const citySelect = document.getElementById("citySelect");
        const districtSelect = document.getElementById("districtSelect");
        const collectDataBtn = document.getElementById("collectDataBtn");

        if (!citySelect || !districtSelect) {
          return;
        }

        citySelect.addEventListener("change", function () {
          const selectedCity = this.value;
          districtSelect.innerHTML = "<option>구/군 선택</option>";

          if (selectedCity === "seoul") {
            const districts = [
              "강남구",
              "강동구",
              "강북구",
              "강서구",
              "관악구",
              "광진구",
              "구로구",
              "금천구",
              "노원구",
              "도봉구",
              "동대문구",
              "동작구",
              "마포구",
              "서대문구",
              "서초구",
              "성동구",
              "성북구",
              "송파구",
              "양천구",
              "영등포구",
              "용산구",
              "은평구",
              "종로구",
              "중구",
              "중랑구",
            ];
            districts.forEach((district) => {
              const option = document.createElement("option");
              option.value = district;
              option.textContent = district;
              districtSelect.appendChild(option);
            });
          } else if (selectedCity === "busan") {
            const districts = [
              "강서구",
              "금정구",
              "남구",
              "동구",
              "동래구",
              "부산진구",
              "북구",
              "사상구",
              "사하구",
              "서구",
              "수영구",
              "연제구",
              "영도구",
              "중구",
              "해운대구",
              "기장군",
            ];
            districts.forEach((district) => {
              const option = document.createElement("option");
              option.value = district;
              option.textContent = district;
              districtSelect.appendChild(option);
            });
          } else if (selectedCity === "daegu") {
            const districts = [
              "남구",
              "달서구",
              "달성군",
              "동구",
              "북구",
              "서구",
              "수성구",
              "중구",
            ];
            districts.forEach((district) => {
              const option = document.createElement("option");
              option.value = district;
              option.textContent = district;
              districtSelect.appendChild(option);
            });
          } else if (selectedCity === "incheon") {
            const districts = [
              "계양구",
              "남구",
              "남동구",
              "동구",
              "부평구",
              "서구",
              "연수구",
              "중구",
              "강화군",
              "옹진군",
            ];
            districts.forEach((district) => {
              const option = document.createElement("option");
              option.value = district;
              option.textContent = district;
              districtSelect.appendChild(option);
            });
          } else if (selectedCity === "gwangju") {
            const districts = ["광산구", "남구", "동구", "북구", "서구"];
            districts.forEach((district) => {
              const option = document.createElement("option");
              option.value = district;
              option.textContent = district;
              districtSelect.appendChild(option);
            });
          } else if (selectedCity === "daejeon") {
            const districts = ["대덕구", "동구", "서구", "유성구", "중구"];
            districts.forEach((district) => {
              const option = document.createElement("option");
              option.value = district;
              option.textContent = district;
              districtSelect.appendChild(option);
            });
          } else if (selectedCity === "ulsan") {
            const districts = ["남구", "동구", "북구", "울주군", "중구"];
            districts.forEach((district) => {
              const option = document.createElement("option");
              option.value = district;
              option.textContent = district;
              districtSelect.appendChild(option);
            });
          } else if (selectedCity === "gyeonggi") {
            const districts = [
              "가평군",
              "고양시",
              "과천시",
              "광명시",
              "광주시",
              "구리시",
              "군포시",
              "김포시",
              "남양주시",
              "동두천시",
              "부천시",
              "성남시",
              "수원시",
              "시흥시",
              "안산시",
              "안성시",
              "안양시",
              "양주시",
              "양평군",
              "여주시",
              "연천군",
              "오산시",
              "용인시",
              "의왕시",
              "의정부시",
              "이천시",
              "파주시",
              "평택시",
              "포천시",
              "하남시",
              "화성시",
            ];
            districts.forEach((district) => {
              const option = document.createElement("option");
              option.value = district;
              option.textContent = district;
              districtSelect.appendChild(option);
            });
          } else if (selectedCity === "gangwon") {
            const districts = [
              "강릉시",
              "고성군",
              "동해시",
              "삼척시",
              "속초시",
              "양구군",
              "양양군",
              "영월군",
              "원주시",
              "인제군",
              "정선군",
              "철원군",
              "춘천시",
              "태백시",
              "평창군",
              "홍천군",
              "화천군",
              "횡성군",
            ];
            districts.forEach((district) => {
              const option = document.createElement("option");
              option.value = district;
              option.textContent = district;
              districtSelect.appendChild(option);
            });
          } else if (selectedCity === "chungbuk") {
            const districts = [
              "괴산군",
              "단양군",
              "보은군",
              "영동군",
              "옥천군",
              "음성군",
              "제천시",
              "증평군",
              "진천군",
              "청주시",
              "충주시",
            ];
            districts.forEach((district) => {
              const option = document.createElement("option");
              option.value = district;
              option.textContent = district;
              districtSelect.appendChild(option);
            });
          } else if (selectedCity === "chungnam") {
            const districts = [
              "공주시",
              "금산군",
              "논산시",
              "당진시",
              "보령시",
              "부여군",
              "서산시",
              "서천군",
              "아산시",
              "예산군",
              "천안시",
              "청양군",
              "태안군",
              "홍성군",
            ];
            districts.forEach((district) => {
              const option = document.createElement("option");
              option.value = district;
              option.textContent = district;
              districtSelect.appendChild(option);
            });
          } else if (selectedCity === "jeonbuk") {
            const districts = [
              "고창군",
              "군산시",
              "김제시",
              "남원시",
              "무주군",
              "부안군",
              "순창군",
              "완주군",
              "익산시",
              "임실군",
              "장수군",
              "전주시",
              "정읍시",
              "진안군",
            ];
            districts.forEach((district) => {
              const option = document.createElement("option");
              option.value = district;
              option.textContent = district;
              districtSelect.appendChild(option);
            });
          } else if (selectedCity === "jeonnam") {
            const districts = [
              "강진군",
              "고흥군",
              "곡성군",
              "광양시",
              "구례군",
              "나주시",
              "담양군",
              "목포시",
              "무안군",
              "보성군",
              "순천시",
              "신안군",
              "여수시",
              "영광군",
              "영암군",
              "완도군",
              "장성군",
              "장흥군",
              "진도군",
              "함평군",
              "해남군",
              "화순군",
            ];
            districts.forEach((district) => {
              const option = document.createElement("option");
              option.value = district;
              option.textContent = district;
              districtSelect.appendChild(option);
            });
          } else if (selectedCity === "gyeongbuk") {
            const districts = [
              "경산시",
              "경주시",
              "고령군",
              "구미시",
              "군위군",
              "김천시",
              "문경시",
              "봉화군",
              "상주시",
              "성주군",
              "안동시",
              "영덕군",
              "영양군",
              "영주시",
              "영천시",
              "예천군",
              "울릉군",
              "울진군",
              "의성군",
              "청도군",
              "청송군",
              "칠곡군",
              "포항시",
            ];
            districts.forEach((district) => {
              const option = document.createElement("option");
              option.value = district;
              option.textContent = district;
              districtSelect.appendChild(option);
            });
          } else if (selectedCity === "gyeongnam") {
            const districts = [
              "거제시",
              "거창군",
              "고성군",
              "김해시",
              "남해군",
              "밀양시",
              "사천시",
              "산청군",
              "양산시",
              "의령군",
              "진주시",
              "창녕군",
              "창원시",
              "통영시",
              "하동군",
              "함안군",
              "함양군",
              "합천군",
            ];
            districts.forEach((district) => {
              const option = document.createElement("option");
              option.value = district;
              option.textContent = district;
              districtSelect.appendChild(option);
            });
          } else if (selectedCity === "jeju") {
            const districts = ["서귀포시", "제주시"];
            districts.forEach((district) => {
              const option = document.createElement("option");
              option.value = district;
              option.textContent = district;
              districtSelect.appendChild(option);
            });
          }
        });

        if (collectDataBtn) {
          collectDataBtn.addEventListener("click", function () {
            const selectedCity = citySelect.value;
            const selectedDistrict = districtSelect.value;

            if (selectedCity === "city" || selectedDistrict === "구/군 선택") {
              alert("시/도와 구/군을 모두 선택해주세요.");
              return;
            }

            const cityName = getCityDisplayName(selectedCity);
            const districtName = selectedDistrict;

            const btn = this;
            const originalText = btn.textContent;
            btn.textContent = "데이터 수집 중...";
            btn.disabled = true;

            const csrfToken = document
              .querySelector('meta[name="_csrf"]')
              .getAttribute("content");
            const csrfHeader = document
              .querySelector('meta[name="_csrf_header"]')
              .getAttribute("content");

            const headers = {
              "Content-Type": "application/json",
            };

            if (csrfToken && csrfHeader) {
              headers[csrfHeader] = csrfToken;
            }

            fetch("/coseller/save-simple", {
              method: "POST",
              headers: headers,
              body: JSON.stringify({
                city: cityName,
                district: districtName,
              }),
            })
              .then((response) => response.json())
              .then((data) => {
                if (data.success) {
                  const savedCount = data.data || 0;
                  alert(
                    `데이터 수집이 완료되었습니다.\n저장된 데이터: ${savedCount}건`
                  );
                  // 통계 업데이트
                  updateStats();
                } else {
                  alert(`데이터 수집에 실패했습니다.\n오류: ${data.message}`);
                }
              })
              .catch((error) => {
                alert("데이터 수집 중 오류가 발생했습니다.");
              })
              .finally(() => {
                btn.textContent = originalText;
                btn.disabled = false;
              });
          });
        }

        function getCityDisplayName(cityValue) {
          const cityMap = {
            seoul: "서울특별시",
            busan: "부산광역시",
            daegu: "대구광역시",
            incheon: "인천광역시",
            gwangju: "광주광역시",
            daejeon: "대전광역시",
            ulsan: "울산광역시",
            gyeonggi: "경기도",
            gangwon: "강원특별자치도",
            chungbuk: "충청북도",
            chungnam: "충청남도",
            jeonbuk: "전라북도",
            jeonnam: "전라남도",
            gyeongbuk: "경상북도",
            gyeongnam: "경상남도",
            jeju: "제주특별자치도",
          };
          return cityMap[cityValue] || cityValue;
        }

        // 전역 함수로 정의
        window.openFileUploadModal = function () {
          const modal = document.getElementById("fileUploadModal");
          if (modal) {
            modal.classList.add("show");
            document.body.style.overflow = "hidden";
          }
        };

        window.closeFileUploadModal = function () {
          const modal = document.getElementById("fileUploadModal");
          if (modal) {
            modal.classList.remove("show");
            document.body.style.overflow = "";
          }
        };

        // 통계 업데이트 함수
        function updateStats() {
          fetch("/api/stats")
            .then((response) => response.json())
            .then((data) => {
              if (data.success) {
                const stats = data.data;
                // 통계 카드 업데이트
                const totalElement = document.querySelector(
                  ".stats-card:nth-child(1) .stats-value"
                );
                const validCorpRegNoElement = document.querySelector(
                  ".stats-card:nth-child(2) .stats-value"
                );
                const validRegionCdElement = document.querySelector(
                  ".stats-card:nth-child(3) .stats-value"
                );
                const successRateElement = document.querySelector(
                  ".stats-card:nth-child(4) .stats-value"
                );

                if (totalElement) totalElement.textContent = stats.total || 0;
                if (validCorpRegNoElement)
                  validCorpRegNoElement.textContent = stats.validCorpRegNo || 0;
                if (validRegionCdElement)
                  validRegionCdElement.textContent = stats.validRegionCd || 0;
                if (successRateElement)
                  successRateElement.textContent =
                    (stats.successRate || 0).toFixed(1) + "%";
              }
            })
            .catch((error) => {
              console.error("통계 업데이트 실패:", error);
            });
        }

        const fileUploadModal = document.getElementById("fileUploadModal");
        if (fileUploadModal) {
          fileUploadModal.addEventListener("click", function (e) {
            if (e.target === this) {
              closeFileUploadModal();
            }
          });
        }

        document.addEventListener("keydown", function (e) {
          if (e.key === "Escape") {
            closeFileUploadModal();
          }
        });
      });
    </script>
  </body>
</html>
