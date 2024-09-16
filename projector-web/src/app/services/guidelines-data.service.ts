import { Injectable } from '@angular/core';
import { Guideline, YOU_TUBE_LINKING } from '../models/guideline';

@Injectable()
export class GuidelineDataService {

  constructor(
  ) {
  }

  getAll() {
    let guidelines = [];
    this.addGuideline(guidelines, 'Format', 'Keep song lyrics in a poem format, presenting each section as a verse or a chorus. Avoid unnecessary line breaks for improved clarity.');
    this.addGuideline(guidelines, 'Section Marking', 'Clearly mark different sections such as choruses, verses, bridges, etc., specifying the type of each section.');
    this.addGuideline(guidelines, 'Spacing and Characters', 'Be vigilant about eliminating extra spaces and unnecessary characters in the lyrics.');
    this.addGuideline(guidelines, 'Verse Numbers', 'Avoid including verse numbers within the text to maintain a clean and streamlined presentation.');
    this.addGuideline(guidelines, 'Title Convention', 'Do not include the ordinal number of the collection in the title. Keep the title focused solely on the song\'s name.');
    this.addGuideline(guidelines, 'Content Inclusion', 'Include only the lyrics of the song in the text. Exclude chords, tabs, or unrelated content like Bible verses.');
    const youTubeGuideline = this.addGuideline(guidelines, YOU_TUBE_LINKING, 'Ensure that the song lyrics accurately represent the sung words when linking to a YouTube video.');
    youTubeGuideline.enabled = false;
    this.addGuideline(guidelines, 'Browser Spell Checker', 'Utilize the browser\'s spell checker if available to ensure accurate spelling and grammar in the song lyrics.');
    return guidelines;
  }

   getAllWithDefaultCheckedState(defaultState: Boolean) {
    let guidelines = this.getAll();
    if (defaultState) {
      for (const guideLine of guidelines) {
        guideLine.checkboxState = true;
      }
    }
    return guidelines;
  }

  addGuideline(guidelines: any[], title, text): Guideline {
    const guideline = new Guideline();
    guideline.title = title;
    guideline.text = text;
    guidelines.push(guideline);
    return guideline;
  }
}
