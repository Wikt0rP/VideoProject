import { API_BASE, formatDate } from './utils.js';
import { logout } from './auth.js';

// State to track current video being edited
let currentEditVideoUuid = null;

export async function loadMyVideos() {
    const userStr = localStorage.getItem('user');
    if (!userStr) return;
    const user = JSON.parse(userStr);

    try {
        const response = await fetch(`${API_BASE}/api/videos/myVideos?page=0&size=20`, {
            headers: {
                'Authorization': `Bearer ${user.token}`
            }
        });

        if (response.status === 401 || response.status === 403) {
            alert('Session expired. Please login again.');
            logout();
            return;
        }

        if (!response.ok) throw new Error('Failed to load videos');

        const data = await response.json();
        renderMyVideos(data.content);
    } catch (error) {
        console.error('Error loading my videos:', error);
        alert('Failed to load your videos.');
    }
}

function renderMyVideos(videos) {
    const videoGrid = document.getElementById('video-grid');
    if (!videoGrid) return;

    videoGrid.innerHTML = '';

    if (videos.length === 0) {
        videoGrid.innerHTML = '<p style="color: var(--text-secondary); text-align: center; grid-column: 1/-1;">You haven\'t uploaded any videos yet.</p>';
        return;
    }

    videos.forEach(video => {
        const card = document.createElement('div');
        card.className = 'video-card';

        // Check if thumbnail exists
        const hasThumbnail = video.thumbnailPath && video.thumbnailPath.trim() !== '';

        let thumbnailHtml;
        if (hasThumbnail) {
            thumbnailHtml = `
                <div class="thumbnail-container">
                    <img src="${API_BASE}/api/videos/${video.uuid}/thumbnail" alt="${video.title}" class="thumbnail">
                </div>
            `;
        } else {
            thumbnailHtml = `
                <div class="thumbnail-container">
                    <div class="thumbnail-placeholder" style="flex-direction: column; text-align: center; padding: 10px;">
                        <span style="font-size: 2rem; margin-bottom: 0.5rem;">⚠️</span>
                        <span style="color: #ff4444; font-weight: bold; font-size: 0.9rem;">No thumbnail</span>
                        <span style="color: #ff4444; font-size: 0.8rem;">Video hidden</span>
                    </div>
                </div>
            `;
        }

        card.innerHTML = `
            ${thumbnailHtml}
            <div class="video-info">
                <h3 class="video-title">${video.title}</h3>
                <p class="video-author">${video.author ? video.author.name : 'Unknown'}</p>
                <p class="video-meta">
                    ${formatDate(video.createdAt)}
                </p>
                <button class="edit-video-btn" style="
                    margin-top: 10px;
                    background-color: var(--bg-secondary);
                    color: var(--text-main);
                    border: 1px solid var(--border-color);
                    padding: 5px 10px;
                    border-radius: 4px;
                    cursor: pointer;
                    font-size: 0.8rem;
                    width: 100%;
                ">Edit Video</button>
            </div>
        `;

        // Attach event listener directly to the button
        const editBtn = card.querySelector('.edit-video-btn');
        editBtn.addEventListener('click', (e) => {
            e.stopPropagation(); // Prevent card click
            openEditModal(video);
        });

        videoGrid.appendChild(card);
    });
}

export function setupProfileListeners() {
    const editModal = document.getElementById('edit-video-modal');
    const editSubmitBtn = document.getElementById('edit-video-submit-btn');
    const editCancelBtn = document.getElementById('edit-video-cancel-btn');
    const editDeleteBtn = document.getElementById('edit-video-delete-btn');

    if (!editModal) {
        console.error('Edit modal not found in DOM');
        return;
    }

    // Cancel Button
    if (editCancelBtn) {
        editCancelBtn.addEventListener('click', closeEditModal);
    }

    // Submit Button
    if (editSubmitBtn) {
        editSubmitBtn.addEventListener('click', handleEditSubmit);
    }

    // Delete Button
    if (editDeleteBtn) {
        editDeleteBtn.addEventListener('click', handleDeleteVideo);
    }

    // Close on click outside
    window.addEventListener('click', (event) => {
        if (event.target === editModal) {
            closeEditModal();
        }
    });
}

function openEditModal(video) {
    const editModal = document.getElementById('edit-video-modal');
    const editTitleInput = document.getElementById('edit-video-title');
    const editDescInput = document.getElementById('edit-video-desc');
    const editVideoUuidInput = document.getElementById('edit-video-uuid');

    if (!editModal || !editTitleInput || !editDescInput || !editVideoUuidInput) {
        console.error('Missing edit modal elements');
        return;
    }

    currentEditVideoUuid = video.uuid;
    editVideoUuidInput.value = video.uuid;
    editTitleInput.value = video.title || '';
    editDescInput.value = video.description || '';

    editModal.style.display = 'flex';
}

function closeEditModal() {
    const editModal = document.getElementById('edit-video-modal');
    const editThumbnailInput = document.getElementById('edit-video-thumbnail');

    if (editModal) editModal.style.display = 'none';
    if (editThumbnailInput) editThumbnailInput.value = '';

    currentEditVideoUuid = null;
}

async function handleEditSubmit() {
    if (!currentEditVideoUuid) return;

    const editTitleInput = document.getElementById('edit-video-title');
    const editDescInput = document.getElementById('edit-video-desc');
    const editThumbnailInput = document.getElementById('edit-video-thumbnail');

    const title = editTitleInput.value;
    const description = editDescInput.value;
    const thumbnailFile = editThumbnailInput.files[0];

    if (!title) return alert('Title is required.');

    const userStr = localStorage.getItem('user');
    if (!userStr) return;
    const user = JSON.parse(userStr);
    const headers = { 'Authorization': `Bearer ${user.token}` };

    try {
        // 1. Update Details
        const updateData = { title, description };
        const updateResponse = await fetch(`${API_BASE}/api/videos/${currentEditVideoUuid}/update`, {
            method: 'PATCH',
            headers: {
                ...headers,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(updateData)
        });

        if (!updateResponse.ok) {
            const errorText = await updateResponse.text();
            throw new Error('Failed to update details: ' + errorText);
        }

        // 2. Update Thumbnail if selected
        if (thumbnailFile) {
            const formData = new FormData();
            formData.append('thumbnail', thumbnailFile);

            const thumbResponse = await fetch(`${API_BASE}/api/videos/${currentEditVideoUuid}/update/thumbnail`, {
                method: 'PATCH',
                headers: headers,
                body: formData
            });

            if (!thumbResponse.ok) {
                const errorText = await thumbResponse.text();
                throw new Error('Failed to update thumbnail: ' + errorText);
            }
        }

        alert('Video updated successfully!');
        closeEditModal();
        loadMyVideos();
    } catch (error) {
        console.error('Update error:', error);
        alert('Failed to update video: ' + error.message);
    }
}

async function handleDeleteVideo() {
    if (!currentEditVideoUuid) return;

    if (!confirm('Are you sure you want to delete this video? This action cannot be undone.')) {
        return;
    }

    const userStr = localStorage.getItem('user');
    if (!userStr) return;
    const user = JSON.parse(userStr);

    try {
        const response = await fetch(`${API_BASE}/api/videos/delete/${currentEditVideoUuid}`, {
            method: 'DELETE',
            headers: {
                'Authorization': `Bearer ${user.token}`
            }
        });

        if (!response.ok) {
            const errorText = await response.text();
            throw new Error('Failed to delete video: ' + errorText);
        }

        alert('Video deleted successfully.');
        closeEditModal();
        loadMyVideos();
    } catch (error) {
        console.error('Delete error:', error);
        alert('Failed to delete video: ' + error.message);
    }
}
