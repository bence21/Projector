export const COMPARE_SONGS_SETTINGS_KEY = 'compareSongsSettings';

export interface CompareNormalizeOptions {
  ignoreCase: boolean;
  ignoreAccents: boolean;
  ignorePunctuation: boolean;
  ignoreSlashPipeBackslash: boolean;
  ignoreAnnotations: boolean;
  ignoreNumbers: boolean;
  normalizeWhitespace: boolean;
  normalizeQuotes: boolean;
}

export interface CompareSongsSettings {
  version: 1;
  strictDiff: boolean;
  repeatChorus: boolean;
  ignoreCase: boolean;
  ignoreAccents: boolean;
  ignorePunctuation: boolean;
  ignoreSlashPipeBackslash: boolean;
  ignoreAnnotations: boolean;
  ignoreNumbers: boolean;
  normalizeWhitespace: boolean;
  normalizeQuotes: boolean;
}

export interface ComparisonSequence {
  sequence: string;
  indexMap: number[];
  /** Original character indices omitted from comparison (should not show as diffs). */
  ignoredOrigIndices: Set<number>;
}

export function defaultCompareSongsSettings(): CompareSongsSettings {
  return {
    version: 1,
    strictDiff: true,
    repeatChorus: true,
    ignoreCase: true,
    ignoreAccents: true,
    ignorePunctuation: false,
    ignoreSlashPipeBackslash: false,
    ignoreAnnotations: false,
    ignoreNumbers: false,
    normalizeWhitespace: true,
    normalizeQuotes: true,
  };
}

export function getEffectiveNormalizeOptions(settings: CompareSongsSettings): CompareNormalizeOptions {
  if (settings.strictDiff) {
    return {
      ignoreCase: false,
      ignoreAccents: false,
      ignorePunctuation: false,
      ignoreSlashPipeBackslash: false,
      ignoreAnnotations: false,
      ignoreNumbers: false,
      normalizeWhitespace: false,
      normalizeQuotes: false,
    };
  }
  return {
    ignoreCase: settings.ignoreCase,
    ignoreAccents: settings.ignoreAccents,
    ignorePunctuation: settings.ignorePunctuation,
    ignoreSlashPipeBackslash: settings.ignoreSlashPipeBackslash,
    ignoreAnnotations: settings.ignoreAnnotations,
    ignoreNumbers: settings.ignoreNumbers,
    normalizeWhitespace: settings.normalizeWhitespace,
    normalizeQuotes: settings.normalizeQuotes,
  };
}

export function loadCompareSongsSettings(): CompareSongsSettings {
  const base = defaultCompareSongsSettings();
  try {
    const raw = localStorage.getItem(COMPARE_SONGS_SETTINGS_KEY);
    if (raw != null && raw.trim().length > 0) {
      const parsed = JSON.parse(raw) as Partial<CompareSongsSettings>;
      if (parsed && parsed.version === 1) {
        localStorage.removeItem('repeatChorus');
        localStorage.removeItem('ignoreCase');
        return { ...base, ...parsed, version: 1 };
      }
    }
  } catch (_) {
    // fall through to migration
  }
  const rc = localStorage.getItem('repeatChorus');
  if (rc != null && rc.trim().length > 0) {
    try {
      base.repeatChorus = JSON.parse(rc);
    } catch (_) {
      /* keep default */
    }
  }
  const ic = localStorage.getItem('ignoreCase');
  if (ic != null && ic.trim().length > 0) {
    try {
      base.ignoreCase = JSON.parse(ic);
    } catch (_) {
      /* keep default */
    }
  }
  localStorage.removeItem('repeatChorus');
  localStorage.removeItem('ignoreCase');
  saveCompareSongsSettings(base);
  return base;
}

export function saveCompareSongsSettings(settings: CompareSongsSettings): void {
  localStorage.setItem(COMPARE_SONGS_SETTINGS_KEY, JSON.stringify(settings));
}

function normalizeQuoteChar(c: string): string {
  switch (c) {
    case '\u2018':
    case '\u2019':
    case '\u00B4':
    case '`':
      return "'";
    case '\u201C':
    case '\u201D':
      return '"';
    default:
      return c;
  }
}

const PUNCTUATION_CHARS = new Set<string>([
  '.', ',', ';', ':', '!', '?',
  '…', '·', '•',
  '-', '–', '—',
  '\'', '"',
  '«', '»',
  '¿', '¡',
]);

function isIgnorablePunctuation(c: string): boolean {
  return PUNCTUATION_CHARS.has(c);
}

const SLASH_PIPE_BACKSLASH = new Set<string>(['/', '|', '\\']);

function isSlashPipeOrBackslash(c: string): boolean {
  return SLASH_PIPE_BACKSLASH.has(c);
}

function stripAccentsChar(c: string): string {
  return c.normalize('NFD').replace(/[\u0300-\u036f]/g, '');
}

function isCombiningOnlyChar(c: string): boolean {
  const nfd = c.normalize('NFD');
  if (nfd.length === 0) {
    return false;
  }
  for (let j = 0; j < nfd.length; j++) {
    if (!/[\u0300-\u036f]/.test(nfd.charAt(j))) {
      return false;
    }
  }
  return true;
}

/**
 * Turn original full song text into a comparison string plus map from comparison index -> original char index.
 */
export function buildComparisonSequence(text: string, options: CompareNormalizeOptions): ComparisonSequence {
  const outChars: string[] = [];
  const indexMap: number[] = [];
  const ignoredOrigIndices = new Set<number>();
  let i = 0;

  const markIgnored = (from: number, toExclusive: number) => {
    for (let j = from; j < toExclusive; j++) {
      ignoredOrigIndices.add(j);
    }
  };

  while (i < text.length) {
    if (options.ignoreAnnotations) {
      const open = text.charAt(i);
      if (open === '[' || open === '(' || open === '{') {
        const close = open === '[' ? ']' : open === '(' ? ')' : '}';
        const end = text.indexOf(close, i + 1);
        const next = end >= 0 ? end + 1 : i + 1;
        markIgnored(i, next);
        i = next;
        continue;
      }
    }

    let c = text.charAt(i);
    if (options.normalizeQuotes) {
      c = normalizeQuoteChar(c);
    }

    if (options.ignorePunctuation && isIgnorablePunctuation(c)) {
      ignoredOrigIndices.add(i);
      i++;
      continue;
    }

    if (options.ignoreSlashPipeBackslash && isSlashPipeOrBackslash(c)) {
      ignoredOrigIndices.add(i);
      i++;
      continue;
    }

    if (options.ignoreNumbers) {
      const code = c.charCodeAt(0);
      if (code >= 48 && code <= 57) {
        ignoredOrigIndices.add(i);
        i++;
        continue;
      }
    }

    if (options.normalizeWhitespace && /\s/.test(c)) {
      const startRun = i;
      while (i < text.length && /\s/.test(text.charAt(i))) {
        i++;
      }
      markIgnored(startRun, i);
      continue;
    }

    if (/\s/.test(c) && !options.normalizeWhitespace) {
      outChars.push(c);
      indexMap.push(i);
      i++;
      continue;
    }

    const emitted = options.ignoreAccents ? stripAccentsChar(c) : c;
    if (emitted.length === 0) {
      ignoredOrigIndices.add(i);
      i++;
      continue;
    }
    outChars.push(emitted);
    indexMap.push(i);
    i++;
    if (options.ignoreAccents) {
      while (i < text.length && isCombiningOnlyChar(text.charAt(i))) {
        ignoredOrigIndices.add(i);
        i++;
      }
    }
  }

  return {
    sequence: outChars.join(''),
    indexMap,
    ignoredOrigIndices,
  };
}

export function buildOrigIndexToSeqIndex(indexMap: number[]): Map<number, number> {
  const map = new Map<number, number>();
  for (let si = 0; si < indexMap.length; si++) {
    const oi = indexMap[si];
    if (!map.has(oi)) {
      map.set(oi, si);
    }
  }
  return map;
}

export function charactersEqual(a: string, b: string, options: CompareNormalizeOptions): boolean {
  if (options.ignoreCase) {
    return a.toLocaleLowerCase() === b.toLocaleLowerCase();
  }
  return a === b;
}

function getTextFromReverseLetters(r: string[]): string[] {
  return r;
}

export function highestCommonStrings(
  a: string,
  b: string,
  options: CompareNormalizeOptions,
): string[] {
  let t = [];
  let i: number;
  let j: number;
  for (i = 0; i < a.length + 2; ++i) {
    t[i] = [];
    t[i][0] = 0;
  }
  for (j = 1; j < b.length + 2; ++j) {
    t[0][j] = 0;
  }
  let c: string;
  for (i = 0; i < a.length; ++i) {
    c = a.charAt(i);
    for (j = 0; j < b.length; ++j) {
      if (charactersEqual(c, b.charAt(j), options)) {
        t[i + 1][j + 1] = t[i][j] + 1;
      } else if (t[i + 1][j] > t[i][j + 1]) {
        t[i + 1][j + 1] = t[i + 1][j];
      } else {
        t[i + 1][j + 1] = t[i][j + 1];
      }
    }
  }
  let r = [];
  i = a.length;
  j = b.length;
  const strings = [];
  while (i != 0 && j != 0) {
    if (t[i - 1][j] + 1 == t[i][j] && t[i][j] == t[i][j - 1] + 1) {
      r.push(a.charAt(i - 1));
      --i;
      --j;
    } else {
      if (r.length > 0) {
        for (const letter of getTextFromReverseLetters(r)) {
          strings.push(letter);
        }
        r = [];
      }
      if (t[i][j - 1] > t[i - 1][j]) {
        --j;
      } else {
        --i;
      }
    }
  }
  if (r.length > 0) {
    for (const letter of getTextFromReverseLetters(r)) {
      strings.push(letter);
    }
  }
  return strings.reverse();
}
