<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <title>법인 목록</title>
    <link rel="stylesheet" href="/webjars/bootstrap/5.1.3/css/bootstrap.min.css"/>
</head>
<body>
<div class="container mt-4">
    <h2>법인 목록</h2>
    <form method="get" action="/corp/list" class="row g-2 mb-3">
        <input type="text" name="bizNm" value="${form.bizNm}" placeholder="법인명" class="form-control col"/>
        <input type="text" name="bizNo" value="${form.bizNo}" placeholder="사업자번호" class="form-control col"/>
        <input type="text" name="corpRegNo" value="${form.corpRegNo}" placeholder="법인등록번호" class="form-control col"/>
        <input type="text" name="siNm" value="${form.siNm}" placeholder="시/도" class="form-control col"/>
        <input type="text" name="sggNm" value="${form.sggNm}" placeholder="구/군" class="form-control col"/>
        <button type="submit" class="btn btn-primary col">검색</button>
        <a href="/corp/create" class="btn btn-success col">신규등록</a>
    </form>

    <c:choose>
        <c:when test="${not empty corpList.content}">
            <table class="table table-bordered">
                <thead>
                <tr>
                    <th>법인명</th>
                    <th>사업자번호</th>
                    <th>법인등록번호</th>
                    <th>시/도</th>
                    <th>구/군</th>
                    <th>등록자</th>
                    <th>관리</th>
                </tr>
                </thead>
                <tbody>
                <c:forEach var="corp" items="${corpList.content}">
                    <tr>
                        <td><a href="/corp/modify/${corp.id}">${corp.bizNm}</a></td>
                        <td>${corp.bizNo}</td>
                        <td>${corp.corpRegNo}</td>
                        <td>${corp.siNm}</td>
                        <td>${corp.sggNm}</td>
                        <td>
                            <c:choose>
                                <c:when test="${isAdmin}">${corp.username}</c:when>
                                <c:otherwise>본인</c:otherwise>
                            </c:choose>
                        </td>
                        <td>
                            <a href="/corp/edit/${corp.id}" class="btn btn-sm btn-warning">수정</a>
                            <form action="/corp/delete/${corp.id}" method="post" style="display:inline;">
                                <button type="submit" class="btn btn-sm btn-danger"
                                        onclick="return confirm('삭제하시겠습니까?');">삭제</button>
                            </form>
                        </td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>

            <!-- 페이징 - 안전성 개선 -->
            <c:if test="${corpList.totalPages > 0}">
                <nav>
                    <ul class="pagination">
                        <c:forEach begin="0" end="${corpList.totalPages-1}" var="i">
                            <li class="page-item ${corpList.number == i ? 'active' : ''}">
                                <a class="page-link" href="?page=${i}&size=${corpList.size}">${i+1}</a>
                            </li>
                        </c:forEach>
                    </ul>
                </nav>
            </c:if>
        </c:when>
        <c:otherwise>
            <div class="alert alert-info">
                <p>등록된 법인 정보가 없습니다.</p>
                <a href="/corp/create" class="btn btn-primary">첫 번째 법인 등록하기</a>
            </div>
        </c:otherwise>
    </c:choose>
</div>
</body>
</html>