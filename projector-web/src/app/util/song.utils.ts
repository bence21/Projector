import { FormControl, FormGroup } from "@angular/forms";
import { SectionType, Song, SongVerseUI } from "../services/song-service.service";
import { SongWordValidationService } from "../services/song-word-validation.service";
import { MatDialog, MatSnackBar } from "@angular/material";
import { Language } from "../models/language";
import { SongWordValidationDialogComponent } from "../ui/song-word-validation-dialog/song-word-validation-dialog.component";
import { ConfirmActionDialogComponent } from "../ui/confirm-action-dialog/confirm-action-dialog.component";
import { isSongPublic } from './song-public.util';

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
}

export function validateWordsAndSave(config: WordValidationConfig): void {
  const { song, validationService, dialog, snackBar, language, publish, onSave } = config;

  // Check if song is currently public (before validation)
  const wasPublic = isSongPublic(song);

  validationService.validateWords(song).subscribe(
    (validationResult) => {
      const hasBlockingIssues =
        (validationResult.bannedWords && validationResult.bannedWords.length > 0) ||
        (validationResult.rejectedWords && validationResult.rejectedWords.length > 0);

      if (validationResult.hasIssues) {
        // If song is already public and user is trying to publish, just ask for confirmation
        if (publish && wasPublic && !hasBlockingIssues) {
          const confirmDialogRef = dialog.open(ConfirmActionDialogComponent, {
            width: '500px',
            data: {
              title: 'Confirm Publish',
              message: 'There are unreviewed words, but you can still publish the changes. Do you want to proceed?',
              confirmText: 'Yes, Publish',
              cancelText: 'Cancel'
            }
          });

          confirmDialogRef.afterClosed().subscribe(confirmed => {
            if (confirmed) {
              // User confirmed - proceed with save (keeping it public)
              onSave();
            }
            // If user cancelled, do nothing
          });
          return;
        }

        // Show informative message when trying to publish
        if (publish) {
          const publishMessage = hasBlockingIssues
            ? 'Please review and resolve banned or rejected word issues before publishing. Opening validation dialog...'
            : 'Unreviewed words were found. You can still publish, or review them first. Opening validation dialog...';
          snackBar.open(publishMessage, 'Close', {
            duration: 4000
          });
        }
        // Always show validation dialog when there are issues
        const dialogRef = dialog.open(SongWordValidationDialogComponent, {
          width: '700px',
          data: { validationResult: validationResult, language: language, publish: publish, song: song, validationService: validationService }
        });

        dialogRef.afterClosed().subscribe(result => {
          if (result && result.proceed) {
            // Keep publish intent when only unreviewed words remain; otherwise fall back to draft.
            song.uploaded = result.saveAsDraft ? false : publish;
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