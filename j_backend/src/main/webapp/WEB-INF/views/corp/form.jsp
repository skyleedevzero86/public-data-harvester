<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="pageTitle" value="법인 등록/수정" />
<c:set var="pageCSS" value="${['corp.css']}" />
<c:set var="pageJS" value="${['corp.js']}" />
<c:set var="includeCKEditor" value="true" />

<%@ include file="../common/header.jsp" %>
<%@ include file="../common/navigation.jsp" %>

<div class="container-fluid mt-4">
    <div class="d-flex justify-content-between align-items-center mb-4">
        <h2><i class="bi bi-building"></i> 법인 등록/수정</h2>
        <div>
            <a href="/corp/list" class="btn btn-outline-secondary">
                <i class="bi bi-arrow-left"></i> 목록으로 돌아가기
            </a>
        </div>
    </div>

    <div class="form-container">
        <div class="form-header">
            <h3 class="form-title">
                <i class="bi bi-pencil-square"></i>
                <c:choose>
                    <c:when test="${empty form.id}">새 법인 등록</c:when>
                    <c:otherwise>법인 정보 수정</c:otherwise>
                </c:choose>
            </h3>
        </div>

        <form method="post" id="corpForm">
            <input type="hidden" name="id" value="${form.id}" />

            <div class="form-section">
                <h5 class="section-title">
                    <i class="bi bi-info-circle"></i> 기본 정보
                </h5>
                <div class="row">
                    <div class="col-md-6">
                        <div class="form-group">
                            <label for="bizNm" class="form-label required-field"
                            >법인명</label
                            >
                            <input
                                    type="text"
                                    id="bizNm"
                                    name="bizNm"
                                    value="${form.bizNm}"
                                    class="form-control"
                                    required
                            />
                            <div class="field-help">법인의 공식 명칭을 입력하세요</div>
                        </div>
                    </div>
                    <div class="col-md-6">
                        <div class="form-group">
                            <label for="bizNo" class="form-label required-field"
                            >사업자번호</label
                            >
                            <input
                                    type="text"
                                    id="bizNo"
                                    name="bizNo"
                                    value="${form.bizNo}"
                                    class="form-control"
                                    required
                                    placeholder="000-00-00000"
                            />
                            <div class="field-help">하이픈(-)을 포함하여 입력하세요</div>
                        </div>
                    </div>
                </div>
                <div class="row">
                    <div class="col-md-6">
                        <div class="form-group">
                            <label for="corpRegNo" class="form-label required-field"
                            >법인등록번호</label
                            >
                            <input
                                    type="text"
                                    id="corpRegNo"
                                    name="corpRegNo"
                                    value="${form.corpRegNo}"
                                    class="form-control"
                                    required
                            />
                            <div class="field-help">
                                법인등기부등본의 등록번호를 입력하세요
                            </div>
                        </div>
                    </div>
                    <div class="col-md-6">
                        <div class="form-group">
                            <label for="regionCd" class="form-label required-field"
                            >지역코드</label
                            >
                            <input
                                    type="text"
                                    id="regionCd"
                                    name="regionCd"
                                    value="${form.regionCd}"
                                    class="form-control"
                                    required
                            />
                            <div class="field-help">행정구역 코드를 입력하세요</div>
                        </div>
                    </div>
                </div>
            </div>

            <div class="form-section">
                <h5 class="section-title">
                    <i class="bi bi-geo-alt"></i> 주소 정보
                </h5>
                <div class="row">
                    <div class="col-md-6">
                        <div class="form-group">
                            <label for="siNm" class="form-label required-field"
                            >시/도</label
                            >
                            <input
                                    type="text"
                                    id="siNm"
                                    name="siNm"
                                    value="${form.siNm}"
                                    class="form-control"
                                    required
                            />
                            <div class="field-help">
                                예: 서울특별시, 부산광역시, 전라남도 등
                            </div>
                        </div>
                    </div>
                    <div class="col-md-6">
                        <div class="form-group">
                            <label for="sggNm" class="form-label required-field"
                            >구/군</label
                            >
                            <input
                                    type="text"
                                    id="sggNm"
                                    name="sggNm"
                                    value="${form.sggNm}"
                                    class="form-control"
                                    required
                            />
                            <div class="field-help">예: 강남구, 해운대구, 나주시 등</div>
                        </div>
                    </div>
                </div>
            </div>

            <div class="form-section">
                <h5 class="section-title">
                    <i class="bi bi-person-badge"></i> 판매자 정보
                </h5>
                <div class="row">
                    <div class="col-md-6">
                        <div class="form-group">
                            <label for="sellerId" class="form-label required-field"
                            >판매자ID</label
                            >
                            <input
                                    type="text"
                                    id="sellerId"
                                    name="sellerId"
                                    value="${form.sellerId}"
                                    class="form-control"
                                    required
                            />
                            <div class="field-help">
                                통신판매업 신고 시 발급받은 판매자 ID
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <div class="form-section">
                <h5 class="section-title">
                    <i class="bi bi-file-text"></i> 상세 정보
                </h5>
                <div class="form-group">
                    <label for="description" class="form-label">설명</label>
                    <div class="ckeditor-container">
                <textarea
                        name="description"
                        id="description"
                        class="form-control"
                        rows="6"
                >
                    ${form.description}</textarea
                >
                    </div>
                    <div class="field-help">
                        법인에 대한 추가 설명이나 특이사항을 입력하세요
                    </div>
                </div>
            </div>

            <div class="btn-container">
                <button type="submit" class="btn btn-success btn-save">
                    <i class="bi bi-check-circle"></i>
                    <c:choose>
                        <c:when test="${empty form.id}">등록</c:when>
                        <c:otherwise>수정</c:otherwise>
                    </c:choose>
                </button>
                <a href="/corp/list" class="btn btn-secondary btn-list">
                    <i class="bi bi-list"></i> 목록
                </a>
                <button
                        type="button"
                        class="btn btn-danger btn-cancel"
                        onclick="history.back()"
                >
                    <i class="bi bi-x-circle"></i> 취소
                </button>
            </div>
        </form>
    </div>
</div>

<%@ include file="../common/footer.jsp" %>
</body>
</html>
