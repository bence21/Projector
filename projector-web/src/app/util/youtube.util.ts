export function extractYouTubeVideoId(url: string | undefined | null): string | null {
  if (!url || typeof url !== 'string') {
    return null;
  }

  const trimmed = url.trim();
  if (!trimmed) {
    return null;
  }

  const match = trimmed.match(
    /(?:youtube\.com\/(?:watch\?v=|embed\/|shorts\/)|youtu\.be\/)([a-zA-Z0-9_-]{11})(?:[?&].*)?$/
  );
  if (match) {
    return match[1];
  }

  const bareMatch = trimmed.match(/^([a-zA-Z0-9_-]{11})$/);
  if (bareMatch) {
    return bareMatch[1];
  }

  return null;
}

export function getYouTubeUrlProblem(url: string | undefined | null): string | null {
  if (!url || typeof url !== 'string') {
    return null;
  }

  const trimmed = url.trim();
  if (!trimmed) {
    return null;
  }

  if (extractYouTubeVideoId(trimmed)) {
    return null;
  }

  const lowerCaseUrl = trimmed.toLowerCase();
  if (lowerCaseUrl.indexOf('youtube.com/watch') >= 0 && lowerCaseUrl.indexOf('v=') < 0) {
    return 'The YouTube watch URL is missing the video ID (`v=` parameter).';
  }

  if (lowerCaseUrl.indexOf('youtube.com') >= 0 || lowerCaseUrl.indexOf('youtu.be') >= 0) {
    return 'Could not find a valid 11-character YouTube video ID in the URL.';
  }

  if (trimmed.indexOf('/') < 0 && trimmed.indexOf('?') < 0) {
    return 'The YouTube video ID must be exactly 11 characters.';
  }

  return 'Please enter a YouTube URL from youtube.com or youtu.be, or paste an 11-character video ID.';
}
