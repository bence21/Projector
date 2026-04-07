import { Song } from '../services/song-service.service';

/**
 * Checks if a song is public using the same logic as the server-side isPublic() method:
 * !isReviewerErased() && !isDeleted() && !isBackUp() && !hasUnsolvedWords()
 * and only for persisted songs that already have an id.
 */
export function isSongPublic(song: Song): boolean {
  const hasId = !!song.id;
  const isBackUp = song.isBackUp === true;
  return hasId && !song.reviewerErased && !song.deleted && !isBackUp && !song.hasUnsolvedWords;
}
