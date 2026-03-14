import { Component, Input, Output, EventEmitter, OnDestroy } from '@angular/core';
import { WordWithStatus } from '../../models/wordWithStatus';
import { ReviewedWordStatus } from '../../models/reviewedWord';
import {
  getStatusTooltip as buildStatusTooltip,
  getSourceLanguageLabel as getSourceLanguageLabelFromUtil
} from '../../util/word-with-status-tooltip.util';

@Component({
  selector: 'app-word-list',
  templateUrl: './word-list.component.html',
  styleUrls: ['./word-list.component.css']
})
export class WordListComponent implements OnDestroy {
  @Input() words: WordWithStatus[] = [];
  /** Word text used for showing brief copy highlight; cleared after a short delay. */
  lastCopiedWord: string | null = null;
  private copyHighlightTimeout: number | null = null;

  ngOnDestroy(): void {
    if (this.copyHighlightTimeout != null) {
      clearTimeout(this.copyHighlightTimeout);
    }
  }

  @Input() hasReviewerRole: boolean = false;
  @Input() disabled: boolean = false;
  /** When true, "only in this song" is shown when countInSong === countInAllSongs. When false, that case is not treated as only-in-this-song (e.g. song not yet public). */
  @Input() songIsPublic: boolean = false;
  /** When false, the suggestions row (e.g. "Suggestions: ...") is hidden. Used in Word Validation dialog where replacements are chosen separately. */
  @Input() showSuggestions: boolean = true;
  @Output() goodClick = new EventEmitter<string>();
  @Output() contextSpecificClick = new EventEmitter<string>();
  @Output() acceptedClick = new EventEmitter<string>();
  @Output() bannedClick = new EventEmitter<string>();
  @Output() rejectedClick = new EventEmitter<string>();
  @Output() notSureClick = new EventEmitter<string>();

  readonly ReviewedWordStatus = ReviewedWordStatus;

  private readonly COMMON_WORD_THRESHOLD = 10;

  private static isGoodStatus(status: ReviewedWordStatus): boolean {
    return status === ReviewedWordStatus.REVIEWED_GOOD ||
      status === ReviewedWordStatus.CONTEXT_SPECIFIC ||
      status === ReviewedWordStatus.ACCEPTED ||
      status === ReviewedWordStatus.AUTO_ACCEPTED_FROM_PUBLIC ||
      status === ReviewedWordStatus.AUTO_ACCEPTED_FROM_BIBLE;
  }

  getStatusIcon(status: ReviewedWordStatus): string {
    switch (status) {
      case ReviewedWordStatus.REVIEWED_GOOD:
      case ReviewedWordStatus.CONTEXT_SPECIFIC:
      case ReviewedWordStatus.ACCEPTED:
        return 'check_circle';
      case ReviewedWordStatus.AUTO_ACCEPTED_FROM_PUBLIC:
      case ReviewedWordStatus.AUTO_ACCEPTED_FROM_BIBLE:
        return 'verified';
      case ReviewedWordStatus.UNREVIEWED: return 'info';
      case ReviewedWordStatus.NOT_SURE: return 'info';
      case ReviewedWordStatus.BANNED: return 'error';
      case ReviewedWordStatus.REJECTED: return 'warning';
      default: return 'help';
    }
  }

  isGoodStatus(status: ReviewedWordStatus): boolean {
    return WordListComponent.isGoodStatus(status);
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
      case ReviewedWordStatus.AUTO_ACCEPTED_FROM_BIBLE:
        return 'status-icon-auto-accepted';
      case ReviewedWordStatus.BANNED:
        return 'status-icon-banned';
      case ReviewedWordStatus.REJECTED:
        return 'status-icon-rejected';
      case ReviewedWordStatus.UNREVIEWED:
        return 'status-icon-unreviewed';
      case ReviewedWordStatus.NOT_SURE:
        return 'status-icon-unreviewed';
      default:
        return '';
    }
  }

  getStatusTooltip(wordWithStatus: WordWithStatus): string {
    return buildStatusTooltip(wordWithStatus);
  }

  getSourceLanguageLabel(sourceLanguage: { englishName?: string; nativeName?: string; printLanguage?: () => string }): string {
    return getSourceLanguageLabelFromUtil(sourceLanguage);
  }

  private isCommonWord(count: number | null | undefined): boolean | null {
    if (count == null) {
      return null;
    }
    return count >= this.COMMON_WORD_THRESHOLD;
  }

  getCommonnessColorClass(count: number | null | undefined): string {
    const isCommon = this.isCommonWord(count);
    if (isCommon === null) {
      return '';
    }
    return isCommon ? 'common-word' : 'rare-word';
  }

  /**
   * True when the word appears only in the current song: countInAllSongs is 0,
   * or (when song is public) countInSong equals countInAllSongs.
   */
  isOnlyInThisSong(wordWithStatus: WordWithStatus): boolean {
    const total = wordWithStatus.countInAllSongs;
    if (total == null) {
      return false;
    }
    if (total === 0) {
      return true;
    }
    if (!this.songIsPublic) {
      return false;
    }
    const inSong = wordWithStatus.countInSong;
    return inSong != null && inSong === total;
  }

  getCommonnessTooltip(wordWithStatus: WordWithStatus): string {
    if (this.isOnlyInThisSong(wordWithStatus)) {
      return 'Only in this song';
    }
    const count = wordWithStatus.countInAllSongs;
    if (count == null) {
      return '';
    }
    return `${count.toLocaleString()} total occurrences in all songs`;
  }

  onGoodClick(word: string) {
    if (!this.disabled) {
      this.goodClick.emit(word);
    }
  }

  onContextSpecificClick(word: string) {
    if (!this.disabled) {
      this.contextSpecificClick.emit(word);
    }
  }

  onAcceptedClick(word: string) {
    if (!this.disabled) {
      this.acceptedClick.emit(word);
    }
  }

  onBannedClick(word: string) {
    if (!this.disabled) {
      this.bannedClick.emit(word);
    }
  }

  onRejectedClick(word: string) {
    if (!this.disabled) {
      this.rejectedClick.emit(word);
    }
  }

  onNotSureClick(word: string) {
    if (!this.disabled) {
      this.notSureClick.emit(word);
    }
  }

  /**
   * Filters suggestions to only include those that are different from the current word.
   * Returns an empty array if no good suggestions exist (all suggestions are the same as the word).
   */
  getFilteredSuggestions(wordWithStatus: WordWithStatus): string[] {
    if (!wordWithStatus.suggestions || wordWithStatus.suggestions.length === 0) {
      return [];
    }

    const currentWord = wordWithStatus.word;
    const filtered = wordWithStatus.suggestions.filter(suggestion => {
      if (!suggestion) {
        return false;
      }
      // Only include suggestions that are different from the current word (case-sensitive)
      return suggestion !== currentWord;
    });

    // Return empty array if no good suggestions (all were the same as current word)
    return filtered.length > 0 ? filtered : [];
  }

  /**
   * Checks if suggestions should be shown for a word.
   * Shows suggestions for REJECTED and UNREVIEWED words, but only if there are filtered suggestions.
   */
  shouldShowSuggestions(wordWithStatus: WordWithStatus): boolean {
    const status = wordWithStatus.status;
    if (status !== ReviewedWordStatus.REJECTED && status !== ReviewedWordStatus.UNREVIEWED) {
      return false;
    }
    const filteredSuggestions = this.getFilteredSuggestions(wordWithStatus);
    return filteredSuggestions.length > 0;
  }

  /**
   * Copy text to clipboard. Returns true if copy was performed.
   */
  private copyToClipboard(text: string): boolean {
    if (text && navigator.clipboard && navigator.clipboard.writeText) {
      navigator.clipboard.writeText(text);
      return true;
    }
    return false;
  }

  /**
   * Shows brief highlight on the word span for the given word, then clears it after a short delay.
   */
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

  /**
   * Handles click: stops propagation and copies the given text to clipboard.
   * Returns true if copy was performed.
   */
  private copyOnClick(event: Event, text: string): boolean {
    event.preventDefault();
    event.stopPropagation();
    return this.copyToClipboard(text);
  }

  /**
   * Returns the word with the first character lowercased.
   */
  private static lowerFirstChar(word: string): string {
    if (word.length <= 1) {
      return word.toLowerCase();
    }
    return word.charAt(0).toLowerCase() + word.slice(1);
  }

  /**
   * Copy the word to clipboard. If the word is auto-capped (inherited or all occurrences),
   * copies the lower-first form; otherwise copies as displayed.
   */
  copyWord(event: Event, wordWithStatus: WordWithStatus): void {
    const text = WordWithStatus.isAutoCapped(wordWithStatus)
      ? WordListComponent.lowerFirstChar(wordWithStatus.word)
      : wordWithStatus.word;
    if (this.copyOnClick(event, text)) {
      this.setCopyHighlight(wordWithStatus.word);
    }
  }

  /**
   * Copy the word to clipboard with first character lowercased (for auto-capped form).
   */
  copyWordLowerFirst(event: Event, word: string): void {
    if (this.copyOnClick(event, WordListComponent.lowerFirstChar(word))) {
      this.setCopyHighlight(word);
    }
  }
}
