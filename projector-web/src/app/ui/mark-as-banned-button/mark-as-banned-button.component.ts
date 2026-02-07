import { Component, Output, EventEmitter } from '@angular/core';
import { MatDialog } from '@angular/material';
import { BaseWordActionButtonComponent } from '../base-word-action-button/base-word-action-button.component';
import { ReviewedWordStatus } from '../../models/reviewedWord';

@Component({
  selector: 'app-mark-as-banned-button',
  templateUrl: './mark-as-banned-button.component.html',
  styleUrls: ['./mark-as-banned-button.component.css']
})
export class MarkAsBannedButtonComponent extends BaseWordActionButtonComponent {
  @Output() bannedClick = new EventEmitter<void>();

  constructor(dialog: MatDialog) {
    super(dialog);
  }

  protected getStatusName(): string {
    return 'Banned';
  }

  protected getTargetStatus(): ReviewedWordStatus {
    return ReviewedWordStatus.BANNED;
  }

  protected emitAction(): void {
    this.bannedClick.emit();
  }
}
