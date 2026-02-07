import { Component, Input, OnInit, OnChanges, SimpleChanges, Output, EventEmitter } from '@angular/core';
import { Song } from '../../services/song-service.service';
import { Language } from '../../models/language';
import { SongWordValidationService } from '../../services/song-word-validation.service';
import { SongWordValidationResult } from '../../models/songWordValidationResult';
import { WordWithStatus, STATUS_GOOD, STATUS_UNREVIEWED, STATUS_BANNED, STATUS_REJECTED } from '../../models/wordWithStatus';
import { MatDialog } from '@angular/material';
import { WordReviewDialogComponent } from '../word-review-dialog/word-review-dialog.component';
import { ReviewedWordDataService } from '../../services/reviewed-word-data.service';
import { ReviewedWord, ReviewedWordStatus } from '../../models/reviewedWord';
import { MatSnackBar } from '@angular/material';

@Component({
  selector: 'app-song-word-list-panel',
  templateUrl: './song-word-list-panel.component.html',
  styleUrls: ['./song-word-list-panel.component.css']
})
export class SongWordListPanelComponent implements OnInit, OnChanges {
  @Input() song: Song;
  @Input() language: Language;
  @Input() hasChanges: boolean = false;
  @Output() refreshRequested = new EventEmitter<void>();

  words: WordWithStatus[] = [];
  validationResult: SongWordValidationResult;
  loading = false;
  filterStatus: 'all' | WordWithStatus['status'] = 'all';

  readonly STATUS_GOOD = STATUS_GOOD;
  readonly STATUS_UNREVIEWED = STATUS_UNREVIEWED;
  readonly STATUS_BANNED = STATUS_BANNED;
  readonly STATUS_REJECTED = STATUS_REJECTED;
  
  private readonly COMMON_WORD_THRESHOLD = 10;

  counts = {
    total: 0,
    good: 0,
    unreviewed: 0,
    banned: 0,
    rejected: 0
  };

  constructor(
    private validationService: SongWordValidationService,
    private dialog: MatDialog,
    private reviewedWordDataService: ReviewedWordDataService,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit() {
    this.loadWords();
  }

  ngOnChanges(changes: SimpleChanges) {
    if (changes.song || changes.language) {
      this.loadWords();
    }
    // Don't auto-refresh when hasChanges changes - user must click refresh button
  }

  onRefreshClick() {
    this.loadWords();
    this.refreshRequested.emit();
  }

  loadWords() {
    if (!this.song || !this.language) {
      return;
    }

    this.loading = true;
    this.validationService.validateWords(this.song).subscribe(
      (result) => {
        this.validationResult = result;
        if (result.wordsWithStatus) {
          this.words = result.wordsWithStatus;
        } else {
          this.words = [];
        }
        this.calculateCounts();
        this.loading = false;
      },
      (err) => {
        console.error('Error loading word validation:', err);
        this.loading = false;
      }
    );
  }

  calculateCounts() {
    this.counts = {
      total: this.words.length,
      good: this.words.filter(w => w.status === STATUS_GOOD).length,
      unreviewed: this.words.filter(w => w.status === STATUS_UNREVIEWED).length,
      banned: this.words.filter(w => w.status === STATUS_BANNED).length,
      rejected: this.words.filter(w => w.status === STATUS_REJECTED).length
    };
    
    // Reset filter to 'all' if current filter has no items
    if (this.filterStatus !== 'all') {
      let currentCount = 0;
      if (this.filterStatus === STATUS_GOOD) {
        currentCount = this.counts.good;
      } else if (this.filterStatus === STATUS_UNREVIEWED) {
        currentCount = this.counts.unreviewed;
      } else if (this.filterStatus === STATUS_BANNED) {
        currentCount = this.counts.banned;
      } else if (this.filterStatus === STATUS_REJECTED) {
        currentCount = this.counts.rejected;
      }
      
      if (currentCount === 0) {
        this.filterStatus = 'all';
      }
    }
  }

  private getStatusSortOrder(status: string): number {
    switch (status) {
      case STATUS_BANNED: return 0;
      case STATUS_REJECTED: return 1;
      case STATUS_UNREVIEWED: return 2;
      case STATUS_GOOD: return 3;
      default: return 4;
    }
  }

  getFilteredWords(): WordWithStatus[] {
    const filtered = this.filterStatus === 'all'
      ? this.words
      : this.words.filter(w => w.status === this.filterStatus);
    return [...filtered].sort((a, b) => {
      const statusOrder = this.getStatusSortOrder(a.status) - this.getStatusSortOrder(b.status);
      if (statusOrder !== 0) {
        return statusOrder;
      }
      const allCountA = a.countInAllSongs != null ? a.countInAllSongs : 0;
      const allCountB = b.countInAllSongs != null ? b.countInAllSongs : 0;
      if (allCountB !== allCountA) {
        return allCountA - allCountB;
      }
      const caseInsensitive = a.word.localeCompare(b.word, undefined, { sensitivity: 'base' });
      if (caseInsensitive !== 0) {
        return caseInsensitive;
      }
      return a.word.localeCompare(b.word);
    });
  }

  onReviewWord(word: string) {
    const dialogRef = this.dialog.open(WordReviewDialogComponent, {
      width: '500px',
      data: { word: word, language: this.language }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        const reviewedWord = new ReviewedWord();
        reviewedWord.word = word;
        reviewedWord.status = ReviewedWordStatus.ACCEPTED;
        reviewedWord.category = result.category;
        reviewedWord.notes = result.notes;
        this.reviewedWordDataService.createOrUpdate(this.language, reviewedWord).subscribe(
          () => {
            this.snackBar.open('Word reviewed successfully', 'Close', { duration: 3000 });
            // Reload words to update status
            this.loadWords();
          },
          (err) => {
            this.snackBar.open('Error reviewing word', 'Close', { duration: 3000 });
          }
        );
      }
    });
  }

  getStatusIcon(status: string): string {
    switch (status) {
      case STATUS_GOOD: return 'check_circle';
      case STATUS_UNREVIEWED: return 'info';
      case STATUS_BANNED: return 'error';
      case STATUS_REJECTED: return 'warning';
      default: return 'help';
    }
  }

  getStatusColor(status: string): string {
    switch (status) {
      case STATUS_GOOD: return 'primary';
      case STATUS_UNREVIEWED: return 'accent';
      case STATUS_BANNED: return 'warn';
      case STATUS_REJECTED: return 'warn';
      default: return '';
    }
  }

  private isCommonWord(count: number | null | undefined): boolean | null {
    if (count == null) {
      return null;
    }
    return count >= this.COMMON_WORD_THRESHOLD;
  }

  getCommonnessCategory(count: number | null | undefined): string {
    const isCommon = this.isCommonWord(count);
    if (isCommon === null) {
      return '';
    }
    return isCommon ? 'Common' : 'Rare';
  }

  getCommonnessColorClass(count: number | null | undefined): string {
    const isCommon = this.isCommonWord(count);
    if (isCommon === null) {
      return '';
    }
    return isCommon ? 'common-word' : 'rare-word';
  }

  getCommonnessTooltip(count: number | null | undefined): string {
    if (count == null) {
      return '';
    }
    return `${count.toLocaleString()} total occurrences in all songs`;
  }
}
