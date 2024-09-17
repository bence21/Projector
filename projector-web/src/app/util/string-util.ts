import XRegExp from "xregexp";

class StringBuilder {
  private value: string;

  constructor(value: string = '') {
    this.value = value;
  }

  append(value: string): void {
    this.value += value;
  }

  setCharAt(index: number, char: string): void {
    this.value = this.value.substr(0, index) + char + this.value.substr(index + 1);
  }

  charAt(index: number): string {
    return this.value.charAt(index);
  }

  length(): number {
    return this.value.length;
  }

  toString(): string {
    return this.value;
  }
}

const WHITE_SPACES = ' \\t\\f\\r\u00A0';
const whiteSpace = `[ ${WHITE_SPACES}]`; // \s matches also to \n
const nonLetters = '\\P{L}';
const nonLetters_saved = `([${nonLetters}])`;
const letters_saved = '(\\p{L})';
const someSymbols_saved = '([.?!,:()])';
const dot = '\\.';
const simpleQuotationMarks = "\"'";
const simpleQuotationMarks_saved = getSaved(simpleQuotationMarks);
const whiteSpaceThanNonLetters_saved = `([${WHITE_SPACES}${nonLetters}])`;
const quotationMark = '"';
const enDash = '–';
const emDash = '—';
const rightDoubleQuotationMark = '”';
const endQuotationMark = rightDoubleQuotationMark;
const endQuotationMark_saved = getSaved(endQuotationMark);
const leftDoubleQuotationMark = '“';
const doubleLowQuotationMark = '„';
const openingMark = "" + leftDoubleQuotationMark + doubleLowQuotationMark;
const withoutEndQuotationMark = openingMark + simpleQuotationMarks;
const quotationMarks = withoutEndQuotationMark + endQuotationMark;
const quotationMarks_saved = `([${quotationMarks}])`;
const symbols = quotationMarks + `.?!,:)`;
const otherThenSomeSymbols_saved = `([^ \t\n${symbols}|])`;
const VERTICAL_LINE = '|';
const VERTICAL_LINE_ESCAPE = '\\' + VERTICAL_LINE;

export function format(s: string): string {
  let newValue = s.trim();
  newValue = fixQuotationMarks(newValue);
  newValue = newValue.replace(/([ \t])([.?!,:])/g, '$2');
  newValue = newValue.replace(/´/g, "'");
  newValue = XRegExp.replace(newValue, XRegExp(`([${openingMark}]) +`, 'g'), '$1');
  newValue = XRegExp.replace(newValue, XRegExp(`${someSymbols_saved}${otherThenSomeSymbols_saved}`, 'g'), '$1 $2');
  newValue = XRegExp.replace(newValue, XRegExp(`${someSymbols_saved}([${withoutEndQuotationMark}].+)`, 'g'), '$1 $2');
  const s1 = `([${withoutEndQuotationMark}])${whiteSpace}*`;
  newValue = XRegExp.replace(newValue, XRegExp(`^${s1}`, 'g'), '$1');
  newValue = XRegExp.replace(newValue, XRegExp(`\n${s1}`, 'g'), '\n$1');
  newValue = XRegExp.replace(newValue, XRegExp(`${someSymbols_saved} +([${endQuotationMark}])`, 'g'), '$1$2');
  newValue = XRegExp.replace(newValue, XRegExp(`${letters_saved}\\(`, 'g'), '$1 (');
  newValue = XRegExp.replace(newValue, XRegExp(`\\) +${someSymbols_saved}`, 'g'), ')$1');
  newValue = dividerReplace(newValue, '/');
  newValue = dividerReplace(newValue, VERTICAL_LINE);
  newValue = dividerReplaceLeft(newValue);
  newValue = newValue.replace(/ {2,}/g, ' ');
  newValue = newValue.replace(/\t{2,}/g, '\t');
  newValue = XRegExp.replace(newValue, XRegExp(`${dot} ${dot} ${dot}`, 'g'), '…');
  newValue = XRegExp.replace(newValue, XRegExp(`${dot}${dot}${dot}`, 'g'), '…');
  newValue = XRegExp.replace(newValue, XRegExp(`${dot}${otherThenSomeSymbols_saved}`, 'g'), '. $1');
  newValue = XRegExp.replace(newValue, XRegExp(`${dot} +([${quotationMarks}])${whiteSpaceThanNonLetters_saved}`, 'g'), '. $1$2');
  newValue = newValue.replace(/ \)/g, ')');
  newValue = newValue.replace(/\( /g, '(');
  newValue = XRegExp.replace(newValue, XRegExp(`${dot} ${quotationMarks_saved}(${whiteSpace}+)([\\n$${nonLetters}])`, 'g'), '.$1$2$3');
  newValue = XRegExp.replace(newValue, XRegExp(`${whiteSpace}*${endQuotationMark_saved}`, 'g'), '$1');
  newValue = XRegExp.replace(newValue, XRegExp(`!${whiteSpace}*${simpleQuotationMarks_saved}${whiteSpaceThanNonLetters_saved}`, 'g'), '!$1$2');
  newValue = removeSpaceAtEndLineForQuotationMarks(newValue);
  newValue = replaceDashType(newValue, '-', enDash);
  newValue = replaceDashType(newValue, enDash, enDash);
  newValue = replaceDashType(newValue, emDash, emDash);
  newValue = newValue.replace(/\r *\n?/g, '\n');
  newValue = newValue.replace(/\n\n/g, '\n');
  newValue = newValue.replace(/\t \n/g, '\n');
  newValue = newValue.replace(/ \t/g, ' ');
  newValue = newValue.replace(/\t /g, ' ');
  newValue = newValue.replace(/ \n/g, '\n');
  newValue = newValue.replace(/\n /g, '\n');
  newValue = newValue.replace(/\t\n/g, '\n');
  newValue = newValue.replace(/Ş/g, 'Ș');
  newValue = newValue.replace(/ş/g, 'ș');
  newValue = newValue.replace(/Ţ/g, 'Ț');
  newValue = newValue.replace(/ţ/g, 'ț');
  newValue = newValue.replace(/ã/g, 'ă');
  newValue = newValue.replace(/ā/g, 'ă');
  newValue = newValue.replace(/à/g, 'á');
  newValue = newValue.replace(/è/g, 'é');
  newValue = newValue.replace(/È/g, 'É');
  newValue = newValue.replace(/õ/g, 'ő');
  newValue = newValue.replace(/ō/g, 'ő');
  newValue = newValue.replace(/ô/g, 'ő');
  newValue = newValue.replace(/Õ/g, 'Ő');
  newValue = newValue.replace(/û/g, 'ű');

  return newValue;
}

function removeSpaceAtEndLineForQuotationMarks(newValue: string): string {
  const s = `${whiteSpace}*${quotationMarks_saved}${whiteSpace}*`;
  newValue = XRegExp.replace(newValue, XRegExp(`${s}\\n`, 'g'), '$1\n');
  newValue = XRegExp.replace(newValue, XRegExp(`${s}$`, 'g'), '$1');
  return newValue;
}

function replaceAsEnDashFromADash(newValue: string, dashType: string, toDash: string): string {
  const enDashReplacement = `${toDash} `;
  newValue = XRegExp.replace(newValue, XRegExp(`^${whiteSpace}*${dashType} *`, 'g'), enDashReplacement);
  newValue = XRegExp.replace(newValue, XRegExp(`\n${whiteSpace}*${dashType} *`, 'g'), `\n${enDashReplacement}`);
  const toDashSaved = `([${toDash}])`;
  const s = toDashSaved + '(.*) *' + dashType;
  const s2 = '$1$2 ' + toDash;
  newValue = replaceAsEndLineDash2(newValue, s + nonLetters_saved, s2 + '$3');
  newValue = replaceAsEndLineDash2(newValue, s, s2);
  newValue = newValue.replace(/ {2}/g, ' ');
  return newValue;
}

function replaceAsEndLineDash2(newValue: string, s: string, endLineReplacement: string): string {
  newValue = XRegExp.replace(newValue, XRegExp(`${s}\n`, 'g'), `${endLineReplacement}\n`);
  newValue = XRegExp.replace(newValue, XRegExp(`${s}$`, 'g'), endLineReplacement);
  return newValue;
}

function replaceDashType(newValue: string, dashType: string, toDash: string): string {
  const s1 = `(?!${whiteSpace})${nonLetters_saved}`;
  const replacement = `$1 ${toDash} $2`;
  newValue = replaceDashTypeIfOneSpaceNear(newValue, dashType, s1, replacement);
  const s2 = `(?!${whiteSpace})${letters_saved}`;
  newValue = replaceDashTypeIfOneSpaceNear(newValue, dashType, s2, replacement);
  newValue = replaceAsEnDashFromADash(newValue, dashType, toDash);
  return newValue;
}

function replaceDashTypeIfOneSpaceNear(newValue: string, dashType: string, s1: string, replacement: string): string {
  newValue = XRegExp.replace(newValue, XRegExp(`${s1} +${dashType} *${letters_saved}`, 'g'), `${replacement}`);
  newValue = XRegExp.replace(newValue, XRegExp(`${s1} *${dashType} +${letters_saved}`, 'g'), `${replacement}`);
  return newValue;
}

function getSaved(s: string) {
  return '([' + s + '])';
}

export function fixQuotationMarks(input: string): string {
  const preferredClosingQuote = getPreferredClosingQuote(input);
  if (preferredClosingQuote === '\0') {
    return input;
  }
  const preferredOpeningQuote = getPreferredOpeningQuote(input, preferredClosingQuote);
  const builder = new StringBuilder(input);
  let foundClosingQuote = false;
  let inQuote = false;
  let openingQuote = '\0';
  let openingQuoteIndex = -1;
  for (let i = 0; i < builder.length(); i++) {
    const currentChar = builder.charAt(i);
    if (inQuote) {
      if (isAQuote(currentChar)) {
        inQuote = false;
        if (!isOpeningQuote(openingQuote)) {
          builder.setCharAt(openingQuoteIndex, preferredOpeningQuote);
        }
        if (!isClosingQuote(currentChar)) {
          builder.setCharAt(i, preferredClosingQuote);
        }
        foundClosingQuote = !foundClosingQuote;
      }
    } else {
      if (isAQuote(currentChar)) {
        inQuote = true;
        openingQuote = currentChar;
        openingQuoteIndex = i;
      }
    }
  }

  return builder.toString();
}

function getOpeningMap(s: string): Map<string, number> {
  const hashMap = new Map<string, number>();
  for (let i = 0; i < s.length; i++) {
    const currentChar = s.charAt(i);
    if (isOpeningQuote(currentChar)) {
      incInMap(hashMap, currentChar);
    }
  }
  return hashMap;
}

function getClosingMap(s: string): Map<string, number> {
  const closingHashMap = new Map<string, number>();
  for (let i = 0; i < s.length; i++) {
    const currentChar = s.charAt(i);
    if (isClosingQuote(currentChar)) {
      incInMap(closingHashMap, currentChar);
    }
  }
  return closingHashMap;
}

function getPreferredClosingQuote(s: string): string {
  const closingHashMap = getClosingMap(s);
  const closingMaxChar = getMaxChar(closingHashMap);
  if (closingMaxChar !== '\0') {
    return closingMaxChar;
  }
  const hashMap = getOpeningMap(s);
  const maxChar = getMaxChar(hashMap);
  if (maxChar === '\0') {
    return '\0';
  }
  if (maxChar === leftDoubleQuotationMark) {
    return rightDoubleQuotationMark;
  }
  if (maxChar === doubleLowQuotationMark) {
    return rightDoubleQuotationMark;
  }
  return maxChar;
}

function getPreferredOpeningQuote(s: string, preferredClosingQuote: string): string {
  const hashMap = getOpeningMap(s);
  let preferredOpeningQuote = getMaxChar(hashMap);
  if (preferredOpeningQuote !== '\0') {
    if (preferredOpeningQuote === quotationMark && preferredClosingQuote !== quotationMark) {
      preferredOpeningQuote = getOpeningQuoteByClosing(preferredClosingQuote, preferredOpeningQuote);
    }
    return preferredOpeningQuote;
  }
  if (preferredClosingQuote !== '\0') {
    return getOpeningQuoteByClosing(preferredClosingQuote, quotationMark);
  }
  return '\0';
}

function getOpeningQuoteByClosing(closingQuote: string, defaultQuote: string): string {
  if (closingQuote === rightDoubleQuotationMark) {
    return leftDoubleQuotationMark;
  }
  return defaultQuote;
}

function incInMap(hashMap: Map<string, number>, currentChar: string) {
  let count = hashMap.get(currentChar);
  if (count === undefined) {
    count = 1;
  } else {
    count++;
  }
  hashMap.set(currentChar, count);
}

function isOpeningQuote(c: string): boolean {
  return isAQuote(c) && !isClosingQuote(c) && (c !== quotationMark);
}

function getMaxChar(hashMap: Map<string, number>): string {
  let maxCount = 0;
  let maxChar = '\0';

  hashMap.forEach((count, char) => {
    if (count > maxCount) {
      maxCount = count;
      maxChar = char;
    }
  });

  return maxChar;
}

function isClosingQuote(c: string): boolean {
  return c === rightDoubleQuotationMark;
}

function isAQuote(c: string): boolean {
  return (
    c === rightDoubleQuotationMark ||
    c === leftDoubleQuotationMark ||
    c === doubleLowQuotationMark ||
    c === quotationMark
  );
}

function dividerReplace(newValue: string, divider: string): string {
  let aDivider = divider;
  if (aDivider === VERTICAL_LINE) {
    aDivider = VERTICAL_LINE_ESCAPE;
  }
  newValue = XRegExp.replace(newValue, XRegExp(` *:+ *${aDivider}+`, 'g'), ` :${divider}`);
  newValue = XRegExp.replace(newValue, XRegExp(`${aDivider}+ *:+`, 'g'), `${divider}:`);
  newValue = XRegExp.replace(newValue, XRegExp(`: +${aDivider}`, 'g'), ` :${divider}`);
  newValue = XRegExp.replace(newValue, XRegExp(`${aDivider} +:`, 'g'), `${divider}: `);
  return newValue;
}

function dividerReplaceLeft(newValue: string): string {
  const divider = '\\\\';
  newValue = XRegExp.replace(newValue, XRegExp(` *:+ *${divider}+`, 'g'), ` :/`);
  return newValue;
}

