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
        .hero-section {
            background: linear-gradient(135deg, #007bff, #0056b3);
            color: white;
            padding: 80px 0;
            text-align: center;
        }

        .hero-title {
            font-size: 3rem;
            font-weight: 700;
            margin-bottom: 20px;
        }

        .hero-subtitle {
            font-size: 1.2rem;
            opacity: 0.9;
            margin-bottom: 40px;
        }

        .feature-card {
            background: white;
            border-radius: 15px;
            padding: 30px;
            text-align: center;
            box-shadow: 0 4px 20px rgba(0,0,0,0.1);
            transition: transform 0.3s ease, box-shadow 0.3s ease;
            height: 100%;
            border: none;
        }

        .feature-card:hover {
            transform: translateY(-5px);
            box-shadow: 0 8px 30px rgba(0,0,0,0.15);
        }

        .feature-icon {
            font-size: 3rem;
            color: #007bff;
            margin-bottom: 20px;
        }

        .feature-title {
            font-size: 1.3rem;
            font-weight: 600;
            color: #212529;
            margin-bottom: 15px;
        }

        .feature-description {
            color: #6c757d;
            line-height: 1.6;
            margin-bottom: 20px;
        }

        .feature-link {
            display: inline-block;
            padding: 10px 25px;
            background: linear-gradient(45deg, #007bff, #0056b3);
            color: white;
            text-decoration: none;
            border-radius: 25px;
            font-weight: 500;
            transition: all 0.3s ease;
        }

        .feature-link:hover {
            color: white;
            transform: scale(1.05);
            box-shadow: 0 4px 15px rgba(0,123,255,0.3);
        }

        .stats-section {
            background: #f8f9fa;
            padding: 60px 0;
        }

        .stat-item {
            text-align: center;
            padding: 20px;
        }

        .stat-number {
            font-size: 2.5rem;
            font-weight: 700;
            color: #007bff;
        }

        .stat-label {
            color: #6c757d;
            font-weight: 500;
        }

        .quick-actions {
            background: white;
            padding: 40px 0;
        }

        .quick-action-btn {
            display: block;
            width: 100%;
            padding: 20px;
            background: linear-gradient(45deg, #28a745, #20c997);
            color: white;
            text-decoration: none;
            border-radius: 10px;
            margin-bottom: 15px;
            transition: all 0.3s ease;
            text-align: center;
        }

        .quick-action-btn:hover {
            color: white;
            transform: translateY(-2px);
            box-shadow: 0 6px 20px rgba(40,167,69,0.3);
        }

        .quick-action-icon {
            font-size: 1.5rem;
            margin-right: 10px;
        }
    </style>
</head>
<body>

<nav class="navbar navbar-expand-lg navbar-dark bg-dark">
    <div class="container">
        <a class="navbar-brand" href="/">
            <i class="bi bi-shield-check"></i> Antock System
        </a>
        <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarNav">
            <span class="navbar-toggler-icon"></span>
        </button>
        <div class="collapse navbar-collapse" id="navbarNav">
            <div class="navbar-nav ms-auto">
                <a class="nav-link" href="/members/profile">
                    <i class="bi bi-person-circle"></i> 내 프로필
                </a>
                <a class="nav-link" href="/members/admin/pending">
                    <i class="bi bi-clock"></i> 승인 대기
                </a>
                <a class="nav-link" href="/members/logout">
                    <i class="bi bi-box-arrow-right"></i> 로그아웃
                </a>
            </div>
        </div>
    </div>
</nav>

<!-- 히어로 섹션 -->
<section class="hero-section">
    <div class="container">
        <h1 class="hero-title">
            <i class="bi bi-shield-check-fill"></i>
            Antock System
        </h1>
        <p class="hero-subtitle">
            통합 관리 시스템으로 효율적인 업무 환경을 제공합니다
        </p>
        <a href="/corp/search" class="btn btn-light btn-lg px-4">
            <i class="bi bi-search"></i> 법인 검색 시작하기
        </a>
    </div>
</section>

<!-- 주요 기능 섹션 -->
<section class="py-5">
    <div class="container">
        <div class="row text-center mb-5">
            <div class="col">
                <h2 class="fw-bold">주요 기능</h2>
                <p class="text-muted">Antock System이 제공하는 핵심 서비스를 확인해보세요</p>
            </div>
        </div>

        <div class="row g-4">
            <!-- 법인 정보 검색 -->
            <div class="col-lg-4 col-md-6">
                <div class="feature-card">
                    <div class="feature-icon">
                        <i class="bi bi-building"></i>
                    </div>
                    <h3 class="feature-title">법인 정보 검색</h3>
                    <p class="feature-description">
                        법인명, 사업자번호, 지역별로 등록된 법인 정보를 빠르게 검색하고 관리할 수 있습니다.
                    </p>
                    <a href="/corp/search" class="feature-link">
                        <i class="bi bi-search"></i> 검색하기
                    </a>
                </div>
            </div>

            <!-- 회원 관리 -->
            <div class="col-lg-4 col-md-6">
                <div class="feature-card">
                    <div class="feature-icon">
                        <i class="bi bi-people"></i>
                    </div>
                    <h3 class="feature-title">회원 관리</h3>
                    <p class="feature-description">
                        시스템 사용자의 가입 승인, 권한 관리, 계정 상태 등을 통합적으로 관리합니다.
                    </p>
                    <a href="/members/admin" class="feature-link">
                        <i class="bi bi-gear"></i> 관리하기
                    </a>
                </div>
            </div>

            <!-- 승인 대기 -->
            <div class="col-lg-4 col-md-6">
                <div class="feature-card">
                    <div class="feature-icon">
                        <i class="bi bi-clock-history"></i>
                    </div>
                    <h3 class="feature-title">승인 대기</h3>
                    <p class="feature-description">
                        신규 회원 가입 요청과 각종 승인 대기 건들을 효율적으로 처리할 수 있습니다.
                    </p>
                    <a href="/members/admin/pending" class="feature-link">
                        <i class="bi bi-list-check"></i> 확인하기
                    </a>
                </div>
            </div>
        </div>
    </div>
</section>

<!-- 통계 섹션 -->
<section class="stats-section">
    <div class="container">
        <div class="row">
            <div class="col-md-3 col-sm-6">
                <div class="stat-item">
                    <div class="stat-number" id="totalCorps">0</div>
                    <div class="stat-label">등록된 법인</div>
                </div>
            </div>
            <div class="col-md-3 col-sm-6">
                <div class="stat-item">
                    <div class="stat-number" id="totalMembers">0</div>
                    <div class="stat-label">가입 회원</div>
                </div>
            </div>
            <div class="col-md-3 col-sm-6">
                <div class="stat-item">
                    <div class="stat-number" id="pendingApprovals">0</div>
                    <div class="stat-label">승인 대기</div>
                </div>
            </div>
            <div class="col-md-3 col-sm-6">
                <div class="stat-item">
                    <div class="stat-number" id="totalRegions">17</div>
                    <div class="stat-label">지원 지역</div>
                </div>
            </div>
        </div>
    </div>
</section>

<!-- 빠른 액션 섹션 -->
<section class="quick-actions">
    <div class="container">
        <div class="row">
            <div class="col-md-8">
                <h3 class="mb-4">빠른 작업</h3>
                <div class="row">
                    <div class="col-md-6">
                        <a href="/corp/search" class="quick-action-btn">
                            <i class="bi bi-search quick-action-icon"></i>
                            법인 정보 검색
                        </a>
                    </div>
                    <div class="col-md-6">
                        <a href="/members/admin" class="quick-action-btn">
                            <i class="bi bi-people-fill quick-action-icon"></i>
                            전체 회원 관리
                        </a>
                    </div>
                    <div class="col-md-6">
                        <a href="/members/admin/pending" class="quick-action-btn">
                            <i class="bi bi-clock quick-action-icon"></i>
                            승인 대기 처리
                        </a>
                    </div>
                    <div class="col-md-6">
                        <a href="/dashboard" class="quick-action-btn">
                            <i class="bi bi-graph-up quick-action-icon"></i>
                            대시보드 보기
                        </a>
                    </div>
                </div>
            </div>
            <div class="col-md-4">
                <div class="card border-0 shadow-sm">
                    <div class="card-body text-center">
                        <i class="bi bi-info-circle text-primary" style="font-size: 2rem;"></i>
                        <h5 class="card-title mt-3">시스템 정보</h5>
                        <p class="card-text text-muted">
                            Antock System v1.0<br>
                            안정적이고 효율적인<br>
                            통합 관리 솔루션
                        </p>
                    </div>
                </div>
            </div>
        </div>
    </div>
</section>

<!-- 푸터 -->
<footer class="bg-dark text-white py-4 mt-5">
    <div class="container">
        <div class="row">
            <div class="col-md-6">
                <h5><i class="bi bi-shield-check"></i> Antock System</h5>
                <p class="text-muted">효율적인 법인 정보 관리 시스템</p>
            </div>
            <div class="col-md-6 text-md-end">
                <p class="text-muted mb-0">
                    © 2025 Antock System. All rights reserved.
                </p>
            </div>
        </div>
    </div>
</footer>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/js/bootstrap.bundle.min.js"></script>
<script>
    // 애니메이션 효과로 숫자 카운트업
    function animateCounter(element, target, duration = 2000) {
        let start = 0;
        const increment = target / (duration / 16);

        function updateCounter() {
            start += increment;
            if (start < target) {
                element.textContent = Math.floor(start).toLocaleString();
                requestAnimationFrame(updateCounter);
            } else {
                element.textContent = target.toLocaleString();
            }
        }

        updateCounter();
    }

    // 페이지 로드 시 통계 애니메이션
    document.addEventListener('DOMContentLoaded', function() {
        // 통계 섹션이 화면에 보일 때 애니메이션 시작
        const observer = new IntersectionObserver((entries) => {
            entries.forEach(entry => {
                if (entry.isIntersecting) {
                    // 실제 데이터는 서버에서 받아와야 함 (임시 데이터)
                    animateCounter(document.getElementById('totalCorps'), 1247);
                    animateCounter(document.getElementById('totalMembers'), 89);
                    animateCounter(document.getElementById('pendingApprovals'), 12);
                    animateCounter(document.getElementById('totalRegions'), 17);

                    observer.unobserve(entry.target);
                }
            });
        });

        observer.observe(document.querySelector('.stats-section'));

        // 카드 호버 효과
        document.querySelectorAll('.feature-card').forEach(card => {
            card.addEventListener('mouseenter', function() {
                this.style.transform = 'translateY(-5px) scale(1.02)';
            });

            card.addEventListener('mouseleave', function() {
                this.style.transform = 'translateY(0) scale(1)';
            });
        });
    });

    // 키보드 단축키
    document.addEventListener('keydown', function(e) {
        // Ctrl + F: 법인 검색 페이지로
        if (e.ctrlKey && e.key === 'f') {
            e.preventDefault();
            window.location.href = '/corp/search';
        }
        // Ctrl + M: 회원 관리 페이지로
        if (e.ctrlKey && e.key === 'm') {
            e.preventDefault();
            window.location.href = '/members/admin';
        }
    });
</script>

</body>
</html>