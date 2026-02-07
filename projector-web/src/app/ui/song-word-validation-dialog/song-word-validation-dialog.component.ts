import { Component, Inject, OnInit } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialog, MatDialogRef } from '@angular/material';
import { SongWordValidationResult } from '../../models/songWordValidationResult';
import { RejectedWordSuggestion } from '../../models/rejectedWordSuggestion';
import { ReviewedWordDataService } from '../../services/reviewed-word-data.service';
import { ReviewedWord, ReviewedWordStatus } from '../../models/reviewedWord';
import { Language } from '../../models/language';
import { WordReviewDialogComponent } from '../word-review-dialog/word-review-dialog.component';
import { MatSnackBar } from '@angular/material';

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

  constructor(
    private dialogRef: MatDialogRef<SongWordValidationDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: { validationResult: SongWordValidationResult; language: Language; publish?: boolean },
    private reviewedWordDataService: ReviewedWordDataService,
    private dialog: MatDialog,
    private snackBar: MatSnackBar
  ) {
    this.validationResult = data.validationResult;
    this.language = data.language;
    this.publish = data.publish || false;
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
            // Remove from unreviewed list
            const index = this.validationResult.unreviewedWords.indexOf(word);
            if (index > -1) {
              this.validationResult.unreviewedWords.splice(index, 1);
              this.updateHasIssues();
            }
          },
          (err) => {
            this.snackBar.open('Error reviewing word', 'Close', { duration: 3000 });
          }
        );
      }
    });
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
  }
}
