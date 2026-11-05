<%@ page language="java" contentType="text/html; charset=UTF-8"
pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="pageTitle" value="파일 업로드" />

<!DOCTYPE html>
<html>
<head>
    <%@ include file="../common/head.jsp" %>
</head>
<body>
<%@ include file="../common/navigation.jsp" %>

<div class="container-fluid mt-4">
    <div class="d-flex justify-content-between align-items-center mb-4">
        <h2><i class="bi bi-cloud-upload"></i> 파일 업로드</h2>
        <div>
            <a href="${pageContext.request.contextPath}/web/files" class="btn btn-outline-primary">
                <i class="bi bi-files"></i> 파일 목록
            </a>
        </div>
    </div>

    <c:if test="${not empty message}">
        <div class="alert alert-success alert-dismissible fade show" role="alert">
            <i class="bi bi-check-circle"></i> ${message}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
    </c:if>

    <c:if test="${not empty error}">
        <div class="alert alert-danger alert-dismissible fade show" role="alert">
            <i class="bi bi-exclamation-triangle"></i> ${error}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
    </c:if>

    <div class="row">
        <div class="col-lg-8 mb-4">
            <div class="card">
                <div class="card-header">
                    <h5 class="mb-0"><i class="bi bi-upload"></i> 새 파일 등록</h5>
                </div>
                <div class="card-body">
                    <form action="${pageContext.request.contextPath}/web/files/upload"
                          method="post" enctype="multipart/form-data">
                        <c:if test="${not empty _csrf}">
                            <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}"/>
                        </c:if>

                        <div class="mb-3">
                            <label for="file" class="form-label fw-bold">
                                파일 선택 <span class="text-danger">*</span>
                            </label>
                            <input type="file" id="file" name="file" class="form-control" required>
                            <div id="fileInfo" class="form-text">업로드할 파일을 선택하세요.</div>
                        </div>

                        <div class="mb-4">
                            <label for="description" class="form-label fw-bold">파일 설명</label>
                            <input type="text" id="description" name="description" class="form-control"
                                   maxlength="500" placeholder="파일에 대한 설명을 입력하세요">
                        </div>

                        <div class="d-flex justify-content-end gap-2">
                            <a href="${pageContext.request.contextPath}/web/files" class="btn btn-secondary">
                                <i class="bi bi-x-lg"></i> 취소
                            </a>
                            <button type="submit" class="btn btn-success">
                                <i class="bi bi-upload"></i> 업로드
                            </button>
                        </div>
                    </form>
                </div>
            </div>
        </div>

        <div class="col-lg-4 mb-4">
            <div class="card">
                <div class="card-header">
                    <h5 class="mb-0"><i class="bi bi-info-circle"></i> 업로드 안내</h5>
                </div>
                <div class="card-body">
                    <ul class="mb-3 ps-3">
                        <li class="mb-2">업로드한 파일은 <strong>파일 목록</strong>에서 다운로드, 수정, 삭제할 수 있습니다.</li>
                        <li class="mb-2">CSV 양식이 필요하면 아래 버튼으로 내려받을 수 있습니다.</li>
                        <li>파일 설명을 입력하면 목록에서 검색할 때 함께 찾을 수 있습니다.</li>
                    </ul>
                    <a href="/api/v1/files/template" class="btn btn-info w-100 text-white" download>
                        <i class="bi bi-download"></i> CSV 양식 다운로드
                    </a>
                </div>
            </div>
        </div>
    </div>
</div>

<%@ include file="../common/footer.jsp" %>

<%@ include file="../common/scripts.jsp" %>

<script>
    document.getElementById('file').addEventListener('change', function () {
        var info = document.getElementById('fileInfo');
        if (this.files.length === 0) {
            info.textContent = '업로드할 파일을 선택하세요.';
            return;
        }
        var file = this.files[0];
        var size = file.size >= 1048576
            ? (file.size / 1048576).toFixed(2) + ' MB'
            : (file.size / 1024).toFixed(1) + ' KB';
        info.textContent = file.name + ' (' + size + ')';
    });
</script>
</body>
</html>
