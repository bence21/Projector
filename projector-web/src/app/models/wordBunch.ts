import { Song } from '../services/song-service.service';

export class WordBunch {

  word = '';
  count = 0;
  song: Song;
  problematic: boolean;

  constructor(values: Object = {}) {
    Object.assign(this, values);
    this.song = new Song(this.song);
  }
}
