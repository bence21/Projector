

export class ChangeWord {
  word: string;
  correction: string;
  occurrence: number;

  constructor(values: Object = {}) {
    Object.assign(this, values);
  }
}
