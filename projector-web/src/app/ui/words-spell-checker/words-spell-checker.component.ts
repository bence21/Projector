import { Component, OnInit, OnDestroy } from '@angular/core';
import { BehaviorSubject } from 'rxjs/BehaviorSubject';
import { Observable } from 'rxjs/Observable';
import { DataSource } from '@angular/cdk/table';
import { MatDialog, MatSnackBar, PageEvent } from '@angular/material';
import { NormalizedWordBunchDataService, NormalizedWordBunchFilterType } from '../../services/normalized-word-bunch-data.service';
import { NormalizedWordBunchRowDTO } from '../../models/normalized-word-bunch-row-dto';
import { WordBunch } from '../../models/wordBunch';
import { LanguageDataService } from '../../services/language-data.service';
import { User } from '../../models/user';
import { AuthService } from '../../services/auth.service';
import { Language } from '../../models/language';
import { SongListComponent } from '../song-list/song-list.component';
import { Title } from '@angular/platform-browser';
import { checkAuthenticationError, generalError } from '../../util/error-util';
import { SELECTED_LANGUGAGE, WORDS_SPELL_CHECKER_FILTER_TYPE } from '../../util/constants';
import { Song } from '../../services/song-service.service';
import { ChangeWord } from '../../models/changeWord';
import { ReviewedWordDataService } from '../../services/reviewed-word-data.service';
import { WordReviewHelperService } from '../../services/word-review-helper.service';
import { ReviewedWord, ReviewedWordStatus } from '../../models/reviewedWord';
import { WordWithStatus } from '../../models/wordWithStatus';

class NormalizedWordBunchRow {
  nr: number = 0;
  confidencePercentage: number;
  word: string;
  count: number;
  correction: string;
  song: Song;
  wordBunch: WordBunch;
  selected: boolean = false;

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

  getWordWithStatus(): WordWithStatus {
    return {
      word: this.word,
      status: (this.wordBunch.reviewedWord && this.wordBunch.reviewedWord.status) ? this.wordBunch.reviewedWord.status : ReviewedWordStatus.UNREVIEWED,
      countInSong: this.count,
      allOccurrencesAutoCapitalized: this.wordBunch.allOccurrencesAutoCapitalized
    };
  }

  get wordWithStatus(): WordWithStatus {
    return this.getWordWithStatus();
  }
}

export class NormalizedWordBunchDatabase {
  dataChange: BehaviorSubject<NormalizedWordBunchRow[]> = new BehaviorSubject<NormalizedWordBunchRow[]>([]);

  constructor(normalizedWordBunchRows: NormalizedWordBunchRow[]) {
    if (normalizedWordBunchRows !== null) {
      const copiedData = this.data;
      for (const normalizedWordBunchRow of normalizedWordBunchRows) {
        // Ensure the row maintains its class prototype
        if (!(normalizedWordBunchRow instanceof NormalizedWordBunchRow)) {
          Object.setPrototypeOf(normalizedWordBunchRow, NormalizedWordBunchRow.prototype);
        }
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
export class WordsSpellCheckerComponent implements OnInit, OnDestroy {

  displayedColumns = ['select', 'Nr', 'confidencePercentage', 'actionsLeft', 'word', 'actionsRight', 'correction', 'count', 'song'];
  dataSource: NormalizedWordBunchDataSource | null;
  pageE: PageEvent;
  normalizedWordBunchRows: NormalizedWordBunchRow[] = [];
  totalRowCount: number = 0;
  languages: Language[] = [];
  selectedLanguage: Language;
  filterType: string = 'all'; // 'all', 'problematic', 'banned', 'reviewed-good', 'context-specific', 'accepted', 'rejected', 'auto-accepted-from-public', 'unreviewed'
  allSelected: boolean = false;
  someSelected: boolean = false;
  /** Word text used for showing brief copy highlight; cleared after a short delay. */
  lastCopiedWord: string | null = null;
  private copyHighlightTimeout: number | null = null;
  readonly WordWithStatus = WordWithStatus;
  /** Row number (1-based) for "Go to row" input. */
  goToRowValue: number | null = null;

  constructor(
    private normalizedWordBunchDataService: NormalizedWordBunchDataService,
    private languageDataService: LanguageDataService,
    private auth: AuthService,
    private titleService: Title,
    private dialog: MatDialog,
    private snackBar: MatSnackBar,
    private reviewedWordDataService: ReviewedWordDataService,
    private wordReviewHelper: WordReviewHelperService,
  ) { }

  ngOnDestroy(): void {
    if (this.copyHighlightTimeout != null) {
      clearTimeout(this.copyHighlightTimeout);
    }
  }

  ngOnInit() {
    this.titleService.setTitle('Words spell checker');
    this.initializePageEvent();
    // Load filterType from localStorage
    const savedFilterType = localStorage.getItem(WORDS_SPELL_CHECKER_FILTER_TYPE);
    if (savedFilterType && ['all', 'problematic', 'banned', 'reviewed-good', 'context-specific', 'accepted', 'rejected', 'unreviewed', 'not-sure', 'auto-accepted-from-public'].includes(savedFilterType)) {
      this.filterType = savedFilterType;
    }
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
        // Apply the saved filter or load all data
        this.onFilterChange();
      });
    this.languageDataService.getAll();
  }

  /**
   * Loads the current page of spell checker rows from the server (server-side pagination).
   * Called when filter/language changes or when user changes page/size.
   */
  private loadCurrentPage() {
    if (!this.selectedLanguage) {
      return;
    }
    this.titleService.setTitle('Words spell checker - ' + this.selectedLanguage.nativeName);
    this.normalizedWordBunchDataService.getPage(
      this.selectedLanguage,
      this.filterType,
      this.pageE.pageIndex,
      this.pageE.pageSize
    ).subscribe(
      result => {
        this.normalizedWordBunchRows = result.content.map(dto => this.dtoToRow(dto));
        this.totalRowCount = result.totalElements;
        this.fillData(this.normalizedWordBunchRows);
        this.updateSelectionState();
        if (result.content.some((dto: NormalizedWordBunchRowDTO) => dto.correctionLengthMismatch)) {
          this.snackBar.open(
            'Source and target strings must have the same length; showing correction as-is. Tip: run WordCanonicalizationGenerator on the server (enable @Component then start the app) to canonicalize Unicode in words and songs.',
            'Close',
            { duration: 3000 }
          );
        }
      },
      (err) => {
        checkAuthenticationError(this.loadCurrentPage, this, err, this.dialog);
      }
    );
  }

  /** Maps a paginated API row DTO to NormalizedWordBunchRow for the table. */
  private dtoToRow(dto: NormalizedWordBunchRowDTO): NormalizedWordBunchRow {
    const row = new NormalizedWordBunchRow();
    row.nr = dto.nr;
    row.confidencePercentage = dto.confidencePercentage;
    row.word = dto.word;
    row.count = dto.count;
    row.correction = dto.correction;
    row.song = dto.song ? new Song(dto.song) : null;
    row.wordBunch = new WordBunch({
      word: dto.word,
      count: dto.count,
      song: dto.song,
      problematic: dto.problematic,
      reviewedWord: dto.reviewedWord ? new ReviewedWord(dto.reviewedWord) : undefined,
      allOccurrencesAutoCapitalized: dto.allOccurrencesAutoCapitalized
    });
    row.selected = false;
    return row;
  }

  onLanguageChange(language: Language) {
    this.selectedLanguage = language;
    localStorage.setItem(SELECTED_LANGUGAGE, JSON.stringify(this.selectedLanguage));
    this.pageE.pageIndex = 0;
    this.loadCurrentPage();
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
    this.loadCurrentPage();
  }

  /** Navigate to the page that contains the given 1-based row number. */
  goToRow() {
    if (this.totalRowCount === 0 || this.pageE == null) {
      return;
    }
    const raw = this.goToRowValue;
    if (raw == null || typeof raw !== 'number' || isNaN(raw)) {
      return;
    }
    const clampedRow = Math.max(1, Math.min(Math.floor(raw), this.totalRowCount));
    const newPageIndex = Math.floor((clampedRow - 1) / this.pageE.pageSize);
    this.pageE.pageIndex = newPageIndex;
    this.loadCurrentPage();
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

  formatStatus(status: any): string {
    if (!status) {
      return '';
    }
    // Convert status to string (handles both string and enum values)
    const statusString = String(status);
    // Convert "REVIEWED_GOOD" to "Reviewed Good", "CONTEXT_SPECIFIC" to "Context Specific", etc.
    return statusString
      .split('_')
      .map(word => word.charAt(0) + word.slice(1).toLowerCase())
      .join(' ');
  }

  getStatusChipClass(status: any): string {
    if (!status) {
      return '';
    }
    // Convert status to string (handles both string and enum values)
    const statusString = String(status);
    // Return CSS class based on status for color coding - matching button colors
    const statusLower = statusString.toLowerCase();
    if (statusLower.includes('good')) {
      return 'status-chip-good';
    } else if (statusLower.includes('accepted')) {
      return 'status-chip-accepted';
    } else if (statusLower.includes('banned')) {
      return 'status-chip-banned';
    } else if (statusLower.includes('rejected')) {
      return 'status-chip-rejected';
    } else if (statusLower.includes('context')) {
      return 'status-chip-context';
    } else if (statusLower.includes('not_sure')) {
      return 'status-chip-not-sure';
    }
    return 'status-chip-default';
  }

  getWordWithStatusForRow(row: NormalizedWordBunchRow): WordWithStatus {
    // Fallback method in case row loses its prototype methods
    if (row && typeof row.getWordWithStatus === 'function') {
      return row.getWordWithStatus();
    }
    // Fallback: create WordWithStatus directly
    return {
      word: row.word,
      status: (row.wordBunch && row.wordBunch.reviewedWord && row.wordBunch.reviewedWord.status)
        ? row.wordBunch.reviewedWord.status
        : ReviewedWordStatus.UNREVIEWED,
      countInSong: row.count,
      allOccurrencesAutoCapitalized: row.wordBunch && row.wordBunch.allOccurrencesAutoCapitalized
    };
  }

  private static lowerFirstChar(word: string): string {
    if (word.length <= 1) {
      return word.toLowerCase();
    }
    return word.charAt(0).toLowerCase() + word.slice(1);
  }

  private copyToClipboard(text: string): boolean {
    if (text && navigator.clipboard && navigator.clipboard.writeText) {
      navigator.clipboard.writeText(text);
      return true;
    }
    return false;
  }

  private setCopyHighlight(word: string): void {
    if (this.copyHighlightTimeout != null) {
      clearTimeout(this.copyHighlightTimeout);
    }
    this.lastCopiedWord = word;
    this.copyHighlightTimeout = window.setTimeout(() => {
      this.lastCopiedWord = null;
      this.copyHighlightTimeout = null;
    }, 500);
  }

  copyWord(event: Event, row: NormalizedWordBunchRow): void {
    event.preventDefault();
    event.stopPropagation();
    const wordWithStatus = this.getWordWithStatusForRow(row);
    const text = WordWithStatus.isAutoCapped(wordWithStatus)
      ? WordsSpellCheckerComponent.lowerFirstChar(row.word)
      : row.word;
    if (this.copyToClipboard(text)) {
      this.setCopyHighlight(row.word);
    }
  }

  copyWordLowerFirst(event: Event, word: string): void {
    event.preventDefault();
    event.stopPropagation();
    if (this.copyToClipboard(WordsSpellCheckerComponent.lowerFirstChar(word))) {
      this.setCopyHighlight(word);
    }
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
        this.onFilterChange();
      }, (err) => {
        generalError(this.onFilterChange.bind(this), this, err, this.dialog, this.snackBar);
      });
  }

  /**
   * Marks a word as good (REVIEWED_GOOD status).
   * After successful marking, the data table is automatically refreshed via onFilterChange()
   * to show the updated status chip and reflect any filter changes.
   */
  markAsGood(row: NormalizedWordBunchRow) {
    this.wordReviewHelper.markWordWithStatus(ReviewedWordStatus.REVIEWED_GOOD, {
      language: this.selectedLanguage,
      word: row.word,
      onSuccess: () => this.onFilterChange()
    });
  }

  /**
   * Marks a word as accepted (ACCEPTED status) after user provides category and notes via dialog.
   * After successful marking, the data table is automatically refreshed via onFilterChange()
   * to show the updated status chip and reflect any filter changes.
   */
  markAsAccepted(row: NormalizedWordBunchRow) {
    this.wordReviewHelper.markAsAccepted({
      language: this.selectedLanguage,
      word: row.word,
      onSuccess: () => this.onFilterChange()
    });
  }

  /**
   * Marks a word as banned (BANNED status).
   * After successful marking, the data table is automatically refreshed via onFilterChange()
   * to show the updated status chip and reflect any filter changes.
   */
  markAsBanned(row: NormalizedWordBunchRow) {
    this.wordReviewHelper.markWordWithStatus(ReviewedWordStatus.BANNED, {
      language: this.selectedLanguage,
      word: row.word,
      onSuccess: () => this.onFilterChange()
    });
  }

  /**
   * Marks a word as rejected (REJECTED status).
   * After successful marking, the data table is automatically refreshed via onFilterChange()
   * to show the updated status chip and reflect any filter changes.
   */
  markAsRejected(row: NormalizedWordBunchRow) {
    this.wordReviewHelper.markWordWithStatus(ReviewedWordStatus.REJECTED, {
      language: this.selectedLanguage,
      word: row.word,
      onSuccess: () => this.onFilterChange()
    });
  }

  /**
   * Marks a word as context-specific (CONTEXT_SPECIFIC status) after user provides context details via dialog.
   * After successful marking, the data table is automatically refreshed via onFilterChange()
   * to show the updated status chip and reflect any filter changes.
   */
  markAsContextSpecific(row: NormalizedWordBunchRow) {
    this.wordReviewHelper.markAsContextSpecific({
      language: this.selectedLanguage,
      word: row.word,
      onSuccess: () => this.onFilterChange()
    });
  }

  /**
   * Marks a word as not sure (NOT_SURE status).
   */
  markAsNotSure(row: NormalizedWordBunchRow) {
    this.wordReviewHelper.markWordWithStatus(ReviewedWordStatus.NOT_SURE, {
      language: this.selectedLanguage,
      word: row.word,
      onSuccess: () => this.onFilterChange()
    });
  }

  private getFilterTypeFromString(filterTypeString: string): NormalizedWordBunchFilterType | null {
    switch (filterTypeString) {
      case 'banned':
        return NormalizedWordBunchFilterType.BANNED;
      case 'reviewed-good':
        return NormalizedWordBunchFilterType.REVIEWED_GOOD;
      case 'context-specific':
        return NormalizedWordBunchFilterType.CONTEXT_SPECIFIC;
      case 'accepted':
        return NormalizedWordBunchFilterType.ACCEPTED;
      case 'rejected':
        return NormalizedWordBunchFilterType.REJECTED;
      case 'auto-accepted-from-public':
        return NormalizedWordBunchFilterType.AUTO_ACCEPTED_FROM_PUBLIC;
      case 'unreviewed':
        return NormalizedWordBunchFilterType.UNREVIEWED;
      case 'not-sure':
        return NormalizedWordBunchFilterType.NOT_SURE;
      default:
        return null;
    }
  }

  /**
   * Refreshes the data table based on the current filter.
   * Resets to page 0 and loads the current page from the server (server-side pagination).
   */
  onFilterChange() {
    localStorage.setItem(WORDS_SPELL_CHECKER_FILTER_TYPE, this.filterType);
    this.pageE.pageIndex = 0;
    this.loadCurrentPage();
  }

  toggleAllSelection() {
    this.allSelected = !this.allSelected;
    this.normalizedWordBunchRows.forEach(row => row.selected = this.allSelected);
    this.updateSelectionState();
  }

  toggleRowSelection(row: NormalizedWordBunchRow) {
    row.selected = !row.selected;
    this.updateSelectionState();
  }

  private updateSelectionState() {
    const selectedCount = this.normalizedWordBunchRows.filter(r => r.selected).length;
    this.allSelected = selectedCount === this.normalizedWordBunchRows.length;
    this.someSelected = selectedCount > 0 && selectedCount < this.normalizedWordBunchRows.length;
  }

  /**
   * Marks multiple selected words as good (REVIEWED_GOOD status) in a single bulk operation.
   * After successful marking, the data table is automatically refreshed via onFilterChange()
   * to show the updated status chips and reflect any filter changes.
   */
  bulkMarkAsGood() {
    const selectedRows = this.normalizedWordBunchRows.filter(r => r.selected);
    if (selectedRows.length === 0) {
      return;
    }
    const reviewedWords = selectedRows.map(row => {
      const rw = new ReviewedWord();
      rw.word = row.word;
      rw.status = ReviewedWordStatus.REVIEWED_GOOD;
      return rw;
    });
    this.reviewedWordDataService.bulkUpdate(this.selectedLanguage, reviewedWords).subscribe(
      () => {
        this.snackBar.open(`Marked ${selectedRows.length} word(s) as good`, 'Close', { duration: 3000 });
        // Refresh data to show updated status and respect current filter
        this.onFilterChange();
      },
      (err) => {
        generalError(null, this, err, this.dialog, this.snackBar);
      }
    );
  }

  /**
   * Refreshes the data by clearing the cache and reloading.
   * This method clears the backend cache for the current language and then reloads the data.
   */
  refresh() {
    if (!this.selectedLanguage) {
      return;
    }
    this.normalizedWordBunchDataService.clearCache(this.selectedLanguage).subscribe(
      () => {
        this.snackBar.open('Cache cleared, refreshing data...', 'Close', { duration: 2000 });
        this.onFilterChange();
      },
      (err) => {
        generalError(null, this, err, this.dialog, this.snackBar);
      }
    );
  }

}
