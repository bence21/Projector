import { FormControl, FormGroup } from "@angular/forms";
import { SectionType, Song, SongVerseUI } from "../services/song-service.service";
import { SongWordValidationService } from "../services/song-word-validation.service";
import { MatDialog, MatSnackBar } from "@angular/material";
import { Language } from "../models/language";
import { SongWordValidationDialogComponent } from "../ui/song-word-validation-dialog/song-word-validation-dialog.component";

export function calculateOrder_(customSectionOrder: boolean, song: Song, usedSectionTypes: { name: string; type: SectionType; text: string; verse: SongVerseUI; index: number; }[]) {
  let sectionOrder = [];
  if (customSectionOrder) {
    sectionOrder = [];
    for (const sectionIndex of song.verseOrderList) {
      for (const usedSection of usedSectionTypes) {
        if (usedSection.index == sectionIndex) {
          sectionOrder.push(usedSection);
          break;
        }
      }
    }
  } else {
    sectionOrder = [];
    let chorus = null;
    let delta = 1;
    for (const usedSection of usedSectionTypes) {
      if (usedSection.type == SectionType.Chorus) {
        chorus = usedSection;
        delta = 0;
      } else {
        if (chorus != null && delta > 0) {
          sectionOrder.push(chorus);
        }
        ++delta;
      }
      sectionOrder.push(usedSection);
    }
    if (sectionOrder.length > 0) {
      const type = sectionOrder[sectionOrder.length - 1].type;
      if (chorus != null && type != SectionType.Chorus && type != SectionType.Coda && delta > 0) {
        sectionOrder.push(chorus);
      }
    }
  }
  return sectionOrder;
}

export function addNewVerse_(verses: SongVerseUI[], verseControls: FormControl[], form: FormGroup, song: Song): FormControl {
  const control = new FormControl('');
  let section = new SongVerseUI();
  section.type = SectionType.Verse;
  verses.push(section);
  verseControls.push(control);
  const index = verses.length - 1;
  form.addControl('verse' + (index), control);
  if (song.verseOrderList == undefined) {
    song.verseOrderList = [];
  }
  song.verseOrderList.push(index);
  return control;
}

export interface WordValidationConfig {
  song: Song;
  validationService: SongWordValidationService;
  dialog: MatDialog;
  snackBar: MatSnackBar;
  language: Language;
  publish: boolean;
  onSave: () => void;
  isAdmin?: boolean;
}

export function validateWordsAndSave(config: WordValidationConfig): void {
  const { song, validationService, dialog, snackBar, language, publish, onSave, isAdmin } = config;

  // Skip validation if user is not an admin
  if (!isAdmin) {
    onSave();
    return;
  }

  validationService.validateWords(song).subscribe(
    (validationResult) => {
      if (validationResult.hasIssues) {
        // Show informative message when trying to publish
        if (publish) {
          snackBar.open('Please review and resolve word issues before publishing. Opening validation dialog...', 'Close', {
            duration: 4000
          });
        }
        // Always show validation dialog when there are issues
        const dialogRef = dialog.open(SongWordValidationDialogComponent, {
          width: '700px',
          data: { validationResult: validationResult, language: language, publish: publish }
        });

        dialogRef.afterClosed().subscribe(result => {
          if (result && result.proceed) {
            // User wants to proceed with issues - save as non-public
            song.uploaded = false;
            onSave();
          }
          // If user cancelled, do nothing
        });
      } else {
        // No issues, proceed with save
        onSave();
      }
    },
    (err) => {
      console.error('Error validating words:', err);
      // On error, proceed with save anyway
      onSave();
    }
  );
}