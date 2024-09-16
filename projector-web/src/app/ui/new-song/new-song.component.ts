import { ChangeDetectionStrategy, Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { FormBuilder, FormControl, FormGroup, Validators } from '@angular/forms';
import { Song, SongService, SongVerseDTO, SongVerseUI, SectionType } from '../../services/song-service.service';
import { Router } from '@angular/router';
import { Language } from "../../models/language";
import { LanguageDataService } from "../../services/language-data.service";
import { MatDialog, MatIconRegistry } from "@angular/material";
import { NewLanguageComponent } from "../new-language/new-language.component";
import { DomSanitizer, SafeResourceUrl, Title } from "@angular/platform-browser";
import { CdkDragDrop, moveItemInArray, copyArrayItem } from '@angular/cdk/drag-drop';
import { addNewVerse_, calculateOrder_ } from '../../util/song.utils';
import { checkAuthenticationError } from '../../util/error-util';
import { AuthService } from '../../services/auth.service';
import { format } from '../../util/string-util';
import { GuidelineDataService } from '../../services/guidelines-data.service';
import { Guideline, YOU_TUBE_LINKING } from '../../models/guideline';

export function replace(value: string) {
  let newValue = replaceMatch(value, / /g, ' '); // NBSP - replaces non breaking space characters with space
  newValue = format(newValue);
  return newValue;
}

function replaceMatch(newValue: string, matcher, replaceValue) {
  while (newValue.match(matcher)) {
    newValue = newValue.replace(matcher, replaceValue);
  }
  return newValue;
}

@Component({
  selector: 'app-new-song',
  templateUrl: './new-song.component.html',
  styleUrls: ['../edit-song/edit-song.component.css'],
  changeDetection: ChangeDetectionStrategy.OnPush
})
export class NewSongComponent implements OnInit {
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
  languages: Language[];
  selectedLanguage = null;
  editorType = 'verse';
  song: Song;
  showSimilarities = false;
  similar: Song[];
  secondSong: Song;
  receivedSimilar = false;
  public youtubeUrl = '';
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
  showWarning = false;
  guidelines: Guideline[] = [];
  publish = false;

  constructor(private fb: FormBuilder,
    private songService: SongService,
    private router: Router,
    private languageDataService: LanguageDataService,
    private auth: AuthService,
    private titleService: Title,
    private dialog: MatDialog,
    iconRegistry: MatIconRegistry,
    public sanitizer: DomSanitizer,
    private guidelineDataService: GuidelineDataService,
    private _changeDetectionRef: ChangeDetectorRef) {
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
    this.titleService.setTitle('New song');
    this.createForm();
    this.loadLanguage(false);
    this.calculateUsedSectionTypes();
    this.guidelines = this.guidelineDataService.getAllWithDefaultCheckedState(this.isAdmin());
    this.submitButtonOrPublish();
  }

  private getUser() {
    const user = this.auth.getUser();
    if (user == undefined || user == null) {
      return null;
    }
    return user;
  }

  private submitButtonOrPublish() {
    const user = this.getUser();
    if (user == null) {
      return;
    }
    this.publish = user.activated;
  }

  private isAdmin(): Boolean {
    const user = this.getUser();
    if (user == null) {
      return false;
    }
    return user.isAdmin();
  }

  SubmitOrPublish() {
    return SubmitOrPublish(this.publish);
  }

  ngAfterViewChecked(): void {
    this._changeDetectionRef.detectChanges();
  }

  noReturnPredicate() {
    return false;
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
        }
      }
    );
  }

  createForm() {
    this.song = new Song();
    this.song.verseOrderList = [];
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
    this.addNewVerse();
    this.songTextFormControl = new FormControl('');
    this.form.addControl('songText', this.songTextFormControl);
    this.form.valueChanges.subscribe(() => this.onValueChanged());
    this.onValueChanged();
  }

  addNewVerse() {
    addNewVerse_(this.verses, this.verseControls, this.form, this.song);
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

  submitForm() {
    this.showWarning = this.needToDisable();
    if (!this.showWarning) {
      this.onSubmit();
    }
  }

  onSubmit() {
    const formValue = this.form.value;
    this.song.title = formValue.title;
    this.song.verseOrder = null;
    this.song.author = formValue.author;
    this.song.songVerseDTOS = [];
    this.song.languageDTO = this.selectedLanguage;
    let i = 0;
    for (const key in formValue) {
      if (formValue.hasOwnProperty(key) && key.startsWith('verse') && !key.startsWith('verseOrder')) {
        const value = formValue[key];
        const songVerseDTO = new SongVerseDTO();
        songVerseDTO.text = value;
        songVerseDTO.chorus = this.verses[i].chorus;
        songVerseDTO.type = this.verses[i].type
        this.song.songVerseDTOS.push(songVerseDTO);
        i = i + 1;
      }
    }
    this.similar = [];
    this.setVerseOrderListFromSectionOrder();
    this.songService.getSimilarByPost(this.song).subscribe((songs) => {
      this.similar = songs;
      if (songs.length > 0) {
        this.secondSong = this.similar[0];
      } else {
        this.insertNewSong();
      }
      this.receivedSimilar = true;
    });
    this.showSimilarities = true;
  }

  insertNewSong() {
    let url = this.form.value.youtubeUrl;
    this.song.youtubeUrl = null;
    if (url) {
      let youtubeUrl = url.replace("https://www.youtube.com/watch?v=", "");
      youtubeUrl = youtubeUrl.replace("https://www.youtube.com/embed/", "");
      youtubeUrl = youtubeUrl.replace("https://youtu.be/", "");
      if (youtubeUrl.length < 21 && youtubeUrl.length > 9) {
        this.song.youtubeUrl = youtubeUrl;
      }
    }
    this.songService.createSong(this.song).subscribe(
      (song) => {
        this.updateUserWhenCreatedSong();
        // noinspection JSIgnoredPromiseFromCall
        this.router.navigate(['/song/' + song.uuid]);
      },
      (err) => {
        checkAuthenticationError(this.insertNewSong, this, err, this.dialog);
      }
    );
  }

  private updateUserWhenCreatedSong() {
    const user = this.auth.getUser();
    user.hadUploadedSongs = true;
    this.auth.setUserToLocalStorage(user);
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
          if (verse.startsWith(sectionString)) {
            songVerse.type = sectionType.type;
            verse = verseI.substring(sectionString.length, verseI.length);
          }
        }
        songVerse.text = verse;
        let verseIndex;
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

  reasonForNotSubmitting(): string {
    const formValue = this.form.value;
    const title = formValue.title;
    if (title === undefined || title.trim() == "") {
      return "Please enter a title!";
    }
    if (!this.form.valid) {
      return "Form is not valid!";
    }
    if (this.editorType === 'raw') {
      return "Please switch back to verse editor!";
    }
    if (this.selectedLanguage == null) {
      return "Please select language!";
    }
    for (const guideline of this.guidelines) {
      if (!guideline.checkboxState && guideline.enabled) {
        return "Please check guideline: " + guideline.title;
      }
    }
    return null;
  }

  needToDisable() {
    return this.reasonForNotSubmitting() != null;
  }

  refactor() {
    if (this.editorType !== 'raw') {
      const formValue = this.form.value;
      let i = 0;
      for (const key in formValue) {
        if (formValue.hasOwnProperty(key) && key.startsWith('verse') && !key.startsWith('verseOrder')) {
          let newValue = replace(formValue[key]);
          this.form.controls['verse' + i].setValue(newValue);
          this.form.controls['verse' + i].updateValueAndValidity();
          i = i + 1;
        }
        const aKey = 'title';
        if (formValue.hasOwnProperty(key) && key.startsWith(aKey)) {
          let newValue = replace(formValue[key]);
          this.form.controls[aKey].setValue(newValue);
          this.form.controls[aKey].updateValueAndValidity();
        }
      }
    }
  }

  refactorable(): boolean {
    if (this.editorType !== 'raw') {
      const formValue = this.form.value;
      for (const key in formValue) {
        if (formValue.hasOwnProperty(key) && key.startsWith('verse') && !key.startsWith('verseOrder')) {
          const value = formValue[key];
          if (value != replace(value)) {
            return true;
          }
        }
        const aKey = 'title';
        if (formValue.hasOwnProperty(key) && key.startsWith(aKey)) {
          const value = formValue[key];
          if (value != replace(value)) {
            return true;
          }
        }
      }
    }
    return false;
  }

  selectSecondSong(song: Song) {
    this.secondSong = song;
  }

  private getYouTubeGuideLine(): Guideline {
    for (const guideline of this.guidelines) {
      if (guideline.title == YOU_TUBE_LINKING) {
        return guideline;
      }
    }
    return undefined;
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
    this.enablingYouTubeGuideline(this.safeUrl != null);
  }

  private enablingYouTubeGuideline(enable: boolean) {
    const youTubeGuideLine = this.getYouTubeGuideLine();
    if (youTubeGuideLine == undefined) {
      return;
    }
    youTubeGuideLine.enabled = enable;
  }
}

export function SubmitOrPublish(publish: boolean) {
  if (publish) {
    return "Publish";
  } else {
    return "Submit";
  }
}

