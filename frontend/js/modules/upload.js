import { API_BASE } from './utils.js';
import { fetchVideos } from './video.js';
import { logout } from './auth.js';

export function setupUploadModal() {
    const modal = document.getElementById('upload-modal');
    const btn = document.getElementById('upload-btn');
    const closeBtn = document.getElementById('upload-cancel-btn');
    const submitBtn = document.getElementById('upload-submit-btn');

    if (btn) {
        btn.addEventListener('click', () => {
            modal.style.display = 'flex';
        });
    }

    if (closeBtn) {
        closeBtn.addEventListener('click', () => {
            modal.style.display = 'none';
            resetUploadForm();
        });
    }

    // Close on click outside
    window.addEventListener('click', (event) => {
        if (event.target === modal) {
            modal.style.display = 'none';
            resetUploadForm();
        }
    });

    if (submitBtn) {
        submitBtn.addEventListener('click', handleUpload);
    }
}

function resetUploadForm() {
    document.getElementById('upload-title').value = '';
    document.getElementById('upload-desc').value = '';
    document.getElementById('upload-video').value = '';
    document.getElementById('upload-thumbnail').value = '';
}

async function handleUpload() {
    const title = document.getElementById('upload-title').value;
    const description = document.getElementById('upload-desc').value;
    const videoFile = document.getElementById('upload-video').files[0];
    const thumbnailFile = document.getElementById('upload-thumbnail').files[0];

    if (!title || !videoFile || !thumbnailFile) {
        alert('Please fill in all required fields (Title, Video, Thumbnail)');
        return;
    }

    if (description.length > 5000) {
        alert('Description is too long! Maximum 5000 characters allowed.');
        return;
    }

    if (description.length > 5000) {
        alert('Description is too long! Maximum 5000 characters allowed.');
        return;
    }

    const user = JSON.parse(localStorage.getItem('user'));
    if (!user || !user.token) {
        alert('You must be logged in to upload videos.');
        return;
    }

    const formData = new FormData();
    formData.append('title', title);
    formData.append('description', description);
    formData.append('video', videoFile);
    formData.append('thumbnail', thumbnailFile);

    const submitBtn = document.getElementById('upload-submit-btn');
    submitBtn.disabled = true;
    submitBtn.textContent = 'Uploading...';

    try {
        const response = await fetch(`${API_BASE}/api/videos/upload`, {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${user.token}`
            },
            body: formData
        });

        if (response.status === 401 || response.status === 403) {
            alert('Session expired. Please login again.');
            logout();
            document.getElementById('upload-modal').style.display = 'none';
            return;
        }

        if (!response.ok) {
            throw new Error('Upload failed');
        }

        alert('Video uploaded successfully!');
        document.getElementById('upload-modal').style.display = 'none';
        resetUploadForm();
        fetchVideos(); // Refresh the list
    } catch (error) {
        console.error('Upload error:', error);
        alert('Failed to upload video.');
    } finally {
        submitBtn.disabled = false;
        submitBtn.textContent = 'Upload';
    }
}
