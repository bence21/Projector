import { MatDialog, MatSnackBar } from '@angular/material';
import { Language } from '../models/language';
import { NewLanguageComponent } from '../ui/new-language/new-language.component';

export const LANGUAGE_ALREADY_EXISTS_MESSAGE =
  'This language already exists — it has been selected for you.';

export const LANGUAGE_ALREADY_EXISTS_SNACKBAR_DURATION_MS = 4000;

export interface NewLanguageDialogResult {
  existing: Language;
}

export type NewLanguageDialogCloseResult = 'ok' | NewLanguageDialogResult;

export interface LanguageSelectionOptions {
  selectUuid?: string;
  selectLast?: boolean;
  fallbackUuid?: string;
}

export function selectLanguageFromList(
  languages: Language[],
  options: LanguageSelectionOptions
): Language | undefined {
  if (options.selectUuid) {
    return languages.find(language => language.uuid === options.selectUuid);
  }
  if (options.selectLast && languages.length > 0) {
    return languages[languages.length - 1];
  }
  if (options.fallbackUuid) {
    return languages.find(language => language.uuid === options.fallbackUuid);
  }
  return undefined;
}

export function openNewLanguageDialog(
  dialog: MatDialog,
  snackBar: MatSnackBar,
  onLanguagesReload: (selectLast: boolean, selectUuid?: string) => void
): void {
  const dialogRef = dialog.open(NewLanguageComponent);
  dialogRef.afterClosed().subscribe((result: NewLanguageDialogCloseResult) => {
    if (result === 'ok') {
      onLanguagesReload(true);
    } else if (result && result.existing) {
      onLanguagesReload(false, result.existing.uuid);
      snackBar.open(LANGUAGE_ALREADY_EXISTS_MESSAGE, 'Close', {
        duration: LANGUAGE_ALREADY_EXISTS_SNACKBAR_DURATION_MS
      });
    }
  });
}
