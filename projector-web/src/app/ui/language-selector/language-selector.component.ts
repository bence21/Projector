import { Component, Input, Output, EventEmitter } from '@angular/core';
import { Language } from '../../models/language';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-language-selector',
  templateUrl: './language-selector.component.html',
  styleUrls: ['./language-selector.component.css']
})
export class LanguageSelectorComponent {
  @Input() languages: Language[] = [];
  @Input() selectedLanguage: Language;
  @Input() darkTheme: boolean = false;
  @Output() selectedLanguageChange = new EventEmitter<Language>();
  @Output() allSelected = new EventEmitter<void>();

  constructor(public auth: AuthService) {
  }

  onLanguageChange(event: any) {
    const selectedValue = event.value;
    if (selectedValue === 'All') {
      // Don't update selectedLanguage when "All" is selected
      // Reset the select to show the current selectedLanguage
      event.source.value = this.selectedLanguage;
      this.onAllSelected();
    } else {
      // Update selectedLanguage and emit the change
      this.selectedLanguage = selectedValue;
      this.selectedLanguageChange.emit(this.selectedLanguage);
    }
  }

  onAllSelected() {
    this.allSelected.emit();
  }
}
