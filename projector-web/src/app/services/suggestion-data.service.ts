import { Injectable } from '@angular/core';
import { Observable } from 'rxjs/Observable';
import { ApiService } from './api.service';
import { Suggestion } from '../models/suggestion';
import { Language } from '../models/language';
import { Song } from './song-service.service';

@Injectable()
export class SuggestionDataService {

  constructor(private api: ApiService) {
  }

  getAll(): Observable<Suggestion[]> {
    return this.api.getAll(Suggestion, 'admin/api/suggestions');
  }

  getAllInReviewByLanguage(role: string, selectedLanguage: Language): Observable<Suggestion[]> {
    return this.api.getAll(Suggestion, role + '/api/suggestions/language/' + selectedLanguage.uuid);
  }

  getAllBySong(role: string, song: Song): Observable<Suggestion[]> {
    return this.api.getAll(Suggestion, role + '/api/suggestions/song/' + song.uuid);
  }

  getSuggestion(role: string, suggestionId): Observable<Suggestion> {
    return this.api.getById(Suggestion, role + '/api/suggestion/', suggestionId);
  }

  update(role: string, suggestion: Suggestion) {
    suggestion.id = suggestion.uuid;
    return this.api.update(Suggestion, role + '/api/suggestion/', suggestion);
  }
}
