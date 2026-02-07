import { Component, Input, OnInit, OnChanges, SimpleChanges, Output, EventEmitter } from '@angular/core';
import { Song } from '../../services/song-service.service';
import { Language } from '../../models/language';
import { SongWordValidationService } from '../../services/song-word-validation.service';
import { SongWordValidationResult } from '../../models/songWordValidationResult';
import { WordWithStatus } from '../../models/wordWithStatus';
import { MatDialog } from '@angular/material';
import { ReviewedWordDataService } from '../../services/reviewed-word-data.service';
import { WordReviewHelperService } from '../../services/word-review-helper.service';
import { ReviewedWordStatus } from '../../models/reviewedWord';
import { MatSnackBar } from '@angular/material';
import { AuthService } from '../../services/auth.service';

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
  filterStatus: 'all' | ReviewedWordStatus = 'all';

  readonly ReviewedWordStatus = ReviewedWordStatus;

  private readonly COMMON_WORD_THRESHOLD = 10;

  private static isGoodStatus(status: ReviewedWordStatus): boolean {
    return status === ReviewedWordStatus.REVIEWED_GOOD ||
      status === ReviewedWordStatus.CONTEXT_SPECIFIC ||
      status === ReviewedWordStatus.ACCEPTED ||
      status === ReviewedWordStatus.AUTO_ACCEPTED_FROM_PUBLIC;
  }

  counts = {
    total: 0,
    good: 0,
    unreviewed: 0,
    banned: 0,
    rejected: 0
  };

  hasReviewerRole = false;

  constructor(
    private validationService: SongWordValidationService,
    private dialog: MatDialog,
    private reviewedWordDataService: ReviewedWordDataService,
    private wordReviewHelper: WordReviewHelperService,
    private snackBar: MatSnackBar,
    private auth: AuthService
  ) {}

  ngOnInit() {
    this.updateHasReviewerRole();
    this.loadWords();
  }

  ngOnChanges(changes: SimpleChanges) {
    if (changes.song || changes.language) {
      this.updateHasReviewerRole();
      this.loadWords();
    }
    // Don't auto-refresh when hasChanges changes - user must click refresh button
  }

  private updateHasReviewerRole(): void {
    const user = this.auth.getUser();
    this.hasReviewerRole = !!(user && this.language && user.hasReviewerRoleForLanguage(this.language));
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
      good: this.words.filter(w => SongWordListPanelComponent.isGoodStatus(w.status)).length,
      unreviewed: this.words.filter(w => w.status === ReviewedWordStatus.UNREVIEWED).length,
      banned: this.words.filter(w => w.status === ReviewedWordStatus.BANNED).length,
      rejected: this.words.filter(w => w.status === ReviewedWordStatus.REJECTED).length
    };

    // Reset filter to 'all' if current filter has no items
    if (this.filterStatus !== 'all') {
      let currentCount = 0;
      if (this.filterStatus === ReviewedWordStatus.REVIEWED_GOOD || SongWordListPanelComponent.isGoodStatus(this.filterStatus as ReviewedWordStatus)) {
        currentCount = this.counts.good;
      } else if (this.filterStatus === ReviewedWordStatus.UNREVIEWED) {
        currentCount = this.counts.unreviewed;
      } else if (this.filterStatus === ReviewedWordStatus.BANNED) {
        currentCount = this.counts.banned;
      } else if (this.filterStatus === ReviewedWordStatus.REJECTED) {
        currentCount = this.counts.rejected;
      }

      if (currentCount === 0) {
        this.filterStatus = 'all';
      }
    }
  }

  private getStatusSortOrder(status: ReviewedWordStatus): number {
    switch (status) {
      case ReviewedWordStatus.BANNED: return 0;
      case ReviewedWordStatus.REJECTED: return 1;
      case ReviewedWordStatus.UNREVIEWED: return 2;
      default:
        return SongWordListPanelComponent.isGoodStatus(status) ? 3 : 4;
    }
  }

  getFilteredWords(): WordWithStatus[] {
    const filtered = this.filterStatus === 'all'
      ? this.words
      : SongWordListPanelComponent.isGoodStatus(this.filterStatus as ReviewedWordStatus)
        ? this.words.filter(w => SongWordListPanelComponent.isGoodStatus(w.status))
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

  /**
   * Marks a word as banned (BANNED status).
   * After successful marking, the data table is automatically refreshed via loadWords()
   * to show the updated status chip and reflect any filter changes.
   */
  markAsBanned(word: string) {
    this.wordReviewHelper.markWordWithStatus(ReviewedWordStatus.BANNED, {
      language: this.language,
      word: word,
      successMessage: 'Word marked as banned',
      onSuccess: () => this.loadWords()
    });
  }

  markAsRejected(word: string) {
    this.wordReviewHelper.markWordWithStatus(ReviewedWordStatus.REJECTED, {
      language: this.language,
      word: word,
      successMessage: 'Word marked as rejected',
      onSuccess: () => this.loadWords()
    });
  }

  markAsGood(word: string) {
    this.wordReviewHelper.markWordWithStatus(ReviewedWordStatus.REVIEWED_GOOD, {
      language: this.language,
      word: word,
      successMessage: 'Word marked as good',
      onSuccess: () => this.loadWords()
    });
  }

  markAsAccepted(word: string) {
    this.wordReviewHelper.markAsAccepted({
      language: this.language,
      word: word,
      successMessage: 'Word marked as accepted',
      onSuccess: () => this.loadWords()
    });
  }

  markAsContextSpecific(word: string) {
    this.wordReviewHelper.markAsContextSpecific({
      language: this.language,
      word: word,
      successMessage: 'Word marked as context-specific',
      onSuccess: () => this.loadWords()
    });
  }

  getStatusIcon(status: ReviewedWordStatus): string {
    switch (status) {
      case ReviewedWordStatus.REVIEWED_GOOD:
      case ReviewedWordStatus.CONTEXT_SPECIFIC:
      case ReviewedWordStatus.ACCEPTED:
      case ReviewedWordStatus.AUTO_ACCEPTED_FROM_PUBLIC:
        return 'check_circle';
      case ReviewedWordStatus.UNREVIEWED: return 'info';
      case ReviewedWordStatus.BANNED: return 'error';
      case ReviewedWordStatus.REJECTED: return 'warning';
      default: return 'help';
    }
  }

  isGoodStatus(status: ReviewedWordStatus): boolean {
    return SongWordListPanelComponent.isGoodStatus(status);
  }

  getStatusColor(status: ReviewedWordStatus): string {
    // Return empty string to use CSS classes instead of Material theme colors
    // This allows us to match the exact button colors
    return '';
  }

  getStatusColorClass(status: ReviewedWordStatus): string {
    switch (status) {
      case ReviewedWordStatus.REVIEWED_GOOD:
        return 'status-icon-good';
      case ReviewedWordStatus.CONTEXT_SPECIFIC:
        return 'status-icon-context';
      case ReviewedWordStatus.ACCEPTED:
        return 'status-icon-accepted';
      case ReviewedWordStatus.AUTO_ACCEPTED_FROM_PUBLIC:
        return 'status-icon-good'; // Same as reviewed good
      case ReviewedWordStatus.BANNED:
        return 'status-icon-banned';
      case ReviewedWordStatus.REJECTED:
        return 'status-icon-rejected';
      case ReviewedWordStatus.UNREVIEWED:
        return 'status-icon-unreviewed';
      default:
        return '';
    }
  }

  getStatusTooltip(wordWithStatus: WordWithStatus): string {
    const status = wordWithStatus.status;
    let tooltip = '';
    
    switch (status) {
      case ReviewedWordStatus.REVIEWED_GOOD:
        tooltip = 'Reviewed Good';
        break;
      case ReviewedWordStatus.CONTEXT_SPECIFIC:
        tooltip = 'Context-Specific';
        // Add context category, context description, and notes for context-specific words
        if (wordWithStatus.contextCategory || wordWithStatus.contextDescription || wordWithStatus.notes) {
          const parts: string[] = [tooltip];
          if (wordWithStatus.contextCategory) {
            parts.push(`\nCategory: ${wordWithStatus.contextCategory}`);
          }
          if (wordWithStatus.contextDescription) {
            parts.push(`\nDescription: ${wordWithStatus.contextDescription}`);
          }
          if (wordWithStatus.notes) {
            parts.push(`\nNotes: ${wordWithStatus.notes}`);
          }
          tooltip = parts.join('');
        }
        break;
      case ReviewedWordStatus.ACCEPTED:
        tooltip = 'Accepted';
        // Add category, source language, foreign language type, and notes for accepted words
        if (wordWithStatus.category || wordWithStatus.notes || wordWithStatus.sourceLanguage || wordWithStatus.foreignLanguageType) {
          const parts: string[] = [tooltip];
          if (wordWithStatus.category) {
            parts.push(`\nCategory: ${wordWithStatus.category}`);
          }
          if (wordWithStatus.sourceLanguage) {
            const langLabel = this.getSourceLanguageLabel(wordWithStatus.sourceLanguage);
            parts.push(`\nSource language: ${langLabel}`);
          }
          if (wordWithStatus.foreignLanguageType !== undefined && wordWithStatus.foreignLanguageType !== null) {
            const typeLabel = wordWithStatus.foreignLanguageType === 0 || wordWithStatus.foreignLanguageType === 'BORROWED'
              ? 'Borrowed (written in song language style)'
              : 'Foreign (OK in source language, not in song language)';
            parts.push(`\nType: ${typeLabel}`);
          }
          if (wordWithStatus.notes) {
            parts.push(`\nNotes: ${wordWithStatus.notes}`);
          }
          tooltip = parts.join('');
        }
        break;
      case ReviewedWordStatus.AUTO_ACCEPTED_FROM_PUBLIC:
        tooltip = 'Auto Accepted From Public';
        break;
      case ReviewedWordStatus.BANNED:
        tooltip = 'Banned';
        break;
      case ReviewedWordStatus.REJECTED:
        tooltip = 'Rejected';
        break;
      case ReviewedWordStatus.UNREVIEWED:
        tooltip = 'Unreviewed';
        break;
      default:
        tooltip = 'Unknown Status';
    }
    
    return tooltip;
  }

  getSourceLanguageLabel(sourceLanguage: { englishName?: string; nativeName?: string; printLanguage?: () => string }): string {
    if (!sourceLanguage) {
      return '';
    }
    if (typeof sourceLanguage.printLanguage === 'function') {
      return sourceLanguage.printLanguage();
    }
    if (sourceLanguage.englishName && sourceLanguage.nativeName && sourceLanguage.englishName !== sourceLanguage.nativeName) {
      return `${sourceLanguage.englishName} | ${sourceLanguage.nativeName}`;
    }
    return sourceLanguage.englishName || sourceLanguage.nativeName || '';
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
