const API_BASE = window.location.origin + '/api';
let authToken = localStorage.getItem('authToken');
let currentUser = JSON.parse(localStorage.getItem('currentUser') || '{}');
let charts = {};

function showToast(message, type = 'success') {
    const container = document.getElementById('toast-container');
    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    toast.textContent = message;
    container.appendChild(toast);
    
    setTimeout(() => {
        toast.remove();
    }, 3000);
}

async function apiCall(endpoint, method = 'GET', body = null) {
    const options = {
        method,
        headers: {
            'Content-Type': 'application/json',
        }
    };
    
    if (authToken) {
        options.headers['Authorization'] = `Bearer ${authToken}`;
    }
    
    if (body) {
        options.body = JSON.stringify(body);
    }
    
    const response = await fetch(API_BASE + endpoint, options);
    const data = await response.json();
    
    if (!response.ok) {
        throw new Error(data.error || 'Request failed');
    }
    
    return data;
}

function showLogin() {
    document.getElementById('login-form').style.display = 'block';
    document.getElementById('register-form').style.display = 'none';
}

function showRegister() {
    document.getElementById('login-form').style.display = 'none';
    document.getElementById('register-form').style.display = 'block';
}

async function register() {
    const username = document.getElementById('register-username').value;
    const email = document.getElementById('register-email').value;
    const displayName = document.getElementById('register-displayname').value;
    const password = document.getElementById('register-password').value;
    
    if (!username || !email || !password) {
        showToast('All fields are required', 'error');
        return;
    }
    
    try {
        const data = await apiCall('/auth/register', 'POST', {
            username, email, displayName, password
        });
        
        authToken = data.token;
        currentUser = { username: data.username, displayName: data.displayName };
        localStorage.setItem('authToken', authToken);
        localStorage.setItem('currentUser', JSON.stringify(currentUser));
        
        showToast('Registration successful!');
        showApp();
    } catch (error) {
        showToast(error.message, 'error');
    }
}

async function login() {
    const username = document.getElementById('login-username').value;
    const password = document.getElementById('login-password').value;
    
    if (!username || !password) {
        showToast('Username and password are required', 'error');
        return;
    }
    
    try {
        const data = await apiCall('/auth/login', 'POST', { username, password });
        
        authToken = data.token;
        currentUser = { username: data.username, displayName: data.displayName };
        localStorage.setItem('authToken', authToken);
        localStorage.setItem('currentUser', JSON.stringify(currentUser));
        
        showToast('Login successful!');
        showApp();
    } catch (error) {
        showToast(error.message, 'error');
    }
}

function logout() {
    authToken = null;
    currentUser = {};
    localStorage.removeItem('authToken');
    localStorage.removeItem('currentUser');
    
    document.getElementById('auth-container').style.display = 'flex';
    document.getElementById('app-container').style.display = 'none';
    showToast('Logged out successfully');
}

function showApp() {
    document.getElementById('auth-container').style.display = 'none';
    document.getElementById('app-container').style.display = 'flex';
    document.getElementById('user-display-name').textContent = currentUser.displayName;
    
    const today = new Date().toISOString().split('T')[0];
    document.getElementById('steps-date').value = today;
    document.getElementById('meal-date').value = today;
    
    const now = new Date();
    const month = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`;
    document.getElementById('report-month').value = month;
    
    loadDashboard();
}

async function loadDashboard() {
    try {
        const summary = await apiCall('/summary');
        
        document.getElementById('today-steps').textContent = summary.todaySteps || 0;
        document.getElementById('today-calories').textContent = summary.todayCalories || 0;
        document.getElementById('current-streak').textContent = summary.currentStreak || 0;
        document.getElementById('longest-streak').textContent = summary.longestStreak || 0;
        
        updateProgress('steps-progress', 'steps-progress-text', summary.stepsProgress || 0);
        updateProgress('calories-progress', 'calories-progress-text', summary.caloriesProgress || 0);
        
        const suggestionsDiv = document.getElementById('ai-suggestions');
        suggestionsDiv.innerHTML = '';
        if (summary.aiSuggestions && summary.aiSuggestions.length > 0) {
            summary.aiSuggestions.forEach(suggestion => {
                const div = document.createElement('div');
                div.className = 'suggestion-item';
                div.textContent = suggestion;
                suggestionsDiv.appendChild(div);
            });
        }
        
        const mealsDiv = document.getElementById('today-meals-list');
        mealsDiv.innerHTML = '';
        if (summary.todayMeals && summary.todayMeals.length > 0) {
            summary.todayMeals.forEach(meal => {
                const div = document.createElement('div');
                div.className = 'meal-item';
                div.innerHTML = `<span>${meal.name}</span><span>${meal.calories} cal</span>`;
                mealsDiv.appendChild(div);
            });
        } else {
            mealsDiv.innerHTML = '<p style="color: #6b7280;">No meals logged today</p>';
        }
        
        const targets = await apiCall('/summary');
        document.getElementById('daily-steps-goal').value = targets.dailyStepsGoal || 10000;
        document.getElementById('weekly-steps-goal').value = targets.dailyStepsGoal * 7 || 70000;
        document.getElementById('daily-calories-goal').value = targets.dailyCaloriesGoal || 2000;
        document.getElementById('weekly-calories-goal').value = targets.dailyCaloriesGoal * 7 || 14000;
        
        checkReminders(summary);
    } catch (error) {
        showToast('Failed to load dashboard: ' + error.message, 'error');
    }
}

function updateProgress(elementId, textId, percentage) {
    document.getElementById(elementId).style.width = percentage + '%';
    document.getElementById(textId).textContent = percentage + '%';
}

function checkReminders(summary) {
    if (summary.stepsProgress < 50) {
        showToast('Don\'t forget to reach your step goal today!', 'error');
    }
}

async function saveQuickSteps() {
    const steps = parseInt(document.getElementById('quick-steps').value);
    
    if (!steps || steps < 0) {
        showToast('Please enter valid steps', 'error');
        return;
    }
    
    try {
        await apiCall('/entry', 'POST', {
            date: new Date().toISOString().split('T')[0],
            steps: steps
        });
        
        showToast('Steps saved successfully!');
        document.getElementById('quick-steps').value = '';
        loadDashboard();
    } catch (error) {
        showToast(error.message, 'error');
    }
}

async function saveQuickMeal() {
    const name = document.getElementById('quick-meal-name').value;
    const calories = parseInt(document.getElementById('quick-meal-calories').value);
    
    if (!name || !calories || calories < 0) {
        showToast('Please enter valid meal information', 'error');
        return;
    }
    
    try {
        await apiCall('/meal', 'POST', {
            date: new Date().toISOString().split('T')[0],
            name: name,
            calories: calories
        });
        
        showToast('Meal added successfully!');
        document.getElementById('quick-meal-name').value = '';
        document.getElementById('quick-meal-calories').value = '';
        loadDashboard();
    } catch (error) {
        showToast(error.message, 'error');
    }
}

async function saveSteps() {
    const date = document.getElementById('steps-date').value;
    const steps = parseInt(document.getElementById('steps-input').value);
    
    if (!steps || steps < 0) {
        showToast('Please enter valid steps', 'error');
        return;
    }
    
    try {
        await apiCall('/entry', 'POST', { date, steps });
        showToast('Steps saved successfully!');
        document.getElementById('steps-input').value = '';
        loadStepsCharts();
    } catch (error) {
        showToast(error.message, 'error');
    }
}

async function saveMeal() {
    const date = document.getElementById('meal-date').value;
    const name = document.getElementById('meal-name').value;
    const calories = parseInt(document.getElementById('meal-calories').value);
    
    if (!name || !calories || calories < 0) {
        showToast('Please enter valid meal information', 'error');
        return;
    }
    
    try {
        await apiCall('/meal', 'POST', { date, name, calories });
        showToast('Meal added successfully!');
        document.getElementById('meal-name').value = '';
        document.getElementById('meal-calories').value = '';
        loadCaloriesCharts();
    } catch (error) {
        showToast(error.message, 'error');
    }
}

async function loadStepsCharts() {
    try {
        const entries = await apiCall('/entries?days=14');
        const predictions = await apiCall('/predict?days=3');
        
        renderStepsChart(entries);
        renderPredictionChart(predictions, 'steps');
    } catch (error) {
        showToast('Failed to load charts: ' + error.message, 'error');
    }
}

async function loadCaloriesCharts() {
    try {
        const entries = await apiCall('/entries?days=14');
        const predictions = await apiCall('/predict?days=3');
        
        renderCaloriesChart(entries);
        renderPredictionChart(predictions, 'calories');
    } catch (error) {
        showToast('Failed to load charts: ' + error.message, 'error');
    }
}

function renderStepsChart(entries) {
    const ctx = document.getElementById('steps-chart');
    
    if (charts.stepsChart) {
        charts.stepsChart.destroy();
    }
    
    const labels = entries.map(e => new Date(e.date).toLocaleDateString()).reverse();
    const data = entries.map(e => e.steps).reverse();
    const colors = entries.map(e => e.isAnomaly ? '#ef4444' : '#667eea').reverse();
    
    charts.stepsChart = new Chart(ctx, {
        type: 'bar',
        data: {
            labels: labels,
            datasets: [{
                label: 'Steps',
                data: data,
                backgroundColor: colors,
                borderRadius: 8
            }]
        },
        options: {
            responsive: true,
            plugins: {
                legend: { display: false }
            }
        }
    });
}

function renderCaloriesChart(entries) {
    const ctx = document.getElementById('calories-chart');
    
    if (charts.caloriesChart) {
        charts.caloriesChart.destroy();
    }
    
    const labels = entries.map(e => new Date(e.date).toLocaleDateString()).reverse();
    const data = entries.map(e => e.calories).reverse();
    const colors = entries.map(e => e.isAnomaly ? '#ef4444' : '#10b981').reverse();
    
    charts.caloriesChart = new Chart(ctx, {
        type: 'bar',
        data: {
            labels: labels,
            datasets: [{
                label: 'Calories',
                data: data,
                backgroundColor: colors,
                borderRadius: 8
            }]
        },
        options: {
            responsive: true,
            plugins: {
                legend: { display: false }
            }
        }
    });
}

function renderPredictionChart(predictions, type) {
    const chartId = type === 'steps' ? 'steps-prediction-chart' : 'calories-prediction-chart';
    const ctx = document.getElementById(chartId);
    
    const chartKey = type === 'steps' ? 'stepsPredChart' : 'caloriesPredChart';
    if (charts[chartKey]) {
        charts[chartKey].destroy();
    }
    
    const labels = predictions.predictions.map(p => new Date(p.date).toLocaleDateString());
    const data = predictions.predictions.map(p => type === 'steps' ? p.steps : p.calories);
    
    charts[chartKey] = new Chart(ctx, {
        type: 'line',
        data: {
            labels: labels,
            datasets: [{
                label: `Predicted ${type}`,
                data: data,
                borderColor: type === 'steps' ? '#667eea' : '#10b981',
                backgroundColor: type === 'steps' ? 'rgba(102, 126, 234, 0.1)' : 'rgba(16, 185, 129, 0.1)',
                tension: 0.4,
                fill: true
            }]
        },
        options: {
            responsive: true,
            plugins: {
                legend: { display: true }
            }
        }
    });
}

async function loadHistory() {
    try {
        const entries = await apiCall('/entries?days=30');
        
        const historyDiv = document.getElementById('history-list');
        historyDiv.innerHTML = '';
        
        entries.forEach(entry => {
            const div = document.createElement('div');
            div.className = 'history-item' + (entry.isAnomaly ? ' anomaly' : '');
            div.innerHTML = `
                <div>
                    <strong>${new Date(entry.date).toLocaleDateString()}</strong>
                    ${entry.isAnomaly ? `<span style="color: #ef4444; margin-left: 10px;">⚠ ${entry.anomalyType}</span>` : ''}
                </div>
                <div>
                    <span style="margin-right: 15px;">👟 ${entry.steps} steps</span>
                    <span>🍽️ ${entry.calories} cal</span>
                </div>
            `;
            historyDiv.appendChild(div);
        });
    } catch (error) {
        showToast('Failed to load history: ' + error.message, 'error');
    }
}

async function loadLeaderboard() {
    try {
        const leaderboard = await apiCall('/leaderboard');
        
        const leaderboardDiv = document.getElementById('leaderboard-list');
        leaderboardDiv.innerHTML = '';
        
        leaderboard.forEach(user => {
            const div = document.createElement('div');
            div.className = 'leaderboard-item' + (user.isCurrentUser ? ' current-user' : '');
            div.innerHTML = `
                <div class="rank">#${user.rank}</div>
                <div class="user-info">
                    <strong>${user.displayName}</strong>
                    <div style="font-size: 12px; color: #6b7280;">🔥 ${user.currentStreak} day streak</div>
                </div>
                <div class="user-stats">
                    <div>${user.weeklySteps.toLocaleString()} steps</div>
                    <div style="font-size: 12px; color: #6b7280;">Today: ${user.todaySteps}</div>
                </div>
            `;
            leaderboardDiv.appendChild(div);
        });
        
        const activity = await apiCall('/friends-activity');
        const activityDiv = document.getElementById('friends-activity-list');
        activityDiv.innerHTML = '';
        
        if (activity.length === 0) {
            activityDiv.innerHTML = '<p style="color: #6b7280;">No friend activity today</p>';
        } else {
            activity.forEach(act => {
                const div = document.createElement('div');
                div.className = 'meal-item';
                div.innerHTML = `
                    <div>
                        <strong>${act.displayName}</strong>
                        <div style="font-size: 12px; color: #6b7280;">Today</div>
                    </div>
                    <div style="text-align: right;">
                        <div>👟 ${act.steps} steps</div>
                        <div>🍽️ ${act.calories} cal</div>
                    </div>
                `;
                activityDiv.appendChild(div);
            });
        }
    } catch (error) {
        showToast('Failed to load leaderboard: ' + error.message, 'error');
    }
}

let searchTimeout;
document.addEventListener('DOMContentLoaded', () => {
    const searchInput = document.getElementById('friend-search');
    if (searchInput) {
        searchInput.addEventListener('input', (e) => {
            clearTimeout(searchTimeout);
            searchTimeout = setTimeout(() => searchFriends(e.target.value), 300);
        });
    }
});

async function searchFriends(query) {
    if (!query || query.length < 2) {
        document.getElementById('friend-search-results').innerHTML = '';
        return;
    }
    
    try {
        const users = await apiCall(`/users/search?q=${query}`);
        
        const resultsDiv = document.getElementById('friend-search-results');
        resultsDiv.innerHTML = '';
        
        users.forEach(user => {
            if (user.username === currentUser.username) return;
            
            const div = document.createElement('div');
            div.className = 'friend-result';
            div.innerHTML = `
                <span>${user.displayName} (@${user.username})</span>
                <button onclick="addFriend('${user.username}')">Add Friend</button>
            `;
            resultsDiv.appendChild(div);
        });
    } catch (error) {
        showToast('Search failed: ' + error.message, 'error');
    }
}

async function addFriend(friendUsername) {
    try {
        await apiCall('/friends', 'POST', { friendUsername });
        showToast('Friend added successfully!');
        document.getElementById('friend-search').value = '';
        document.getElementById('friend-search-results').innerHTML = '';
        loadLeaderboard();
    } catch (error) {
        showToast(error.message, 'error');
    }
}

async function updateTargets() {
    const dailyStepsGoal = parseInt(document.getElementById('daily-steps-goal').value);
    const weeklyStepsGoal = parseInt(document.getElementById('weekly-steps-goal').value);
    const dailyCaloriesGoal = parseInt(document.getElementById('daily-calories-goal').value);
    const weeklyCaloriesGoal = parseInt(document.getElementById('weekly-calories-goal').value);
    
    if (dailyStepsGoal < 0 || weeklyStepsGoal < 0 || dailyCaloriesGoal < 0 || weeklyCaloriesGoal < 0) {
        showToast('Goals must be positive numbers', 'error');
        return;
    }
    
    try {
        await apiCall('/targets', 'PUT', {
            dailyStepsGoal,
            weeklyStepsGoal,
            dailyCaloriesGoal,
            weeklyCaloriesGoal
        });
        
        showToast('Goals updated successfully!');
        loadDashboard();
    } catch (error) {
        showToast(error.message, 'error');
    }
}

async function generateReport() {
    const monthInput = document.getElementById('report-month').value;
    const [year, month] = monthInput.split('-');
    
    try {
        const report = await apiCall(`/monthly-report?year=${year}&month=${month}`);
        
        const container = document.getElementById('report-container');
        container.innerHTML = `
            <div class="card">
                <h3>Monthly Report - ${month}/${year}</h3>
                <div class="report-stats">
                    <div class="report-stat">
                        <h4>Total Steps</h4>
                        <p>${report.totalSteps.toLocaleString()}</p>
                    </div>
                    <div class="report-stat">
                        <h4>Average Steps</h4>
                        <p>${report.avgSteps.toLocaleString()}</p>
                    </div>
                    <div class="report-stat">
                        <h4>Total Calories</h4>
                        <p>${report.totalCalories.toLocaleString()}</p>
                    </div>
                    <div class="report-stat">
                        <h4>Average Calories</h4>
                        <p>${report.avgCalories.toLocaleString()}</p>
                    </div>
                    <div class="report-stat">
                        <h4>Active Days</h4>
                        <p>${report.activeDays}</p>
                    </div>
                    <div class="report-stat">
                        <h4>Anomalies Detected</h4>
                        <p>${report.anomalies}</p>
                    </div>
                </div>
                
                <h3>AI Recommendations</h3>
                <div id="report-suggestions"></div>
                
                <div class="download-buttons">
                    <button onclick="downloadReportPDF(${JSON.stringify(report).replace(/"/g, '&quot;')})">Download PDF</button>
                    <button onclick="downloadReportCSV(${JSON.stringify(report).replace(/"/g, '&quot;')})">Download CSV</button>
                </div>
            </div>
            
            <div class="card">
                <h3>Monthly Activity Chart</h3>
                <canvas id="monthly-chart"></canvas>
            </div>
        `;
        
        const suggestionsDiv = document.getElementById('report-suggestions');
        report.aiSuggestions.forEach(suggestion => {
            const div = document.createElement('div');
            div.className = 'suggestion-item';
            div.textContent = suggestion;
            suggestionsDiv.appendChild(div);
        });
        
        renderMonthlyChart(report.entries);
        showToast('Report generated successfully!');
    } catch (error) {
        showToast('Failed to generate report: ' + error.message, 'error');
    }
}

function renderMonthlyChart(entries) {
    const ctx = document.getElementById('monthly-chart');
    
    if (charts.monthlyChart) {
        charts.monthlyChart.destroy();
    }
    
    const labels = entries.map(e => new Date(e.date).toLocaleDateString());
    const stepsData = entries.map(e => e.steps);
    const caloriesData = entries.map(e => e.calories);
    
    charts.monthlyChart = new Chart(ctx, {
        type: 'line',
        data: {
            labels: labels,
            datasets: [
                {
                    label: 'Steps',
                    data: stepsData,
                    borderColor: '#667eea',
                    backgroundColor: 'rgba(102, 126, 234, 0.1)',
                    tension: 0.4,
                    yAxisID: 'y'
                },
                {
                    label: 'Calories',
                    data: caloriesData,
                    borderColor: '#10b981',
                    backgroundColor: 'rgba(16, 185, 129, 0.1)',
                    tension: 0.4,
                    yAxisID: 'y1'
                }
            ]
        },
        options: {
            responsive: true,
            plugins: {
                legend: { display: true }
            },
            scales: {
                y: { type: 'linear', position: 'left' },
                y1: { type: 'linear', position: 'right', grid: { drawOnChartArea: false } }
            }
        }
    });
}

function downloadReportPDF(report) {
    const { jsPDF } = window.jspdf;
    const doc = new jsPDF();
    
    doc.setFontSize(18);
    doc.text(`Monthly Report - ${report.month}/${report.year}`, 20, 20);
    
    doc.setFontSize(12);
    doc.text(`Total Steps: ${report.totalSteps.toLocaleString()}`, 20, 40);
    doc.text(`Average Steps: ${report.avgSteps.toLocaleString()}`, 20, 50);
    doc.text(`Total Calories: ${report.totalCalories.toLocaleString()}`, 20, 60);
    doc.text(`Average Calories: ${report.avgCalories.toLocaleString()}`, 20, 70);
    doc.text(`Active Days: ${report.activeDays}`, 20, 80);
    doc.text(`Current Streak: ${report.currentStreak} days`, 20, 90);
    
    doc.text('AI Recommendations:', 20, 110);
    let y = 120;
    report.aiSuggestions.forEach((suggestion, i) => {
        doc.text(`${i + 1}. ${suggestion}`, 20, y);
        y += 10;
    });
    
    doc.save(`health-report-${report.month}-${report.year}.pdf`);
    showToast('PDF downloaded successfully!');
}

function downloadReportCSV(report) {
    const csv = Papa.unparse(report.entries.map(e => ({
        Date: e.date,
        Steps: e.steps,
        Calories: e.calories,
        Anomaly: e.isAnomaly ? 'Yes' : 'No',
        'Anomaly Type': e.anomalyType || 'N/A'
    })));
    
    const blob = new Blob([csv], { type: 'text/csv' });
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `health-report-${report.month}-${report.year}.csv`;
    a.click();
    showToast('CSV downloaded successfully!');
}

function showSection(section) {
    document.querySelectorAll('.section').forEach(s => s.classList.remove('active'));
    document.querySelectorAll('.nav-menu a').forEach(a => a.classList.remove('active'));
    
    document.getElementById(section + '-section').classList.add('active');
    event.target.classList.add('active');
    
    switch(section) {
        case 'dashboard':
            loadDashboard();
            break;
        case 'steps':
            loadStepsCharts();
            break;
        case 'calories':
            loadCaloriesCharts();
            break;
        case 'history':
            loadHistory();
            break;
        case 'friends':
            loadLeaderboard();
            break;
    }
}

if (authToken) {
    showApp();
}
