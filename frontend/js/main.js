import { checkAuth, setupAuthModal } from './modules/auth.js';
import { fetchVideos } from './modules/video.js';
import { setupModal } from './modules/player.js';
import { setupUploadModal } from './modules/upload.js';

document.addEventListener('DOMContentLoaded', () => {
    checkAuth();
    fetchVideos();
    setupModal();
    setupAuthModal();
    setupUploadModal();

    // Sidebar Toggle
    const menuBtn = document.getElementById('menu-btn');
    const sidebar = document.getElementById('sidebar');

    if (menuBtn && sidebar) {
        menuBtn.addEventListener('click', () => {
            sidebar.classList.toggle('collapsed');
        });
    }
});
