import { Component, OnInit } from '@angular/core';
import { BehaviorSubject } from 'rxjs/BehaviorSubject';
import { Observable } from 'rxjs/Observable';
import { DataSource } from '@angular/cdk/table';
import { Router } from '@angular/router';
import { SongLinkDataService } from '../../services/song-link-data.service';
import { SongLink } from '../../models/song-link';
import { MatDialog, MatSnackBar } from "@angular/material";
import { AuthService } from "../../services/auth.service";
import { Title } from "@angular/platform-browser";
import { Language } from '../../models/language';
import { LanguageDataService } from '../../services/language-data.service';
import { SELECTED_LANGUGAGE } from '../../util/constants';
import { SongListComponent } from '../song-list/song-list.component';
import { User } from '../../models/user';
import { checkAuthenticationError } from '../../util/error-util';

export class SongLinkDatabase {
  dataChange: BehaviorSubject<SongLink[]> = new BehaviorSubject<SongLink[]>([]);

  constructor(songLinkList: SongLink[]) {
    if (songLinkList !== null) {
      const copiedData = this.data;
      let nr = 0;
      for (const songLink of songLinkList) {
        songLink.nr = ++nr;
        copiedData.push(songLink);
        this.dataChange.next(copiedData);
      }

      this.dataChange.next(copiedData);
    }
  }

  get data(): SongLink[] {
    return this.dataChange.value;
  }
}

export class SongLinkDataSource extends DataSource<any> {
  constructor(private _songLinkDatabase: SongLinkDatabase) {
    super();
  }

  connect(): Observable<SongLink[]> {
    return this._songLinkDatabase.dataChange;
  }

  disconnect() {
  }
}

@Component({
  selector: 'app-version-link-list',
  templateUrl: './version-link-list.component.html',
  styleUrls: ['./version-link-list.component.css']
})
export class VersionLinkListComponent implements OnInit {

  songLinkList: SongLink[] = [];
  displayedColumns = ['Nr', 'createdDate', 'title1', 'title2', 'email'];
  songLinkDatabase: any;
  dataSource: SongLinkDataSource | null;
  languages: Language[];
  selectedLanguage: Language;
  resolvedAlreadyApplied = false;

  constructor(public router: Router,
    private songLinkDataService: SongLinkDataService,
    private titleService: Title,
    private auth: AuthService,
    private languageDataService: LanguageDataService,
    private snackBar: MatSnackBar,
    private dialog: MatDialog) {
    this.languages = [];
  }

  ngOnInit() {
    this.titleService.setTitle('Song version links');
    this.selectedLanguage = SongListComponent.getSelectedLanguageFromLocalStorage([]);
    this.languageDataService.getAll().subscribe(
      (languages) => {
        const user: User = this.auth.getUser();
        this.languages = [];
        const selectedLanguageFromLocalStorage = SongListComponent.getSelectedLanguageFromLocalStorage(languages);
        let was = false;
        for (let language of languages) {
          if (user.hasReviewerRoleForLanguage(language)) {
            this.languages.push(language);
            if (language.uuid == selectedLanguageFromLocalStorage.uuid) {
              this.selectedLanguage = language;
              was = true;
            }
          }
        }
        if (!was && this.languages.length > 0) {
          this.selectedLanguage = this.languages[0];
        }
        this.loadSongLinks();
      });
    this.loadSongLinks();
  }

  private loadSongLinks() {
    const role = this.auth.getUser().getRolePath();
    this.songLinkDataService.getAllInReviewByLanguage(role, this.selectedLanguage).subscribe(
      (songLinkList) => {
        this.setSongLinkList(songLinkList);
      },
      (err) => {
        checkAuthenticationError(this.loadSongLinks, this, err, this.dialog);
      }
    );
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

  private setSongLinkList(songLinkList: SongLink[]) {
    this.songLinkList = [];
    for (let songLink of songLinkList) {
      this.songLinkList.push(songLink);
    }
    this.songLinkList.sort((songLink1, songLink2) => {
      return this.compare(songLink2.modifiedDate, songLink1.modifiedDate);
    });
    this.songLinkDatabase = new SongLinkDatabase(this.songLinkList);
    this.dataSource = new SongLinkDataSource(this.songLinkDatabase);
  }

  loadAll() {
    this.songLinkDataService.getAll().subscribe(
      (songLinkList) => {
        this.setSongLinkList(songLinkList);
      },
      (err) => {
        checkAuthenticationError(this.loadAll, this, err, this.dialog);
      }
    );
  }

  onClick(row) {
    const songLink = this.songLinkList[row.nr - 1];
    // noinspection JSIgnoredPromiseFromCall
    this.router.navigate(['/songLink/', songLink.uuid]);
  }

  openAuthenticateDialogOpened = false;

  changeLanguage() {
    localStorage.setItem(SELECTED_LANGUGAGE, JSON.stringify(this.selectedLanguage));
    this.loadSongLinks();
  }

  isAdmin(): Boolean {
    return this.auth.isLoggedIn && this.auth.getUser().isAdmin();
  }

  resolveAlreadyApplied() {
    this.resolvedAlreadyApplied = false;
    this.songLinkDataService.resolveAlreadyApplied().subscribe(
      (songLinks) => {
        if (songLinks == undefined) {
          return;
        }
        this.resolvedAlreadyApplied = true;
        this.snackBar.open('Resolved ' + songLinks.length + ' song links.', 'Close', {
          duration: 4000
        });
        this.ngOnInit();
      }
    );
  }
}
