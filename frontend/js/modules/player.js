import { API_BASE } from './utils.js';

let hls = null;

export function openPlayer(video) {
    const modal = document.getElementById('player-modal');
    const videoEl = document.getElementById('video');
    const titleEl = document.getElementById('player-title');
    const descEl = document.getElementById('player-desc');

    titleEl.textContent = video.title;
    descEl.textContent = video.description;
    modal.style.display = 'flex';

    // HLS Logic
    const hlsUrl = `${API_BASE}/api/videos/${video.uuid}`;

    if (Hls.isSupported()) {
        if (hls) {
            hls.destroy();
        }
        hls = new Hls();
        hls.loadSource(hlsUrl);
        hls.attachMedia(videoEl);
        hls.on(Hls.Events.MANIFEST_PARSED, function () {
            videoEl.play().catch(e => console.log('Autoplay blocked', e));
        });
    } else if (videoEl.canPlayType('application/vnd.apple.mpegurl')) {
        videoEl.src = hlsUrl;
        videoEl.addEventListener('loadedmetadata', function () {
            videoEl.play();
        });
    }
}

export function setupModal() {
    const modal = document.getElementById('player-modal');
    const closeBtn = document.querySelector('.close-btn');
    const videoEl = document.getElementById('video');

    const closeModal = () => {
        modal.style.display = 'none';
        videoEl.pause();
        if (hls) {
            hls.destroy();
            hls = null;
        }
    };

    closeBtn.onclick = closeModal;

    window.addEventListener('click', (event) => {
        if (event.target === modal) {
            closeModal();
        }
    });
}
