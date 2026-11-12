// src/stores/scheduleStore.ts
import { defineStore } from 'pinia';
import { ref, reactive, computed } from 'vue';
import type {
  OutcomeMapping, PerformanceIndicatorDetail, PiRubric,
  ScheduleMapping, NewMapping, BackendScheduleRow, SemesterSlot,
} from '@/types/schedule';
import { TBD, SEMESTER_OPTIONS } from '@/types/schedule';
import {
  compareCourseCodes, courseCodesMatch, parseCourseCode,
  TEXT_LIMITS, validateDescriptiveText,
} from '@/utils/scheduleValidation';

function emptyRubric(): PiRubric {
  return { below: '', met: '', exceeds: '' };
}

function makePi(name: string, definition: string, rubric?: Partial<PiRubric>): PerformanceIndicatorDetail {
  return { name, definition, rubric: { ...emptyRubric(), ...rubric } };
}

//May need to fix how handling. In current form will send "Both" as if it were semester and search for that semester ID, that won't
//work when sending to backend, so need to fix and make a way that way both will check which year it is in, and designate the Fall for year
//start and spring for year end (i.e., how normal semesters are designated).
const SEMESTER_LABEL: Record<SemesterSlot, string> = {
  FALL: 'Fall', SPRING: 'Spring', BOTH: 'BOTH', TBD: 'TBD',
};

export const useScheduleStore = defineStore('schedule', () => {
  const academicYears = ['2024-2025', '2025-2026', '2026-2027', '2027-2028', '2028-2029', '2029-2030'];
  /* Grid columns: years, then the TBD column last. */
  const scheduleColumns = [...academicYears, TBD];
  const highlightQuery = ref('');

  const soPiMap = reactive<OutcomeMapping[]>([
    {
      id: 1,
      description: 'Students show professional working behavior',
      pis: [
        makePi('PI 1.1', 'Demonstrates professional communication and collaboration in team settings.', {
          below: 'Rarely communicates professionally; struggles to collaborate.',
          met: 'Communicates clearly and collaborates appropriately most of the time.',
          exceeds: 'Consistently leads professional collaboration and elevates team communication.',
        }),
        makePi('PI 1.2', 'Applies ethical standards and takes responsibility for work products.', {
          below: 'Overlooks ethical considerations or ownership of deliverables.',
          met: 'Follows ethical standards and accepts responsibility for assigned work.',
          exceeds: 'Proactively raises ethical concerns and mentors peers on accountability.',
        }),
      ],
    },
    {
      id: 2,
      description: 'Students can apply mathematical foundations',
      pis: [
        makePi('PI 2.1', 'Selects and applies appropriate mathematical models to computing problems.', {
          below: 'Cannot identify suitable models or applies them incorrectly.',
          met: 'Chooses suitable models and applies them with minor guidance.',
          exceeds: 'Independently selects optimal models and justifies the approach rigorously.',
        }),
        makePi('PI 2.2', 'Interprets quantitative results and relates them to system behavior.', {
          below: 'Misinterprets results or cannot connect them to the system.',
          met: 'Correctly interprets results and relates them to expected behavior.',
          exceeds: 'Draws insightful conclusions and anticipates edge-case behavior.',
        }),
      ],
    },
    {
      id: 3,
      description: 'Students can design and implement solutions',
      pis: [
        makePi('PI 3.1', 'Designs modular, maintainable solutions that meet stated requirements.', {
          below: 'Design is incomplete, brittle, or fails key requirements.',
          met: 'Design meets requirements with acceptable structure and clarity.',
          exceeds: 'Design is elegant, extensible, and clearly documents trade-offs.',
        }),
        makePi('PI 3.2', 'Implements and verifies working solutions against acceptance criteria.', {
          below: 'Implementation fails tests or lacks verification evidence.',
          met: 'Implementation passes acceptance criteria with basic verification.',
          exceeds: 'Delivers robust verification coverage and handles edge cases well.',
        }),
      ],
    },
    {
      id: 4,
      description: 'Students recognize professional responsibilities and make informed judgments',
      pis: [
        makePi('PI 4.1', 'Evaluates ethical, legal, and professional issues in computing contexts.'),
        makePi('PI 4.2', 'Makes informed judgments considering the impact of computing solutions.'),
      ],
    },
    {
      id: 5,
      description: 'Students function effectively on a team whose members together provide leadership',
      pis: [
        makePi('PI 5.1', 'Contributes to team planning, execution, and shared decision making.'),
        makePi('PI 5.2', 'Supports team leadership and collaborative problem solving.'),
      ],
    },
    {
      id: 6,
      description: 'Students communicate effectively with a range of audiences',
      pis: [
        makePi('PI 6.1', 'Produces clear written technical communication for the intended audience.'),
        makePi('PI 6.2', 'Delivers effective oral explanations of computing work.'),
      ],
    },
  ]);

  const allAvailableCourses = ref([
    'CS 101', 'CS 201', 'CS 320', 'CS 330', 'CS 335',
    'CS 340', 'CS 350', 'CS 360', 'CS 400', 'CS 402',
    'CS 420', 'CS 456', 'CS 499', 'MATH 210',
  ]);

  const mappings = reactive<ScheduleMapping[]>([]);
  let nextId = 1;

  const outcomes = computed(() => soPiMap.map(so => so.id));
  const courses = computed(() => [...allAvailableCourses.value].sort(compareCourseCodes));

  /** Courses that appear anywhere in the schedule (drives the read-only matrix). */
  const mappedCourses = computed(() =>
    [...new Set(mappings.map(m => m.course))].sort(compareCourseCodes),
  );

  const backendRows = computed<BackendScheduleRow[]>(() =>
    mappings.flatMap((m): BackendScheduleRow[] =>
      m.year === TBD
        ? [{ course: m.course, academicYear: null, semester: TBD, outcomeId: m.outcome, piNumber: null, outcomeLevel: true }]
        : m.piNumbers.map(pi => ({
          course: m.course, academicYear: m.year, semester: m.semester,
          outcomeId: m.outcome, piNumber: pi, outcomeLevel: false,
        })),
    ),
  );

  // ---------- helpers ----------
  function piCountForOutcome(outcomeId: number): number {
    return soPiMap.find(so => so.id === outcomeId)?.pis.length ?? 0;
  }

  function piName(outcomeId: number, piNumber: number): string {
    return soPiMap.find(so => so.id === outcomeId)?.pis[piNumber - 1]?.name ?? `PI ${outcomeId}.${piNumber}`;
  }

  function formatSlot(m: Pick<ScheduleMapping, 'year' | 'semester'>): string {
    return m.year === TBD ? 'TBD' : `${m.year} ${SEMESTER_LABEL[m.semester]}`;
  }

  function describeMapping(m: ScheduleMapping): string {
    if (m.year === TBD) return 'TBD (semester and PIs not yet known)';
    return `${formatSlot(m)}: ${m.piNumbers.map(n => piName(m.outcome, n)).join(', ')}`;
  }

  function coursesInCell(outcome: number, year: string): { course: string; entries: ScheduleMapping[] }[] {
    const grouped = new Map<string, ScheduleMapping[]>();
    mappings.filter(m => m.outcome === outcome && m.year === year).forEach(m => {
      const list = grouped.get(m.course) ?? [];
      list.push(m);
      grouped.set(m.course, list);
    });
    return [...grouped.entries()]
      .map(([course, entries]) => ({ course, entries }))
      .sort((a, b) => compareCourseCodes(a.course, b.course));
  }

  function courseOutcomeEntries(course: string, outcome: number): ScheduleMapping[] {
    return mappings.filter(m => m.course === course && m.outcome === outcome);
  }

  /** PI numbers a course already covers for an outcome (real semesters only). */
  function courseOutcomePis(course: string, outcome: number): number[] {
    const set = new Set<number>();
    courseOutcomeEntries(course, outcome).forEach(m => m.piNumbers.forEach(n => set.add(n)));
    return [...set].sort((a, b) => a - b);
  }

  /** Short text like "PI 1.1, PI 1.2" or "TBD" — empty if the course has no entry for the outcome. */
  function courseSummary(course: string, outcome: number): string {
    const entries = courseOutcomeEntries(course, outcome);
    if (!entries.length) return '';
    const pis = courseOutcomePis(course, outcome).map(n => piName(outcome, n));
    const hasTbd = entries.some(m => m.year === TBD);
    return [...pis, ...(hasTbd ? ['TBD'] : [])].join(', ');
  }

  /** piNumber -> where that PI is already assessed for this course. */
  function piUsage(course: string, outcome: number): Record<number, string[]> {
    const usage: Record<number, string[]> = {};
    courseOutcomeEntries(course, outcome).forEach(m => {
      m.piNumbers.forEach(n => (usage[n] ??= []).push(formatSlot(m)));
    });
    return usage;
  }

  // ---------- mapping CRUD ----------
  function validateMapping(i: NewMapping): string | null {
    if (!findExistingCourse(i.course)) return `${i.course} is not in the course list.`;
    if (!soPiMap.some(so => so.id === i.outcome)) return 'Student outcome was not found.';
    if (!scheduleColumns.includes(i.year)) return 'Choose a valid year column.';
    if (i.year === TBD) return null;
    if (!SEMESTER_OPTIONS.some(s => s.value === i.semester)) return 'Select a semester for this year.';
    if (!i.piNumbers.length) return 'Select at least one performance indicator.';
    const limit = piCountForOutcome(i.outcome);
    if (i.piNumbers.some(n => n < 1 || n > limit)) return `PIs must belong to Outcome ${i.outcome}.`;
    return null;
  }

  function addMapping(input: NewMapping): string | null {
    const error = validateMapping(input);
    if (error) return error;

    const isTbd = input.year === TBD;
    const semester: SemesterSlot = isTbd ? TBD : input.semester;
    const pis = isTbd ? [] : [...new Set(input.piNumbers)].sort((a, b) => a - b);

    const existing = mappings.find(m =>
      m.course === input.course && m.outcome === input.outcome &&
      m.year === input.year && m.semester === semester);

    if (existing) {
      if (isTbd) return `${input.course} is already in the TBD column for Outcome ${input.outcome}.`;
      const added = pis.filter(p => !existing.piNumbers.includes(p));
      if (!added.length) return `${input.course} already covers those PIs in ${formatSlot(existing)}.`;
      existing.piNumbers = [...existing.piNumbers, ...added].sort((a, b) => a - b);
    } else {
      mappings.push({ id: `m${nextId++}`, course: input.course, outcome: input.outcome, year: input.year, semester, piNumbers: pis });
    }

    // A concrete assignment resolves the TBD placeholder for the same course + outcome.
    if (!isTbd) {
      for (let k = mappings.length - 1; k >= 0; k--) {
        const m = mappings[k];
        if (m.year === TBD && m.course === input.course && m.outcome === input.outcome) mappings.splice(k, 1);
      }
    }
    return null;
  }

  function updateMapping(id: string, patch: { year: string; semester: SemesterSlot; piNumbers: number[] }): string | null {
    const index = mappings.findIndex(m => m.id === id);
    if (index === -1) return 'Entry was not found.';
    const old = mappings[index];
    mappings.splice(index, 1);
    const error = addMapping({ course: old.course, outcome: old.outcome, ...patch });
    if (error) mappings.splice(index, 0, old);
    return error;
  }

  function removeMapping(id: string) {
    const index = mappings.findIndex(m => m.id === id);
    if (index !== -1) mappings.splice(index, 1);
  }

  // ---------- outcomes / PIs ----------
  function renamePisForOutcome(so: OutcomeMapping) {
    so.pis.forEach((pi, index) => { pi.name = `PI ${so.id}.${index + 1}`; });
  }

  function renumberStudentOutcomes() {
    soPiMap.forEach((so, index) => {
      so.id = index + 1;
      renamePisForOutcome(so);
    });
  }

  function findPi(outcomeId: number, piNameValue: string): PerformanceIndicatorDetail | null {
    return soPiMap.find(so => so.id === outcomeId)?.pis.find(pi => pi.name === piNameValue) ?? null;
  }

  function outcomeDescriptionError(description: string): string | null {
    return validateDescriptiveText(description, 'Student outcome', TEXT_LIMITS.outcome);
  }

  function updateOutcomeDescription(outcomeId: number, description: string): string | null {
    const error = outcomeDescriptionError(description);
    if (error) return error;
    const so = soPiMap.find(item => item.id === outcomeId);
    if (!so) return 'Student outcome was not found.';
    so.description = description.replace(/\s+/g, ' ').trim();
    return null;
  }

  function addStudentOutcome(): OutcomeMapping {
    const nextOutcomeId = soPiMap.length + 1;
    const so: OutcomeMapping = { id: nextOutcomeId, description: '', pis: [makePi(`PI ${nextOutcomeId}.1`, '')] };
    soPiMap.push(so);
    return so;
  }

  function removeStudentOutcome(outcomeId: number): string | null {
    if (soPiMap.length <= 1) return 'At least one student outcome is required.';
    const index = soPiMap.findIndex(so => so.id === outcomeId);
    if (index === -1) return 'Student outcome was not found.';
    soPiMap.splice(index, 1);
    for (let k = mappings.length - 1; k >= 0; k--) {
      if (mappings[k].outcome === outcomeId) mappings.splice(k, 1);
      else if (mappings[k].outcome > outcomeId) mappings[k].outcome -= 1;
    }
    renumberStudentOutcomes();
    return null;
  }

  function addPerformanceIndicator(outcomeId: number): PerformanceIndicatorDetail | null {
    const so = soPiMap.find(item => item.id === outcomeId);
    if (!so) return null;
    const pi = makePi(`PI ${so.id}.${so.pis.length + 1}`, '');
    so.pis.push(pi);
    renamePisForOutcome(so);
    return pi;
  }

  function removePerformanceIndicator(outcomeId: number, piNameValue: string): string | null {
    const so = soPiMap.find(item => item.id === outcomeId);
    if (!so) return 'Student outcome was not found.';
    if (so.pis.length <= 1) return 'Each student outcome needs at least one performance indicator.';
    const index = so.pis.findIndex(pi => pi.name === piNameValue);
    if (index === -1) return 'Performance indicator was not found.';
    so.pis.splice(index, 1);
    renamePisForOutcome(so);

    // Keep schedule mappings consistent: drop the PI, shift higher PIs down.
    const removed = index + 1;
    for (let k = mappings.length - 1; k >= 0; k--) {
      const m = mappings[k];
      if (m.outcome !== outcomeId || m.year === TBD) continue;
      m.piNumbers = m.piNumbers.filter(n => n !== removed).map(n => (n > removed ? n - 1 : n));
      if (!m.piNumbers.length) mappings.splice(k, 1);
    }
    return null;
  }

  function updatePiDetails(
    outcomeId: number,
    piNameValue: string,
    updates: { definition?: string; rubric?: PiRubric },
  ): string | null {
    const pi = findPi(outcomeId, piNameValue);
    if (!pi) return 'Performance indicator was not found.';

    if (updates.definition !== undefined) {
      const definitionError = validateDescriptiveText(updates.definition, 'PI definition', TEXT_LIMITS.piDefinition);
      if (definitionError) return definitionError;
      pi.definition = updates.definition.replace(/\s+/g, ' ').trim();
    }

    if (updates.rubric) {
      const rubricError =
        validateDescriptiveText(updates.rubric.below, 'Below expectations', TEXT_LIMITS.rubric)
        || validateDescriptiveText(updates.rubric.met, 'Meets expectations', TEXT_LIMITS.rubric)
        || validateDescriptiveText(updates.rubric.exceeds, 'Exceeds expectations', TEXT_LIMITS.rubric);
      if (rubricError) return rubricError;
      pi.rubric.below = updates.rubric.below.replace(/\s+/g, ' ').trim();
      pi.rubric.met = updates.rubric.met.replace(/\s+/g, ' ').trim();
      pi.rubric.exceeds = updates.rubric.exceeds.replace(/\s+/g, ' ').trim();
    }
    return null;
  }

  // ---------- courses ----------
  function findExistingCourse(courseName: string): string | undefined {
    return allAvailableCourses.value.find(course => courseCodesMatch(course, courseName));
  }

  function registerNewCourse(courseName: string): { ok: true; code: string } | { ok: false; error: string } {
    const parsed = parseCourseCode(courseName);
    if (!parsed.ok) return parsed;
    const existing = findExistingCourse(parsed.course.code);
    if (existing) {
      return { ok: false, error: `${existing} already exists. Choose it from the list instead of creating a duplicate.` };
    }
    allAvailableCourses.value.push(parsed.course.code);
    allAvailableCourses.value.sort(compareCourseCodes);
    return { ok: true, code: parsed.course.code };
  }

  allAvailableCourses.value.sort(compareCourseCodes);

  // ---------- sample data (replace with backend load) ----------
  [
    { course: 'CS 350', outcome: 1, year: '2024-2025', semester: 'FALL', piNumbers: [1] },
    { course: 'CS 400', outcome: 1, year: '2024-2025', semester: 'SPRING', piNumbers: [1, 2] },
    { course: 'CS 320', outcome: 2, year: '2024-2025', semester: 'SPRING', piNumbers: [1] },
    { course: 'CS 320', outcome: 3, year: '2025-2026', semester: 'FALL', piNumbers: [1, 2] },
    { course: 'CS 340', outcome: 5, year: TBD, semester: TBD, piNumbers: [] },
  ].forEach(seed => addMapping(seed as NewMapping));

  return {
    academicYears, scheduleColumns, outcomes, highlightQuery, soPiMap,
    allAvailableCourses, courses, mappedCourses, mappings, backendRows,
    piCountForOutcome, piName, formatSlot, describeMapping,
    coursesInCell, courseOutcomeEntries, courseOutcomePis, courseSummary, piUsage,
    addMapping, updateMapping, removeMapping,
    outcomeDescriptionError, updateOutcomeDescription,
    addStudentOutcome, removeStudentOutcome,
    addPerformanceIndicator, removePerformanceIndicator, findPi, updatePiDetails,
    findExistingCourse, registerNewCourse,
  };
});
