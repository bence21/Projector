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
  /**
   * When true (and the parent sets this after choosing the admin "All" option), the select shows "All" instead of
   * the last concrete language. Optional; when unset, behavior matches the historical "All" action (load all, still
   * show the previous language in the trigger).
   */
  @Input() allLanguagesSelected: boolean = false;
  @Input() darkTheme: boolean = false;
  @Output() selectedLanguageChange = new EventEmitter<Language>();
  @Output() allSelected = new EventEmitter<void>();

  constructor(public auth: AuthService) {
  }

  onLanguageChange(event: any) {
    const selectedValue = event.value;
    if (selectedValue === 'All') {
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
