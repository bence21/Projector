import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/Observable';
import { ApiService } from './api.service';
import { Song } from './song-service.service';
import { SongWordValidationResult } from '../models/songWordValidationResult';

@Injectable()
export class SongWordValidationService {

  constructor(private api: ApiService) {
  }

  validateWords(song: Song): Observable<SongWordValidationResult> {
    const songCopy = new Song(song);
    songCopy.removeCircularReference();
    return this.api.postWithBody(SongWordValidationResult, '/api/song/validateWords', songCopy);
  }
}
