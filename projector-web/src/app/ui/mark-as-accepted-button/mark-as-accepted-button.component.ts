import { Component, Output, EventEmitter } from '@angular/core';
import { MatDialog } from '@angular/material';
import { BaseWordActionButtonComponent } from '../base-word-action-button/base-word-action-button.component';
import { ReviewedWordStatus } from '../../models/reviewedWord';

@Component({
  selector: 'app-mark-as-accepted-button',
  templateUrl: './mark-as-accepted-button.component.html',
  styleUrls: ['./mark-as-accepted-button.component.css']
})
export class MarkAsAcceptedButtonComponent extends BaseWordActionButtonComponent {
  @Output() acceptedClick = new EventEmitter<void>();

  constructor(dialog: MatDialog) {
    super(dialog);
  }

  protected getStatusName(): string {
    return 'Accepted';
  }

  protected getTargetStatus(): ReviewedWordStatus {
    return ReviewedWordStatus.ACCEPTED;
  }

  protected emitAction(): void {
    this.acceptedClick.emit();
  }
}
