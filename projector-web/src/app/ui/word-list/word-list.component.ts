import { Component, Input, Output, EventEmitter } from '@angular/core';
import { WordWithStatus } from '../../models/wordWithStatus';
import { ReviewedWordStatus } from '../../models/reviewedWord';

@Component({
  selector: 'app-word-list',
  templateUrl: './word-list.component.html',
  styleUrls: ['./word-list.component.css']
})
export class WordListComponent {
  @Input() words: WordWithStatus[] = [];
  @Input() hasReviewerRole: boolean = false;
  @Input() disabled: boolean = false;
  @Output() goodClick = new EventEmitter<string>();
  @Output() contextSpecificClick = new EventEmitter<string>();
  @Output() acceptedClick = new EventEmitter<string>();
  @Output() bannedClick = new EventEmitter<string>();
  @Output() rejectedClick = new EventEmitter<string>();

  readonly ReviewedWordStatus = ReviewedWordStatus;

  private readonly COMMON_WORD_THRESHOLD = 10;

  private static isGoodStatus(status: ReviewedWordStatus): boolean {
    return status === ReviewedWordStatus.REVIEWED_GOOD ||
      status === ReviewedWordStatus.CONTEXT_SPECIFIC ||
      status === ReviewedWordStatus.ACCEPTED ||
      status === ReviewedWordStatus.AUTO_ACCEPTED_FROM_PUBLIC;
  }

  getStatusIcon(status: ReviewedWordStatus): string {
    switch (status) {
      case ReviewedWordStatus.REVIEWED_GOOD:
      case ReviewedWordStatus.CONTEXT_SPECIFIC:
      case ReviewedWordStatus.ACCEPTED:
        return 'check_circle';
      case ReviewedWordStatus.AUTO_ACCEPTED_FROM_PUBLIC:
        return 'verified';
      case ReviewedWordStatus.UNREVIEWED: return 'info';
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
        return 'status-icon-auto-accepted';
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
}
