import { Component, OnInit } from '@angular/core';
import { Song, SongService } from '../../services/song-service.service';
import { FormControl } from '@angular/forms';
import { Observable } from 'rxjs/Observable';
import 'rxjs/add/operator/startWith';
import 'rxjs/add/operator/map';
import { PageEvent } from '@angular/material/paginator';
import { ActivatedRoute, Params, Router } from '@angular/router';
import { AuthService } from "../../services/auth.service";
import { Language } from "../../models/language";
import { LanguageDataService } from "../../services/language-data.service";
import { Subscription } from "rxjs/Subscription";
import { Title } from "@angular/platform-browser";
import { User } from '../../models/user';
import { SELECTED_LANGUGAGE } from '../../util/constants';
import { compress, decompress } from 'lz-string';
import { generalError } from '../../util/error-util';
import { MatDialog, MatSnackBar } from '@angular/material';

@Component({
  selector: 'app-song-list',
  templateUrl: './song-list.component.html',
  styleUrls: ['./song-list.component.css']
})
export class SongListComponent implements OnInit {
  sortMode = "sortMode";
  filteredSongsList: Song[];
  songTitles: Song[];
  songControl: FormControl;
  filteredSongs: Observable<Song[]>;
  song: Song;
  pageE: PageEvent;
  paginatedSongs: Song[];
  sortType = "RELEVANCE";
  songTitlesLocalStorage: Song[];
  songsType = Song.PUBLIC;
  languages: Language[];
  selectedLanguage: Language;
  oldLanguagesKey = 'languages_v2';
  languagesKey = 'languages_v3';
  myUploadsCheck = false;
  private songListComponent_songsType = 'songListComponent_songsType';
  private songListComponent_myUploadsCheck = 'songListComponent_myUploadsCheck';
  private _subscription: Subscription;

  constructor(private songService: SongService,
    private router: Router,
    private languageDataService: LanguageDataService,
    private titleService: Title,
    private activatedRoute: ActivatedRoute,
    public auth: AuthService,
    private dialog: MatDialog,
    private snackBar: MatSnackBar,
  ) {
    localStorage.setItem(this.oldLanguagesKey, '');
    this.songControl = new FormControl();
    this.songTitles = [];
    this.paginatedSongs = [];
    this.filteredSongsList = [];
    this.songTitlesLocalStorage = JSON.parse(localStorage.getItem('songTitles'));
    if (this.songTitlesLocalStorage) {
      this.songTitles = this.songTitlesLocalStorage;
      this.sortSongTitles();
    } else {
      this.songTitlesLocalStorage = [];
      const song1 = new Song();
      song1.title = 'loading';
      song1.modifiedDate = new Date(0).getMilliseconds();
      this.songTitles.push(song1);
    }
    this.filteredSongs = this.songControl.valueChanges
      .startWith(null)
      .map(song => song ? this.filterStates(song) : this.songTitles.slice());
    let pageEvent = new PageEvent();
    pageEvent.pageSize = JSON.parse(sessionStorage.getItem("pageSize"));
    pageEvent.pageIndex = JSON.parse(sessionStorage.getItem("pageIndex"));
    if (pageEvent.pageSize == undefined) {
      pageEvent.pageSize = 10;
    }
    if (pageEvent.pageIndex == undefined) {
      pageEvent.pageIndex = 0;
    }
    this.pageE = pageEvent;
    this.languages = [];
  }

  ngOnInit() {
    this.titleService.setTitle('Songs');
    this.sortType = JSON.parse(localStorage.getItem(this.sortMode));
    if (this.sortType === null) {
      this.sortType = "RELEVANCE";
    }
    this.songsType = localStorage.getItem(this.songListComponent_songsType);
    if (this.songsType === null) {
      this.songsType = Song.PUBLIC;
    }
    this.myUploadsCheck = localStorage.getItem(this.songListComponent_myUploadsCheck) == 'true';
    this.songControl.valueChanges.subscribe(value => {
      for (const song of this.songTitles) {
        if (song == undefined) {
          continue;
        }
        if (song.title === value) {
          this.song = song;
          return;
        }
      }
    });
    this.filteredSongs.subscribe(filteredSongsList => {
      let pageIndex = JSON.parse(sessionStorage.getItem("pageIndex"));
      let start = pageIndex * this.pageE.pageSize;
      while (start > filteredSongsList.length) {
        pageIndex -= 1;
        start = pageIndex * this.pageE.pageSize;
      }
      this.pageE.pageIndex = pageIndex;
      const end = (pageIndex + 1) * this.pageE.pageSize;
      this.paginatedSongs = filteredSongsList.slice(start, end);
      this.filteredSongsList = filteredSongsList;
    }
    );
    this.loadLanguage();
  }

  loadLanguage() {
    this.languageDataService.getAll().subscribe(
      (languages) => {
        this.languages = languages;
        if (localStorage.getItem(this.languagesKey) == null) {
          this.saveToLocalStorage(this.languagesKey, languages);
        } else {
          let localStorageLanguages: Language[] = JSON.parse(this.getFromLocalStorage(this.languagesKey));
          if (localStorageLanguages.length != languages.length) {
            let index = 0;
            let wasChange = false;
            for (let storageLanguage of localStorageLanguages) {
              let was = false;
              for (let language of languages) {
                if (language.uuid == storageLanguage.uuid) {
                  was = true;
                  break;
                }
              }
              if (!was) {
                localStorageLanguages.splice(index, 1);
                wasChange = true;
              } else {
                ++index;
              }
            }
            for (let language of languages) {
              let was = false;
              for (let storageLanguage of localStorageLanguages) {
                if (language.uuid == storageLanguage.uuid) {
                  was = true;
                  break;
                }
              }
              if (!was) {
                localStorageLanguages = localStorageLanguages.concat(language);
                wasChange = true;
              }
            }
            if (wasChange) {
              this.saveToLocalStorage(this.languagesKey, localStorageLanguages);
            }
          }
        }
        this.selectedLanguage = SongListComponent.getSelectedLanguageFromLocalStorage(languages);
        this.loadSongs();
        this.getSearchData();
      }
    );
  }

  saveToLocalStorage(key: string, languages: Language[]) {
    const jsonData = JSON.stringify(languages);
    const compressedData = compress(jsonData);
    localStorage.setItem(key, compressedData);
  }

  getFromLocalStorage(key: string): string {
    const compressedData = localStorage.getItem(key);
    if (compressedData == undefined) {
      return undefined;
    }
    return decompress(compressedData);
  }

  static getSelectedLanguageFromLocalStorage(languages: Language[]) {
    let selectedLanguage = JSON.parse(localStorage.getItem(SELECTED_LANGUGAGE));
    if (selectedLanguage == null) {
      selectedLanguage = languages[0];
      localStorage.setItem(SELECTED_LANGUGAGE, JSON.stringify(selectedLanguage));
    } else {
      for (let language of languages) {
        if (language.uuid == selectedLanguage.uuid) {
          selectedLanguage = language;
          break;
        }
      }
    }
    return selectedLanguage;
  }

  public static stripAccents(s) {
    s = s.normalize('NFD').replace(/[\u0300-\u036f]/g, "");
    s = s.replace(/[^a-zA-Z]/g, '');
    return s.toLowerCase();
  }

  filterStates(filter: string) {
    filter = SongListComponent.stripAccents(filter);
    return this.songTitles.filter(song => {
      return SongListComponent.stripAccents(song.title).indexOf(filter) >= 0;
    }
    );
  }

  selectSong(selectedSong: Song) {
    if (selectedSong != null && selectedSong.id != null) {
      // noinspection JSIgnoredPromiseFromCall
      this.router.navigate(['/song/', selectedSong.id]);
    }
  }

  pageEvent(pageEvent: PageEvent) {
    this.pageE = pageEvent;
    const start = this.pageE.pageIndex * this.pageE.pageSize;
    sessionStorage.setItem("pageIndex", JSON.stringify(this.pageE.pageIndex));
    sessionStorage.setItem("pageSize", JSON.stringify(this.pageE.pageSize));
    const end = (this.pageE.pageIndex + 1) * this.pageE.pageSize;
    this.paginatedSongs = this.filteredSongsList.slice(start, end);
  }

  changeSorting() {
    localStorage.setItem(this.sortMode, JSON.stringify(this.sortType));
    this.sortSongTitles();
    this.filteredSongs = this.songControl.valueChanges
      .startWith(null)
      .map(song => song ? this.filterStates(song) : this.songTitles.slice());
    this.songControl.updateValueAndValidity();
  }

  songsTypeChange() {
    localStorage.setItem(this.songListComponent_songsType, this.songsType);
    this.loadSongs();
  }

  changeLanguage() {
    localStorage.setItem('selectedLanguage', JSON.stringify(this.selectedLanguage));
    this.loadSongs();
  }

  private resolveOldSubscription() {
    if (this._subscription != undefined) {
      this._subscription.unsubscribe();
    }
  }

  selectLanguage(language: Language) {
    this.resolveOldSubscription();
    let languages: Language[] = JSON.parse(this.getFromLocalStorage(this.languagesKey));
    for (let lang of languages) {
      if (lang.uuid === language.uuid) {
        if (lang.songTitles === undefined) {
          lang.songTitles = [];
        } else {
          this.songTitles = lang.songTitles;
          this.sortSongTitles();
          this.songControl.updateValueAndValidity();
        }
        let modifiedDate = 0;
        for (let song of lang.songTitles) {
          if (modifiedDate < song.modifiedDate) {
            modifiedDate = song.modifiedDate;
          }
        }
        this._subscription = this.songService.getAllSongTitlesAfterModifiedDate(modifiedDate, language.uuid).subscribe(songTitles => {
          if (songTitles.length == 0) {
            return;
          }
          if (lang.songTitles.length == 0) {
            this.songTitles = songTitles;
            for (let song of songTitles) {
              if (song.deleted) {
                this.removeSong(song, this.songTitles);
              }
            }
            lang.songTitles = this.songTitles;
          } else {
            let modifiedSongs = [];
            for (const song of songTitles) {
              if (song.deleted) {
                this.removeSong(song, lang.songTitles);
              } else {
                const index = this.containsInLocalStorage(song, lang.songTitles);
                if (index > -1) {
                  lang.songTitles.splice(index, 1);
                }
                modifiedSongs.push(song);
              }
            }
            this.songTitles = lang.songTitles.concat(modifiedSongs);
            lang.songTitles = this.songTitles;
            this.removeFromOtherLanguages(modifiedSongs, languages, lang);
          }
          this.saveToLocalStorage(this.languagesKey, languages);
          this.sortAndUpdate();
        });
      }
    }
  }

  private removeFromOtherLanguages(modifiedSongs: any[], languages: Language[], lang: Language) {
    for (const song of modifiedSongs) {
      for (const language of languages) {
        if (language != lang) {
          const index = this.containsInLocalStorage(song, language.songTitles);
          if (index > -1) {
            language.songTitles.splice(index, 1);
          }
        }
      }
    }
  }

  allLanguages() {
    if (this._subscription != undefined) {
      this._subscription.unsubscribe();
    }
    let languages: Language[] = JSON.parse(this.getFromLocalStorage(this.languagesKey));
    this.songTitles = [];
    for (let language of languages) {
      this.songTitles = this.songTitles.concat(language.songTitles);
    }
    this.sortAndUpdate();
  }

  searchTermTyped() {
    const params: Params = Object.assign({}, this.activatedRoute.snapshot.queryParams);
    const searchValue = this.songControl.value;
    const SEARCH_PARAM = 'search';
    const LANGUAGE_PARAM = 'language';
    if (searchValue !== undefined && searchValue != "") {
      params[SEARCH_PARAM] = searchValue;
      params[LANGUAGE_PARAM] = this.selectedLanguage.uuid;
    } else {
      delete params[SEARCH_PARAM];
      delete params[LANGUAGE_PARAM];
    }
    this.router.navigate(['.'], { queryParams: params });
  }

  private sortAndUpdate() {
    this.sortSongTitles();
    this.songControl.updateValueAndValidity();
    const pageEvent = new PageEvent();
    pageEvent.pageSize = JSON.parse(sessionStorage.getItem("pageSize"));
    pageEvent.pageIndex = JSON.parse(sessionStorage.getItem("pageIndex"));
    if (pageEvent.pageSize == undefined) {
      pageEvent.pageSize = 10;
    }
    if (pageEvent.pageIndex == undefined) {
      pageEvent.pageIndex = 0;
    }
    this.pageEvent(pageEvent);
  }

  private loadSongs() {
    switch (this.songsType) {
      case Song.PUBLIC:
        this.loadPublicSongs();
        break;
      case Song.UPLOADED:
        this.songService.getAllUploadedSongTitles().subscribe(
          (songTitles) => {
            this.setSongTitles(songTitles);
          }
        );
        break;
      case Song.REVIEWER:
        const user = this.auth.getUser();
        if (user == undefined || (!this.hasRoleForSongReview() && !user.isAdmin()) || this.selectedLanguage == undefined) {
          this.loadPublicSongs();
          return;
        }
        this.songService.getAllInReviewSongsByLanguage(this.selectedLanguage, this.myUploadsCheck).subscribe(
          (songTitles) => {
            this.setSongTitles(songTitles);
          }, (error) => {
            this.setSongTitles([]);
            generalError(this.loadSongs, this, error, this.dialog, this.snackBar);
          }
        );
        break;
    }
  }

  private getSearchData() {
    this.activatedRoute.queryParams.subscribe((queryParams) => {
      let search = queryParams['search'];
      let language = queryParams['language'];
      for (let l of this.languages) {
        if (l.uuid == language) {
          language = l;
          if (this.selectedLanguage != language) {
            this.selectedLanguage = l;
            this.selectLanguage(language);
          }
          break;
        }
      }
      this.songControl.patchValue(search);
    });
  }

  private loadPublicSongs() {
    if (!this.myUploadsCheck) {
      this.selectLanguage(this.selectedLanguage);
    } else {
      this.loadPublicSongsByMyUploads();
    }
  }

  private loadPublicSongsByMyUploads() {
    this.resolveOldSubscription();
    this._subscription = this.songService.getAllSongTitlesByMyUploads(this.selectedLanguage).subscribe(songTitles => {
      this.setSongTitles(songTitles);
    });
  }

  private setSongTitles(songTitles: Song[]) {
    this.songTitles = songTitles;
    this.sortSongTitles();
    this.songControl.updateValueAndValidity();
    const pageEvent = new PageEvent();
    pageEvent.pageSize = JSON.parse(sessionStorage.getItem("pageSize"));
    pageEvent.pageIndex = JSON.parse(sessionStorage.getItem("pageIndex"));
    if (pageEvent.pageSize == undefined) {
      pageEvent.pageSize = 10;
    }
    if (pageEvent.pageIndex == undefined) {
      pageEvent.pageIndex = 0;
    }
    this.pageEvent(pageEvent);
  }

  private containsInLocalStorage(song, titlesLocalStorage = this.songTitlesLocalStorage) {
    let index = 0;
    for (const songTitle of titlesLocalStorage) {
      if (songTitle.id === song.id) {
        return index;
      }
      ++index;
    }
    return -1;
  }

  private removeSong(song, songs = this.songTitlesLocalStorage) {
    const index = this.getIndex(song, songs);
    if (index > -1) {
      songs.splice(index, 1);
    }
    return index;
  }

  private getIndex(searchedSong, songs) {
    let index = -1;
    let i = 0;
    for (const song of songs) {
      if (song.id != null && song.id === searchedSong.id) {
        index = i;
        break;
      }
      ++i;
    }
    return index;
  }

  private sortSongTitles() {
    if (this.sortType === "MODIFIED_DATE") {
      this.sortSongTitlesByModifiedDate();
    } else if (this.sortType === "VIEWS") {
      this.songTitles.sort((song1, song2) => {
        if (song1.views < song2.views) {
          return 1;
        }
        if (song1.views > song2.views) {
          return -1;
        }
        return 0;
      });
    } else if (this.sortType === "RELEVANCE") {
      this.songTitles.sort((song1, song2) => {
        let score1 = Song.getScore(song1);
        let score2 = Song.getScore(song2);
        if (score1 < score2) {
          return 1;
        }
        if (score1 > score2) {
          return -1;
        }
        return this.compare(song2.modifiedDate, song1.modifiedDate);
      });
    } else {
      this.songTitles.sort((song1, song2) => {
        return this.compare(song1.title.toLocaleLowerCase(), song2.title.toLocaleLowerCase());
      });
    }
  }

  private compare(a, b) {
    if (a < b) {
      return -1;
    }
    if (a > b) {
      return 1;
    }
    return 0;
  }

  private sortSongTitlesByModifiedDate() {
    this.songTitles.sort((song1, song2) => {
      return this.compare(song2.modifiedDate, song1.modifiedDate);
    });
  }

  hasRoleForSongReview() {
    const user: User = this.auth.getUser();
    return this.auth.isLoggedIn && user.hasReviewerRoleForLanguage(this.selectedLanguage);
  }

  showMyUploads(): boolean {
    if (!this.auth.isLoggedIn) {
      return false;
    }
    const user: User = this.auth.getUser();
    if (user == null) {
      return false;
    }
    return user.hadUploadedSongs;
  }

  onChangeMyUploadsCheck() {
    localStorage.setItem(this.songListComponent_myUploadsCheck, this.myUploadsCheck.toString());
    this.loadSongs();
  }
}

