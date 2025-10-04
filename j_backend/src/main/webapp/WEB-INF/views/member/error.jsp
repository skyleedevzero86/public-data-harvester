<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>

<c:set var="pageTitle" value="접근 오류" />

<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${pageTitle} - Antock</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.10.0/font/bootstrap-icons.css" rel="stylesheet">
    <link href="/assets/css/common.css" rel="stylesheet">
    <link href="/assets/css/member.css" rel="stylesheet">
</head>
<body>
    <div class="container-fluid">
        <div class="row">

            <%@ include file="../common/navigation.jsp" %>

            <main class="col-md-9 ms-sm-auto col-lg-10 px-md-4">
                <div class="d-flex justify-content-between flex-wrap flex-md-nowrap align-items-center pt-3 pb-2 mb-3 border-bottom">
                    <h1 class="h2"><i class="bi bi-exclamation-triangle text-warning"></i> ${pageTitle}</h1>
                </div>

                <div class="row">
                    <div class="col-12">
                        <div class="card">
                            <div class="card-body text-center py-5">
                                <div class="mb-4">
                                    <i class="bi bi-shield-exclamation text-danger" style="font-size: 4rem;"></i>
                                </div>
                                
                                <h3 class="text-danger mb-3">접근 권한이 없습니다</h3>
                                
                                <c:choose>
                                    <c:when test="${not empty error}">
                                        <p class="text-muted mb-4">${error}</p>
                                    </c:when>
                                    <c:otherwise>
                                        <p class="text-muted mb-4">요청하신 페이지에 접근할 권한이 없습니다.</p>
                                    </c:otherwise>
                                </c:choose>

                                <c:if test="${errorCode == 'ACCESS_DENIED'}">
                                    <div class="alert alert-warning" role="alert">
                                        <i class="bi bi-info-circle"></i>
                                        <strong>관리자 또는 매니저 권한</strong>이 필요한 페이지입니다.
                                        <br>권한이 필요한 경우 관리자에게 문의하세요.
                                    </div>
                                </c:if>

                                <div class="mt-4">
                                    <a href="/" class="btn btn-primary me-2">
                                        <i class="bi bi-house"></i> 대시보드로 이동
                                    </a>
                                    <a href="javascript:history.back()" class="btn btn-outline-secondary">
                                        <i class="bi bi-arrow-left"></i> 이전 페이지
                                    </a>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </main>
        </div>
    </div>

    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
