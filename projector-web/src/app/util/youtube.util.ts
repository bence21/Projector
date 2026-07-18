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

/** Song-like shape used when grouping shared YouTube IDs. */
export interface YouTubeSongRef {
  id?: string;
  uuid?: string;
  title?: string;
  youtubeUrl?: string | null;
  deleted?: boolean;
  reviewerErased?: boolean;
}

export interface DuplicateYouTubeGroup {
  youtubeId: string;
  songs: YouTubeSongRef[];
}

function songRefKey(song: YouTubeSongRef): string {
  return String(song.uuid || song.id || '');
}

/**
 * Groups active songs that share the same YouTube video ID (2+ songs per ID).
 * Groups are sorted by song count (desc), then by YouTube ID.
 */
export function groupDuplicateYouTubeIds(songs: YouTubeSongRef[] | null | undefined): DuplicateYouTubeGroup[] {
  if (!songs || songs.length === 0) {
    return [];
  }

  const byId = new Map<string, YouTubeSongRef[]>();
  for (const song of songs) {
    if (!song || song.deleted || song.reviewerErased) {
      continue;
    }
    const youtubeId = extractYouTubeVideoId(song.youtubeUrl);
    if (!youtubeId) {
      continue;
    }
    let group = byId.get(youtubeId);
    if (!group) {
      group = [];
      byId.set(youtubeId, group);
    }
    group.push(song);
  }

  const duplicates: DuplicateYouTubeGroup[] = [];
  byId.forEach((groupSongs, youtubeId) => {
    if (groupSongs.length >= 2) {
      duplicates.push({ youtubeId, songs: groupSongs });
    }
  });

  duplicates.sort((a, b) => {
    const countDiff = b.songs.length - a.songs.length;
    if (countDiff !== 0) {
      return countDiff;
    }
    return a.youtubeId.localeCompare(b.youtubeId);
  });
  return duplicates;
}

/**
 * Returns similar songs that share this song's YouTube video ID.
 */
export function findSimilarSongsSharingYouTubeId(
  song: YouTubeSongRef | null | undefined,
  similarSongs: YouTubeSongRef[] | null | undefined
): YouTubeSongRef[] {
  if (!song || !similarSongs || similarSongs.length === 0) {
    return [];
  }
  const youtubeId = extractYouTubeVideoId(song.youtubeUrl);
  if (!youtubeId) {
    return [];
  }
  const currentKey = songRefKey(song);
  return similarSongs.filter((similar) => {
    if (!similar || similar.deleted || similar.reviewerErased) {
      return false;
    }
    if (songRefKey(similar) && songRefKey(similar) === currentKey) {
      return false;
    }
    return extractYouTubeVideoId(similar.youtubeUrl) === youtubeId;
  });
}
