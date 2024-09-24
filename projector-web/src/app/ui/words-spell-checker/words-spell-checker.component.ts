import { Component, OnInit } from '@angular/core';
import { BehaviorSubject } from 'rxjs/BehaviorSubject';
import { Observable } from 'rxjs/Observable';
import { DataSource } from '@angular/cdk/table';
import { MatDialog, MatSnackBar, PageEvent } from '@angular/material';
import { NormalizedWordBunchDataService } from '../../services/normalized-word-bunch-data.service';
import { NormalizedWordBunch } from '../../models/normalizedWordBunch';
import { WordBunch } from '../../models/wordBunch';
import { LanguageDataService } from '../../services/language-data.service';
import { User } from '../../models/user';
import { AuthService } from '../../services/auth.service';
import { Language } from '../../models/language';
import { SongListComponent } from '../song-list/song-list.component';
import { Title } from '@angular/platform-browser';
import { checkAuthenticationError, generalError } from '../../util/error-util';
import { Song } from '../../services/song-service.service';
import { ChangeWord } from '../../models/changeWord';

class NormalizedWordBunchRow {
  nr: number = 0;
  confidencePercentage: number;
  word: string;
  count: number;
  correction: string;
  song: Song;
  wordBunch: WordBunch;

  getConfidencePercentageS(): string {
    if (this.confidencePercentage >= 100) {
      return '';
    }
    if (!this.wordBunch.problematic) {
      return '';
    }
    return this.confidencePercentage.toFixed(1) + '%';
  }

  getSongLink(): string {
    return this.song.getLink();
  }

  getSongTitle(): string {
    return this.song.title;
  }
}

export class NormalizedWordBunchDatabase {
  dataChange: BehaviorSubject<NormalizedWordBunchRow[]> = new BehaviorSubject<NormalizedWordBunchRow[]>([]);

  constructor(normalizedWordBunchRows: NormalizedWordBunchRow[]) {
    if (normalizedWordBunchRows !== null) {
      const copiedData = this.data;
      for (const normalizedWordBunchRow of normalizedWordBunchRows) {
        copiedData.push(normalizedWordBunchRow);
        this.dataChange.next(copiedData);
      }
      this.dataChange.next(copiedData);
    }
  }

  get data(): NormalizedWordBunchRow[] {
    return this.dataChange.value;
  }
}

export class NormalizedWordBunchDataSource extends DataSource<any> {
  constructor(private _normalizedWordBunchDatabase: NormalizedWordBunchDatabase) {
    super();
  }

  connect(): Observable<NormalizedWordBunchRow[]> {
    return this._normalizedWordBunchDatabase.dataChange;
  }

  disconnect() {
  }
}
@Component({
  selector: 'app-words-spell-checker',
  templateUrl: './words-spell-checker.component.html',
  styleUrls: ['./words-spell-checker.component.css']
})
export class WordsSpellCheckerComponent implements OnInit {

  displayedColumns = ['Nr', 'confidencePercentage', 'word', 'correction', 'count', 'song'];
  dataSource: NormalizedWordBunchDataSource | null;
  pageE: PageEvent;
  normalizedWordBunchRows: NormalizedWordBunchRow[] = [];
  languages: Language[] = [];
  selectedLanguage: Language;

  constructor(
    private normalizedWordBunchDataService: NormalizedWordBunchDataService,
    private languageDataService: LanguageDataService,
    private auth: AuthService,
    private titleService: Title,
    private dialog: MatDialog,
    private snackBar: MatSnackBar,
  ) { }

  ngOnInit() {
    this.titleService.setTitle('Words spell checker');
    this.initializePageEvent();
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
        this.loadData();
      });
    this.languageDataService.getAll();
  }

  private loadData() {
    this.titleService.setTitle('Words spell checker - ' + this.selectedLanguage.nativeName);
    this.normalizedWordBunchDataService.getAll(this.selectedLanguage).subscribe(
      normalizedWordBunchs => {
        this.normalizedWordBunchRows = this.getNormalizedWordBunchRows(normalizedWordBunchs);
        this.fillDataByPageEvent();
      }, (err) => {
        checkAuthenticationError(this.loadData, this, err, this.dialog);
      });
  }

  private applyCaseByReference(source: string, target: string): string {
    if (source.length !== target.length) {
      throw new Error("Source and target strings must have the same length.");
    }

    return target
      .split('')
      .map((char, index) => source[index] === source[index].toUpperCase()
        ? char.toUpperCase()
        : char.toLowerCase()
      )
      .join('');
  }

  private getNormalizedWordBunchRows(normalizedWordBunchs: NormalizedWordBunch[]): NormalizedWordBunchRow[] {
    let normalizedWordBunchRows: NormalizedWordBunchRow[] = [];
    let nr = 0;
    for (const normalizedWordBunch of normalizedWordBunchs) {
      for (const wordBunch of normalizedWordBunch.wordBunches) {
        const normalizedWordBunchRow = new NormalizedWordBunchRow();
        normalizedWordBunchRow.nr = ++nr;
        normalizedWordBunchRow.confidencePercentage = normalizedWordBunch.ratio;
        normalizedWordBunchRow.word = wordBunch.word;
        normalizedWordBunchRow.count = wordBunch.count;
        normalizedWordBunchRow.song = wordBunch.song;
        normalizedWordBunchRow.wordBunch = wordBunch;
        if (wordBunch.problematic) {
          normalizedWordBunchRow.correction = this.applyCaseByReference(wordBunch.word, normalizedWordBunch.bestWord);
        }
        normalizedWordBunchRows.push(normalizedWordBunchRow);
      }
    }
    return normalizedWordBunchRows;
  }

  private fillDataByPageEvent() {
    const start = this.pageE.pageIndex * this.pageE.pageSize;
    // sessionStorage.setItem(this.PAGE_INDEX, JSON.stringify(this.pageE.pageIndex));
    // sessionStorage.setItem(this.PAGE_SIZE, JSON.stringify(this.pageE.pageSize));
    const end = (this.pageE.pageIndex + 1) * this.pageE.pageSize;
    const paginatedNormalizedWordBunchs = this.normalizedWordBunchRows.slice(start, end);
    this.fillData(paginatedNormalizedWordBunchs);

  }

  private fillData(normalizedWordBunchs: NormalizedWordBunchRow[]) {
    const database = new NormalizedWordBunchDatabase(normalizedWordBunchs);
    this.dataSource = new NormalizedWordBunchDataSource(database);
  }

  private initializePageEvent() {
    const pageEvent = new PageEvent();
    // pageEvent.pageSize = JSON.parse(sessionStorage.getItem(this.PAGE_SIZE));
    // pageEvent.pageIndex = JSON.parse(sessionStorage.getItem(this.PAGE_INDEX));
    if (pageEvent.pageSize == undefined) {
      pageEvent.pageSize = 50;
    }
    if (pageEvent.pageIndex == undefined) {
      pageEvent.pageIndex = 0;
    }
    this.pageE = pageEvent;
  }

  pageEvent(pageEvent: PageEvent) {
    this.pageE = pageEvent;
    this.fillDataByPageEvent();
  }

  private toHex(value: number): string {
    return value.toString(16).padStart(2, '0').toUpperCase();
  }

  getRedIntensityColorString(normalizedWordBunchRow: NormalizedWordBunchRow) {
    let ratio = normalizedWordBunchRow.confidencePercentage;
    if (!normalizedWordBunchRow.wordBunch.problematic) {
      ratio = 0;
    }
    const redIntensity = Math.round(255 * (ratio / 100));
    return `#${this.toHex(redIntensity)}0000`;
  }

  changeAll(normalizedWordBunchRow: NormalizedWordBunchRow) {
    let changeWord = new ChangeWord();
    changeWord.word = normalizedWordBunchRow.word;
    changeWord.correction = normalizedWordBunchRow.correction;
    changeWord.occurrence = normalizedWordBunchRow.count;
    this.normalizedWordBunchDataService.changeAll(this.selectedLanguage, changeWord).subscribe(
      _changeWord => {
        this.snackBar.open('Changed to ' + changeWord.correction, 'Close', {
          duration: 5000
        });
        this.loadData();
      }, (err) => {
        generalError(this.loadData, this, err, this.dialog, this.snackBar);
      });
  }

}
