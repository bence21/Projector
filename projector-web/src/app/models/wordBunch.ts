import { Song } from '../services/song-service.service';
import { ReviewedWord } from './reviewedWord';

export class WordBunch {

  word = '';
  count = 0;
  song: Song;
  problematic: boolean;
  reviewedWord: ReviewedWord;

  constructor(values: Object = {}) {
    Object.assign(this, values);
    this.song = new Song(this.song);
    if (this.reviewedWord) {
      this.reviewedWord = new ReviewedWord(this.reviewedWord);
    }
  }
}
