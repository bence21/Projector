import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/Observable';
import { ApiService } from './api.service';
import { SongCollection, SongCollectionElement } from "../models/songCollection";
import { Song } from './song-service.service';
import { Language } from '../models/language';

@Injectable()
export class SongCollectionDataService {

  constructor(private api: ApiService) {
  }

  create(songCollection: SongCollection): Observable<SongCollection> {
    return this.api.create(SongCollection, 'admin/api/songCollection', songCollection);
  }

  getAll(): Observable<SongCollection[]> {
    return this.api.getAll(SongCollection, 'api/songCollections');
  }

  getAllBySongId(songId: string): Observable<SongCollection[]> {
    return this.api.getAll(SongCollection, 'api/songCollections/song/' + songId);
  }

  getAllByLanguageMinimal(language: Language): Observable<SongCollection[]> {
    return this.api.getAll(SongCollection, 'api/songCollections/language/' + language.uuid + '/minimal');
  }

  putInCollection(songCollection: SongCollection, songCollectionElement: SongCollectionElement, role: string) {
    return this.api.put(SongCollectionElement, role + '/api/songCollection/' + songCollection.uuid + '/songCollectionElement', songCollectionElement);
  }

  deleteSongCollectionElement(songCollectionElement: SongCollectionElement, songCollection: SongCollection, song: Song, role: string) {
    return this.api.deleteById(role + '/api/songCollection/' + songCollection.uuid + '/song/' + song.uuid + '/ordinalNumber/', songCollectionElement.ordinalNumber);
  }
}
