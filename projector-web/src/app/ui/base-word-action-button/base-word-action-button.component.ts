import { Input } from '@angular/core';
import { MatDialog } from '@angular/material';
import { WordWithStatus } from '../../models/wordWithStatus';
import { ReviewedWordStatus } from '../../models/reviewedWord';
import { ConfirmActionDialogComponent } from '../confirm-action-dialog/confirm-action-dialog.component';

export abstract class BaseWordActionButtonComponent {
  @Input() wordWithStatus?: WordWithStatus;

  constructor(protected dialog: MatDialog) {}

  get isReviewed(): boolean {
    return this.wordWithStatus && this.wordWithStatus.status !== ReviewedWordStatus.UNREVIEWED;
  }

  /**
   * Returns true if the button should be visible.
   * By default, hides the button if the word already has this status.
   */
  get visible(): boolean {
    const targetStatus = this.getTargetStatus();
    return !this.wordWithStatus || this.wordWithStatus.status !== targetStatus;
  }

  protected abstract getStatusName(): string;
  protected abstract getTargetStatus(): ReviewedWordStatus;
  protected abstract emitAction(): void;

  onClick(): void {
    if (this.isReviewed) {
      const word = (this.wordWithStatus && this.wordWithStatus.word) ? this.wordWithStatus.word : 'this word';
      const statusName = this.getStatusName();
      
      const dialogRef = this.dialog.open(ConfirmActionDialogComponent, {
        width: '400px',
        data: {
          title: 'Confirm Action',
          message: `The word "${word}" is already reviewed. Are you sure you want to change its status to ${statusName}?`,
          confirmText: 'Yes',
          cancelText: 'No'
        }
      });

      dialogRef.afterClosed().subscribe(result => {
        if (result) {
          this.emitAction();
        }
      });
    } else {
      this.emitAction();
    }
  }
}
