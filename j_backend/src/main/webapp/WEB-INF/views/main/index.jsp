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

        <div class="region-stat-card" style="margin-top:30px; background:#ede7f6; border-radius:18px; padding:24px;">
            <div style="font-weight:bold; font-size:1.1em; margin-bottom:12px;">
                <span>📊 지역별 통계 현황</span>
            </div>
            <table class="table table-bordered" style="background:#fff;">
                <thead>
                <tr>
                    <th>지역명</th>
                    <th>총 업체 수</th>
                    <th>법인등록번호</th>
                    <th>행정구역코드</th>
                    <th>완성도</th>
                </tr>
                </thead>
                <tbody>
                <c:if test="${not empty topRegionStat and topRegionStat.totalCount > 0}">
                    <tr>
                        <td>${topRegionStat.city} ${topRegionStat.district}</td>
                        <td><fmt:formatNumber value="${topRegionStat.totalCount}" pattern="#,##0"/></td>
                        <td>
                            <fmt:formatNumber value="${topRegionStat.validCorpRegNoCount}" pattern="#,##0"/>
                            <span style="color:#888;">(<fmt:formatNumber value="${topRegionStat.validCorpRegNoCount * 100.0 / topRegionStat.totalCount}" pattern="0.0"/>%)</span>
                        </td>
                        <td>
                            <fmt:formatNumber value="${topRegionStat.validRegionCdCount}" pattern="#,##0"/>
                            <span style="color:#888;">(<fmt:formatNumber value="${topRegionStat.validRegionCdCount * 100.0 / topRegionStat.totalCount}" pattern="0.0"/>%)</span>
                        </td>
                        <td>
                            <div style="width:120px; background:#eee; border-radius:8px; overflow:hidden;">
                                <div style="width:${topRegionStat.completionRate}%; background:#7e57c2; height:12px;"></div>
                            </div>
                            <span style="font-size:0.95em; color:#7e57c2; font-weight:bold;">${topRegionStat.completionRate}%</span>
                        </td>
                    </tr>
                </c:if>
                </tbody>
            </table>
            <div style="text-align:center; margin-top:16px;">
                <button class="btn btn-primary" onclick="location.href='/region/detail'">전체 통계 보기</button>
            </div>

</body>
</html>