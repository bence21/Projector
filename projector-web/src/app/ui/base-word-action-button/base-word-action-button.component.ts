import { Input } from '@angular/core';
import { MatDialog } from '@angular/material';
import { WordWithStatus } from '../../models/wordWithStatus';
import { ReviewedWordStatus } from '../../models/reviewedWord';
import { ConfirmActionDialogComponent } from '../confirm-action-dialog/confirm-action-dialog.component';

export abstract class BaseWordActionButtonComponent {
  @Input() wordWithStatus?: WordWithStatus;
  @Input() disabled: boolean = false;

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
    if (this.disabled) {
      return;
    }
    if (this.isReviewed) {
      // Skip confirmation for AUTO_ACCEPTED_FROM_PUBLIC when changing to reviewed good, accept, or context accept
      const currentStatus = this.wordWithStatus && this.wordWithStatus.status;
      const targetStatus = this.getTargetStatus();
      const isAutoAcceptedFromPublic = currentStatus === ReviewedWordStatus.AUTO_ACCEPTED_FROM_PUBLIC;
      const isChangingToGoodStatus = targetStatus === ReviewedWordStatus.REVIEWED_GOOD ||
                                      targetStatus === ReviewedWordStatus.ACCEPTED ||
                                      targetStatus === ReviewedWordStatus.CONTEXT_SPECIFIC;
      
      if (isAutoAcceptedFromPublic && isChangingToGoodStatus) {
        // No confirmation needed - directly emit the action
        this.emitAction();
        return;
      }
      
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
