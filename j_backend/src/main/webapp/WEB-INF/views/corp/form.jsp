<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8"%> <%@ taglib prefix="c"
                                           uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8" />
    <meta name="_csrf" content="${_csrf.token}" />
    <meta name="_csrf_header" content="${_csrf.headerName}" />
    <title>법인 등록/수정 - 통신판매사업자관리 시스템</title>
    <link
            href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css"
            rel="stylesheet"
    />
    <link
            href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.7.2/font/bootstrap-icons.css"
            rel="stylesheet"
    />
    <script src="https://cdn.jsdelivr.net/npm/jquery@3.6.0/dist/jquery.min.js"></script>
    <script src="/static/ckeditor/ckeditor.js?v=4.23.1"></script>

    <style>
        .form-container {
            background: white;
            border-radius: 10px;
            box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
            padding: 30px;
            margin-bottom: 30px;
        }

        .form-header {
            background: linear-gradient(45deg, #007bff, #0056b3);
            color: white;
            padding: 20px 30px;
            border-radius: 10px 10px 0 0;
            margin: -30px -30px 30px -30px;
        }

        .form-title {
            margin: 0;
            font-weight: 600;
        }

        .form-section {
            background: #f8f9fa;
            border-radius: 8px;
            padding: 20px;
            margin-bottom: 25px;
            border-left: 4px solid #007bff;
        }

        .section-title {
            color: #495057;
            font-weight: 600;
            margin-bottom: 20px;
            font-size: 1.1rem;
        }

        .form-label {
            font-weight: 500;
            color: #495057;
            margin-bottom: 8px;
        }

        .form-control {
            border-radius: 6px;
            border: 1px solid #dee2e6;
            padding: 10px 12px;
            transition: all 0.3s ease;
        }

        .form-control:focus {
            border-color: #007bff;
            box-shadow: 0 0 0 0.2rem rgba(13, 110, 253, 0.25);
            transform: translateY(-1px);
        }

        .form-control:invalid {
            border-color: #dc3545;
        }

        .form-control:valid {
            border-color: #28a745;
        }

        .btn-container {
            display: flex;
            gap: 15px;
            justify-content: center;
            margin-top: 30px;
            padding-top: 25px;
            border-top: 1px solid #dee2e6;
        }

        .btn-save {
            background: linear-gradient(45deg, #28a745, #20c997);
            border: none;
            padding: 12px 40px;
            font-weight: 600;
            font-size: 1.1rem;
        }

        .btn-list {
            background: linear-gradient(45deg, #6c757d, #495057);
            border: none;
            padding: 12px 40px;
            font-weight: 600;
            font-size: 1.1rem;
        }

        .btn-cancel {
            background: linear-gradient(45deg, #dc3545, #c82333);
            border: none;
            padding: 12px 40px;
            font-weight: 600;
            font-size: 1.1rem;
        }

        .required-field::after {
            content: " *";
            color: #dc3545;
            font-weight: bold;
        }

        .field-help {
            font-size: 0.85rem;
            color: #6c757d;
            margin-top: 5px;
        }

        .ckeditor-container {
            border-radius: 6px;
            overflow: hidden;
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

        .alert-container {
            margin-bottom: 25px;
        }

        .form-row {
            margin-bottom: 20px;
        }

        .form-group {
            margin-bottom: 20px;
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
</nav>

<div class="container-fluid mt-4">
    <div class="d-flex justify-content-between align-items-center mb-4">
        <h2><i class="bi bi-building"></i> 법인 등록/수정</h2>
        <div>
            <a href="/corp/list" class="btn btn-outline-secondary">
                <i class="bi bi-arrow-left"></i> 목록으로 돌아가기
            </a>
        </div>
    </div>

    <div class="form-container">
        <div class="form-header">
            <h3 class="form-title">
                <i class="bi bi-pencil-square"></i>
                <c:choose>
                    <c:when test="${empty form.id}">새 법인 등록</c:when>
                    <c:otherwise>법인 정보 수정</c:otherwise>
                </c:choose>
            </h3>
        </div>

        <form method="post" id="corpForm">
            <input type="hidden" name="id" value="${form.id}" />

            <div class="form-section">
                <h5 class="section-title">
                    <i class="bi bi-info-circle"></i> 기본 정보
                </h5>
                <div class="row">
                    <div class="col-md-6">
                        <div class="form-group">
                            <label for="bizNm" class="form-label required-field"
                            >법인명</label
                            >
                            <input
                                    type="text"
                                    id="bizNm"
                                    name="bizNm"
                                    value="${form.bizNm}"
                                    class="form-control"
                                    required
                            />
                            <div class="field-help">법인의 공식 명칭을 입력하세요</div>
                        </div>
                    </div>
                    <div class="col-md-6">
                        <div class="form-group">
                            <label for="bizNo" class="form-label required-field"
                            >사업자번호</label
                            >
                            <input
                                    type="text"
                                    id="bizNo"
                                    name="bizNo"
                                    value="${form.bizNo}"
                                    class="form-control"
                                    required
                                    placeholder="000-00-00000"
                            />
                            <div class="field-help">하이픈(-)을 포함하여 입력하세요</div>
                        </div>
                    </div>
                </div>
                <div class="row">
                    <div class="col-md-6">
                        <div class="form-group">
                            <label for="corpRegNo" class="form-label required-field"
                            >법인등록번호</label
                            >
                            <input
                                    type="text"
                                    id="corpRegNo"
                                    name="corpRegNo"
                                    value="${form.corpRegNo}"
                                    class="form-control"
                                    required
                            />
                            <div class="field-help">
                                법인등기부등본의 등록번호를 입력하세요
                            </div>
                        </div>
                    </div>
                    <div class="col-md-6">
                        <div class="form-group">
                            <label for="regionCd" class="form-label required-field"
                            >지역코드</label
                            >
                            <input
                                    type="text"
                                    id="regionCd"
                                    name="regionCd"
                                    value="${form.regionCd}"
                                    class="form-control"
                                    required
                            />
                            <div class="field-help">행정구역 코드를 입력하세요</div>
                        </div>
                    </div>
                </div>
            </div>

            <div class="form-section">
                <h5 class="section-title">
                    <i class="bi bi-geo-alt"></i> 주소 정보
                </h5>
                <div class="row">
                    <div class="col-md-6">
                        <div class="form-group">
                            <label for="siNm" class="form-label required-field"
                            >시/도</label
                            >
                            <input
                                    type="text"
                                    id="siNm"
                                    name="siNm"
                                    value="${form.siNm}"
                                    class="form-control"
                                    required
                            />
                            <div class="field-help">
                                예: 서울특별시, 부산광역시, 전라남도 등
                            </div>
                        </div>
                    </div>
                    <div class="col-md-6">
                        <div class="form-group">
                            <label for="sggNm" class="form-label required-field"
                            >구/군</label
                            >
                            <input
                                    type="text"
                                    id="sggNm"
                                    name="sggNm"
                                    value="${form.sggNm}"
                                    class="form-control"
                                    required
                            />
                            <div class="field-help">예: 강남구, 해운대구, 나주시 등</div>
                        </div>
                    </div>
                </div>
            </div>

            <div class="form-section">
                <h5 class="section-title">
                    <i class="bi bi-person-badge"></i> 판매자 정보
                </h5>
                <div class="row">
                    <div class="col-md-6">
                        <div class="form-group">
                            <label for="sellerId" class="form-label required-field"
                            >판매자ID</label
                            >
                            <input
                                    type="text"
                                    id="sellerId"
                                    name="sellerId"
                                    value="${form.sellerId}"
                                    class="form-control"
                                    required
                            />
                            <div class="field-help">
                                통신판매업 신고 시 발급받은 판매자 ID
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <div class="form-section">
                <h5 class="section-title">
                    <i class="bi bi-file-text"></i> 상세 정보
                </h5>
                <div class="form-group">
                    <label for="description" class="form-label">설명</label>
                    <div class="ckeditor-container">
                <textarea
                        name="description"
                        id="description"
                        class="form-control"
                        rows="6"
                >
                    ${form.description}</textarea
                >
                    </div>
                    <div class="field-help">
                        법인에 대한 추가 설명이나 특이사항을 입력하세요
                    </div>
                </div>
            </div>

            <div class="btn-container">
                <button type="submit" class="btn btn-success btn-save">
                    <i class="bi bi-check-circle"></i>
                    <c:choose>
                        <c:when test="${empty form.id}">등록</c:when>
                        <c:otherwise>수정</c:otherwise>
                    </c:choose>
                </button>
                <a href="/corp/list" class="btn btn-secondary btn-list">
                    <i class="bi bi-list"></i> 목록
                </a>
                <button
                        type="button"
                        class="btn btn-danger btn-cancel"
                        onclick="history.back()"
                >
                    <i class="bi bi-x-circle"></i> 취소
                </button>
            </div>
        </form>
    </div>
</div>

<footer class="footer">
    <div class="footer-container">
        <div class="row">
            <div class="col-md-6">
                <div class="footer-logo">
                    <div class="festival-number"></div>
                    <div class="main-title">public-data-harvester</div>
                    <div class="sub-title">
                        CHUNGJANG STREET FESTIVAL OF RECOLLECTION
                    </div>
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

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/js/bootstrap.min.js"></script>
<script>
    CKEDITOR.replace("description", {
        height: 200,
        removePlugins: "elementspath,resize",
        toolbar: [
            { name: "document", items: ["Source"] },
            {
                name: "clipboard",
                items: [
                    "Cut",
                    "Copy",
                    "Paste",
                    "PasteText",
                    "PasteFromWord",
                    "-",
                    "Undo",
                    "Redo",
                ],
            },
            {
                name: "editing",
                items: [
                    "Find",
                    "Replace",
                    "-",
                    "SelectAll",
                    "-",
                    "SpellChecker",
                    "Scayt",
                ],
            },
            "/",
            {
                name: "basicstyles",
                items: [
                    "Bold",
                    "Italic",
                    "Underline",
                    "Strike",
                    "Subscript",
                    "Superscript",
                    "-",
                    "RemoveFormat",
                ],
            },
            {
                name: "paragraph",
                items: [
                    "NumberedList",
                    "BulletedList",
                    "-",
                    "Outdent",
                    "Indent",
                    "-",
                    "Blockquote",
                    "CreateDiv",
                    "-",
                    "JustifyLeft",
                    "JustifyCenter",
                    "JustifyRight",
                    "JustifyBlock",
                    "-",
                    "BidiLtr",
                    "BidiRtl",
                ],
            },
            { name: "links", items: ["Link", "Unlink", "Anchor"] },
            {
                name: "insert",
                items: [
                    "Image",
                    "Flash",
                    "Table",
                    "HorizontalRule",
                    "Smiley",
                    "SpecialChar",
                    "PageBreak",
                    "Iframe",
                ],
            },
            "/",
            { name: "styles", items: ["Styles", "Format", "Font", "FontSize"] },
            { name: "colors", items: ["TextColor", "BGColor"] },
            { name: "tools", items: ["Maximize", "ShowBlocks"] },
        ],
    });

    document.getElementById("bizNo").addEventListener("input", function (e) {
        let value = e.target.value.replace(/[^0-9]/g, "");
        if (value.length <= 10) {
            if (value.length > 5) {
                value =
                    value.substring(0, 3) +
                    "-" +
                    value.substring(3, 5) +
                    "-" +
                    value.substring(5);
            } else if (value.length > 3) {
                value = value.substring(0, 3) + "-" + value.substring(3);
            }
            e.target.value = value;
        }
    });

    document
        .getElementById("corpForm")
        .addEventListener("submit", function (e) {
            const requiredFields = document.querySelectorAll("[required]");
            let isValid = true;

            requiredFields.forEach((field) => {
                if (!field.value.trim()) {
                    field.classList.add("is-invalid");
                    isValid = false;
                } else {
                    field.classList.remove("is-invalid");
                    field.classList.add("is-valid");
                }
            });

            if (!isValid) {
                e.preventDefault();
                alert("필수 항목을 모두 입력해주세요.");
            }
        });

    document.querySelectorAll("input[required]").forEach((input) => {
        input.addEventListener("blur", function () {
            if (this.value.trim()) {
                this.classList.add("is-valid");
                this.classList.remove("is-invalid");
            } else {
                this.classList.add("is-invalid");
                this.classList.remove("is-valid");
            }
        });
    });
</script>
</body>
</html>