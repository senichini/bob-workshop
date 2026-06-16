// API 基礎 URL
const API_BASE = '/api/transactions';
const ALERT_API_BASE = '/api/alerts';

// 頁面載入時初始化
document.addEventListener('DOMContentLoaded', () => {
  loadStatistics();
  loadAllTransactions();
  loadAllAlerts();
});

// 載入統計資料
async function loadStatistics() {
  try {
    const response = await fetch(`${API_BASE}/statistics`);
    const data = await response.json();
    
    document.getElementById('totalTransactions').textContent = data.totalTransactions;
    document.getElementById('totalAmount').textContent = formatCurrency(data.totalAmount);
    document.getElementById('approvedCount').textContent = data.approvedCount;
    document.getElementById('averageAmount').textContent = formatCurrency(data.averageAmount);
  } catch (error) {
    console.error('載入統計資料失敗:', error);
    showError('無法載入統計資料');
  }
}

// 載入所有交易
async function loadAllTransactions() {
  setActiveButton(0);
  try {
    const response = await fetch(API_BASE);
    const transactions = await response.json();
    displayTransactions(transactions);
  } catch (error) {
    console.error('載入交易失敗:', error);
    showError('無法載入交易資料');
  }
}

// 載入最近24小時交易
async function loadRecentTransactions() {
  setActiveButton(1);
  try {
    const response = await fetch(`${API_BASE}/recent?hours=24`);
    const transactions = await response.json();
    displayTransactions(transactions);
  } catch (error) {
    console.error('載入最近交易失敗:', error);
    showError('無法載入最近交易');
  }
}

// 載入高額交易
async function loadHighAmountTransactions() {
  setActiveButton(2);
  try {
    const response = await fetch(`${API_BASE}/high-amount?threshold=50000`);
    const transactions = await response.json();
    displayTransactions(transactions);
  } catch (error) {
    console.error('載入高額交易失敗:', error);
    showError('無法載入高額交易');
  }
}

// 顯示交易列表
function displayTransactions(transactions) {
  const tbody = document.getElementById('transactionsBody');
  
  if (transactions.length === 0) {
    tbody.innerHTML = '<tr><td colspan="7" class="loading">查無資料</td></tr>';
    return;
  }
  
  tbody.innerHTML = transactions.map(tx => `
    <tr>
      <td>${tx.id}</td>
      <td><code>${tx.card.maskedCardNumber}</code></td>
      <td>${tx.card.cardholderName}</td>
      <td>${tx.merchant.merchantName}</td>
      <td class="${tx.amount > 50000 ? 'amount amount-high' : 'amount'}">
        ${formatCurrency(tx.amount)}
      </td>
      <td>
        <span class="status-badge-table status-${tx.status}">
          ${getStatusText(tx.status)}
        </span>
      </td>
      <td>${formatDateTime(tx.transactionTime)}</td>
    </tr>
  `).join('');
}

// 設定按鈕啟用狀態
function setActiveButton(index) {
  const buttons = document.querySelectorAll('.filter-btn');
  buttons.forEach((btn, i) => {
    if (i === index) {
      btn.classList.add('active');
    } else {
      btn.classList.remove('active');
    }
  });
}

// 格式化金額
function formatCurrency(amount) {
  return new Intl.NumberFormat('zh-TW', {
    style: 'currency',
    currency: 'TWD',
    minimumFractionDigits: 0,
    maximumFractionDigits: 0
  }).format(amount);
}

// 格式化日期時間
function formatDateTime(dateTimeString) {
  const date = new Date(dateTimeString);
  return new Intl.DateTimeFormat('zh-TW', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
    hour12: false
  }).format(date);
}

// 取得狀態文字
function getStatusText(status) {
  const statusMap = {
    'APPROVED': '已核准',
    'PENDING': '處理中',
    'DECLINED': '已拒絕',
    'CANCELLED': '已取消',
    'REFUNDED': '已退款'
  };
  return statusMap[status] || status;
}

// 顯示錯誤訊息
function showError(message) {
  const tbody = document.getElementById('transactionsBody');
  tbody.innerHTML = `
    <tr>
      <td colspan="7" style="color: var(--red); text-align: center;">
        ⚠️ ${message}
      </td>
    </tr>
  `;
}

// ==================== 警示相關功能 ====================

// 載入所有警示
async function loadAllAlerts() {
  setActiveAlertButton(0);
  try {
    const response = await fetch(ALERT_API_BASE);
    const alerts = await response.json();
    displayAlerts(alerts);
  } catch (error) {
    console.error('載入警示失敗:', error);
    showAlertError('無法載入警示資料');
  }
}

// 載入高風險警示
async function loadHighRiskAlerts() {
  setActiveAlertButton(1);
  try {
    const response = await fetch(`${ALERT_API_BASE}/high-risk`);
    const alerts = await response.json();
    displayAlerts(alerts);
  } catch (error) {
    console.error('載入高風險警示失敗:', error);
    showAlertError('無法載入高風險警示');
  }
}

// 載入未處理警示
async function loadUnresolvedAlerts() {
  setActiveAlertButton(2);
  try {
    const response = await fetch(`${ALERT_API_BASE}/unresolved`);
    const alerts = await response.json();
    displayAlerts(alerts);
  } catch (error) {
    console.error('載入未處理警示失敗:', error);
    showAlertError('無法載入未處理警示');
  }
}

// 顯示警示列表
function displayAlerts(alerts) {
  const tbody = document.getElementById('alertsBody');
  
  if (alerts.length === 0) {
    tbody.innerHTML = '<tr><td colspan="6" class="loading">查無警示資料</td></tr>';
    return;
  }
  
  tbody.innerHTML = alerts.map(alert => `
    <tr>
      <td>${alert.alertId}</td>
      <td>${alert.transaction.id}</td>
      <td><span class="alert-type">${getAlertTypeText(alert.alertType)}</span></td>
      <td>
        <span class="severity-badge severity-${alert.severity.toLowerCase()}">
          ${getSeverityText(alert.severity)}
        </span>
      </td>
      <td class="alert-description">${alert.description}</td>
      <td>${formatDateTime(alert.detectedAt)}</td>
    </tr>
  `).join('');
}

// 設定警示按鈕啟用狀態
function setActiveAlertButton(index) {
  const buttons = document.querySelectorAll('.filter-btn');
  // 警示區塊的按鈕從第4個開始（前3個是交易篩選按鈕）
  const alertButtons = Array.from(buttons).slice(3, 6);
  alertButtons.forEach((btn, i) => {
    if (i === index) {
      btn.classList.add('active');
    } else {
      btn.classList.remove('active');
    }
  });
}

// 取得警示類型文字
function getAlertTypeText(alertType) {
  const typeMap = {
    'HIGH_AMOUNT': '高額交易',
    'FREQUENT_TRANSACTIONS': '頻繁交易',
    'DUPLICATE_TRANSACTION': '重複交易',
    'SUSPICIOUS_MERCHANT': '可疑商店',
    'UNUSUAL_TIME': '異常時段',
    'CROSS_BORDER': '跨境交易'
  };
  return typeMap[alertType] || alertType;
}

// 手動觸發異常偵測
async function triggerDetection() {
  try {
    const response = await fetch(`${ALERT_API_BASE}/detect`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      }
    });
    
    if (!response.ok) {
      throw new Error('偵測失敗');
    }
    
    const result = await response.json();
    
    // 顯示偵測結果
    alert(`異常偵測完成！\n\n` +
          `總交易數: ${result.totalTransactions}\n` +
          `偵測到警示: ${result.totalAlerts} 個\n\n` +
          `- 高額交易: ${result.highAmountAlerts} 個\n` +
          `- 頻繁交易: ${result.frequentAlerts} 個\n` +
          `- 重複交易: ${result.duplicateAlerts} 個`);
    
    // 重新載入警示列表
    loadAllAlerts();
  } catch (error) {
    console.error('觸發偵測失敗:', error);
    alert('⚠️ 觸發偵測失敗，請稍後再試');
  }
}

// 取得風險等級文字
function getSeverityText(severity) {
  const severityMap = {
    'HIGH': '高風險',
    'MEDIUM': '中風險',
    'LOW': '低風險'
  };
  return severityMap[severity] || severity;
}

// 顯示警示錯誤訊息
function showAlertError(message) {
  const tbody = document.getElementById('alertsBody');
  tbody.innerHTML = `
    <tr>
      <td colspan="6" style="color: var(--red); text-align: center;">
        ⚠️ ${message}
      </td>
    </tr>
  `;
}

// Made with Bob
