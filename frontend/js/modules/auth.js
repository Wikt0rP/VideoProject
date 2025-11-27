import { API_BASE } from './utils.js';
import { loadMyVideos } from './profile.js';

export async function checkAuth() {
    const userStr = localStorage.getItem('user');
    if (!userStr) {
        renderAuth(null);
        return;
    }

    const user = JSON.parse(userStr);

    // Verify token with backend
    try {
        const response = await fetch(`${API_BASE}/api/videos/recent?page=0&size=1`, {
            headers: {
                'Authorization': `Bearer ${user.token}`
            }
        });

        // If backend is down or token is invalid, logout
        if (!response.ok) {
            console.warn('Token validation failed, logging out');
            localStorage.removeItem('user');
            renderAuth(null);
            return;
        }

        // Token is valid, render as logged in
        renderAuth(user);
    } catch (error) {
        // Backend is unreachable, logout
        console.warn('Backend unreachable, logging out:', error);
        localStorage.removeItem('user');
        renderAuth(null);
    }
}

export function renderAuth(user) {
    const container = document.getElementById('auth-container');
    const uploadBtn = document.getElementById('upload-btn');

    if (user) {
        container.innerHTML = `
            <div class="user-dropdown">
                <div class="user-profile" id="user-profile-btn">
                    <div class="user-avatar">${user.name.charAt(0)}</div>
                    <div class="user-name">${user.name}</div>
                </div>
                <div class="user-menu" id="user-menu">
                    <div class="user-menu-item" id="profile-btn">
                        <svg viewBox="0 0 24 24"><path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 3c1.66 0 3 1.34 3 3s-1.34 3-3 3-3-1.34-3-3 1.34-3 3-3zm0 14.2c-2.5 0-4.71-1.28-6-3.22.03-1.99 4-3.08 6-3.08 1.99 0 5.97 1.09 6 3.08-1.29 1.94-3.5 3.22-6 3.22z"/></svg>
                        Profile
                    </div>
                    <div class="user-menu-item" id="logout-btn">
                        <svg viewBox="0 0 24 24"><path d="M17 7l-1.41 1.41L18.17 11H8v2h10.17l-2.58 2.58L17 17l5-5zM4 5h8V3H4c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h8v-2H4V5z"/></svg>
                        Logout
                    </div>
                </div>
            </div>
        `;

        // Toggle dropdown
        document.getElementById('user-profile-btn').addEventListener('click', (e) => {
            e.stopPropagation();
            document.getElementById('user-menu').classList.toggle('show');
        });

        // Close dropdown when clicking outside
        document.addEventListener('click', () => {
            const menu = document.getElementById('user-menu');
            if (menu) menu.classList.remove('show');
        });

        // Profile button (placeholder for now)
        document.getElementById('profile-btn').addEventListener('click', () => {
            loadMyVideos();
            // Close menu
            document.getElementById('user-menu').classList.remove('show');
        });

        // Logout button
        document.getElementById('logout-btn').addEventListener('click', logout);

        if (uploadBtn) uploadBtn.style.display = 'flex';
    } else {
        container.innerHTML = `
            <button class="sign-in-btn" id="login-btn">
                <svg height="24" viewBox="0 0 24 24" width="24" focusable="false" style="pointer-events: none; display: block; width: 24px; height: 24px; fill: currentColor;"><path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 1c4.96 0 9 4.04 9 9 0 1.42-.34 2.76-.93 3.96-1.53-1.72-3.98-2.89-7.38-3.03A3.996 3.996 0 0016 9c0-2.21-1.79-4-4-4S8 6.79 8 9c0 1.84 1.27 3.37 3.03 3.86-3.61.26-6.23 1.56-7.63 3.5C2.67 15.15 2.25 13.62 2.25 12 2.25 7.04 6.29 3 11.25 3z"></path></svg>
                Login
            </button>
        `;
        document.getElementById('login-btn').addEventListener('click', openAuthModal);
        if (uploadBtn) uploadBtn.style.display = 'none';
    }
}

export function logout() {
    localStorage.removeItem('user');
    renderAuth(null);
}

export function openAuthModal() {
    const modal = document.getElementById('auth-modal');
    modal.style.display = 'flex';
    switchAuthTab('login');
}

export function closeAuthModal() {
    document.getElementById('auth-modal').style.display = 'none';
}

export function switchAuthTab(tab) {
    const loginForm = document.getElementById('login-form');
    const registerForm = document.getElementById('register-form');
    const tabs = document.querySelectorAll('.auth-tab');

    if (tab === 'login') {
        loginForm.style.display = 'flex';
        registerForm.style.display = 'none';
        tabs[0].classList.add('active');
        tabs[1].classList.remove('active');
    } else {
        loginForm.style.display = 'none';
        registerForm.style.display = 'flex';
        tabs[0].classList.remove('active');
        tabs[1].classList.add('active');
    }
}

async function authRequest(endpoint, body) {
    try {
        const response = await fetch(`${API_BASE}${endpoint}`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(body)
        });

        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(errorText || 'Request failed');
        }

        return await response.json();
    } catch (error) {
        throw error;
    }
}

export async function handleLogin() {
    const username = document.getElementById('login-username').value;
    const password = document.getElementById('login-password').value;

    if (!username || !password) return alert('Please enter username and password');

    try {
        const data = await authRequest('/user/auth/login', { username, password });

        const user = {
            name: data.username,
            token: data.token,
            id: data.id,
            roles: data.roles
        };

        localStorage.setItem('user', JSON.stringify(user));
        renderAuth(user);
        closeAuthModal();
    } catch (error) {
        console.error('Login error:', error);
        alert(error.message);
    }
}

export async function handleRegister() {
    const username = document.getElementById('register-username').value;
    const password = document.getElementById('register-password').value;

    if (!username || !password) return alert('Please enter username and password');

    try {
        await authRequest('/user/auth/register', { username, password });

        alert('Registration successful! Please login.');
        switchAuthTab('login');
    } catch (error) {
        console.error('Registration error:', error);
        alert(error.message);
    }
}

export function setupAuthModal() {
    const modal = document.getElementById('auth-modal');

    // Close on click outside
    window.addEventListener('click', (event) => {
        if (event.target === modal) {
            closeAuthModal();
        }
    });

    // Attach event listeners to static elements (tabs, buttons)
    // Note: We need to be careful not to duplicate listeners if this is called multiple times.
    // Ideally, we attach these once in main.js or here.

    document.querySelectorAll('.auth-tab')[0].addEventListener('click', () => switchAuthTab('login'));
    document.querySelectorAll('.auth-tab')[1].addEventListener('click', () => switchAuthTab('register'));

    document.querySelector('#login-form .auth-submit-btn').addEventListener('click', handleLogin);
    document.querySelector('#login-form .auth-back-btn').addEventListener('click', closeAuthModal);

    document.querySelector('#register-form .auth-submit-btn').addEventListener('click', handleRegister);
    document.querySelector('#register-form .auth-back-btn').addEventListener('click', closeAuthModal);
}
