const API_BASE = 'http://localhost:8080';

document.addEventListener('DOMContentLoaded', () => {
    fetchVideos();
    setupModal();
});

async function fetchVideos() {
    try {
        const response = await fetch(`${API_BASE}/api/videos/recent`);
        if (!response.ok) throw new Error('Failed to fetch videos');

        const data = await response.json();
        const videos = data.content || []; // Assuming Page<Video> returns content array

        renderVideos(videos);
    } catch (error) {
        console.error('Error loading videos:', error);
        renderPlaceholders();
    }
}

function renderPlaceholders() {
    const grid = document.getElementById('video-grid');
    grid.innerHTML = '';

    // Create 8 placeholder items
    for (let i = 0; i < 8; i++) {
        const card = document.createElement('div');
        card.className = 'video-card placeholder-card';

        card.innerHTML = `
            <div class="thumbnail-container placeholder-animate"></div>
            <div class="info">
                <div class="title placeholder-text placeholder-animate" style="width: 80%;"></div>
                <div class="desc placeholder-text placeholder-animate" style="width: 60%;"></div>
            </div>
        `;

        grid.appendChild(card);
    }
}

function renderVideos(videos) {
    const grid = document.getElementById('video-grid');
    grid.innerHTML = '';

    videos.forEach(video => {
        const card = document.createElement('div');
        card.className = 'video-card';
        card.onclick = () => openPlayer(video);

        // Thumbnail logic: Try to load, fallback to placeholder
        // Since backend returns absolute paths, we can't load them directly.
        // We will just show the placeholder as per current constraints, 
        // unless there is a valid URL we can guess.
        // For now, we assume we can't load local files.

        const placeholderHtml = `
            <div class="thumbnail-placeholder">
                <span>No Thumbnail</span>
            </div>
        `;

        card.innerHTML = `
            <div class="thumbnail-container">
                ${placeholderHtml} 
                <!-- If we had a real URL, we would use an img tag here with onerror -->
            </div>
            <div class="info">
                <div class="title">${escapeHtml(video.title || 'Untitled')}</div>
                <div class="desc">${escapeHtml(video.description || 'No description')}</div>
            </div>
        `;

        grid.appendChild(card);
    });
}

let hls = null;

function openPlayer(video) {
    const modal = document.getElementById('player-modal');
    const videoEl = document.getElementById('video');
    const titleEl = document.getElementById('player-title');
    const descEl = document.getElementById('player-desc');

    titleEl.textContent = video.title;
    descEl.textContent = video.description;
    modal.style.display = 'flex';

    // HLS Logic
    const hlsUrl = `${API_BASE}/api/videos/${video.uuid}`; // Using the endpoint that returns the m3u8 file

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

function setupModal() {
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

    window.onclick = (event) => {
        if (event.target === modal) {
            closeModal();
        }
    };
}

function escapeHtml(text) {
    if (!text) return '';
    return text
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#039;");
}

// Test function to render mock data
function renderMockData() {
    const adjectives = ['Amazing', 'Incredible', 'Funny', 'Serious', 'Educational', 'Random', 'Dark', 'Bright'];
    const nouns = ['Video', 'Tutorial', 'Clip', 'Movie', 'Review', 'Vlog', 'Stream', 'Show'];

    const mocks = Array.from({ length: 8 }, (_, i) => {
        const title = `${adjectives[Math.floor(Math.random() * adjectives.length)]} ${nouns[Math.floor(Math.random() * nouns.length)]} #${i + 1}`;
        const desc = `This is a random description for testing fonts and layout. It contains some sample text to verify how the card looks with content.`;

        return {
            uuid: `mock-${i}`,
            title: title,
            description: desc
        };
    });

    renderVideos(mocks);
}

// Expose to window for console usage
window.renderMockData = renderMockData;
