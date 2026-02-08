import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/Observable';
import { ApiService } from './api.service';
import { Song, SongVerseDTO } from './song-service.service';
import { SongWordValidationResult } from '../models/songWordValidationResult';
import { Language } from '../models/language';

@Injectable()
export class SongWordValidationService {

  constructor(private api: ApiService) {
  }

  validateWords(song: Song): Observable<SongWordValidationResult> {
    const songCopy = this.createSimpleSongCopy(song);
    return this.api.postWithBody(SongWordValidationResult, '/api/song/validateWords', songCopy);
  }

  createSimpleSongCopy(sourceSong: Song): Song {
    const copy = new Song();
    copy.title = sourceSong.title;
    if (sourceSong.languageDTO && sourceSong.languageDTO.uuid) {
      copy.languageDTO = new Language();
      copy.languageDTO.uuid = sourceSong.languageDTO.uuid;
    }
    copy.songVerseDTOS = [];
    if (sourceSong.songVerseDTOS) {
      for (const verse of sourceSong.songVerseDTOS) {
        const verseCopy = new SongVerseDTO();
        verseCopy.text = verse.text;
        verseCopy.type = verse.type;
        copy.songVerseDTOS.push(verseCopy);
      }
    }
    return copy;
  }
}
