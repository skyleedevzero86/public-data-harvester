<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <title>법인 등록/수정</title>
    <link rel="stylesheet" href="/webjars/bootstrap/5.1.3/css/bootstrap.min.css"/>
    <script src="/webjars/jquery/3.6.0/jquery.min.js"></script>
    <script src="/webjars/ckeditor/4.20.2/ckeditor.js"></script>
</head>
<body>
<div class="container mt-4">
    <h2>법인 등록/수정</h2>
    <form method="post">
        <input type="hidden" name="id" value="${form.id}"/>
        <div class="mb-2">
            <label>법인명</label>
            <input type="text" name="bizNm" value="${form.bizNm}" class="form-control"/>
        </div>
        <div class="mb-2">
            <label>사업자번호</label>
            <input type="text" name="bizNo" value="${form.bizNo}" class="form-control"/>
        </div>
        <div class="mb-2">
            <label>법인등록번호</label>
            <input type="text" name="corpRegNo" value="${form.corpRegNo}" class="form-control"/>
        </div>
        <div class="mb-2">
            <label>시/도</label>
            <input type="text" name="siNm" value="${form.siNm}" class="form-control"/>
        </div>
        <div class="mb-2">
            <label>구/군</label>
            <input type="text" name="sggNm" value="${form.sggNm}" class="form-control"/>
        </div>
        <div class="mb-2">
            <label>설명</label>
            <textarea name="description" id="description" class="form-control">${form.description}</textarea>
        </div>
        <button type="submit" class="btn btn-primary">저장</button>
        <a href="/corp/list" class="btn btn-secondary">목록</a>
    </form>
</div>
<script>
    CKEDITOR.replace('description');
</script>
</body>
</html>