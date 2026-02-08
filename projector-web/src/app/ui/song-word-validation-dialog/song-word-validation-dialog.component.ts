import { Component, Inject, OnInit } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialog, MatDialogRef } from '@angular/material';
import { SongWordValidationResult } from '../../models/songWordValidationResult';
import { RejectedWordSuggestion } from '../../models/rejectedWordSuggestion';
import { ReviewedWordDataService } from '../../services/reviewed-word-data.service';
import { Language } from '../../models/language';
import { MatSnackBar } from '@angular/material';
import { WordReviewHelperService } from '../../services/word-review-helper.service';
import { WordWithStatus } from '../../models/wordWithStatus';
import { AuthService } from '../../services/auth.service';
import { ReviewedWordStatus } from '../../models/reviewedWord';
import { Song } from '../../services/song-service.service';
import { SongWordValidationService } from '../../services/song-word-validation.service';

@Component({
  selector: 'app-song-word-validation-dialog',
  templateUrl: './song-word-validation-dialog.component.html',
  styleUrls: ['./song-word-validation-dialog.component.css']
})
export class SongWordValidationDialogComponent implements OnInit {
  validationResult: SongWordValidationResult;
  language: Language;
  publish: boolean = false;
  selectedRejections: Map<string, string> = new Map(); // word -> selected suggestion
  hasReviewerRole = false;
  loading = false;
  inputPaused = false;
  
  // Cached arrays to avoid infinite change detection loops
  unreviewedWordsWithStatus: WordWithStatus[] = [];
  rejectedWordsWithStatus: WordWithStatus[] = [];
  bannedWordsWithStatus: WordWithStatus[] = [];

  readonly ReviewedWordStatus = ReviewedWordStatus;

  private song: Song;
  private validationService: SongWordValidationService;

  constructor(
    private dialogRef: MatDialogRef<SongWordValidationDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { validationResult: SongWordValidationResult; language: Language; publish?: boolean; song?: Song; validationService?: SongWordValidationService },
    private reviewedWordDataService: ReviewedWordDataService,
    private dialog: MatDialog,
    private snackBar: MatSnackBar,
    private wordReviewHelper: WordReviewHelperService,
    private auth: AuthService
  ) {
    this.validationResult = data.validationResult;
    this.language = data.language;
    this.publish = data.publish || false;
    this.song = data.song;
    this.validationService = data.validationService;
  }

  ngOnInit() {
    // Initialize selected suggestions for rejected words
    if (this.validationResult.rejectedWords) {
      for (const rejected of this.validationResult.rejectedWords) {
        if (rejected.primarySuggestion) {
          this.selectedRejections.set(rejected.word, rejected.primarySuggestion);
        }
      }
    }
    this.updateHasReviewerRole();
    this.updateWordsWithStatus();
  }

  private updateHasReviewerRole(): void {
    const user = this.auth.getUser();
    this.hasReviewerRole = !!(user && this.language && user.hasReviewerRoleForLanguage(this.language));
  }

  getWordWithStatus(word: string, status: ReviewedWordStatus, rejectedWord?: RejectedWordSuggestion): WordWithStatus {
    let suggestions: string[] | undefined;
    if (rejectedWord) {
      suggestions = [];
      if (rejectedWord.primarySuggestion) {
        suggestions.push(rejectedWord.primarySuggestion);
      }
      if (rejectedWord.alternativeSuggestions) {
        suggestions.push(...rejectedWord.alternativeSuggestions);
      }
    }
    return {
      word: word,
      status: status,
      suggestions: suggestions
    };
  }

  getUnreviewedWordsWithStatus(): WordWithStatus[] {
    return this.unreviewedWordsWithStatus;
  }

  getBannedWordsWithStatus(): WordWithStatus[] {
    return this.bannedWordsWithStatus;
  }

  private updateWordsWithStatus(): void {
    // Use wordsWithStatus from the result if available, as it contains count information
    // Otherwise fall back to creating objects from the separate arrays
    if (this.validationResult.wordsWithStatus && this.validationResult.wordsWithStatus.length > 0) {
      // Filter wordsWithStatus by status to get the full objects with counts
      this.unreviewedWordsWithStatus = this.validationResult.wordsWithStatus.filter(
        w => w.status === ReviewedWordStatus.UNREVIEWED
      );
      
      this.rejectedWordsWithStatus = this.validationResult.wordsWithStatus.filter(
        w => w.status === ReviewedWordStatus.REJECTED
      );
      
      this.bannedWordsWithStatus = this.validationResult.wordsWithStatus.filter(
        w => w.status === ReviewedWordStatus.BANNED
      );
    } else {
      // Fallback: create objects from separate arrays (without counts)
      // Update unreviewed words with status
      if (!this.validationResult.unreviewedWords) {
        this.unreviewedWordsWithStatus = [];
      } else {
        this.unreviewedWordsWithStatus = this.validationResult.unreviewedWords.map(word => ({
          word: word,
          status: ReviewedWordStatus.UNREVIEWED
        }));
      }
      
      // Update rejected words with status
      if (!this.validationResult.rejectedWords) {
        this.rejectedWordsWithStatus = [];
      } else {
        this.rejectedWordsWithStatus = this.validationResult.rejectedWords.map(rejected => {
          const suggestions: string[] = [];
          if (rejected.primarySuggestion) {
            suggestions.push(rejected.primarySuggestion);
          }
          if (rejected.alternativeSuggestions) {
            suggestions.push(...rejected.alternativeSuggestions);
          }
          return {
            word: rejected.word,
            status: ReviewedWordStatus.REJECTED,
            suggestions: suggestions.length > 0 ? suggestions : undefined
          };
        });
      }
      
      // Update banned words with status
      if (!this.validationResult.bannedWords) {
        this.bannedWordsWithStatus = [];
      } else {
        this.bannedWordsWithStatus = this.validationResult.bannedWords.map(word => ({
          word: word,
          status: ReviewedWordStatus.BANNED
        }));
      }
    }
  }

  getRejectedWordsWithStatus(): WordWithStatus[] {
    return this.rejectedWordsWithStatus;
  }

  getUnreviewedCount(): number {
    return this.validationResult.unreviewedWords ? this.validationResult.unreviewedWords.length : 0;
  }

  getBannedCount(): number {
    return this.validationResult.bannedWords ? this.validationResult.bannedWords.length : 0;
  }

  getRejectedCount(): number {
    return this.validationResult.rejectedWords ? this.validationResult.rejectedWords.length : 0;
  }

  markAsGood(word: string) {
    this.wordReviewHelper.markWordWithStatus(ReviewedWordStatus.REVIEWED_GOOD, {
      language: this.language,
      word: word,
      onSuccess: () => this.removeWordFromIssues(word)
    });
  }

  markAsAccepted(word: string) {
    this.wordReviewHelper.markAsAccepted({
      language: this.language,
      word: word,
      onSuccess: () => this.removeWordFromIssues(word)
    });
  }

  markAsContextSpecific(word: string) {
    this.wordReviewHelper.markAsContextSpecific({
      language: this.language,
      word: word,
      onSuccess: () => this.removeWordFromIssues(word)
    });
  }

  markAsBanned(word: string) {
    this.wordReviewHelper.markWordWithStatus(ReviewedWordStatus.BANNED, {
      language: this.language,
      word: word,
      onSuccess: () => this.removeWordFromIssues(word)
    });
  }

  markAsRejected(word: string) {
    this.wordReviewHelper.markWordWithStatus(ReviewedWordStatus.REJECTED, {
      language: this.language,
      word: word,
      onSuccess: () => this.removeWordFromIssues(word)
    });
  }

  private removeWordFromIssues(word: string) {
    // Optimistically remove from cache immediately
    this.unreviewedWordsWithStatus = this.unreviewedWordsWithStatus.filter(w => w.word !== word);
    this.rejectedWordsWithStatus = this.rejectedWordsWithStatus.filter(w => w.word !== word);
    this.bannedWordsWithStatus = this.bannedWordsWithStatus.filter(w => w.word !== word);
    
    // Also update the validationResult arrays for consistency
    if (this.validationResult.unreviewedWords) {
      this.validationResult.unreviewedWords = this.validationResult.unreviewedWords.filter(w => w !== word);
    }
    if (this.validationResult.rejectedWords) {
      this.validationResult.rejectedWords = this.validationResult.rejectedWords.filter(r => r.word !== word);
    }
    if (this.validationResult.bannedWords) {
      this.validationResult.bannedWords = this.validationResult.bannedWords.filter(w => w !== word);
    }
    this.updateHasIssues();
    
    // Reload validation result from server in background
    this.reloadValidationResult();
  }

  private reloadValidationResult() {
    // Only reload if we have both song and validationService
    if (!this.song || !this.validationService) {
      // Fallback to old behavior if song/service not available
      this.updateWordsWithStatus();
      return;
    }

    // Delay showing loading indicator for 4-5 seconds
    const loadingTimeout = setTimeout(() => {
      this.loading = true;
    }, 4000);

    this.validationService.validateWords(this.song).subscribe(
      (result) => {
        // Clear the loading timeout since we got the result
        clearTimeout(loadingTimeout);
        this.loading = false;
        
        this.validationResult = result;
        // Reinitialize selected suggestions for rejected words
        this.selectedRejections.clear();
        if (this.validationResult.rejectedWords) {
          for (const rejected of this.validationResult.rejectedWords) {
            if (rejected.primarySuggestion) {
              this.selectedRejections.set(rejected.word, rejected.primarySuggestion);
            }
          }
        }
        this.updateHasIssues();
        this.updateWordsWithStatus();
        
        // Pause user input for 200ms after reload completes
        this.inputPaused = true;
        setTimeout(() => {
          this.inputPaused = false;
        }, 200);
      },
      (err) => {
        console.error('Error reloading word validation:', err);
        clearTimeout(loadingTimeout);
        this.loading = false;
        // Fallback to updating cached arrays on error
        this.updateWordsWithStatus();
      }
    );
  }

  onSelectSuggestion(rejectedWord: RejectedWordSuggestion, suggestion: string) {
    this.selectedRejections.set(rejectedWord.word, suggestion);
  }

  onProceedAnyway() {
    // If trying to publish and issues remain, don't allow proceeding
    if (this.publish && this.validationResult.hasIssues) {
      this.snackBar.open('Cannot publish with unresolved word issues. Please fix all issues first.', 'Close', {
        duration: 4000
      });
      return;
    }
    // Return the validation result with any selected replacements
    const result = {
      proceed: true,
      selectedReplacements: Array.from(this.selectedRejections.entries()).map(([word, replacement]) => ({
        word: word,
        replacement: replacement
      }))
    };
    this.dialogRef.close(result);
  }

  canProceed(): boolean {
    // Can only proceed if not publishing or if there are no issues
    return !this.publish || !this.validationResult.hasIssues;
  }

  onCancel() {
    this.dialogRef.close({ proceed: false });
  }

  private updateHasIssues() {
    this.validationResult.hasIssues = 
      (this.validationResult.unreviewedWords && this.validationResult.unreviewedWords.length > 0) ||
      (this.validationResult.bannedWords && this.validationResult.bannedWords.length > 0) ||
      (this.validationResult.rejectedWords && this.validationResult.rejectedWords.length > 0);
    
    // Auto-close dialog if no issues and not loading
    // Add a brief delay so the user can see the "No word issues found" message
    if (!this.validationResult.hasIssues && !this.loading) {
      setTimeout(() => {
        this.dialogRef.close({ proceed: true });
      }, 500);
    }
  }
}
