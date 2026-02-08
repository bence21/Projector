import { Component, Output, EventEmitter } from '@angular/core';
import { MatDialog } from '@angular/material';
import { BaseWordActionButtonComponent } from '../base-word-action-button/base-word-action-button.component';
import { ReviewedWordStatus } from '../../models/reviewedWord';

@Component({
  selector: 'app-mark-as-good-button',
  templateUrl: './mark-as-good-button.component.html',
  styleUrls: ['./mark-as-good-button.component.css']
})
export class MarkAsGoodButtonComponent extends BaseWordActionButtonComponent {
  @Output() goodClick = new EventEmitter<void>();

  constructor(dialog: MatDialog) {
    super(dialog);
  }

  protected getStatusName(): string {
    return 'Good';
  }

  protected getTargetStatus(): ReviewedWordStatus {
    return ReviewedWordStatus.REVIEWED_GOOD;
  }

  protected emitAction(): void {
    this.goodClick.emit();
  }
}
