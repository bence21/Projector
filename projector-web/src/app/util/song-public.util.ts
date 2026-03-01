import { Song } from '../services/song-service.service';

/**
 * Checks if a song is public using the same logic as the server-side isPublic() method:
 * !isReviewerErased() && !isDeleted() && !isBackUp() && !hasUnsolvedWords()
 */
export function isSongPublic(song: Song): boolean {
  const isBackUp = song.isBackUp === true;
  return !song.reviewerErased && !song.deleted && !isBackUp && !song.hasUnsolvedWords;
}
