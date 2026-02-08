export class RejectedWordSuggestion {
  word: string;
  primarySuggestion: string;
  alternativeSuggestions: string[];

  constructor(values: Object = {}) {
    Object.assign(this, values);
  }
}
