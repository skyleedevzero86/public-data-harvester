<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html>
<head>
    <title>지역별 현황</title>
    <style>
        table { border-collapse: collapse; width: 100%; }
        th, td { border: 1px solid #ccc; padding: 8px; text-align: center; }
        th { background: #f0f4fa; }
    </style>
    <script>
        function onCityChange() {
            var city = document.getElementById('city').value;
            window.location.href = '/region/status?city=' + encodeURIComponent(city);
        }
        function onDistrictChange() {
            var city = document.getElementById('city').value;
            var district = document.getElementById('district').value;
            window.location.href = '/region/status?city=' + encodeURIComponent(city) + '&district=' + encodeURIComponent(district);
        }
    </script>
</head>
<body>
<h2>지역별 현황</h2>
<form>
    <label>시/도:
        <select id="city" name="city" onchange="onCityChange()">
            <option value="">선택</option>
            <c:forEach var="city" items="${cities}">
                <option value="${city.value}" <c:if test="${city.value == selectedCity}">selected</c:if>>${city.value}</option>
            </c:forEach>
        </select>
    </label>
    <label>구/군:
        <select id="district" name="district" onchange="onDistrictChange()">
            <option value="">선택</option>
            <c:forEach var="district" items="${districts}">
                <c:if test="${selectedCity == null || district.value.startsWith(selectedCity.substring(0,2))}">
                    <option value="${district.value}" <c:if test="${district.value == selectedDistrict}">selected</c:if>>${district.value}</option>
                </c:if>
            </c:forEach>
        </select>
    </label>
</form>

<c:if test="${not empty corpList}">
    <table>
        <thead>
        <tr>
            <th>ID</th>
            <th>법인명</th>
            <th>사업자번호</th>
            <th>법인등록번호</th>
            <th>시/도</th>
            <th>구/군</th>
            <th>판매자ID</th>
            <th>설명</th>
        </tr>
        </thead>
        <tbody>
        <c:forEach var="corp" items="${corpList}">
            <tr>
                <td>${corp.id}</td>
                <td>${corp.bizNm}</td>
                <td>${corp.bizNo}</td>
                <td>${corp.corpRegNo}</td>
                <td>${corp.siNm}</td>
                <td>${corp.sggNm}</td>
                <td>${corp.sellerId}</td>
                <td><c:out value="${empty corp.description ? '자동수집' : corp.description}"/></td>
            </tr>
        </c:forEach>
        </tbody>
    </table>
</c:if>
<c:if test="${empty corpList}">
    <p>해당 지역에 등록된 데이터가 없습니다.</p>
</c:if>
</body>
</html>