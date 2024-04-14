import { BaseModel } from './base-model';
import { Role } from './role';
import { Language } from './language';
import { Song } from '../services/song-service.service';

export class User extends BaseModel {
  email = '';
  password = '';
  role = Role.ROLE_USER;
  preferredLanguage = '';
  surname = '';
  firstName = '';
  activated = false;
  modifiedDate: number;
  createdDate: number;
  reviewLanguages: Language[];
  hadUploadedSongs = false;

  nr: number;

  constructor(values: Object = {}) {
    super(values);
    Object.assign(this, values);
    if (this.reviewLanguages != undefined) {
      for (let i = 0; i < this.reviewLanguages.length; ++i) {
        this.reviewLanguages[i] = new Language(this.reviewLanguages[i]);
      }
    }
  }

  getActivatedString() {
    return this.activated ? '' : 'Not activated';
  }

  getRoleString() {
    return Role.getAsString(this.role);
  }

  isAdmin(): Boolean {
    return this.role == Role.ROLE_ADMIN;
  }

  isUser(): Boolean {
    return this.role == Role.ROLE_USER;
  }

  isReviewer(): Boolean {
    return this.role == Role.ROLE_REVIEWER;
  }
  
  hasReviewerRoleForSong(song: Song): Boolean {
    if (song == undefined) {
      return false;
    }
    return this.hasReviewerRoleForLanguage(song.languageDTO);
  }
  
  hasReviewerRoleForLanguage(language: Language): Boolean {
    if (this.isAdmin()) {
      return true;
    }
    if (language == undefined || !this.isReviewer()) {
      return false;
    }
    for (let aLanguage of this.reviewLanguages) {
      if (aLanguage.uuid == language.uuid) {
        return true;
      }
    }
    return false;
  }

  getRolePath() {
    if (this.isAdmin()) {
      return 'admin';
    }
    if (this.isReviewer()) {
      return 'reviewer';
    }
    if (this.isUser()) {
      return 'user';
    }
    return '';
  }

}
