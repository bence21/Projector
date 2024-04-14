import { format, fixQuotationMarks } from "./string-util";

fdescribe('StringUtils', () => {
  function assertStringsAreEqual(actual: string, expected: string) {
    if (actual !== expected) {
      const diffIndex = actual.split('').findIndex((char, index) => char !== expected.charAt(index));
      const diffIndicator = diffIndex >= 0 ? '^'.padStart(diffIndex + 1) : '';
      console.log(`Expected: ${expected}`);
      console.log(`Actual  : ${actual}`);
      console.log(`          ${diffIndicator}`);
    }
    expect(actual).toBe(expected);
  }

  it('should format the string correctly - test_format', () => {
    const s = format('4. Megostorozva állott, Nézd: "Íme az Ember. " -');
    assertStringsAreEqual(s, '4. Megostorozva állott, Nézd: "Íme az Ember." -');
  });

  it('should format the string correctly - test_format2', () => {
    const s = format('„Jövel - így szól a Lélek,-');
    assertStringsAreEqual(s, '„Jövel – így szól a Lélek, –');
  });

  it('should format the string correctly - test_format3', () => {
    const s = format('hogy élsz bennem. :|');
    assertStringsAreEqual(s, 'hogy élsz bennem. :|');
  });

  it('should format the string correctly - test_format4', () => {
    const s = format('Óh nagy Isten oh mily dicső! /: Mit bölcsességgel alkotál. :\\\\ 3x a föld teljes, teljes');
    assertStringsAreEqual(s, 'Óh nagy Isten oh mily dicső! /: Mit bölcsességgel alkotál. :/ 3x a föld teljes, teljes');
  });

  it('should format the string correctly - test_format5', () => {
    const s = format('Ezt mondom hogy: „látok,” bárha');
    assertStringsAreEqual(s, 'Ezt mondom hogy: „látok,” bárha');
  });

  it('should format the string correctly - test_format6', () => {
    const s = format('Ezt mondom hogy: „látok, ” bárha');
    assertStringsAreEqual(s, 'Ezt mondom hogy: „látok,” bárha');
  });

  it('should format the string correctly - test_format7', () => {
    const s = format('Enyém vagy, mert megváltottalak. ”');
    assertStringsAreEqual(s, 'Enyém vagy, mert megváltottalak.”');
  });

  it('should format the string correctly - test_format8', () => {
    const s = format('”Később érted meg ezt!\"');
    assertStringsAreEqual(s, '“Később érted meg ezt!”');
  });

  it('should format the string correctly - test_format9', () => {
    const s = format('Ezt mondom hogy: „látok, ” bárha');
    assertStringsAreEqual(s, 'Ezt mondom hogy: „látok,” bárha');
  });

  it('should format the string correctly - test_format10', () => {
    const s = format('„Hol vagy!?\" - kiált az Úr neked,');
    assertStringsAreEqual(s, '„Hol vagy!?” – kiált az Úr neked,');
  });

  it('should format the string correctly - test_format11', () => {
    const s = format('Kik zengik „  Dicsőség Istennek“');
    assertStringsAreEqual(s, 'Kik zengik „Dicsőség Istennek”');
  });

  it('should format the string correctly - test_format12', () => {
    const s = format('„Hol vagy!? \"- kiált az Úr neked,');
    assertStringsAreEqual(s, '„Hol vagy!?” – kiált az Úr neked,');
  });

  it('should format the string correctly - test_format13', () => {
    const s = format('“én”-t');
    assertStringsAreEqual(s, '“én”-t');
  });

  it('should format the string correctly - test_format14', () => {
    const s = format('asdf\n  - „Aki akar, jöjjön ma!”');
    assertStringsAreEqual(s, 'asdf\n– „Aki akar, jöjjön ma!”');
  });

  it('should format the string correctly - test_format15', () => {
    const s = format('-mert úgy szeretett-,');
    assertStringsAreEqual(s, '– mert úgy szeretett –,');
  });

  it('should format the string correctly - test_format16', () => {
    const s = format('Igen, Uram! - ezt felelem-');
    assertStringsAreEqual(s, 'Igen, Uram! – ezt felelem –');
  });

  it('should format the string correctly - test_format17', () => {
    const s = format('" Hozzám kiálts!" - Így szól az Úr tenéked, -');
    assertStringsAreEqual(s, '"Hozzám kiálts!" – Így szól az Úr tenéked, –');
  });

  it('should format the string correctly - test_format18', () => {
    const s = format('„Jövel- így szól a Lélek,-');
    assertStringsAreEqual(s, '„Jövel – így szól a Lélek, –');
  });

  it('should format the string correctly - test_format19', () => {
    const s = format('Nem vonzódol-e felé?');
    assertStringsAreEqual(s, 'Nem vonzódol-e felé?');
  });

  it('should format the string correctly - test_format20', () => {
    const s = format('halld! „Add ide');
    assertStringsAreEqual(s, 'halld! „Add ide');
  });

  it('should format the string correctly - test_format21', () => {
    const s = format('Hisz neked győznöd kell!“ :/');
    assertStringsAreEqual(s, 'Hisz neked győznöd kell! “ :/');
  });

  it('should format the string correctly - test_format22', () => {
    const s = format('Hisz neked győznöd kell! ” :/');
    assertStringsAreEqual(s, 'Hisz neked győznöd kell!” :/');
  });

  it('should format the string correctly - test_format23', () => {
    const s = format('Én veletek vagyok! "  ');
    assertStringsAreEqual(s, 'Én veletek vagyok!"');
  });

  it('should format the string correctly - test_format24', () => {
    const s = format(`„Názáreti Jézus már elment!\n ”Késő!„a föld e jajtól reng –\n”Názáreti Jézus már elment?”`);
    assertStringsAreEqual(s, `„Názáreti Jézus már elment!\n”Késő! „a föld e jajtól reng –\n”Názáreti Jézus már elment?”`);
  });

  it('should format the string correctly - test_format26', () => {
    const s = format(`Mein ganzes Leben lang habe ich gesucht\r aber es ist schwer, den Weg zu finden\r vorbei am Ziel vor mir\nwährend das Wichtige einfach wegrutscht\r \nEs kommt nicht wieder, aber ich werde suchen\r alles in meinem Leben`);
    assertStringsAreEqual(s, `Mein ganzes Leben lang habe ich gesucht\n` +
      `aber es ist schwer, den Weg zu finden\n` +
      `vorbei am Ziel vor mir\n` +
      `während das Wichtige einfach wegrutscht\n` +
      `Es kommt nicht wieder, aber ich werde suchen\n` +
      `alles in meinem Leben`);
  });

  it('should fix quotation marks in the string - test_fixQuotationMarks', () => {
    const s = fixQuotationMarks('This is a string with „quotes”. Another ”example”.');
    assertStringsAreEqual(s, 'This is a string with „quotes”. Another „example”.');
  });

  it('should fix quotation marks in the string - test_fixQuotationMarks2', () => {
    const s = fixQuotationMarks('This is a string with ”quotes”. Another „example”.');
    assertStringsAreEqual(s, 'This is a string with „quotes”. Another „example”.');
  });

  it('should fix quotation marks in the string - test_fixQuotationMarks3', () => {
    const s = fixQuotationMarks('This ”is a string with „quotes”. Another” ”example”.');
    assertStringsAreEqual(s, 'This „is a string with ”quotes„. Another” „example”.');
  });
});
