import { Injectable } from '@angular/core';
import { ApiService } from './api.service';
import { Observable } from 'rxjs/Observable';
import { BaseModel } from '../models/base-model';
import { Language } from "../models/language";
import { User } from '../models/user';
import { BooleanResponse } from '../models/boolean-response';
import { compare } from '../util/sort-util';

export class ColorText {
  text: string;
  color = false;
  commonCount: number = 0;
  forwardIndexK: number;
  backwardIndexK: number;
  backwardColor = false;
}

export class WordCompare {
  text: string;
  color = false;
  characters: ColorText[] = [];
  commonCount: number = 0;
}

export class LineWord {
  words: WordCompare[] = [];
  modified = false;
}

export class LineCompare {
  text: string;
  color = false;
  lineWord: LineWord = new LineWord();
  commonCount: number = 0;
}

export enum SectionType {
  Intro, Verse, Pre_chorus, Chorus, Bridge, Coda
}

export class SongVerseDTO {
  lines: string[];
  lineCompareLines: LineCompare[];
  text = '';
  type: SectionType;
  was: boolean;
  mainSong: Song;

  constructor(values: Object = {}) {
    Object.assign(this, values);
  }

  chorus(): boolean {
    return this.type == SectionType.Chorus;
  }

  public getTypeInitial(): string {
    switch (this.type) {
      case SectionType.Intro:
        return 'I';
      case SectionType.Verse:
        return 'V';
      case SectionType.Pre_chorus:
        return 'PC';
      case SectionType.Chorus:
        return 'C';
      case SectionType.Bridge:
        return 'B';
      case SectionType.Coda:
        return 'E';
      default:
        return '';
    }
  }

  public getTypeInitialWithCount(): string {
    let sectionTypeString = this.getTypeInitial();
    if (this.hasOtherSameTypeInSong()) {
      sectionTypeString += this.getSongVerseCountBySectionType();
    }
    return sectionTypeString;
  }

  private getSongVerseCountBySectionType(): number {
    if (this.mainSong == null) {
      return 0;
    }
    let count = 0;
    for (const verse of this.mainSong.songVerseDTOS) {
      if (verse.type == this.type) {
        ++count;
        if (this.equals(verse)) {
          break;
        }
      }
    }
    return count;
  }

  private hasOtherSameTypeInSong(): boolean {
    if (this.mainSong == null) {
      return false;
    }
    for (const verse of this.mainSong.songVerseDTOS) {
      if (verse.type == this.type && !this.equals(verse)) {
        return true;
      }
    }
    return false;
  }

  private equals(other: SongVerseDTO): boolean {
    if (this.text == null) {
      return other.text == null;
    }
    return this.text == other.text;
  }
}

export class SongVerseUI extends SongVerseDTO {
  selected: boolean;
  verseIndex: number = 0;

  constructor(values: Object = {}) {
    super(values);
    Object.assign(this, values);
  }
}

export class Song extends BaseModel {

  static PUBLIC = "PUBLIC";
  static UPLOADED = "UPLOADED";
  static REVIEWER = "REVIEWER";
  private static currentDate = new Date().getTime();
  originalId: string;
  title = '';
  private typeSafeSongVerses: SongVerseDTO[];
  songVerseDTOS: SongVerseDTO[];
  private songVerses: SongVerseDTO[];
  modifiedDate: number;
  createdDate: number;
  deleted = false;
  uuid: '';
  languageDTO: Language;
  uploaded: Boolean;
  versionGroup: '';
  views = 0;
  favourites = 0;
  youtubeUrl;
  verseOrder: string;
  author: string;
  verseOrderList: number[];
  createdByEmail: string;
  backUpSongId: string;
  lastModifiedByUserEmail: string;
  commonWordsCount = 0;
  commonCharacterCount = 0;
  repeatChorus: boolean = true;
  reviewerErased: boolean = false;

  constructor(values: Object = {}) {
    super(values);
    Object.assign(this, values);
    this.languageDTO = new Language(this.languageDTO);
  }

  static copy(other: Song): Song {
    let song = new Song(other);
    song.songVerseDTOS = Song.copyVerses(other.songVerseDTOS);
    song.typeSafeSongVerses = Song.copyVerses(other.typeSafeSongVerses);
    song.songVerses = Song.copyVerses(other.songVerses);
    return song;
  }

  private static copyVerses(otherSongVerses: SongVerseDTO[]): SongVerseDTO[] {
    if (otherSongVerses != undefined) {
      let songVerses: SongVerseDTO[] = [];
      for (let i = 0; i < otherSongVerses.length; ++i) {
        songVerses[i] = new SongVerseDTO(otherSongVerses[i]);
      }
      return songVerses;
    }
    return undefined;
  }

  static getScore(song) {
    let score = 0;
    if (song.views != null) {
      score += song.views;
    }
    if (song.favourites != null) {
      score += song.favourites * 3;
    }
    if (song.youtubeUrl != null) {
      score += 10;
    }
    let l = Song.getCurrentDate() - song.createdDate;
    if (l < 2592000000) {
      score += 14 * ((1 - l / 2592000000));
    }
    l = Song.getCurrentDate() - song.modifiedDate;
    if (l < 2592000000) {
      score += 4 * ((1 - l / 2592000000));
    }
    return score;
  }

  private static getCurrentDate() {
    return this.currentDate;
  }

  public static getNewSongForUI() {
    let song = new Song();
    song.title = 'Loading';
    song.songVerseDTOS = [];
    return song;
  }

  private getVersesByVerseOrder(repeatChorus: boolean): SongVerseDTO[] {
    let verseList: SongVerseDTO[] = [];
    let verses = this.songVerseDTOS;
    let size = verses.length;
    if (this.verseOrderList == undefined) {
      let chorus = new SongVerseDTO();
      for (let i = 0; i < size; ++i) {
        let songVerse = verses[i];
        verseList.push(songVerse);
        if (repeatChorus) {
          if (songVerse.chorus) {
            chorus = new SongVerseDTO();
            Object.assign(chorus, songVerse);
          } else if (chorus.text.length > 0 && chorus.chorus) {
            if (i + 1 < size) {
              if (!verses[i + 1].chorus) {
                let copyChorus = new SongVerseDTO();
                Object.assign(copyChorus, chorus);
                verseList.push(copyChorus);
              }
            } else {
              let copyChorus = new SongVerseDTO();
              Object.assign(copyChorus, chorus);
              verseList.push(copyChorus);
            }
          }
        }
      }
    } else {
      for (const verse of verses) {
        verse.was = false;
      }
      for (const i of this.verseOrderList) {
        if (i < size) {
          if (!verses[i].was) {
            verseList.push(verses[i]);
            verses[i].was = true;
          } else {
            let verse = new SongVerseDTO();
            Object.assign(verse, verses[i]);
            verseList.push(verse);
          }
        }
      }
    }
    return this.ensureSongVerseClass(verseList);
  }

  private ensureSongVerseClass(verses: SongVerseDTO[]): SongVerseDTO[] {
    let list: SongVerseDTO[] = [];
    for (const verse of verses) {
      list.push(new SongVerseDTO(verse));
    }
    return list;
  }

  public getVerses(): SongVerseDTO[] { // with repeat
    if (this.songVerses == undefined) {
      this.songVerses = this.getVersesByVerseOrder(this.repeatChorus);
      for (const verse of this.songVerses) {
        verse.mainSong = this
      }
    }
    return this.songVerses;
  }

  public getSongVerses(): SongVerseDTO[] { // no repeat
    if (this.typeSafeSongVerses == undefined) {
      if (this.songVerseDTOS == undefined) {
        return [];
      }
      this.typeSafeSongVerses = this.ensureSongVerseClass(this.songVerseDTOS);
      for (const verse of this.typeSafeSongVerses) {
        verse.mainSong = this
      }
    }
    return this.typeSafeSongVerses;
  }

  public removeCircularReference() {
    this.removeMainSong(this.songVerseDTOS);
    this.removeMainSong(this.typeSafeSongVerses);
    this.removeMainSong(this.songVerses);
  }

  private removeMainSong(verses: SongVerseDTO[]) {
    for (const verse of verses) {
      verse.mainSong = undefined;
    }
  }

  public static sortByModifiedDate(songs: Song[]): Song[] {
    let sorted = songs;
    sorted.sort((song1, song2) => {
      return compare(song2.modifiedDate, song1.modifiedDate);
    });
    return sorted;
  }

  public getLink(): string {
    return "/#/song/" + this.id;
  }
}

@Injectable()
export class SongService {

  constructor(private api: ApiService) {
  }

  // noinspection JSUnusedGlobalSymbols
  getAllSongs(): Observable<Song[]> {
    return this.api.getAll(Song, 'api/songs');
  }

  // noinspection JSUnusedGlobalSymbols
  getAllSongTitles() {
    return this.api.getAll(Song, 'api/songTitles');
  }

  // noinspection JSUnusedGlobalSymbols
  getSongByTitle(title: string) {
    return this.api.getAttribute(Song, 'api/song?title=' + title);
  }

  getSong(id) {
    return this.api.getById(Song, 'api/song/', id);
  }

  createSong(song: Song) {
    return this.api.create(Song, 'user/api/song', song);
  }

  getAllSongTitlesAfterModifiedDate(modifiedDate: number, selectedLanguage: any) {
    return this.api.getAll(Song, 'api/songTitlesAfterModifiedDate/' + modifiedDate + '/language/' + selectedLanguage);
  }

  getAllSongTitlesByMyUploads(language: Language) {
    return this.api.getAll(Song, 'user/api/songTitles/language/' + language.uuid);
  }

  getAllInReviewSongsByLanguage(selectedLanguage: Language, myUploads: boolean) {
    let url = 'api/songTitlesInReview/language/' + selectedLanguage.uuid;
    if (myUploads) {
      url = url + '?myUploads=true';
    }
    return this.api.getAll(Song, url);
  }

  getAllSongTitlesReviewedByUser(user: User) {
    return this.api.getAll(Song, 'admin/api/songTitlesReviewed/user/' + user.uuid);
  }

  getSongsContainingYouTube() {
    return this.api.getAll(Song, 'admin/api/songTitlesContainingYouTube');
  }

  deleteById(role: string, songId) {
    return this.api.deleteById(role + '/api/song/delete/', songId);
  }

  eraseById(role: string, songId) {
    return this.api.deleteById(role + '/api/song/erase/', songId);
  }

  updateSong(role: string, song: Song) {
    song.id = song.uuid;
    song.removeCircularReference();
    return this.api.update(Song, role + '/api/song/', song);
  }

  publishById(songId) {
    return this.api.deleteById('admin/api/song/publish/', songId);
  }

  getSimilar(song: Song) {
    return this.api.getAll(Song, 'api/songs/similar/song/' + song.uuid);
  }

  getSimilarByPost(song: Song) {
    return this.api.getAllByPost(Song, 'api/songs/similar/song', song);
  }

  getAllUploadedSongTitles() {
    return this.api.getAll(Song, 'api/songs/upload');
  }

  mergeVersionGroup(songId1, songId2, user) {
    return this.api.post(user + '/api/songVersionGroup/' + songId1 + '/' + songId2);
  }

  removeVersionGroup(songId: string) {
    return this.api.post('admin/api/songVersionGroup/remove/' + songId);
  }

  getSongsByVersionGroup(id) {
    return this.api.getAll(Song, '/api/songs/versionGroup/' + id);
  }

  changeLanguage(role: string, song: Song) {
    song.id = song.uuid;
    song.removeCircularReference();
    return this.api.update(Song, role + '/api/changeLanguageForSong/', song);
  }

  hasReviewerRoleForSong(song: Song) {
    return this.api.getOne(BooleanResponse, 'user/api/song/' + song.uuid + '/hasReviewerRoleForSong');
  }
}
