// API 基礎 URL
const API_BASE_URL = '/api/alerts';

// 頁面載入時執行
document.addEventListener('DOMContentLoaded', () => {
    loadStatistics();
    loadAlerts();
    
    // 綁定搜尋表單事件
    document.getElementById('search-form').addEventListener('submit', (e) => {
        e.preventDefault();
        searchAlerts();
    });
    
    // 綁定重置按鈕事件
    document.getElementById('reset-btn').addEventListener('click', () => {
        document.getElementById('search-form').reset();
        loadAlerts();
    });
    
    // 綁定模態框表單事件
    document.getElementById('update-form').addEventListener('submit', (e) => {
        e.preventDefault();
        submitUpdateForm();
    });
    
    // 點擊模態框背景關閉
    document.getElementById('alert-modal').addEventListener('click', (e) => {
        if (e.target.id === 'alert-modal') {
            closeModal();
        }
    });
});

/**
 * 載入統計資料
 */
async function loadStatistics() {
    try {
        const response = await fetch(`${API_BASE_URL}/statistics`);
        if (!response.ok) {
            throw new Error('載入統計資料失敗');
        }
        
        const stats = await response.json();
        
        // 更新統計卡片 - 使用後端實際回傳的欄位名稱
        document.getElementById('stat-total').textContent = stats.total || 0;
        document.getElementById('stat-pending').textContent = stats.pending || 0;
        document.getElementById('stat-resolved').textContent = stats.resolved || 0;
        document.getElementById('stat-false-positive').textContent = stats.falsePositive || 0;
        
    } catch (error) {
        console.error('載入統計資料錯誤:', error);
        showToast('載入統計資料失敗', 'error');
    }
}

/**
 * 載入警示列表
 */
async function loadAlerts(filters = null) {
    const loadingIndicator = document.getElementById('loading-indicator');
    const tableBody = document.getElementById('alerts-table-body');
    
    try {
        loadingIndicator.style.display = 'block';
        
        let response;
        if (filters) {
            // 使用搜尋 API
            response = await fetch(`${API_BASE_URL}/search`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(filters)
            });
        } else {
            // 取得所有警示
            response = await fetch(API_BASE_URL);
        }
        
        if (!response.ok) {
            throw new Error('載入警示列表失敗');
        }
        
        const alerts = await response.json();
        
        // 渲染表格
        if (alerts.length === 0) {
            tableBody.innerHTML = '<tr><td colspan="7" class="no-data">暫無資料</td></tr>';
        } else {
            tableBody.innerHTML = alerts.map(alert => `
                <tr>
                    <td>${alert.alertId}</td>
                    <td>${alert.transaction?.transactionId || 'N/A'}</td>
                    <td>${alert.alertType}</td>
                    <td>${getSeverityLabel(alert.severity)}</td>
                    <td>${getStatusBadge(alert.status)}</td>
                    <td>${formatDateTime(alert.detectedAt)}</td>
                    <td>
                        <div class="action-buttons">
                            <button class="btn btn-info" onclick="showAlertDetail(${alert.alertId})">
                                👁️ 查看
                            </button>
                            ${alert.status === 'PENDING' ? `
                                <button class="btn btn-success" onclick="markAsResolved(${alert.alertId})">
                                    ✅ 已處理
                                </button>
                                <button class="btn btn-warning" onclick="markAsFalsePositive(${alert.alertId})">
                                    ❌ 誤報
                                </button>
                            ` : ''}
                        </div>
                    </td>
                </tr>
            `).join('');
        }
        
    } catch (error) {
        console.error('載入警示列表錯誤:', error);
        showToast('載入警示列表失敗', 'error');
        tableBody.innerHTML = '<tr><td colspan="7" class="no-data">載入失敗</td></tr>';
    } finally {
        loadingIndicator.style.display = 'none';
    }
}

/**
 * 搜尋警示
 */
async function searchAlerts() {
    const form = document.getElementById('search-form');
    const formData = new FormData(form);
    
    // 建立篩選條件物件
    const filters = {};
    
    // 狀態 - 只有在有值時才加入
    const status = formData.get('status');
    if (status && status.trim() !== '') {
        filters.status = status;
    }
    
    // 嚴重程度 - 只有在有值時才加入
    const severity = formData.get('severity');
    if (severity && severity.trim() !== '') {
        filters.severity = severity;
    }
    
    // 警示類型 - 只有在有值時才加入
    const alertType = formData.get('alertType');
    if (alertType && alertType.trim() !== '') {
        filters.alertType = alertType;
    }
    
    // 開始時間
    const startDate = formData.get('startDate');
    if (startDate && startDate.trim() !== '') {
        filters.startDate = new Date(startDate).toISOString();
    }
    
    // 結束時間
    const endDate = formData.get('endDate');
    if (endDate && endDate.trim() !== '') {
        filters.endDate = new Date(endDate).toISOString();
    }
    
    // 如果沒有任何篩選條件，直接載入全部
    if (Object.keys(filters).length === 0) {
        loadAlerts();
    } else {
        loadAlerts(filters);
    }
}

/**
 * 顯示警示詳情
 */
async function showAlertDetail(alertId) {
    try {
        const response = await fetch(`${API_BASE_URL}/${alertId}`);
        if (!response.ok) {
            throw new Error('載入警示詳情失敗');
        }
        
        const alert = await response.json();
        
        // 填充詳情區域
        const detailsHtml = `
            <div class="detail-row">
                <div class="detail-label">警示 ID:</div>
                <div class="detail-value">${alert.alertId}</div>
            </div>
            <div class="detail-row">
                <div class="detail-label">交易 ID:</div>
                <div class="detail-value">${alert.transaction?.transactionId || 'N/A'}</div>
            </div>
            <div class="detail-row">
                <div class="detail-label">警示類型:</div>
                <div class="detail-value">${alert.alertType}</div>
            </div>
            <div class="detail-row">
                <div class="detail-label">嚴重程度:</div>
                <div class="detail-value">${getSeverityLabel(alert.severity)}</div>
            </div>
            <div class="detail-row">
                <div class="detail-label">狀態:</div>
                <div class="detail-value">${getStatusBadge(alert.status)}</div>
            </div>
            <div class="detail-row">
                <div class="detail-label">描述:</div>
                <div class="detail-value">${alert.description || '無'}</div>
            </div>
            <div class="detail-row">
                <div class="detail-label">偵測時間:</div>
                <div class="detail-value">${formatDateTime(alert.detectedAt)}</div>
            </div>
            ${alert.resolvedAt ? `
                <div class="detail-row">
                    <div class="detail-label">處理時間:</div>
                    <div class="detail-value">${formatDateTime(alert.resolvedAt)}</div>
                </div>
            ` : ''}
            ${alert.resolvedBy ? `
                <div class="detail-row">
                    <div class="detail-label">處理人員:</div>
                    <div class="detail-value">${alert.resolvedBy}</div>
                </div>
            ` : ''}
            ${alert.note ? `
                <div class="detail-row">
                    <div class="detail-label">備註:</div>
                    <div class="detail-value">${alert.note}</div>
                </div>
            ` : ''}
        `;
        
        document.getElementById('alert-details').innerHTML = detailsHtml;
        
        // 填充表單
        document.getElementById('modal-alert-id').value = alert.alertId;
        document.getElementById('modal-status').value = alert.status;
        document.getElementById('modal-resolved-by').value = alert.resolvedBy || '';
        document.getElementById('modal-note').value = alert.note || '';
        
        // 顯示模態框
        document.getElementById('alert-modal').classList.add('show');
        
    } catch (error) {
        console.error('載入警示詳情錯誤:', error);
        showToast('載入警示詳情失敗', 'error');
    }
}

/**
 * 關閉模態框
 */
function closeModal() {
    document.getElementById('alert-modal').classList.remove('show');
    document.getElementById('update-form').reset();
}

/**
 * 提交更新表單
 */
async function submitUpdateForm() {
    const alertId = document.getElementById('modal-alert-id').value;
    const status = document.getElementById('modal-status').value;
    const resolvedBy = document.getElementById('modal-resolved-by').value;
    const note = document.getElementById('modal-note').value;
    
    if (!status || !resolvedBy || !note) {
        showToast('請填寫所有必填欄位', 'error');
        return;
    }
    
    await updateAlertStatus(alertId, status, resolvedBy, note);
}

/**
 * 更新警示狀態
 */
async function updateAlertStatus(alertId, status, resolvedBy, note) {
    try {
        const response = await fetch(`${API_BASE_URL}/${alertId}/status`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                status: status,
                resolvedBy: resolvedBy,
                note: note
            })
        });
        
        if (!response.ok) {
            throw new Error('更新警示狀態失敗');
        }
        
        showToast('警示狀態更新成功', 'success');
        closeModal();
        
        // 重新載入資料
        loadStatistics();
        loadAlerts();
        
    } catch (error) {
        console.error('更新警示狀態錯誤:', error);
        showToast('更新警示狀態失敗', 'error');
    }
}

/**
 * 標記為已處理
 */
async function markAsResolved(alertId) {
    if (!confirm('確定要標記此警示為已處理嗎？')) {
        return;
    }
    
    const resolvedBy = prompt('請輸入處理人員姓名:');
    if (!resolvedBy) {
        showToast('請輸入處理人員姓名', 'error');
        return;
    }
    
    try {
        const response = await fetch(`${API_BASE_URL}/${alertId}/resolve`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                resolvedBy: resolvedBy
            })
        });
        
        if (!response.ok) {
            throw new Error('標記已處理失敗');
        }
        
        showToast('已成功標記為已處理', 'success');
        
        // 重新載入資料
        loadStatistics();
        loadAlerts();
        
    } catch (error) {
        console.error('標記已處理錯誤:', error);
        showToast('標記已處理失敗', 'error');
    }
}

/**
 * 標記為誤報
 */
async function markAsFalsePositive(alertId) {
    if (!confirm('確定要標記此警示為誤報嗎？')) {
        return;
    }
    
    const resolvedBy = prompt('請輸入處理人員姓名:');
    if (!resolvedBy) {
        showToast('請輸入處理人員姓名', 'error');
        return;
    }
    
    try {
        const response = await fetch(`${API_BASE_URL}/${alertId}/mark-false-positive`, {
            method: 'PUT',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                resolvedBy: resolvedBy
            })
        });
        
        if (!response.ok) {
            throw new Error('標記誤報失敗');
        }
        
        showToast('已成功標記為誤報', 'success');
        
        // 重新載入資料
        loadStatistics();
        loadAlerts();
        
    } catch (error) {
        console.error('標記誤報錯誤:', error);
        showToast('標記誤報失敗', 'error');
    }
}

/**
 * 格式化日期時間
 */
function formatDateTime(dateTimeString) {
    if (!dateTimeString) return '無';
    
    const date = new Date(dateTimeString);
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    const hours = String(date.getHours()).padStart(2, '0');
    const minutes = String(date.getMinutes()).padStart(2, '0');
    const seconds = String(date.getSeconds()).padStart(2, '0');
    
    return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
}

/**
 * 取得狀態徽章 HTML
 */
function getStatusBadge(status) {
    const statusMap = {
        'PENDING': { class: 'status-pending', text: '待處理' },
        'RESOLVED': { class: 'status-resolved', text: '已處理' },
        'FALSE_POSITIVE': { class: 'status-false-positive', text: '誤報' }
    };
    
    const statusInfo = statusMap[status] || { class: '', text: status };
    return `<span class="badge ${statusInfo.class}">${statusInfo.text}</span>`;
}

/**
 * 取得嚴重程度標籤 HTML
 */
function getSeverityLabel(severity) {
    const severityMap = {
        'HIGH': { class: 'severity-high', text: '高' },
        'MEDIUM': { class: 'severity-medium', text: '中' },
        'LOW': { class: 'severity-low', text: '低' }
    };
    
    const severityInfo = severityMap[severity] || { class: '', text: severity };
    return `<span class="severity-label ${severityInfo.class}">${severityInfo.text}</span>`;
}

/**
 * 顯示提示訊息
 */
function showToast(message, type = 'info') {
    const toast = document.getElementById('toast');
    toast.textContent = message;
    toast.className = `toast ${type} show`;
    
    setTimeout(() => {
        toast.classList.remove('show');
    }, 3000);
}

// Made with Bob
