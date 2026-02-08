import { Component, Output, EventEmitter } from '@angular/core';
import { MatDialog } from '@angular/material';
import { BaseWordActionButtonComponent } from '../base-word-action-button/base-word-action-button.component';
import { ReviewedWordStatus } from '../../models/reviewedWord';

@Component({
  selector: 'app-mark-as-rejected-button',
  templateUrl: './mark-as-rejected-button.component.html',
  styleUrls: ['./mark-as-rejected-button.component.css']
})
export class MarkAsRejectedButtonComponent extends BaseWordActionButtonComponent {
  @Output() rejectedClick = new EventEmitter<void>();

  constructor(dialog: MatDialog) {
    super(dialog);
  }

  protected getStatusName(): string {
    return 'Rejected';
  }

  protected getTargetStatus(): ReviewedWordStatus {
    return ReviewedWordStatus.REJECTED;
  }

  protected emitAction(): void {
    this.rejectedClick.emit();
  }
}
