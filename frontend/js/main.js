import { checkAuth, setupAuthModal } from './modules/auth.js';
import { fetchVideos } from './modules/video.js';
import { setupModal } from './modules/player.js';

document.addEventListener('DOMContentLoaded', () => {
    checkAuth();
    fetchVideos();
    setupModal();
    setupAuthModal();
});
