import { Injectable } from '@angular/core';
import { MatDialog } from '@angular/material';
import { MatSnackBar } from '@angular/material';
import { Observable } from 'rxjs/Observable';
import { Language } from '../models/language';
import { ReviewedWord, ReviewedWordStatus } from '../models/reviewedWord';
import { ReviewedWordDataService } from './reviewed-word-data.service';
import { WordReviewDialogComponent } from '../ui/word-review-dialog/word-review-dialog.component';
import { WordContextDialogComponent } from '../ui/word-context-dialog/word-context-dialog.component';
import { generalError } from '../util/error-util';

export interface WordReviewOptions {
  language: Language;
  word: string;
  successMessage: string;
  onSuccess?: () => void;
  onError?: (err: any) => void;
}

@Injectable()
export class WordReviewHelperService {
  constructor(
    private reviewedWordDataService: ReviewedWordDataService,
    private snackBar: MatSnackBar,
    private dialog: MatDialog
  ) {}

  /**
   * Marks a word with a simple status (no dialog required).
   */
  markWordWithStatus(
    status: ReviewedWordStatus,
    options: WordReviewOptions
  ): void {
    const reviewedWord = new ReviewedWord();
    reviewedWord.word = options.word;
    reviewedWord.status = status;

    this.reviewedWordDataService.createOrUpdate(options.language, reviewedWord).subscribe(
      () => {
        this.snackBar.open(options.successMessage, 'Close', { duration: 3000 });
        if (options.onSuccess) {
          options.onSuccess();
        }
      },
      (err) => {
        if (options.onError) {
          options.onError(err);
        } else {
          generalError(null, null, err, this.dialog, this.snackBar);
        }
      }
    );
  }

  /**
   * Marks a word as accepted after user provides category and notes via dialog.
   */
  markAsAccepted(options: WordReviewOptions): void {
    const dialogRef = this.dialog.open(WordReviewDialogComponent, {
      width: '500px',
      data: { word: options.word, language: options.language }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        const reviewedWord = new ReviewedWord();
        reviewedWord.word = options.word;
        reviewedWord.status = ReviewedWordStatus.ACCEPTED;
        reviewedWord.category = result.category;
        reviewedWord.notes = result.notes;
        if (result.sourceLanguage) {
          reviewedWord.sourceLanguage = result.sourceLanguage;
        }
        if (result.foreignLanguageType) {
          reviewedWord.foreignLanguageType = result.foreignLanguageType;
        }

        this.reviewedWordDataService.createOrUpdate(options.language, reviewedWord).subscribe(
          () => {
            this.snackBar.open(options.successMessage, 'Close', { duration: 3000 });
            if (options.onSuccess) {
              options.onSuccess();
            }
          },
          (err) => {
            if (options.onError) {
              options.onError(err);
            } else {
              generalError(null, null, err, this.dialog, this.snackBar);
            }
          }
        );
      }
    });
  }

  /**
   * Marks a word as context-specific after user provides context details via dialog.
   */
  markAsContextSpecific(options: WordReviewOptions): void {
    const dialogRef = this.dialog.open(WordContextDialogComponent, {
      width: '500px',
      data: { word: options.word, language: options.language }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        const reviewedWord = new ReviewedWord();
        reviewedWord.word = options.word;
        reviewedWord.status = ReviewedWordStatus.CONTEXT_SPECIFIC;
        reviewedWord.contextCategory = result.contextCategory;
        reviewedWord.contextDescription = result.contextDescription;
        reviewedWord.notes = result.notes;

        this.reviewedWordDataService.createOrUpdate(options.language, reviewedWord).subscribe(
          () => {
            this.snackBar.open(options.successMessage, 'Close', { duration: 3000 });
            if (options.onSuccess) {
              options.onSuccess();
            }
          },
          (err) => {
            if (options.onError) {
              options.onError(err);
            } else {
              generalError(null, null, err, this.dialog, this.snackBar);
            }
          }
        );
      }
    });
  }
}
