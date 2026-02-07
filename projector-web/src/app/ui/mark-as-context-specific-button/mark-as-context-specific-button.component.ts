import { Component, Output, EventEmitter } from '@angular/core';
import { MatDialog } from '@angular/material';
import { BaseWordActionButtonComponent } from '../base-word-action-button/base-word-action-button.component';
import { ReviewedWordStatus } from '../../models/reviewedWord';

@Component({
  selector: 'app-mark-as-context-specific-button',
  templateUrl: './mark-as-context-specific-button.component.html',
  styleUrls: ['./mark-as-context-specific-button.component.css']
})
export class MarkAsContextSpecificButtonComponent extends BaseWordActionButtonComponent {
  @Output() contextSpecificClick = new EventEmitter<void>();

  constructor(dialog: MatDialog) {
    super(dialog);
  }

  protected getStatusName(): string {
    return 'Context-Specific';
  }

  protected getTargetStatus(): ReviewedWordStatus {
    return ReviewedWordStatus.CONTEXT_SPECIFIC;
  }

  protected emitAction(): void {
    this.contextSpecificClick.emit();
  }
}
