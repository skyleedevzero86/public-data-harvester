<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="_csrf" content="${_csrf.token}"/>
    <meta name="_csrf_header" content="${_csrf.headerName}"/>
    <title>비밀번호 재설정 오류 - 통신판매사업자관리 시스템</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.7.2/font/bootstrap-icons.css" rel="stylesheet">

    <style>
        .error-container {
            background: white;
            border-radius: 15px;
            box-shadow: 0 15px 35px rgba(0, 0, 0, 0.1);
            padding: 3rem;
            text-align: center;
            max-width: 600px;
            margin: 2rem auto;
        }

        .error-icon {
            font-size: 4rem;
            color: #e74c3c;
            margin-bottom: 1rem;
        }

        .error-title {
            color: #2c3e50;
            font-size: 1.8rem;
            font-weight: 600;
            margin-bottom: 1rem;
        }

        .error-message {
            color: #7f8c8d;
            font-size: 1.1rem;
            margin-bottom: 2rem;
            line-height: 1.6;
        }

        .btn-primary {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            border: none;
            padding: 12px 30px;
            border-radius: 25px;
            font-weight: 500;
            transition: all 0.3s ease;
        }

        .btn-primary:hover {
            transform: translateY(-2px);
            box-shadow: 0 5px 15px rgba(102, 126, 234, 0.4);
        }

        .btn-outline-secondary {
            border-radius: 25px;
            padding: 12px 30px;
            font-weight: 500;
            transition: all 0.3s ease;
        }

        .btn-outline-secondary:hover {
            transform: translateY(-2px);
        }

        .footer {
            background-color: #343a40;
            color: white;
            padding: 40px 0 20px 0;
            margin-top: 60px;
        }

        .footer-logo {
            margin-bottom: 30px;
        }

        .footer-logo .festival-number {
            font-size: 0.9rem;
            color: #adb5bd;
            margin-bottom: 5px;
            position: relative;
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
            text-align: left;
            font-size: 0.8rem;
            color: #adb5bd;
        }

        .footer-container {
            max-width: 1200px;
            margin: 0 auto;
            padding: 0 20px;
        }

        .main-title {
            font-size: 1.5rem;
            font-weight: bold;
            color: #f8f9fa;
            margin-bottom: 5px;
        }

        .sub-title {
            font-size: 0.9rem;
            color: #adb5bd;
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
            <a class="nav-link" href="/members/login">
                <i class="bi bi-box-arrow-in-right"></i> 로그인
            </a>
            <a class="nav-link" href="/members/join">
                <i class="bi bi-person-plus"></i> 회원가입
            </a>
        </div>
    </div>
</nav>

<div class="container-fluid mt-4">
    <div class="d-flex justify-content-between align-items-center mb-4">
        <h2><i class="bi bi-exclamation-triangle"></i> 비밀번호 재설정 오류</h2>
    </div>

    <div class="row justify-content-center">
        <div class="col-md-8 col-lg-6">
            <div class="error-container">
                <div class="error-icon">
                    <i class="bi bi-exclamation-triangle-fill"></i>
                </div>
                <h1 class="error-title">비밀번호 재설정 오류</h1>
                <div class="error-message">
                    <c:choose>
                        <c:when test="${not empty error}">
                            ${error}
                        </c:when>
                        <c:when test="${not empty errorMessage}">
                            ${errorMessage}
                        </c:when>
                        <c:otherwise>
                            유효하지 않거나 만료된 링크입니다.<br>
                            비밀번호 재설정 링크는 30분 후에 만료되며,<br>
                            한 번만 사용할 수 있습니다.
                        </c:otherwise>
                    </c:choose>
                </div>
                <div class="d-grid gap-2">
                    <a href="/members/password/find" class="btn btn-primary">
                        <i class="bi bi-arrow-clockwise me-2"></i>다시 요청하기
                    </a>
                    <a href="/members/login" class="btn btn-outline-secondary">
                        <i class="bi bi-box-arrow-in-right me-2"></i>로그인 페이지로
                    </a>
                </div>
            </div>
        </div>
    </div>
</div>

<footer class="footer">
    <div class="footer-container">
        <div class="row">
            <div class="col-md-6">
                <div class="footer-logo">
                    <div class="festival-number"></div>
                    <div class="main-title">public-data-harvester</div>
                    <div class="sub-title">CHUNGJANG STREET FESTIVAL OF RECOLLECTION</div>
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
                    <div class="contact-phone">TEL: 010-xxx-ㄱㄴㄷㄹ</div>
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
</body>
</html>