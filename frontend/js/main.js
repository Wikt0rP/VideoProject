import { checkAuth, setupAuthModal } from './modules/auth.js';
import { fetchVideos } from './modules/video.js';
import { setupModal } from './modules/player.js';
import { setupUploadModal } from './modules/upload.js';
import { setupProfileListeners, loadMyVideos } from './modules/profile.js';

document.addEventListener('DOMContentLoaded', () => {
    checkAuth();
    fetchVideos();
    setupModal();
    setupAuthModal();
    setupUploadModal();
    setupProfileListeners();

    // Sidebar Toggle
    const menuBtn = document.getElementById('menu-btn');
    const sidebar = document.getElementById('sidebar');

    if (menuBtn && sidebar) {
        menuBtn.addEventListener('click', () => {
            sidebar.classList.toggle('collapsed');
        });
    }

    // Navigation
    const navHome = document.getElementById('nav-home');
    const navMyVideos = document.getElementById('nav-my-videos');

    if (navHome) {
        navHome.addEventListener('click', (e) => {
            e.preventDefault();
            fetchVideos();
        });
    }

    if (navMyVideos) {
        navMyVideos.addEventListener('click', (e) => {
            e.preventDefault();
            loadMyVideos();
        });
    }
});
