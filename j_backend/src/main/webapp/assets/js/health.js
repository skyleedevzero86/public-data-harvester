document.addEventListener('DOMContentLoaded', function() {
    initializeHealthPage();
});

function initializeHealthPage() {
    initializeAutoRefresh();
    initializeCharts();
    initializeTooltips();
    initializeFilters();
}

function initializeAutoRefresh() {
    if (window.location.pathname === '/health' || window.location.pathname === '/health/') {
        setInterval(function() {
            location.reload();
        }, 300000);
    }
}

function initializeCharts() {
    if (typeof Chart !== 'undefined') {
        initializeMetricsCharts();
        initializeRealtimeCharts();
    }
}

function initializeMetricsCharts() {
    const responseTimeCanvas = document.getElementById('responseTimeChart');
    if (responseTimeCanvas) {
        const ctx = responseTimeCanvas.getContext('2d');
        window.responseTimeChart = new Chart(ctx, {
            type: 'line',
            data: {
                labels: [],
                datasets: [{
                    label: '응답 시간 (ms)',
                    data: [],
                    borderColor: 'rgb(75, 192, 192)',
                    backgroundColor: 'rgba(75, 192, 192, 0.1)',
                    tension: 0.1
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                scales: {
                    y: {
                        beginAtZero: true,
                        title: {
                            display: true,
                            text: '응답 시간 (ms)'
                        }
                    },
                    x: {
                        title: {
                            display: true,
                            text: '시간'
                        }
                    }
                }
            }
        });
    }
}

function initializeRealtimeCharts() {
    const realtimeCanvas = document.getElementById('realtimeChart');
    if (realtimeCanvas) {
        const ctx = realtimeCanvas.getContext('2d');
        window.realtimeChart = new Chart(ctx, {
            type: 'line',
            data: {
                labels: [],
                datasets: []
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                scales: {
                    y: {
                        beginAtZero: true
                    }
                },
                plugins: {
                    legend: {
                        display: true,
                        position: 'top'
                    }
                }
            }
        });
    }
}

function updateRealtimeData() {
    fetch('/api/health/realtime')
        .then(response => response.json())
        .then(data => {
            updateRealtimeChart(data);
            updateRealtimeMetrics(data);
        })
        .catch(error => {
            console.error('실시간 데이터 업데이트 오류:', error);
        });
}

function updateRealtimeChart(data) {
    if (window.realtimeChart && data.chartData) {
        const chart = window.realtimeChart;
        chart.data.labels = data.chartData.labels;
        chart.data.datasets = data.chartData.datasets;
        chart.update('none');
    }
}

function updateRealtimeMetrics(data) {
    if (data.metrics) {
        Object.keys(data.metrics).forEach(key => {
            const element = document.getElementById(`metric-${key}`);
            if (element) {
                element.textContent = data.metrics[key];
            }
        });
    }
}

function initializeTooltips() {
    const tooltipTriggerList = [].slice.call(document.querySelectorAll('[data-bs-toggle="tooltip"]'));
    tooltipTriggerList.map(function (tooltipTriggerEl) {
        return new bootstrap.Tooltip(tooltipTriggerEl);
    });
}

function initializeFilters() {
    const filterForm = document.querySelector('form[action*="history"]');
    if (filterForm) {
        filterForm.addEventListener('submit', function(e) {
            const fromDate = document.getElementById('fromDate');
            const toDate = document.getElementById('toDate');

            if (fromDate.value && toDate.value) {
                if (new Date(fromDate.value) > new Date(toDate.value)) {
                    e.preventDefault();
                    showAlert('시작 시간이 종료 시간보다 늦을 수 없습니다.', 'warning');
                    return false;
                }
            }
        });
    }
}

function performHealthCheck(component = null) {
    const button = event.target.closest('button');
    if (button) {
        button.disabled = true;
        button.innerHTML = '<i class="bi bi-arrow-clockwise"></i> 체크 중...';
    }

    const url = component ? `/health/check/${component}` : '/health/check';

    fetch(url, { method: 'POST' })
        .then(response => response.json())
        .then(data => {
            if (data.success) {
                showAlert('헬스 체크가 완료되었습니다.', 'success');
                setTimeout(() => location.reload(), 1000);
            } else {
                showAlert('헬스 체크 중 오류가 발생했습니다: ' + data.message, 'danger');
            }
        })
        .catch(error => {
            console.error('헬스 체크 오류:', error);
            showAlert('헬스 체크 중 오류가 발생했습니다.', 'danger');
        })
        .finally(() => {
            if (button) {
                button.disabled = false;
                button.innerHTML = '<i class="bi bi-arrow-clockwise"></i> 수동 체크';
            }
        });
}

function filterByComponent(component) {
    const rows = document.querySelectorAll('tbody tr');

    rows.forEach(row => {
        const componentCell = row.cells[0];
        if (componentCell) {
            const componentName = componentCell.textContent.trim();

            if (!component || componentName.includes(component)) {
                row.style.display = '';
            } else {
                row.style.display = 'none';
            }
        }
    });

    showAlert(`${component || '전체'} 필터가 적용되었습니다.`, 'info');
}

function filterByStatus(status) {
    const rows = document.querySelectorAll('tbody tr');

    rows.forEach(row => {
        const statusCell = row.cells[1];
        if (statusCell) {
            const badge = statusCell.querySelector('.badge');

            if (!status || (badge && badge.textContent.includes(status))) {
                row.style.display = '';
            } else {
                row.style.display = 'none';
            }
        }
    });

    showAlert(`${status || '전체'} 상태 필터가 적용되었습니다.`, 'info');
}

function showAlert(message, type = 'info') {
    const existingAlert = document.querySelector('.alert-custom');
    if (existingAlert) {
        existingAlert.remove();
    }

    const alertDiv = document.createElement('div');
    alertDiv.className = `alert alert-${type} alert-dismissible fade show alert-custom`;
    alertDiv.style.position = 'fixed';
    alertDiv.style.top = '20px';
    alertDiv.style.right = '20px';
    alertDiv.style.zIndex = '9999';
    alertDiv.style.minWidth = '300px';

    alertDiv.innerHTML = `
        <i class="bi bi-${getAlertIcon(type)}"></i> ${message}
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    `;

    document.body.appendChild(alertDiv);

    setTimeout(() => {
        if (alertDiv && alertDiv.parentNode) {
            alertDiv.remove();
        }
    }, 3000);
}

function getAlertIcon(type) {
    switch (type) {
        case 'success': return 'check-circle';
        case 'danger': return 'exclamation-triangle';
        case 'warning': return 'exclamation-triangle';
        case 'info': return 'info-circle';
        default: return 'info-circle';
    }
}

function formatDate(date, format = 'yyyy-MM-dd HH:mm:ss') {
    if (!date) return '';

    const d = new Date(date);
    const year = d.getFullYear();
    const month = String(d.getMonth() + 1).padStart(2, '0');
    const day = String(d.getDate()).padStart(2, '0');
    const hours = String(d.getHours()).padStart(2, '0');
    const minutes = String(d.getMinutes()).padStart(2, '0');
    const seconds = String(d.getSeconds()).padStart(2, '0');

    return format
        .replace('yyyy', year)
        .replace('MM', month)
        .replace('dd', day)
        .replace('HH', hours)
        .replace('mm', minutes)
        .replace('ss', seconds);
}

function formatResponseTime(ms) {
    if (ms < 1000) {
        return `${ms}ms`;
    } else if (ms < 60000) {
        return `${(ms / 1000).toFixed(1)}s`;
    } else {
        return `${(ms / 60000).toFixed(1)}m`;
    }
}

function exportMetrics(format = 'csv') {
    const data = collectMetricsData();

    if (format === 'csv') {
        exportToCSV(data);
    } else if (format === 'json') {
        exportToJSON(data);
    }
}

function collectMetricsData() {
    const rows = document.querySelectorAll('tbody tr');
    const data = [];

    const headers = Array.from(document.querySelectorAll('thead th'))
        .map(th => th.textContent.trim());
    data.push(headers);

    rows.forEach(row => {
        if (row.style.display !== 'none') {
            const rowData = Array.from(row.cells)
                .map(cell => cell.textContent.trim());
            data.push(rowData);
        }
    });

    return data;
}

function exportToCSV(data) {
    const csvContent = data.map(row =>
        row.map(cell => `"${cell.replace(/"/g, '""')}"`).join(',')
    ).join('\n');

    const blob = new Blob(['\uFEFF' + csvContent], { type: 'text/csv;charset=utf-8;' });
    const link = document.createElement('a');
    link.href = URL.createObjectURL(blob);
    link.download = `헬스_메트릭_${new Date().toISOString().slice(0, 10)}.csv`;
    link.click();
}

function exportToJSON(data) {
    const headers = data[0];
    const jsonData = data.slice(1).map(row => {
        const obj = {};
        headers.forEach((header, index) => {
            obj[header] = row[index];
        });
        return obj;
    });

    const blob = new Blob([JSON.stringify(jsonData, null, 2)], {
        type: 'application/json;charset=utf-8;'
    });
    const link = document.createElement('a');
    link.href = URL.createObjectURL(blob);
    link.download = `헬스_메트릭_${new Date().toISOString().slice(0, 10)}.json`;
    link.click();
}

document.addEventListener('keydown', function(e) {
    if (e.key === 'F5') {
        e.preventDefault();
        location.reload();
    }

    if (e.ctrlKey && e.key === 'r') {
        e.preventDefault();
        performHealthCheck();
    }

    if (e.key === 'Escape') {
        resetFilters();
    }
});

function resetFilters() {
    const rows = document.querySelectorAll('tbody tr');
    rows.forEach(row => {
        row.style.display = '';
    });

    const form = document.querySelector('form[action*="history"]');
    if (form) {
        form.reset();
    }

    showAlert('필터가 초기화되었습니다.', 'success');
}

document.addEventListener('visibilitychange', function() {
    if (document.hidden) {
        if (window.realtimeInterval) {
            clearInterval(window.realtimeInterval);
        }
    } else {
        if (window.location.pathname.includes('realtime')) {
            window.realtimeInterval = setInterval(updateRealtimeData, 5000);
        }
    }
});