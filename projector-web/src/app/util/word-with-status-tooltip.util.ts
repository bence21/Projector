import { WordWithStatus } from '../models/wordWithStatus';
import { ReviewedWordStatus } from '../models/reviewedWord';

export function getSourceLanguageLabel(sourceLanguage: {
  englishName?: string;
  nativeName?: string;
  printLanguage?: () => string;
}): string {
  if (!sourceLanguage) {
    return '';
  }
  if (typeof sourceLanguage.printLanguage === 'function') {
    return sourceLanguage.printLanguage();
  }
  if (sourceLanguage.englishName && sourceLanguage.nativeName && sourceLanguage.englishName !== sourceLanguage.nativeName) {
    return `${sourceLanguage.englishName} | ${sourceLanguage.nativeName}`;
  }
  return sourceLanguage.englishName || sourceLanguage.nativeName || '';
}

export function getStatusTooltip(wordWithStatus: WordWithStatus): string {
  const status = wordWithStatus.status;
  let tooltip = '';

  switch (status) {
    case ReviewedWordStatus.REVIEWED_GOOD:
      tooltip = 'Reviewed Good';
      break;
    case ReviewedWordStatus.CONTEXT_SPECIFIC:
      tooltip = 'Context-Specific';
      if (wordWithStatus.contextCategory || wordWithStatus.contextDescription || wordWithStatus.notes) {
        const parts: string[] = [tooltip];
        if (wordWithStatus.contextCategory) {
          parts.push(`\nCategory: ${wordWithStatus.contextCategory}`);
        }
        if (wordWithStatus.contextDescription) {
          parts.push(`\nDescription: ${wordWithStatus.contextDescription}`);
        }
        if (wordWithStatus.notes) {
          parts.push(`\nNotes: ${wordWithStatus.notes}`);
        }
        tooltip = parts.join('');
      }
      break;
    case ReviewedWordStatus.ACCEPTED:
      tooltip = 'Accepted';
      if (wordWithStatus.category || wordWithStatus.notes || wordWithStatus.sourceLanguage || wordWithStatus.foreignLanguageType) {
        const parts: string[] = [tooltip];
        if (wordWithStatus.category) {
          parts.push(`\nCategory: ${wordWithStatus.category}`);
        }
        if (wordWithStatus.sourceLanguage) {
          parts.push(`\nSource language: ${getSourceLanguageLabel(wordWithStatus.sourceLanguage)}`);
        }
        if (wordWithStatus.foreignLanguageType !== undefined && wordWithStatus.foreignLanguageType !== null) {
          const typeLabel = wordWithStatus.foreignLanguageType === 0 || wordWithStatus.foreignLanguageType === 'BORROWED'
            ? 'Borrowed (written in song language style)'
            : 'Foreign (OK in source language, not in song language)';
          parts.push(`\nType: ${typeLabel}`);
        }
        if (wordWithStatus.notes) {
          parts.push(`\nNotes: ${wordWithStatus.notes}`);
        }
        tooltip = parts.join('');
      }
      break;
    case ReviewedWordStatus.AUTO_ACCEPTED_FROM_PUBLIC:
      tooltip = 'Auto Accepted From Public';
      break;
    case ReviewedWordStatus.BANNED:
      tooltip = 'Banned';
      break;
    case ReviewedWordStatus.REJECTED:
      tooltip = 'Rejected';
      break;
    case ReviewedWordStatus.UNREVIEWED:
      tooltip = 'Unreviewed';
      break;
    case ReviewedWordStatus.NOT_SURE:
      tooltip = 'Not sure';
      break;
    default:
      tooltip = 'Unknown Status';
  }
  if (wordWithStatus.inheritedFromCapitalizedReview) {
    tooltip += '\n(Treated as reviewed via capitalized form)';
  }
  if (wordWithStatus.allOccurrencesAutoCapitalized) {
    tooltip += '\n(All occurrences in this song are first-in-sentence or first-in-line)';
  }
  return tooltip;
}
