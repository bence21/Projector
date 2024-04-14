import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/Observable';
import { ApiService } from './api.service';
import { SongLink } from '../models/song-link';
import { Language } from '../models/language';

@Injectable()
export class SongLinkDataService {

  constructor(private api: ApiService) {
  }

  getAll(): Observable<SongLink[]> {
    return this.api.getAll(SongLink, 'admin/api/songLinks');
  }

  getAllInReviewByLanguage(role: string, selectedLanguage: Language): Observable<SongLink[]> {
    return this.api.getAll(SongLink, role + '/api/songLinks/language/' + selectedLanguage.uuid);
  }

  getSongLink(role: string, songLinkId): Observable<SongLink> {
    return this.api.getById(SongLink, role + '/api/songLink/', songLinkId);
  }

  update(role: string, songLink: SongLink) {
    songLink.id = songLink.uuid;
    return this.api.update(SongLink, role + '/api/songLink/', songLink);
  }

  resolveAlreadyApplied(): Observable<SongLink[]> {
    return this.api.getAll(SongLink, '/admin/api/songLinks/resolveApplied');
  }
}
