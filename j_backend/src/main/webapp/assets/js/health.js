document.addEventListener('DOMContentLoaded', function() {
    initializeHealthPage();
});

function initializeHealthPage() {
    initializeAutoRefresh();
    initializeCharts();
    initializeTooltips();
    initializeFilters();
    initializeRealtimeMetrics();

    setTimeout(() => {
        if (typeof Chart === 'undefined') {
            showChartFallback();
        }
    }, 3000);
}

function showChartFallback() {
    const chartContainers = document.querySelectorAll('#responseTimeChart, #realtimeChart');
    chartContainers.forEach(container => {
        if (container && !container.querySelector('.chart-fallback')) {
            const fallback = document.createElement('div');
            fallback.className = 'chart-fallback d-flex align-items-center justify-content-center text-muted';
            fallback.style.height = '200px';
            fallback.innerHTML = '<div class="text-center"><i class="bi bi-graph-up fs-1 mb-2"></i><br>차트를 로드할 수 없습니다<br><small>페이지를 새로고침해주세요</small></div>';
            container.appendChild(fallback);
        }
    });
}

function initializeAutoRefresh() {
    if (window.location.pathname === '/health' ||
        window.location.pathname === '/health/' ||
        window.location.pathname.includes('/health/')) {
        setInterval(function() {
            location.reload();
        }, 300000);
    }
}

function initializeCharts() {
    if (window.chartsInitialized) {
        return;
    }

    if (typeof Chart !== 'undefined') {
        destroyExistingCharts();
        initializeMetricsCharts();
        initializeRealtimeCharts();
        window.chartsInitialized = true;
    } else {
        window.addEventListener('chartjs-loaded', function() {
            destroyExistingCharts();
            initializeMetricsCharts();
            initializeRealtimeCharts();
            window.chartsInitialized = true;
        });

        window.addEventListener('chartjs-error', function() {
            showChartFallback();
        });

        let retryCount = 0;
        const maxRetries = 5;

        const checkChartJs = () => {
            if (typeof Chart !== 'undefined') {
                destroyExistingCharts();
                initializeMetricsCharts();
                initializeRealtimeCharts();
                window.chartsInitialized = true;
            } else if (retryCount < maxRetries) {
                retryCount++;
                setTimeout(checkChartJs, 1000 * retryCount);
            } else {
                showAlert('차트를 로드할 수 없습니다. 다른 기능은 정상 작동합니다.', 'warning');
            }
        };

        setTimeout(checkChartJs, 500);
    }
}

function destroyExistingCharts() {
    const chartIds = ['cpuChart', 'memoryChart', 'responseTimeChart', 'realtimeChart', 'availabilityChart', 'successRateChart'];
    chartIds.forEach(chartId => {
        const chartInstance = window[chartId + 'Instance'];
        if (chartInstance) {
            chartInstance.destroy();
            delete window[chartId + 'Instance'];
        }
    });

    if (window.responseTimeChartInstance) {
        window.responseTimeChartInstance.destroy();
        delete window.responseTimeChartInstance;
    }
    if (window.availabilityChartInstance) {
        window.availabilityChartInstance.destroy();
        delete window.availabilityChartInstance;
    }
    if (window.successRateChartInstance) {
        window.successRateChartInstance.destroy();
        delete window.successRateChartInstance;
    }

    if (window.realtimeInterval) {
        clearInterval(window.realtimeInterval);
        delete window.realtimeInterval;
    }

    if (window.cpuChartInterval) {
        clearInterval(window.cpuChartInterval);
        delete window.cpuChartInterval;
    }
    if (window.memoryChartInterval) {
        clearInterval(window.memoryChartInterval);
        delete window.memoryChartInterval;
    }
    if (window.responseTimeChartInterval) {
        clearInterval(window.responseTimeChartInterval);
        delete window.responseTimeChartInterval;
    }

    window.chartsInitialized = false;
}

function initializeMetricsCharts() {
    const responseTimeCanvas = document.getElementById('responseTimeChart');
    if (!responseTimeCanvas) {
        return;
    }

    try {
        const ctx = responseTimeCanvas.getContext('2d');
        window.responseTimeChartInstance = new Chart(ctx, {
            type: 'line',
            data: {
                labels: [],
                datasets: [{
                    label: '응답 시간 (ms)',
                    data: [],
                    borderColor: 'rgb(75, 192, 192)',
                    backgroundColor: 'rgba(75, 192, 192, 0.1)',
                    tension: 0.1,
                    fill: true
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
                },
                plugins: {
                    legend: {
                        display: true
                    }
                }
            }
        });

        window.responseTimeChartInterval = setInterval(() => {
            if (window.responseTimeChartInstance && window.responseTimeChartInstance.data && window.responseTimeChartInstance.data.datasets && window.responseTimeChartInstance.data.datasets[0]) {
                try {
                    const now = new Date();
                    const timeLabel = now.toLocaleTimeString();

                    window.responseTimeChartInstance.data.labels.push(timeLabel);
                    if (window.responseTimeChartInstance.data.labels.length > 20) {
                        window.responseTimeChartInstance.data.labels.shift();
                    }

                    window.responseTimeChartInstance.data.datasets[0].data.push(Math.random() * 500 + 50);
                    if (window.responseTimeChartInstance.data.datasets[0].data.length > 20) {
                        window.responseTimeChartInstance.data.datasets[0].data.shift();
                    }

                    window.responseTimeChartInstance.update('none');
                } catch (error) {
                }
            }
        }, 5000);
    } catch (error) {
        showAlert('응답시간 차트를 초기화할 수 없습니다.', 'warning');
    }
}

function initializeRealtimeCharts() {
    if (window.responseTimeChartInstance) {
        window.responseTimeChartInstance.destroy();
        delete window.responseTimeChartInstance;
    }
    if (window.availabilityChartInstance) {
        window.availabilityChartInstance.destroy();
        delete window.availabilityChartInstance;
    }
    if (window.successRateChartInstance) {
        window.successRateChartInstance.destroy();
        delete window.successRateChartInstance;
    }

    if (window.realtimeInterval) {
        clearInterval(window.realtimeInterval);
        delete window.realtimeInterval;
    }

    const responseTimeCanvas = document.getElementById('responseTimeChart');
    if (responseTimeCanvas) {
        initializeResponseTimeChart(responseTimeCanvas);
    }

    const availabilityCanvas = document.getElementById('availabilityChart');
    if (availabilityCanvas) {
        initializeAvailabilityChart(availabilityCanvas);
    }

    const successRateCanvas = document.getElementById('successRateChart');
    if (successRateCanvas) {
        initializeSuccessRateChart(successRateChart);
    }

    updateRealtimeData();
    window.realtimeInterval = setInterval(updateRealtimeData, 5000);
}

function initializeResponseTimeChart(canvas) {
    try {
        const ctx = canvas.getContext('2d');
        window.responseTimeChartInstance = new Chart(ctx, {
            type: 'line',
            data: {
                labels: [],
                datasets: [{
                    label: '응답 시간 (ms)',
                    data: [],
                    borderColor: 'rgb(75, 192, 192)',
                    backgroundColor: 'rgba(75, 192, 192, 0.1)',
                    tension: 0.1,
                    fill: true
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
    } catch (error) {
    }
}

function initializeAvailabilityChart(canvas) {
    try {
        const ctx = canvas.getContext('2d');
        window.availabilityChartInstance = new Chart(ctx, {
            type: 'doughnut',
            data: {
                labels: ['가용', '불가용'],
                datasets: [{
                    data: [100, 0],
                    backgroundColor: [
                        'rgba(40, 167, 69, 0.8)',
                        'rgba(220, 53, 69, 0.8)'
                    ],
                    borderColor: [
                        'rgba(40, 167, 69, 1)',
                        'rgba(220, 53, 69, 1)'
                    ],
                    borderWidth: 2
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        display: true,
                        position: 'bottom'
                    },
                    tooltip: {
                        callbacks: {
                            label: function(context) {
                                return context.label + ': ' + context.parsed + '%';
                            }
                        }
                    }
                }
            }
        });
    } catch (error) {
    }
}

function initializeSuccessRateChart(canvas) {
    try {
        const ctx = canvas.getContext('2d');
        window.successRateChartInstance = new Chart(ctx, {
            type: 'doughnut',
            data: {
                labels: ['성공', '실패'],
                datasets: [{
                    data: [100, 0],
                    backgroundColor: [
                        'rgba(40, 167, 69, 0.8)',
                        'rgba(220, 53, 69, 0.8)'
                    ],
                    borderColor: [
                        'rgba(40, 167, 69, 1)',
                        'rgba(220, 53, 69, 1)'
                    ],
                    borderWidth: 2
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        display: true,
                        position: 'bottom'
                    },
                    tooltip: {
                        callbacks: {
                            label: function(context) {
                                return context.label + ': ' + context.parsed + '%';
                            }
                        }
                    }
                }
            }
        });
    } catch (error) {
    }
}

function updateRealtimeData() {
    fetch('/api/v1/health/metrics/realtime')
        .then(response => {
            if (!response.ok) {
                if (response.status === 401) {
                    return null;
                }
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            return response.json();
        })
        .then(data => {
            if (data && data.success && data.data) {
                updateRealtimeCharts(data.data);
                updateRealtimeMetrics(data.data);
            }
        })
        .catch(error => {
        });
}

function updateRealtimeCharts(data) {
    if (window.responseTimeChartInstance) {
        updateResponseTimeChart(data);
    }

    if (window.availabilityChartInstance) {
        updateAvailabilityChart(data);
    }

    if (window.successRateChartInstance) {
        updateSuccessRateChart(data);
    }
}

function updateResponseTimeChart(data) {
    if (window.responseTimeChartInstance && window.responseTimeChartInstance.data && window.responseTimeChartInstance.data.datasets && window.responseTimeChartInstance.data.datasets[0]) {
        try {
            const chart = window.responseTimeChartInstance;
            const now = new Date();
            const timeLabel = now.toLocaleTimeString();

            if (chart.data.labels.length >= 20) {
                chart.data.labels.shift();
                chart.data.datasets[0].data.shift();
            }

            chart.data.labels.push(timeLabel);
            chart.data.datasets[0].data.push(data.averageResponseTime || 0);
            chart.update('none');
        } catch (error) {
        }
    }
}

function updateAvailabilityChart(data) {
    if (window.availabilityChartInstance && window.availabilityChartInstance.data && window.availabilityChartInstance.data.datasets && window.availabilityChartInstance.data.datasets[0]) {
        try {
            const chart = window.availabilityChartInstance;
            const availability = data.overallAvailability || 100;
            const unavailability = 100 - availability;

            chart.data.datasets[0].data = [availability, unavailability];
            chart.update('none');
        } catch (error) {
        }
    }
}

function updateSuccessRateChart(data) {
    if (window.successRateChartInstance && window.successRateChartInstance.data && window.successRateChartInstance.data.datasets && window.successRateChartInstance.data.datasets[0]) {
        try {
            const chart = window.successRateChartInstance;
            const successRate = data.successRate || 100;
            const failureRate = 100 - successRate;

            chart.data.datasets[0].data = [successRate, failureRate];
            chart.update('none');
        } catch (error) {
        }
    }
}

function updateRealtimeChart(data) {
    if (window.realtimeChartInstance) {
        const chart = window.realtimeChartInstance;

        const now = new Date();
        const timeLabel = now.toLocaleTimeString();

        chart.data.labels.push(timeLabel);
        if (chart.data.labels.length > 20) {
            chart.data.labels.shift();
        }

        chart.data.datasets[0].data.push(data.averageResponseTime || Math.random() * 500 + 50);
        if (chart.data.datasets[0].data.length > 20) {
            chart.data.datasets[0].data.shift();
        }

        chart.update('none');
    }
}

function updateRealtimeMetrics(data) {
    const availabilityElement = document.getElementById('overallAvailability');
    if (availabilityElement && data.overallAvailability) {
        availabilityElement.textContent = data.overallAvailability.toFixed(2) + '%';
    }

    const successRateElement = document.getElementById('successRate');
    if (successRateElement && data.successRate) {
        successRateElement.textContent = data.successRate.toFixed(2) + '%';
    }

    const totalChecksElement = document.getElementById('totalChecks');
    if (totalChecksElement && data.totalChecks) {
        totalChecksElement.textContent = data.totalChecks.toLocaleString();
    }

    const responseTimeElement = document.getElementById('averageResponseTime');
    if (responseTimeElement && data.averageResponseTime) {
        responseTimeElement.textContent = Math.round(data.averageResponseTime) + 'ms';
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

    const url = '/api/v1/health/check';
    const requestBody = component ? { components: [component] } : {};

    fetch(url, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(requestBody)
    })
        .then(response => {
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            return response.json();
        })
        .then(data => {
            if (data.success) {
                showAlert('헬스 체크가 완료되었습니다.', 'success');
                setTimeout(() => location.reload(), 1000);
            } else {
                showAlert('헬스 체크 중 오류가 발생했습니다: ' + (data.message || '알 수 없는 오류'), 'danger');
            }
        })
        .catch(error => {
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

function initializeRealtimeMetrics() {
    updateRealtimeMetrics();

    setInterval(updateRealtimeMetrics, 120000);
}

function updateRealtimeMetrics() {
    fetch('/api/v1/health/metrics/realtime')
        .then(response => {
            if (!response.ok) {
                throw new Error('HTTP error! status: ' + response.status);
            }
            return response.json();
        })
        .then(data => {
            if (data && data.success && data.data) {
                updateMetricsDisplay(data.data);
            } else {
                showAlert('실시간 메트릭스 데이터를 가져올 수 없습니다.', 'warning');
            }
        })
        .catch(error => {
            showAlert('실시간 메트릭스를 업데이트할 수 없습니다: ' + error.message, 'error');
        });
}

function updateMetricsDisplay(metrics) {
    if (metrics.cpu !== undefined) {
        const cpuElement = document.getElementById('cpuUsage');
        if (cpuElement) {
            cpuElement.textContent = metrics.cpu.toFixed(1) + '%';
            updateMetricStatus(cpuElement, metrics.cpu, 70, 90);
        }
    }

    if (metrics.memory !== undefined) {
        const memoryElement = document.getElementById('memoryUsage');
        if (memoryElement) {
            memoryElement.textContent = metrics.memory.toFixed(1) + '%';
            updateMetricStatus(memoryElement, metrics.memory, 80, 95);
        }
    }

    if (metrics.disk !== undefined) {
        const diskElement = document.getElementById('diskUsage');
        if (diskElement) {
            diskElement.textContent = metrics.disk.toFixed(1) + '%';
            updateMetricStatus(diskElement, metrics.disk, 80, 95);
        }
    }

    if (metrics.averageResponseTime !== undefined) {
        const responseTimeElement = document.getElementById('responseTime');
        if (responseTimeElement) {
            responseTimeElement.textContent = metrics.averageResponseTime.toFixed(1) + 'ms';
            updateResponseTimeStatus(responseTimeElement, metrics.averageResponseTime);
        }
    }
}

function updateMetricStatus(element, value, warningThreshold, criticalThreshold) {
    element.className = 'metrics-value';

    if (value >= criticalThreshold) {
        element.classList.add('text-danger');
    } else if (value >= warningThreshold) {
        element.classList.add('text-warning');
    } else {
        element.classList.add('text-success');
    }
}

function updateResponseTimeStatus(element, value) {
    element.className = 'metrics-value';

    if (value >= 1000) {
        element.classList.add('text-danger');
    } else if (value >= 500) {
        element.classList.add('text-warning');
    } else {
        element.classList.add('text-success');
    }
}