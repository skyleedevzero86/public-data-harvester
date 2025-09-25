document.addEventListener('DOMContentLoaded', function() {
    initHealthPage();
});

function initHealthPage() {
    initAutoRefresh();
    initRealtimeMetrics();
    initCharts();
    initFilters();
    initRefreshButton();
}

function initAutoRefresh() {
    const autoRefreshInterval = getUrlParameter('refresh') || '30';
    const refreshInterval = parseInt(autoRefreshInterval) * 1000;

    if (refreshInterval > 0) {
        setInterval(() => {
            refreshHealthData();
        }, refreshInterval);
        showAutoRefreshIndicator();
    }
}

function showAutoRefreshIndicator() {
    const indicator = document.createElement('div');
    indicator.className = 'auto-refresh-indicator';
    indicator.innerHTML = '<i class="bi bi-arrow-clockwise"></i> 자동 새로고침';
    document.body.appendChild(indicator);
}

function initRealtimeMetrics() {
    const metricsContainer = document.getElementById('realtimeMetrics');
    if (metricsContainer) {
        updateRealtimeMetrics();
        setInterval(updateRealtimeMetrics, 5000);
    }
}

function updateRealtimeMetrics() {
    fetch('/health/metrics')
        .then(response => response.json())
        .then(data => {
            updateMetricsDisplay(data);
        })
        .catch(error => {
            console.error('메트릭스 업데이트 실패:', error);
        });
}

function updateMetricsDisplay(metrics) {
    const cpuElement = document.getElementById('cpuUsage');
    if (cpuElement && metrics.cpu) {
        cpuElement.textContent = metrics.cpu.toFixed(1) + '%';
        updateMetricStatus(cpuElement, metrics.cpu, 80, 60);
    }

    const memoryElement = document.getElementById('memoryUsage');
    if (memoryElement && metrics.memory) {
        memoryElement.textContent = metrics.memory.toFixed(1) + '%';
        updateMetricStatus(memoryElement, metrics.memory, 85, 70);
    }

    const diskElement = document.getElementById('diskUsage');
    if (diskElement && metrics.disk) {
        diskElement.textContent = metrics.disk.toFixed(1) + '%';
        updateMetricStatus(diskElement, metrics.disk, 90, 80);
    }

    const responseTimeElement = document.getElementById('responseTime');
    if (responseTimeElement && metrics.responseTime) {
        responseTimeElement.textContent = metrics.responseTime + 'ms';
        updateResponseTimeStatus(responseTimeElement, metrics.responseTime);
    }
}

function updateMetricStatus(element, value, warningThreshold, criticalThreshold) {
    element.className = 'metric-value';

    if (value >= criticalThreshold) {
        element.classList.add('critical');
    } else if (value >= warningThreshold) {
        element.classList.add('warning');
    } else {
        element.classList.add('normal');
    }
}

function updateResponseTimeStatus(element, value) {
    element.className = 'response-time';

    if (value > 1000) {
        element.classList.add('slow');
    } else if (value > 500) {
        element.classList.add('medium');
    } else {
        element.classList.add('fast');
    }
}

function initCharts() {
    if (typeof Chart !== 'undefined') {
        initCpuChart();
        initMemoryChart();
        initResponseTimeChart();
    }
}

function initCpuChart() {
    const ctx = document.getElementById('cpuChart');
    if (!ctx) return;

    const chart = new Chart(ctx, {
        type: 'line',
        data: {
            labels: [],
            datasets: [{
                label: 'CPU 사용률 (%)',
                data: [],
                borderColor: '#007bff',
                backgroundColor: 'rgba(0, 123, 255, 0.1)',
                tension: 0.4
            }]
        },
        options: {
            responsive: true,
            scales: {
                y: {
                    beginAtZero: true,
                    max: 100
                }
            }
        }
    });

    setInterval(() => {
        updateChartData(chart, getRandomValue(0, 100));
    }, 5000);
}

function initMemoryChart() {
    const ctx = document.getElementById('memoryChart');
    if (!ctx) return;

    const chart = new Chart(ctx, {
        type: 'line',
        data: {
            labels: [],
            datasets: [{
                label: '메모리 사용률 (%)',
                data: [],
                borderColor: '#28a745',
                backgroundColor: 'rgba(40, 167, 69, 0.1)',
                tension: 0.4
            }]
        },
        options: {
            responsive: true,
            scales: {
                y: {
                    beginAtZero: true,
                    max: 100
                }
            }
        }
    });

    setInterval(() => {
        updateChartData(chart, getRandomValue(0, 100));
    }, 5000);
}

function initResponseTimeChart() {
    const ctx = document.getElementById('responseTimeChart');
    if (!ctx) return;

    const chart = new Chart(ctx, {
        type: 'line',
        data: {
            labels: [],
            datasets: [{
                label: '응답 시간 (ms)',
                data: [],
                borderColor: '#ffc107',
                backgroundColor: 'rgba(255, 193, 7, 0.1)',
                tension: 0.4
            }]
        },
        options: {
            responsive: true,
            scales: {
                y: {
                    beginAtZero: true
                }
            }
        }
    });

    setInterval(() => {
        updateChartData(chart, getRandomValue(50, 500));
    }, 5000);
}

function updateChartData(chart, newValue) {
    const now = new Date();
    const timeLabel = now.toLocaleTimeString();

    chart.data.labels.push(timeLabel);
    chart.data.datasets[0].data.push(newValue);

    if (chart.data.labels.length > 20) {
        chart.data.labels.shift();
        chart.data.datasets[0].data.shift();
    }

    chart.update('none');
}

function getRandomValue(min, max) {
    return Math.random() * (max - min) + min;
}

function initFilters() {
    const filterButtons = document.querySelectorAll('.filter-btn');

    filterButtons.forEach(button => {
        button.addEventListener('click', function() {
            const filterType = this.getAttribute('data-filter');
            applyFilter(filterType);
        });
    });
}

function applyFilter(filterType) {
    const filterButtons = document.querySelectorAll('.filter-btn');

    filterButtons.forEach(btn => {
        btn.classList.remove('active');
    });

    const activeButton = document.querySelector(`[data-filter="${filterType}"]`);
    if (activeButton) {
        activeButton.classList.add('active');
    }

    const tableRows = document.querySelectorAll('tbody tr');
    tableRows.forEach(row => {
        const status = row.getAttribute('data-status');

        if (filterType === 'all' || status === filterType) {
            row.style.display = '';
        } else {
            row.style.display = 'none';
        }
    });
}

function initRefreshButton() {
    const refreshButton = document.getElementById('refreshButton');
    if (refreshButton) {
        refreshButton.addEventListener('click', function() {
            refreshHealthData();
        });
    }
}

function refreshHealthData() {
    const refreshButton = document.getElementById('refreshButton');
    if (refreshButton) {
        showLoading(refreshButton, '<i class="bi bi-arrow-clockwise"></i> 새로고침');
    }

    setTimeout(() => {
        window.location.reload();
    }, 1000);
}

function checkComponentStatus(componentId) {
    fetch(`/health/component/${componentId}`)
        .then(response => response.json())
        .then(data => {
            updateComponentStatus(componentId, data);
        })
        .catch(error => {
            console.error('컴포넌트 상태 확인 실패:', error);
            updateComponentStatus(componentId, { status: 'DOWN' });
        });
}

function updateComponentStatus(componentId, data) {
    const statusElement = document.querySelector(`[data-component="${componentId}"] .status-badge`);
    if (statusElement) {
        statusElement.className = `status-badge ${data.status.toLowerCase()}`;
        statusElement.textContent = data.status;
    }

    const responseTimeElement = document.querySelector(`[data-component="${componentId}"] .response-time`);
    if (responseTimeElement && data.responseTime) {
        responseTimeElement.textContent = data.responseTime + 'ms';
        updateResponseTimeStatus(responseTimeElement, data.responseTime);
    }
}

function checkAllComponents() {
    const components = document.querySelectorAll('[data-component]');
    components.forEach(component => {
        const componentId = component.getAttribute('data-component');
        checkComponentStatus(componentId);
    });
}

function loadHealthHistory() {
    fetch('/health/history')
        .then(response => response.json())
        .then(data => {
            updateHealthHistory(data);
        })
        .catch(error => {
            console.error('히스토리 로드 실패:', error);
        });
}

function updateHealthHistory(history) {
    const historyTable = document.getElementById('historyTable');
    if (!historyTable) return;

    const tbody = historyTable.querySelector('tbody');
    if (!tbody) return;

    tbody.innerHTML = '';

    history.forEach(record => {
        const row = document.createElement('tr');
        row.innerHTML = `
            <td>${formatDate(record.timestamp)}</td>
            <td><span class="status-badge ${record.status.toLowerCase()}">${record.status}</span></td>
            <td>${record.responseTime}ms</td>
            <td>${record.cpu}%</td>
            <td>${record.memory}%</td>
            <td>${record.disk}%</td>
        `;
        tbody.appendChild(row);
    });
}

function exportMetrics() {
    fetch('/health/metrics/export')
        .then(response => response.blob())
        .then(blob => {
            const url = window.URL.createObjectURL(blob);
            const a = document.createElement('a');
            a.href = url;
            a.download = `health-metrics-${formatDate(new Date(), 'YYYY-MM-DD')}.json`;
            document.body.appendChild(a);
            a.click();
            document.body.removeChild(a);
            window.URL.revokeObjectURL(url);
        })
        .catch(error => {
            console.error('메트릭스 내보내기 실패:', error);
            showAlert('메트릭스 내보내기에 실패했습니다.', 'danger');
        });
}

function setupHealthAlerts() {
    const alertThresholds = {
        cpu: 80,
        memory: 85,
        disk: 90,
        responseTime: 1000
    };

    setInterval(() => {
        checkHealthAlerts(alertThresholds);
    }, 30000);
}

function checkHealthAlerts(thresholds) {
    fetch('/health/metrics')
        .then(response => response.json())
        .then(data => {
            if (data.cpu > thresholds.cpu) {
                showAlert(`CPU 사용률이 높습니다: ${data.cpu.toFixed(1)}%`, 'warning');
            }

            if (data.memory > thresholds.memory) {
                showAlert(`메모리 사용률이 높습니다: ${data.memory.toFixed(1)}%`, 'warning');
            }

            if (data.disk > thresholds.disk) {
                showAlert(`디스크 사용률이 높습니다: ${data.disk.toFixed(1)}%`, 'danger');
            }

            if (data.responseTime > thresholds.responseTime) {
                showAlert(`응답 시간이 느립니다: ${data.responseTime}ms`, 'warning');
            }
        })
        .catch(error => {
            console.error('알림 확인 실패:', error);
        });
}

function initDashboardWidgets() {
    const widgets = document.querySelectorAll('.dashboard-widget');

    widgets.forEach(widget => {
        const widgetType = widget.getAttribute('data-widget');

        switch (widgetType) {
            case 'cpu':
                initCpuWidget(widget);
                break;
            case 'memory':
                initMemoryWidget(widget);
                break;
            case 'disk':
                initDiskWidget(widget);
                break;
            case 'response-time':
                initResponseTimeWidget(widget);
                break;
        }
    });
}

function initCpuWidget(widget) {
    console.log('CPU 위젯 초기화');
}

function initMemoryWidget(widget) {
    console.log('메모리 위젯 초기화');
}

function initDiskWidget(widget) {
    console.log('디스크 위젯 초기화');
}

function initResponseTimeWidget(widget) {
    console.log('응답 시간 위젯 초기화');
}

document.addEventListener('DOMContentLoaded', function() {
    initHealthPage();
    setupHealthAlerts();
    initDashboardWidgets();
    setInterval(checkAllComponents, 60000);
    loadHealthHistory();
});