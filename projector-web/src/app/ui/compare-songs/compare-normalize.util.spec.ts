import {
  buildComparisonSequence,
  defaultCompareSongsSettings,
  getEffectiveNormalizeOptions,
  highestCommonStrings,
  charactersEqual,
} from './compare-normalize.util';

describe('compare-normalize.util', () => {
  const none = () => ({
    ignoreCase: false,
    ignoreAccents: false,
    ignorePunctuation: false,
    ignoreAnnotations: false,
    ignoreNumbers: false,
    normalizeWhitespace: false,
    normalizeQuotes: false,
  });

  describe('charactersEqual', () => {
    it('respects ignore case', () => {
      expect(charactersEqual('G', 'g', { ...none(), ignoreCase: true })).toBe(true);
      expect(charactersEqual('G', 'g', none())).toBe(false);
      expect(charactersEqual('a', 'b', { ...none(), ignoreCase: true })).toBe(false);
    });
  });

  describe('highestCommonStrings', () => {
    it('finds longer subsequence when ignore case', () => {
      const strict = none();
      const ignoreCaseOn = { ...none(), ignoreCase: true };
      const withCase = highestCommonStrings('God', 'god', strict);
      const ign = highestCommonStrings('God', 'god', ignoreCaseOn);
      expect(withCase.length).toBeLessThan(ign.length);
      expect(ign.length).toBe(3);
    });
  });

  describe('buildComparisonSequence', () => {
    it('strips accents when enabled', () => {
      const seq = buildComparisonSequence('Igéddel', { ...none(), ignoreAccents: true });
      expect(seq.sequence).toBe('Igeddel');
      expect(seq.indexMap.length).toBe(seq.sequence.length);
    });

    it('removes annotations when enabled', () => {
      const seq = buildComparisonSequence('a[C]b', { ...none(), ignoreAnnotations: true });
      expect(seq.sequence).toBe('ab');
    });

    it('omits whitespace from comparison when normalizeWhitespace is enabled', () => {
      const seq = buildComparisonSequence('a  \n\tb', { ...none(), normalizeWhitespace: true });
      expect(seq.sequence).toBe('ab');
      expect(seq.ignoredOrigIndices.has(1)).toBe(true);
    });

    it('matches composed and decomposed accented letters when ignoreAccents is enabled', () => {
      const opts = { ...none(), ignoreAccents: true };
      const composed = buildComparisonSequence('\u00D3', opts);
      const decomposed = buildComparisonSequence('O\u0301', opts);
      const plain = buildComparisonSequence('O', opts);
      expect(composed.sequence).toBe('O');
      expect(decomposed.sequence).toBe('O');
      expect(plain.sequence).toBe('O');
      expect(decomposed.ignoredOrigIndices.has(1)).toBe(true);
      const lcs = highestCommonStrings(composed.sequence, decomposed.sequence, opts);
      expect(lcs.length).toBe(1);
    });

    it('marks punctuation as ignored when ignorePunctuation is enabled', () => {
      const seq = buildComparisonSequence('Hello, world!', { ...none(), ignorePunctuation: true });
      expect(seq.sequence).toBe('Hello world');
      expect(seq.ignoredOrigIndices.has(5)).toBe(true);
      expect(seq.ignoredOrigIndices.has(11)).toBe(true);
    });
  });

  describe('getEffectiveNormalizeOptions', () => {
    it('strictDiff forces all normalization off', () => {
      const s = defaultCompareSongsSettings();
      s.ignoreCase = true;
      s.ignoreAccents = true;
      expect(getEffectiveNormalizeOptions(s).ignoreCase).toBe(true);

      s.strictDiff = true;
      const off = getEffectiveNormalizeOptions(s);
      expect(off.ignoreCase).toBe(false);
      expect(off.ignoreAccents).toBe(false);
    });
  });
});
