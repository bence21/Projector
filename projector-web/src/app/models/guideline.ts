export const YOU_TUBE_LINKING: string = 'YouTube Linking';

export class Guideline {

  title: '';
  text: '';
  private _checkboxState: boolean = false;
  enabled: boolean = true;

  public get checkboxState(): boolean {
    return this._checkboxState;
  }

  public set checkboxState(value: boolean) {
    this._checkboxState = value;
  }

  constructor(values: Object = {}) {
    Object.assign(this, values);
  }
}