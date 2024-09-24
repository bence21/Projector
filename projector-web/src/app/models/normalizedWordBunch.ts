import { WordBunch } from "./wordBunch";


export class NormalizedWordBunch {

  bestWord = '';
  ratio: number;
  wordBunches: WordBunch[];
  maxBunch: WordBunch;

  constructor(values: Object = {}) {
    Object.assign(this, values);
    if (this.wordBunches == undefined) {
      this.wordBunches = [];
    }
    for (let i = 0; i < this.wordBunches.length; ++i) {
      this.wordBunches[i] = new WordBunch(this.wordBunches[i]);
    }
  }
}
