import { API_BASE, escapeHtml } from './utils.js';
import { openPlayer } from './player.js';

export async function fetchVideos() {
    renderPlaceholders();
    try {
        const response = await fetch(`${API_BASE}/api/videos/recent`);
        if (!response.ok) throw new Error('Failed to fetch videos');

        const data = await response.json();
        const videos = data.content || [];

        if (videos.length === 0) {
            renderEmpty();
        } else {
            renderVideos(videos);
        }
    } catch (error) {
        console.error('Error loading videos:', error);
        renderError();
    }
}

function renderPlaceholders() {
    const grid = document.getElementById('video-grid');
    grid.innerHTML = '';

    for (let i = 0; i < 8; i++) {
        const card = document.createElement('div');
        card.className = 'video-card placeholder-card';
        card.innerHTML = `
            <div class="thumbnail-container placeholder-animate"></div>
            <div class="info">
                <div class="title placeholder-text placeholder-animate" style="width: 80%;"></div>
            </div>
        `;
        grid.appendChild(card);
    }
}

function renderError() {
    const grid = document.getElementById('video-grid');
    grid.innerHTML = `
        <div class="message-container">
            <div class="message-title">Oops! Something went wrong.</div>
            <p>We couldn't load the videos. Please check your connection.</p>
            <button class="retry-btn" id="retry-btn">Try Again</button>
        </div>
    `;
    document.getElementById('retry-btn').addEventListener('click', fetchVideos);
}

function renderEmpty() {
    const grid = document.getElementById('video-grid');
    grid.innerHTML = `
        <div class="message-container">
            <div class="message-title">No Videos Found</div>
            <p>It looks like there are no videos yet. Check back later!</p>
        </div>
    `;
}

function renderVideos(videos) {
    const grid = document.getElementById('video-grid');
    grid.innerHTML = '';

    videos.forEach(video => {
        const card = createVideoCard(video);
        grid.appendChild(card);
    });
}

function createVideoCard(video) {
    const card = document.createElement('div');
    card.className = 'video-card';
    card.addEventListener('click', () => openPlayer(video));

    const thumbnailUrl = `${API_BASE}/api/videos/${video.uuid}/thumbnail`;

    card.innerHTML = `
        <div class="thumbnail-container">
            <img src="${thumbnailUrl}" alt="${escapeHtml(video.title)}" class="thumbnail" onerror="this.onerror=null; this.parentElement.innerHTML='<div class=\'thumbnail-placeholder\'><span>No Thumbnail</span></div>'">
        </div>
        <div class="info">
            <div class="title">${escapeHtml(video.title || 'Untitled')}</div>
            <div class="author">${escapeHtml(video.author ? video.author.username : 'Unknown Author')}</div>
        </div>
    `;

    return card;
}
