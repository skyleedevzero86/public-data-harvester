<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="_csrf" content="${_csrf.token}"/>
    <meta name="_csrf_header" content="${_csrf.headerName}"/>
    <title>Antock System - 메인</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.7.2/font/bootstrap-icons.css" rel="stylesheet">

    <style>

        .card { display:inline-block; width:220px; margin:10px; padding:20px; border-radius:16px; background:#fff; box-shadow:0 2px 8px #0001; text-align:center;}
        .icon { font-size:32px; margin-bottom:8px;}
        .card1 { background:linear-gradient(135deg,#7f7fd5,#86a8e7,#91eac9);}
        .card2 { background:linear-gradient(135deg,#43e97b,#38f9d7);}
        .card3 { background:linear-gradient(135deg,#fa709a,#fee140);}
        .card4 { background:linear-gradient(135deg,#ff5858,#f09819);}
        .value { font-size:2em; font-weight:bold;}
        .desc { color:#555;}

        .activity-card { background: #f7f5ff; border-radius: 18px; padding: 20px; width: 320px; }
        .activity-title { font-weight: bold; margin-bottom: 12px; }
        .activity-list { list-style: none; padding: 0; margin: 0; }
        .activity-item { display: flex; align-items: center; margin-bottom: 12px; }
        .dot { width: 10px; height: 10px; border-radius: 50%; margin-right: 10px; }
        .dot.success { background: #4caf50; }
        .dot.fail { background: #f44336; }
        .dot.info { background: #2196f3; }
        .activity-msg { flex: 1; }
        .activity-time { color: #888; font-size: 0.9em; margin-left: 8px; }


    </style>
</head>
<body>
        <button onclick="location.href='/region/status'">지역별 현황 보기</button>
    <br/>
        <div>
            <div class="card card1">
                <div class="icon">🏢</div>
                <div class="value"><c:out value="${stats.total}"/></div>
                <div class="desc">총 등록 업체</div>
            </div>
            <div class="card card2">
                <div class="icon">✅</div>
                <div class="value"><c:out value="${stats.validCorpRegNo}"/></div>
                <div class="desc">유효 법인등록번호</div>
            </div>
            <div class="card card3">
                <div class="icon">📍</div>
                <div class="value"><c:out value="${stats.validRegionCd}"/></div>
                <div class="desc">유효 행정구역코드</div>
            </div>
            <div class="card card4">
                <div class="icon">📈</div>
                <div class="value"><c:out value="${stats.successRate}"/>%</div>
                <div class="desc">데이터 수집 성공률</div>
            </div>
        </div>

        <div class="activity-card">
            <div class="activity-title">📄 최근 활동</div>
            <ul class="activity-list">
                <c:forEach var="a" items="${recentActivities}">
                    <li class="activity-item">
                        <span class="dot ${a.type}"></span>
                        <span class="activity-msg">${a.message}</span>
                        <span class="activity-time">${a.timeAgo}</span>
                    </li>
                </c:forEach>
            </ul>
        </div>

</body>
</html>