import { FormControl, FormGroup } from "@angular/forms";
import { SectionType, Song, SongVerseUI } from "../services/song-service.service";

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

export function addNewVerse_(verses: SongVerseUI[], verseControls: FormControl[], form: FormGroup, song: Song) {
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
}