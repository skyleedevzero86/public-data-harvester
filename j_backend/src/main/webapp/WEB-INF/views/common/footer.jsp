<%@ page language="java" contentType="text/html; charset=UTF-8"
pageEncoding="UTF-8"%> <%@ taglib prefix="c"
uri="http://java.sun.com/jsp/jstl/core" %>

<style>
  .footer {
    background-color: #343a40;
    color: white;
    padding: 40px 0 20px 0;
    margin-top: 60px;
  }

  .footer-logo {
    margin-bottom: 30px;
  }

  .footer-logo .main-title {
    font-size: 1.8rem;
    font-weight: bold;
    margin-bottom: 5px;
  }

  .footer-logo .sub-title {
    font-size: 1rem;
    color: #adb5bd;
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

  .footer-contact .contact-address,
  .footer-contact .contact-phone,
  .footer-contact .contact-email {
    font-size: 0.9rem;
    color: #adb5bd;
    margin-bottom: 5px;
    line-height: 1.4;
  }

  .footer-copyright {
    border-top: 1px solid #495057;
    padding-top: 20px;
    text-align: center;
    font-size: 0.8rem;
    color: #adb5bd;
  }

  .footer-container {
    max-width: 1200px;
    margin: 0 auto;
    padding: 0 20px;
  }

  @media (max-width: 768px) {
    .footer {
      padding: 30px 0 15px 0;
      margin-top: 40px;
    }

    .footer-logo .main-title {
      font-size: 1.5rem;
    }

    .footer-contact .contact-title {
      font-size: 1rem;
    }
  }
</style>

<footer class="footer">
  <div class="footer-container">
    <div class="row">
      <div class="col-md-6">
        <div class="footer-logo">
          <div class="main-title">public-data-harvester</div>
          <div class="sub-title">통신판매사업자 정보 관리시스템</div>
        </div>
        <div class="footer-contact">
          <div class="contact-title">시스템 정보</div>
          <div class="contact-address">대한민국 광주광역시 서구</div>
          <div class="contact-phone">TEL: 010-xxx-xxxx</div>
        </div>
      </div>

      <div class="col-md-6">
        <div class="footer-contact">
          <div class="contact-title">문의사항</div>
          <div class="contact-address">대한민국 광주광역시 서구</div>
          <div class="contact-phone">TEL: 010-xxx-xxxx</div>
          <div class="contact-email">E-MAIL: sleekydz86@naver.com</div>
        </div>
      </div>
    </div>

    <div class="footer-copyright">
      ⓒ 2025 public-data-harvester. All rights reserved.
    </div>
  </div>
</footer>
