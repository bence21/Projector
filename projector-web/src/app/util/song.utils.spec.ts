import { validateWordsAndSave } from './song.utils';

describe('validateWordsAndSave mixed-language warning', () => {
  function immediateObservable<T>(value: T): any {
    return {
      subscribe: (next: (arg: T) => void) => next(value)
    };
  }

  function afterClosedObservable(value: any): any {
    return {
      afterClosed: () => ({
        subscribe: (next: (arg: any) => void) => next(value)
      })
    };
  }

  it('should save directly when there is no mixed-language warning', () => {
    const onSave = jasmine.createSpy('onSave');
    const dialog = {
      open: jasmine.createSpy('open').and.returnValue(afterClosedObservable(true))
    };
    const validationService = {
      validateWords: jasmine.createSpy('validateWords').and.returnValue(immediateObservable({
        hasIssues: false,
        hasMixedLanguageWarning: false
      }))
    };

    validateWordsAndSave({
      song: {} as any,
      validationService: validationService as any,
      dialog: dialog as any,
      snackBar: { open: jasmine.createSpy('snack') } as any,
      language: {} as any,
      publish: false,
      onSave: onSave
    });

    expect(onSave).toHaveBeenCalled();
    expect(dialog.open).not.toHaveBeenCalled();
  });

  it('should show confirm dialog and save only when user confirms mixed-language warning', () => {
    const onSave = jasmine.createSpy('onSave');
    const dialog = {
      open: jasmine.createSpy('open').and.returnValue(afterClosedObservable(true))
    };
    const validationService = {
      validateWords: jasmine.createSpy('validateWords').and.returnValue(immediateObservable({
        hasIssues: false,
        hasMixedLanguageWarning: true,
        foreignWordCount: 12,
        totalReviewedWordCount: 50,
        foreignWordRatio: 0.24,
        foreignLanguages: ['English']
      }))
    };

    validateWordsAndSave({
      song: {} as any,
      validationService: validationService as any,
      dialog: dialog as any,
      snackBar: { open: jasmine.createSpy('snack') } as any,
      language: {} as any,
      publish: false,
      onSave: onSave
    });

    expect(dialog.open).toHaveBeenCalled();
    expect(onSave).toHaveBeenCalled();
  });
});
