/**
 * Centralized API configuration for AquaTrack
 * Handles automatic protocol switching and prevents browser mixed-content blocks.
 */
export const getApiBaseUrl = () => {
  // 1. Explicit Vite environment variable (e.g. injected on Render, Vercel, Netlify)
  if (import.meta.env.VITE_API_BASE_URL) {
    let url = import.meta.env.VITE_API_BASE_URL.trim();
    if (!url.startsWith('http://') && !url.startsWith('https://')) {
      url = `https://${url}`;
    }
    return url.replace(/\/+$/, '');
  }

  // 2. In-browser dynamic resolution
  if (typeof window !== 'undefined') {
    const isLocal = window.location.hostname === 'localhost' || window.location.hostname === '127.0.0.1';
    if (isLocal) {
      return `http://${window.location.hostname}:8080`;
    }
    // In production HTTPS, fallback to same origin or secure protocol
    if (window.location.protocol === 'https:') {
      return window.location.origin;
    }
    return `http://${window.location.hostname}:8080`;
  }

  return 'http://localhost:8080';
};

export const API_BASE_URL = getApiBaseUrl();
export default API_BASE_URL;
