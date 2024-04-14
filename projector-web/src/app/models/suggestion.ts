import { SongVerseDTO } from '../services/song-service.service';
import { BaseModel } from './base-model';

export class Suggestion extends BaseModel {
  songId: string;
  title: string;
  verses: SongVerseDTO[];
  createdDate: Date;
  modifiedDate: Date;
  applied: boolean;
  createdByEmail: string;
  description: string;
  nr: number;
  youtubeUrl: string;
  reviewed: boolean;
  lastModifiedByUserEmail: string;

  constructor(values: Object = {}) {
    super(values);
    Object.assign(this, values);
  }

  getHeader() {
    if (this.description != undefined && this.description.length > 0) {
      return this.description
    }
    if (this.youtubeUrl != undefined && this.youtubeUrl.length > 0) {
      return 'Youtube video: ' + this.youtubeUrl
    }
    return '';
  }
}
