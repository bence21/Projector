import { Component, Input, OnInit, ChangeDetectorRef } from '@angular/core';
import { Song, SongService, SongVerseDTO, SongVerseUI, SectionType } from '../../services/song-service.service';
import { FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { LanguageDataService } from "../../services/language-data.service";
import { NewLanguageComponent } from "../new-language/new-language.component";
import { MatDialog, MatIconRegistry, MatSnackBar } from "@angular/material";
import { DomSanitizer, SafeResourceUrl } from "@angular/platform-browser";
import { SubmitOrPublish, replace, valueRefactorable } from "../new-song/new-song.component";
import { AuthenticateComponent } from "../authenticate/authenticate.component";
import { CdkDragDrop, moveItemInArray, copyArrayItem } from '@angular/cdk/drag-drop';
import { AuthService } from '../../services/auth.service';
import { addNewVerse_, calculateOrder_, validateWordsAndSave } from '../../util/song.utils';
import { Language } from '../../models/language';
import { debounceTime } from 'rxjs/operators';
import { SongWordValidationService } from '../../services/song-word-validation.service';
import { normalizeForPersistence } from '../../util/unicode-text-normalizer';

@Component({
  selector: 'app-edit-song',
  templateUrl: './edit-song.component.html',
  styleUrls: ['./edit-song.component.css']
})
export class EditSongComponent implements OnInit {
  form: FormGroup;
  formErrors = {
    'title': '',
    'verseOrder': ''
  };

  validationMessages = {
    'title': {
      'required': 'Required field',
    },
    'verseOrder': {}
  };
  verses: SongVerseUI[];
  verseControls: FormControl[];
  languages = [];
  private _selectedLanguage: Language;
  set selectedLanguage(value: Language) {
    this._selectedLanguage = value;
    if (value) {
      this.selectedLanguageCopy = new Language();
      Object.assign(this.selectedLanguageCopy, value);
    } else {
      this.selectedLanguageCopy = null;
    }
  }
  get selectedLanguage(): Language {
    return this._selectedLanguage;
  }
  selectedLanguageCopy: Language;
  originalLanguage: Language;
  @Input()
  song: Song;
  editorType = 'verse';
  public youtubeUrl: string;
  public safeUrl: SafeResourceUrl = null;
  private songTextFormControl: FormControl;
  sectionTypes: {
    name: string;
    type: SectionType;
  }[];
  usedSectionTypes: {
    name: string;
    type: SectionType;
    text: string;
    verse: SongVerseUI;
    index: number;
  }[];
  sectionOrder: {
    name: string;
    type: SectionType;
    text: string;
    verse: SongVerseUI;
    index: number;
  }[];
  customSectionOrder = false;
  publish = false;
  formHasChanges = false;
  private initialFormValue: string;
  currentSongForWordList: Song;

  constructor(private fb: FormBuilder,
    private songService: SongService,
    private router: Router,
    private snackBar: MatSnackBar,
    private languageDataService: LanguageDataService,
    private dialog: MatDialog,
    iconRegistry: MatIconRegistry,
    public sanitizer: DomSanitizer,
    public auth: AuthService,
    private _changeDetectionRef: ChangeDetectorRef,
    private songWordValidationService: SongWordValidationService) {
    iconRegistry.addSvgIcon(
      'magic_tool',
      sanitizer.bypassSecurityTrustResourceUrl('assets/icons/magic_tool-icon.svg'));
    this.verses = [];
    this.languages = [];
    this.sectionTypes = [
      { name: 'Intro', type: SectionType.Intro },
      { name: 'Verse', type: SectionType.Verse },
      { name: 'Pre-Chorus', type: SectionType.Pre_chorus },
      { name: 'Chorus', type: SectionType.Chorus },
      { name: 'Bridge', type: SectionType.Bridge },
      { name: 'Coda', type: SectionType.Coda },
    ];
    this.usedSectionTypes = [];
    this.sectionOrder = [];
  }

  ngOnInit() {
    this.createForm();
    this.loadLanguage(false);
    this.calculateUsedSectionTypes();
    this.submitButtonOrPublish();
  }

  ngAfterViewChecked(): void {
    this._changeDetectionRef.detectChanges();
  }

  noReturnPredicate() {
    return false;
  }

  private submitButtonOrPublish() {
    const user = this.auth.getUser();
    if (user == undefined || user == null) {
      return;
    }
    this.publish = user.activated;
  }

  SubmitOrPublish() {
    return SubmitOrPublish(this.publish);
  }

  removeSectionOrder(i: number) {
    this.sectionOrder.splice(i, 1);
  }

  drop(event: CdkDragDrop<string[]>) {
    if (event.previousContainer === event.container) {
      moveItemInArray(event.container.data, event.previousIndex, event.currentIndex);
    } else {
      copyArrayItem(event.previousContainer.data,
        event.container.data,
        event.previousIndex,
        event.currentIndex);
    }
    this.customSectionOrder = true;
  }

  changeMe(chip, verse: SongVerseUI) {
    let vm = this;
    let editSongComponent = this;
    setTimeout(function () {
      verse.type = chip.type;
      vm._changeDetectionRef.detectChanges();
      editSongComponent.calculateUsedSectionTypes();
    }, 10)
  }

  calculateUsedSectionTypes() {
    let i = 0;
    this.usedSectionTypes = [];
    for (const verse of this.verses) {
      let aChip = this.sectionTypes[0];
      for (const sectionType of this.sectionTypes) {
        if (sectionType.type == verse.type) {
          aChip = sectionType;
          break;
        }
      }
      const usedSection = {
        name: this.getSectionName(aChip, verse, i),
        type: verse.type,
        text: this.form.value['verse' + i],
        verse: verse,
        index: i
      };
      this.usedSectionTypes.push(usedSection);
      ++i;
    }
    this.calculateOrder();
  }

  setCustomSectionOrder(x: boolean) {
    this.customSectionOrder = x;
    this.calculateOrder();
  }

  calculateOrder() {
    this.sectionOrder = calculateOrder_(this.customSectionOrder, this.song, this.usedSectionTypes);
  }

  getSectionName(chip, verse: SongVerseUI, k: number) {
    if (chip.type == verse.type) {
      let count = 1;
      for (let i = 0; i < k; ++i) {
        if (this.verses[i].type == chip.type) {
          ++count;
        }
      }
      let allCount = 0;
      for (const aVerse of this.verses) {
        if (aVerse.type == verse.type) {
          ++allCount;
        }
      }
      if (allCount > 1) {
        return chip.name + ' ' + count;
      }
    }
    return chip.name;
  }

  loadLanguage(selectLast: boolean) {
    this.languageDataService.getAll().subscribe(
      (languages) => {
        this.languages = languages;
        if (selectLast) {
          this.selectedLanguage = this.languages[this.languages.length - 1];
        } else {
          for (const language of this.languages) {
            if (language.uuid === this.song.languageDTO.uuid) {
              this.selectedLanguage = language;
              break;
            }
          }
        }
        this.originalLanguage = this.selectedLanguage;
      }
    );
  }

  onApplyLanguageButtonClick() {
    let song = new Song(this.song);
    song.languageDTO = this.selectedLanguage;
    const role = this.auth.getUser().getRolePath();
    this.songService.changeLanguage(role, song).subscribe(
      () => {
        // noinspection JSIgnoredPromiseFromCall
        this.router.navigate(['/songs']);
      },
      (err) => {
        if (err.status === 405) {
          this.openAuthenticateDialog();
        } else {
          console.log(err);
          this.snackBar.open(err._body, 'Close', {
            duration: 5000
          })
        }
      }
    );
  }

  createForm() {
    this.youtubeUrl = "https://www.youtube.com/watch?v=" + this.song.youtubeUrl;
    if (this.youtubeUrl.endsWith("undefined")) {
      this.youtubeUrl = '';
    }
    this.form = this.fb.group({
      'title': [this.song.title, [
        Validators.required,
      ]],
      'youtubeUrl': [this.youtubeUrl, [
        Validators.maxLength(52),
      ]],
      'verseOrder': [this.song.verseOrder, []],
      'author': [this.song.author, []],
    });
    this.verseControls = [];
    this.addVerses();
    this.songTextFormControl = new FormControl('');
    this.form.addControl('songText', this.songTextFormControl);
    this.form.valueChanges.subscribe(() => this.onValueChanged());
    this.onValueChanged();
    this.initialFormValue = JSON.stringify(this.form.value);
    this.currentSongForWordList = this.songWordValidationService.createSimpleSongCopy(this.song);
    this.trackFormChanges();
  }

  private trackFormChanges() {
    this.form.valueChanges.pipe(debounceTime(500)).subscribe(() => {
      const currentFormValue = JSON.stringify(this.form.value);
      this.formHasChanges = currentFormValue !== this.initialFormValue;
    });
  }

  getCurrentSongFromForm(): Song {
    const formValue = this.form.value;
    const currentSong = new Song(this.song);
    currentSong.title = formValue.title;
    currentSong.author = formValue.author;
    currentSong.songVerseDTOS = [];
    currentSong.languageDTO = this.selectedLanguage;
    
    if (this.editorType === 'raw') {
      // Parse raw text into verses
      const songText = formValue.songText || '';
      if (songText) {
        let verseMap = new Map<string, SongVerseUI>();
        let verseCount = 0;
        for (const verseI of songText.split("\n\n")) {
          const songVerse = new SongVerseDTO();
          songVerse.type = SectionType.Verse;
          let verse = verseI;
          for (const sectionType of this.sectionTypes) {
            const sectionString = "[" + sectionType.name + "]\n";
            if (verse.toLowerCase().startsWith(sectionString.toLowerCase())) {
              songVerse.type = sectionType.type;
              verse = verseI.substring(sectionString.length, verseI.length);
            }
          }
          songVerse.text = verse;
          currentSong.songVerseDTOS.push(songVerse);
        }
      }
    } else {
      // Use verse controls
      let i = 0;
      for (const key in formValue) {
        if (formValue.hasOwnProperty(key) && key.startsWith('verse') && !key.startsWith('verseOrder')) {
          const value = formValue[key];
          const songVerseDTO = new SongVerseDTO();
          songVerseDTO.text = value;
          if (i < this.verses.length) {
            songVerseDTO.chorus = this.verses[i].chorus;
            songVerseDTO.type = this.verses[i].type;
          }
          currentSong.songVerseDTOS.push(songVerseDTO);
          i = i + 1;
        }
      }
    }
    
    return currentSong;
  }

  onWordListRefresh() {
    this.currentSongForWordList = this.songWordValidationService.createSimpleSongCopy(this.getCurrentSongFromForm());
    this.initialFormValue = JSON.stringify(this.form.value);
    this.formHasChanges = false;
    // Trigger change detection to ensure child component receives the updated song
    this._changeDetectionRef.detectChanges();
  }

  addNewVerse() {
    const control = addNewVerse_(this.verses, this.verseControls, this.form, this.song);
    this.addSectionControlValueChangeListener(control, this.verseControls.length - 1);
    this.calculateUsedSectionTypes();
  }

  removeSection(sectionIndex: number) {
    const formValue = this.form.value;
    let i = 0;
    let song = new Song();
    song.songVerseDTOS = [];
    for (const key in formValue) {
      if (formValue.hasOwnProperty(key) && key.startsWith('verse') && !key.startsWith('verseOrder')) {
        if (sectionIndex != i) {
          const value = formValue[key];
          const songVerseDTO = new SongVerseDTO();
          songVerseDTO.text = value;
          songVerseDTO.chorus = this.verses[i].chorus;
          songVerseDTO.type = this.verses[i].type
          song.songVerseDTOS.push(songVerseDTO);
        }
        i = i + 1;
      }
    }
    for (const key in formValue) {
      if (formValue.hasOwnProperty(key) && key.startsWith('verse') && !key.startsWith('verseOrder')) {
        this.form.removeControl(key);
      }
    }
    this.verses.splice(0, this.verses.length);
    this.verseControls.splice(0, this.verseControls.length);
    i = 0;
    for (const verseI of song.songVerseDTOS) {
      const songVerse = new SongVerseUI();
      songVerse.type = verseI.type;
      let verse = verseI.text;
      const control = new FormControl(verse);
      control.setValue(verse);
      this.verseControls.push(control);
      this.form.addControl('verse' + i, control);
      control.patchValue(verse);
      this.verses.push(songVerse);
      ++i;
    }
    this.calculateUsedSectionTypes();
  }

  onValueChanged() {
    if (!this.form) {
      return;
    }
    const form = this.form;

    for (const field in this.formErrors) {
      if (this.formErrors.hasOwnProperty(field)) {
        this.formErrors[field] = '';
        const control = form.get(field);

        if (control && control.dirty && !control.valid) {
          const messages = this.validationMessages[field];
          for (const key in control.errors) {
            if (control.errors.hasOwnProperty(key)) {
              this.formErrors[field] += messages[key];
              break;
            }
          }
        }
      }
    }
  }

  onSubmit() {
    const formValue = this.form.value;
    // Normalize song title and author before saving
    this.song.title = normalizeForPersistence(formValue.title) || formValue.title;
    this.song.verseOrder = null;
    this.song.author = normalizeForPersistence(formValue.author) || formValue.author;
    this.song.songVerseDTOS = [];
    this.song.languageDTO = this.selectedLanguage;
    let i = 0;
    for (const key in formValue) {
      if (formValue.hasOwnProperty(key) && key.startsWith('verse') && !key.startsWith('verseOrder')) {
        const value = formValue[key];
        const songVerseDTO = new SongVerseDTO();
        // Normalize verse text before saving
        songVerseDTO.text = normalizeForPersistence(value) || value;
        songVerseDTO.chorus = this.verses[i].chorus;
        songVerseDTO.type = this.verses[i].type
        this.song.songVerseDTOS.push(songVerseDTO);
        i = i + 1;
      }
    }
    this.song.deleted = false;
    let url = formValue.youtubeUrl;
    this.song.youtubeUrl = null;
    if (url) {
      let youtubeUrl = url.replace("https://www.youtube.com/watch?v=", "");
      youtubeUrl = youtubeUrl.replace("https://www.youtube.com/embed/", "");
      youtubeUrl = youtubeUrl.replace("https://youtu.be/", "");
      if (youtubeUrl.length < 21 && youtubeUrl.length > 9) {
        this.song.youtubeUrl = youtubeUrl;
      }
    }
    this.setVerseOrderListFromSectionOrder();
    this.validateAndUpdateSong();
  }

  private isAdmin(): Boolean {
    const user = this.auth.getUser();
    if (user == null) {
      return false;
    }
    return user.isAdmin();
  }

  private validateAndUpdateSong() {
    validateWordsAndSave({
      song: this.song,
      validationService: this.songWordValidationService,
      dialog: this.dialog,
      snackBar: this.snackBar,
      language: this.selectedLanguage,
      publish: this.publish,
      onSave: () => this.updateSong(),
      isAdmin: !!this.isAdmin()
    });
  }

  private setVerseOrderListFromSectionOrder() {
    this.song.verseOrderList = this.getVerseOrderListFromSectionOrder();
  }

  private getVerseOrderListFromSectionOrder(): number[] {
    let verseOrderList: number[] = [];
    for (const section of this.sectionOrder) {
      verseOrderList.push(section.index);
    }
    return verseOrderList;
  }

  openNewLanguageDialog(): void {
    const dialogRef = this.dialog.open(NewLanguageComponent);
    dialogRef.afterClosed().subscribe((result) => {
      if (result === 'ok') {
        this.loadLanguage(true);
      }
    });
  }

  editorTypeChange() {
    if (this.editorType === 'raw') {
      const formValue = this.form.value;
      let i = 0;
      let songVerses = [];
      for (const key in formValue) {
        if (formValue.hasOwnProperty(key) && key.startsWith('verse') && !key.startsWith('verseOrder')) {
          const songVerse = new SongVerseDTO();
          songVerse.text = formValue[key];
          songVerse.type = this.verses[i].type;;
          songVerses.push(songVerse);
          ++i;
        }
      }
      let song = new Song();
      song.songVerseDTOS = songVerses;
      song.verseOrderList = this.getVerseOrderListFromSectionOrder();
      let text = '';
      for (const songVerse of song.getVerses()) {
        if (text.length > 0) {
          text = text + "\n\n";
        }
        const type = songVerse.type;
        if (type != SectionType.Verse) {
          for (const sectionType of this.sectionTypes) {
            if (type == sectionType.type) {
              text = text + "[" + sectionType.name + "]\n";
              break;
            }
          }
        }
        text = text + songVerse.text;
      }
      this.songTextFormControl.patchValue(text);
    } else {
      let i = 0;
      const formValue = this.form.value;
      for (const key in formValue) {
        if (formValue.hasOwnProperty(key) && key.startsWith('verse') && !key.startsWith('verseOrder')) {
          this.form.removeControl(key);
          ++i;
        }
      }
      this.verses.splice(0, this.verses.length);
      this.verseControls.splice(0, this.verseControls.length);
      i = 0;
      let verseMap = new Map<string, SongVerseUI>();
      let verseCount = 0;
      this.song.verseOrderList = [];
      for (const verseI of this.songTextFormControl.value.split("\n\n")) {
        const songVerse = new SongVerseUI();
        songVerse.type = SectionType.Verse;
        let verse = verseI;
        for (const sectionType of this.sectionTypes) {
          const sectionString = "[" + sectionType.name + "]\n";
          if (verse.toLowerCase().startsWith(sectionString.toLowerCase())) {
            songVerse.type = sectionType.type;
            verse = verseI.substring(sectionString.length, verseI.length);
          }
        }
        songVerse.text = verse;
        let verseIndex: number;
        if (verseMap.has(verse)) {
          verseIndex = verseMap.get(verse).verseIndex;
        } else {
          verseIndex = verseCount++;
          songVerse.verseIndex = verseIndex;
          verseMap.set(verse, songVerse);
          const control = new FormControl(verse);
          control.setValue(verse);
          this.verseControls.push(control);
          this.form.addControl('verse' + i, control);
          control.patchValue(verse);
          this.verses.push(songVerse);
          ++i;
        }
        this.song.verseOrderList.push(verseIndex);
      }
      this.customSectionOrder = true;
      this.calculateUsedSectionTypes();
    }
  }

  needToDisable() {
    return !this.form.valid || this.editorType === 'raw' || this.selectedLanguage == null;
  }

  refactor() {
    if (this.editorType !== 'raw') {
      const formValue = this.form.value;
      let i = 0;
      for (const key in formValue) {
        if (formValue.hasOwnProperty(key) && key.startsWith('verse') && !key.startsWith('verseOrder')) {
          this.refactorSectionFormValue(formValue, key, i);
          i = i + 1;
        }
        const aKey = 'title';
        if (formValue.hasOwnProperty(key) && key.startsWith(aKey)) {
          this.refactorFormValue(formValue, key, aKey);
        }
      }
    }
  }

  private refactorFormValue(formValue: any, key: string, aKey: string) {
    let newValue = replace(formValue[key]);
    this.form.controls[aKey].setValue(newValue);
    this.form.controls[aKey].updateValueAndValidity();
  }

  private refactorSectionFormValue(formValue: any, key: string, i: number) {
    this.refactorFormValue(formValue, key, 'verse' + i);
  }

  isThisSection(key: string, formValue, i: number): boolean {
    return formValue.hasOwnProperty(key) && key.startsWith('verse' + i) && !key.startsWith('verseOrder');
  }

  refactorSection(i: number) {
    if (this.editorType !== 'raw') {
      const formValue = this.form.value;
      for (const key in formValue) {
        if (this.isThisSection(key, formValue, i)) {
          this.refactorSectionFormValue(formValue, key, i);
          break;
        }
      }
    }
  }

  refactorable(): boolean {
    if (this.editorType !== 'raw') {
      const formValue = this.form.value;
      for (const key in formValue) {
        if (formValue.hasOwnProperty(key) && key.startsWith('verse') && !key.startsWith('verseOrder')) {
          if (valueRefactorable(formValue[key])) {
            return true;
          }
        }
        const aKey = 'title';
        if (formValue.hasOwnProperty(key) && key.startsWith(aKey)) {
          if (valueRefactorable(formValue[key])) {
            return true;
          }
        }
      }
    }
    return false;
  }

  refactorableSections: boolean[] = [];

  private addSectionControlValueChangeListener(control: FormControl, i: number) {
    this.refactorableSections.push(false);
    control.valueChanges.pipe(debounceTime(700)).subscribe(() => {
      if (this.inRefactorableSectionsRange(i)) {
        this.refactorableSections[i] = this.checkRefactorableSection(i);
      }
    });
  }

  private checkRefactorableSection(i: number): boolean {
    if (this.editorType !== 'raw') {
      const formValue = this.form.value;
      for (const key in formValue) {
        if (this.isThisSection(key, formValue, i)) {
          return valueRefactorable(formValue[key]);
        }
      }
    }
    return false;
  }

  private inRefactorableSectionsRange(i: number): boolean {
    return i >= 0 && i < this.refactorableSections.length;
  }

  refactorableSection(i: number): boolean {
    if (this.inRefactorableSectionsRange(i)) {
      return this.refactorableSections[i];
    }
    return false;
  }

  calculateUrlId() {
    let youtubeUrl = this.form.value.youtubeUrl.replace("https://www.youtube.com/watch?v=", "");
    youtubeUrl = youtubeUrl.replace("https://www.youtube.com/embed/", "");
    youtubeUrl = youtubeUrl.replace("https://youtu.be/", "");
    let indexOf = youtubeUrl.indexOf('?');
    if (indexOf >= 0) {
      youtubeUrl = youtubeUrl.substring(0, indexOf);
    }
    if (youtubeUrl.length < 21 && youtubeUrl.length > 9) {
      this.safeUrl = this.sanitizer.bypassSecurityTrustResourceUrl("https://www.youtube.com/embed/" + youtubeUrl);
    } else {
      this.safeUrl = null;
    }
  }

  private updateSong() {
    const role = this.auth.getUser().getRolePath();
    this.songService.updateSong(role, this.song).subscribe(
      () => {
        window.location.reload();
      },
      (err) => {
        if (err.status === 405) {
          this.openAuthenticateDialog();
        } else {
          console.log(err);
        }
      }
    );
  }

  private openAuthenticateDialog() {
    let user = JSON.parse(localStorage.getItem('currentUser'));
    const dialogRef = this.dialog.open(AuthenticateComponent, {
      data: {
        email: user.email
      }
    });

    dialogRef.afterClosed().subscribe((result) => {
      if (result === 'ok') {
        this.updateSong();
      }
    });
  }

  private addVerses() {
    let i = 0;
    this.refactorableSections = [];
    for (const songVerse of this.song.songVerseDTOS) {
      const control = new FormControl('');
      this.addSectionControlValueChangeListener(control, i);
      i = i + 1;
      control.setValue(songVerse.text);
      const songVerseUI = new SongVerseUI();
      songVerseUI.chorus = songVerse.chorus;
      songVerseUI.type = songVerse.type;
      this.verses.push(songVerseUI);
      this.verseControls.push(control);
      this.form.addControl('verse' + (this.verses.length - 1), control);
    }
    if (this.song.verseOrderList != null && this.song.verseOrderList.length > 0) {
      this.customSectionOrder = true;
    }
    this.calculateUsedSectionTypes();
  }
}
