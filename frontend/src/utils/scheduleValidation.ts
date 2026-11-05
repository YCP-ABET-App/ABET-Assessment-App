export const TEXT_LIMITS = {
  outcome: { min: 10, max: 400 },
  piDefinition: { min: 10, max: 500 },
  rubric: { min: 8, max: 400 },
} as const;

/** Department prefix (CS, MATH) plus 3–4 digit catalog number, optional letter suffix (400L). */
const COURSE_CODE_PATTERN = /^([A-Za-z]{2,8})\s*[-]?\s*(\d{3,4}[A-Za-z]?)$/;

export interface ParsedCourseCode {
  prefix: string;
  number: string;
  code: string;
}

export function validateDescriptiveText(
  value: string,
  label: string,
  limits: { min: number; max: number },
): string | null {
  const trimmed = value.replace(/\s+/g, ' ').trim();
  if (!trimmed) return `${label} is required.`;
  if (trimmed.length < limits.min) {
    return `${label} must be at least ${limits.min} characters.`;
  }
  if (trimmed.length > limits.max) {
    return `${label} must be at most ${limits.max} characters.`;
  }
  if (!/[A-Za-z]/.test(trimmed)) {
    return `${label} must include letters.`;
  }
  return null;
}

export function parseCourseCode(raw: string): { ok: true; course: ParsedCourseCode } | { ok: false; error: string } {
  const trimmed = raw.replace(/\s+/g, ' ').trim();
  if (!trimmed) {
    return { ok: false, error: 'Enter a department prefix and course number (e.g. CS 400).' };
  }

  const match = trimmed.match(COURSE_CODE_PATTERN);
  if (!match) {
    return {
      ok: false,
      error: 'Use a letter prefix and course number together (e.g. CS 400 or MATH 210).',
    };
  }

  const prefix = match[1].toUpperCase();
  const number = match[2].toUpperCase();
  return { ok: true, course: { prefix, number, code: `${prefix} ${number}` } };
}

export function parseCourseParts(prefix: string, number: string) {
  return parseCourseCode(`${prefix.trim()} ${number.trim()}`);
}

export function courseCodesMatch(a: string, b: string): boolean {
  const left = parseCourseCode(a);
  const right = parseCourseCode(b);
  if (left.ok && right.ok) return left.course.code === right.course.code;
  return a.trim().toUpperCase() === b.trim().toUpperCase();
}

export function compareCourseCodes(a: string, b: string): number {
  const left = parseCourseCode(a);
  const right = parseCourseCode(b);
  if (left.ok && right.ok) {
    const prefixOrder = left.course.prefix.localeCompare(right.course.prefix);
    if (prefixOrder !== 0) return prefixOrder;
    const leftNumber = parseInt(left.course.number, 10);
    const rightNumber = parseInt(right.course.number, 10);
    if (leftNumber !== rightNumber) return leftNumber - rightNumber;
    return left.course.number.localeCompare(right.course.number);
  }
  return a.localeCompare(b, undefined, { numeric: true, sensitivity: 'base' });
}

export function parsePiIndexes(value: string): number[] {
  return value
    .split(',')
    .map(part => part.trim())
    .filter(Boolean)
    .map(part => parseInt(part, 10))
    .filter(number => Number.isInteger(number));
}

export function normalizePiIndexList(value: string): string {
  return parsePiIndexes(value).join(', ');
}

export function validatePiIndexList(value: string, maxLimit?: number): string | null {
  const trimmed = value.trim();
  if (!trimmed) return null;
  if (!/^(\d+\s*,\s*)*\d+$/.test(trimmed)) {
    return 'Use PI numbers separated by commas (e.g. 1, 2, 3).';
  }

  const numbers = parsePiIndexes(trimmed);
  if (numbers.some(number => number < 1)) {
    return 'PI numbers must be positive integers.';
  }
  if (new Set(numbers).size !== numbers.length) {
    return 'PI numbers cannot be repeated.';
  }
  if (maxLimit !== undefined) {
    const hasOutOfBounds = numbers.some(number => number > maxLimit);
    if (hasOutOfBounds) {
      return maxLimit === 0
        ? 'This outcome has no performance indicators yet.'
        : `PI numbers must match this outcome's PIs (1–${maxLimit}).`;
    }
  }
  return null;
}
