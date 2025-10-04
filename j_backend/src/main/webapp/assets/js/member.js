document.addEventListener('DOMContentLoaded', function() {

    const forms = document.querySelectorAll('.needs-validation');
    Array.from(forms).forEach(form => {
        form.addEventListener('submit', function(event) {
            if (!form.checkValidity()) {
                event.preventDefault();
                event.stopPropagation();
            }
            form.classList.add('was-validated');
        });
    });

    const loginForm = document.getElementById('loginForm');
    if (loginForm) {
        loginForm.addEventListener('submit', function(e) {
            const username = document.getElementById('username').value.trim();
            const password = document.getElementById('password').value;

            if (!username || !password) {
                e.preventDefault();
                showAlert('아이디와 비밀번호를 모두 입력해주세요.', 'danger');
                return;
            }

            const submitBtn = this.querySelector('button[type="submit"]');
            if (submitBtn) {
                submitBtn.innerHTML = '<span class="spinner-border spinner-border-sm me-2" role="status" aria-hidden="true"></span>로그인 중...';
                submitBtn.disabled = true;
            }
        });
    }

    const profileForm = document.getElementById('profileForm');
    if (profileForm) {
        profileForm.addEventListener('submit', function(e) {
            const submitBtn = this.querySelector('button[type="submit"]');
            if (submitBtn) {
                submitBtn.innerHTML = '<span class="spinner"></span> 저장 중...';
                submitBtn.disabled = true;
            }
        });
    }

    const passwordChangeForm = document.getElementById('passwordChangeForm');
    if (passwordChangeForm) {
        passwordChangeForm.addEventListener('submit', function(e) {
            const currentPassword = document.getElementById('currentPassword');
            const newPassword = document.getElementById('newPassword');
            const confirmPassword = document.getElementById('confirmPassword');

            if (!currentPassword || !newPassword || !confirmPassword) {
                return;
            }

            if (newPassword.value !== confirmPassword.value) {
                e.preventDefault();
                showAlert('새 비밀번호와 확인 비밀번호가 일치하지 않습니다.', 'danger');
                return;
            }

            if (newPassword.value.length < 8) {
                e.preventDefault();
                showAlert('비밀번호는 최소 8자 이상이어야 합니다.', 'danger');
                return;
            }

            const submitBtn = this.querySelector('button[type="submit"]');
            if (submitBtn) {
                submitBtn.innerHTML = '<span class="spinner"></span> 변경 중...';
                submitBtn.disabled = true;
            }
        });
    }

    const alerts = document.querySelectorAll('.alert');
    alerts.forEach(alert => {
        setTimeout(() => {
            alert.style.opacity = '0';
            setTimeout(() => {
                alert.remove();
            }, 300);
        }, 5000);
    });

    const forms = document.querySelectorAll('form');
    forms.forEach(form => {
        form.addEventListener('submit', function(e) {
            if (!this.checkValidity()) {
                e.preventDefault();
                e.stopPropagation();
                showAlert('입력 정보를 확인해주세요.', 'danger');
            }
            this.classList.add('was-validated');
        });
    });

    const passwordInputs = document.querySelectorAll('input[type="password"]');
    passwordInputs.forEach(input => {
        if (input.id === 'newPassword' || input.id === 'password') {
            input.addEventListener('input', function() {
                updatePasswordStrength(this.value);
            });
        }
    });

    const statusFilter = document.getElementById('statusFilter');
    if (statusFilter) {
        statusFilter.addEventListener('change', function() {
            const form = this.closest('form');
            if (form) {
                form.submit();
            }
        });
    }

    const roleFilter = document.getElementById('roleFilter');
    if (roleFilter) {
        roleFilter.addEventListener('change', function() {
            const form = this.closest('form');
            if (form) {
                form.submit();
            }
        });
    }

    const searchInput = document.getElementById('searchInput');
    if (searchInput) {
        let searchTimeout;
        searchInput.addEventListener('input', function() {
            clearTimeout(searchTimeout);
            searchTimeout = setTimeout(() => {
                performSearch(this.value);
            }, 500);
        });
    }

    const deleteButtons = document.querySelectorAll('[data-action="delete"]');
    deleteButtons.forEach(button => {
        button.addEventListener('click', function(e) {
            e.preventDefault();
            const memberName = this.getAttribute('data-member-name') || '이 회원';
            if (confirm(`${memberName}을(를) 정말 삭제하시겠습니까?`)) {
                const form = this.closest('form');
                if (form) {
                    form.submit();
                }
            }
        });
    });

    const actionButtons = document.querySelectorAll('[data-action="approve"], [data-action="reject"]');
    actionButtons.forEach(button => {
        button.addEventListener('click', function(e) {
            e.preventDefault();
            const action = this.getAttribute('data-action');
            const memberName = this.getAttribute('data-member-name') || '이 회원';
            const actionText = action === 'approve' ? '승인' : '거부';

            if (confirm(`${memberName}을(를) ${actionText}하시겠습니까?`)) {
                const form = this.closest('form');
                if (form) {
                    form.submit();
                }
            }
        });
    });
});

function showAlert(message, type = 'info') {
    const alertContainer = document.querySelector('.alert-container') || createAlertContainer();

    const alert = document.createElement('div');
    alert.className = `alert alert-${type} alert-dismissible fade show`;
    alert.innerHTML = `
        <i class="bi bi-${getAlertIcon(type)}"></i> ${message}
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    `;

    alertContainer.appendChild(alert);

    setTimeout(() => {
        alert.style.opacity = '0';
        setTimeout(() => {
            alert.remove();
        }, 300);
    }, 5000);
}

function createAlertContainer() {
    const container = document.createElement('div');
    container.className = 'alert-container';
    container.style.cssText = 'position: fixed; top: 20px; right: 20px; z-index: 9999; max-width: 400px;';
    document.body.appendChild(container);
    return container;
}

function getAlertIcon(type) {
    const icons = {
        'success': 'check-circle',
        'danger': 'exclamation-triangle',
        'warning': 'exclamation-triangle',
        'info': 'info-circle'
    };
    return icons[type] || 'info-circle';
}

function updatePasswordStrength(password) {
    const strengthIndicator = document.getElementById('passwordStrength');
    if (!strengthIndicator) return;

    let strength = 0;
    let strengthText = '';
    let strengthClass = '';

    if (password.length >= 8) strength++;
    if (password.match(/[a-z]/)) strength++;
    if (password.match(/[A-Z]/)) strength++;
    if (password.match(/[0-9]/)) strength++;
    if (password.match(/[^a-zA-Z0-9]/)) strength++;

    switch (strength) {
        case 0:
        case 1:
            strengthText = '매우 약함';
            strengthClass = 'danger';
            break;
        case 2:
            strengthText = '약함';
            strengthClass = 'warning';
            break;
        case 3:
            strengthText = '보통';
            strengthClass = 'info';
            break;
        case 4:
            strengthText = '강함';
            strengthClass = 'success';
            break;
        case 5:
            strengthText = '매우 강함';
            strengthClass = 'success';
            break;
    }

    strengthIndicator.innerHTML = `
        <div class="password-strength">
            <div class="strength-bar">
                <div class="strength-fill strength-${strengthClass}" style="width: ${(strength / 5) * 100}%"></div>
            </div>
            <span class="strength-text text-${strengthClass}">${strengthText}</span>
        </div>
    `;
}

function performSearch(query) {
    console.log('Searching for:', query);
}

function togglePasswordVisibility(inputId) {
    const input = document.getElementById(inputId);
    const toggleBtn = document.querySelector(`[data-toggle="${inputId}"]`);

    if (input && toggleBtn) {
        if (input.type === 'password') {
            input.type = 'text';
            toggleBtn.innerHTML = '<i class="bi bi-eye-slash"></i>';
        } else {
            input.type = 'password';
            toggleBtn.innerHTML = '<i class="bi bi-eye"></i>';
        }
    }
}

function resetForm(formId) {
    const form = document.getElementById(formId);
    if (form) {
        form.reset();
        form.classList.remove('was-validated');

        const strengthIndicator = document.getElementById('passwordStrength');
        if (strengthIndicator) {
            strengthIndicator.innerHTML = '';
        }
    }
}

window.MemberUtils = {
    showAlert,
    togglePasswordVisibility,
    resetForm,
    updatePasswordStrength
};

