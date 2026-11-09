import { defineStore } from 'pinia';
import { ref, reactive, computed } from 'vue';
import type { OutcomeMapping, PerformanceIndicatorDetail, PiRubric } from '@/types/schedule';
import {
  compareCourseCodes,
  courseCodesMatch,
  parseCourseCode,
  parsePiIndexes,
  TEXT_LIMITS,
  validateDescriptiveText,
} from '@/utils/scheduleValidation';

function emptyRubric(): PiRubric {
  return { below: '', met: '', exceeds: '' };
}

function makePi(name: string, definition: string, rubric?: Partial<PiRubric>): PerformanceIndicatorDetail {
  return {
    name,
    definition,
    rubric: { ...emptyRubric(), ...rubric },
  };
}

export const useScheduleStore = defineStore('schedule', () => {
  const academicYears = ["2024-2025", "2025-2026", "2026-2027", "2027-2028", "2028-2029", "2029-2030"];
  const highlightQuery = ref('');

  const soPiMap = reactive<OutcomeMapping[]>([
    {
      id: 1,
      description: "Students show professional working behavior",
      pis: [
        makePi(
          "PI 1.1",
          "Demonstrates professional communication and collaboration in team settings.",
          {
            below: "Rarely communicates professionally; struggles to collaborate.",
            met: "Communicates clearly and collaborates appropriately most of the time.",
            exceeds: "Consistently leads professional collaboration and elevates team communication.",
          }
        ),
        makePi(
          "PI 1.2",
          "Applies ethical standards and takes responsibility for work products.",
          {
            below: "Overlooks ethical considerations or ownership of deliverables.",
            met: "Follows ethical standards and accepts responsibility for assigned work.",
            exceeds: "Proactively raises ethical concerns and mentors peers on accountability.",
          }
        ),
      ],
    },
    {
      id: 2,
      description: "Students can apply mathematical foundations",
      pis: [
        makePi(
          "PI 2.1",
          "Selects and applies appropriate mathematical models to computing problems.",
          {
            below: "Cannot identify suitable models or applies them incorrectly.",
            met: "Chooses suitable models and applies them with minor guidance.",
            exceeds: "Independently selects optimal models and justifies the approach rigorously.",
          }
        ),
        makePi(
          "PI 2.2",
          "Interprets quantitative results and relates them to system behavior.",
          {
            below: "Misinterprets results or cannot connect them to the system.",
            met: "Correctly interprets results and relates them to expected behavior.",
            exceeds: "Draws insightful conclusions and anticipates edge-case behavior.",
          }
        ),
      ],
    },
    {
      id: 3,
      description: "Students can design and implement solutions",
      pis: [
        makePi(
          "PI 3.1",
          "Designs modular, maintainable solutions that meet stated requirements.",
          {
            below: "Design is incomplete, brittle, or fails key requirements.",
            met: "Design meets requirements with acceptable structure and clarity.",
            exceeds: "Design is elegant, extensible, and clearly documents trade-offs.",
          }
        ),
        makePi(
          "PI 3.2",
          "Implements and verifies working solutions against acceptance criteria.",
          {
            below: "Implementation fails tests or lacks verification evidence.",
            met: "Implementation passes acceptance criteria with basic verification.",
            exceeds: "Delivers robust verification coverage and handles edge cases well.",
          }
        ),
      ],
    },
    {
      id: 4,
      description: "Students recognize professional responsibilities and make informed judgments",
      pis: [
        makePi("PI 4.1", "Evaluates ethical, legal, and professional issues in computing contexts."),
        makePi("PI 4.2", "Makes informed judgments considering the impact of computing solutions."),
      ],
    },
    {
      id: 5,
      description: "Students function effectively on a team whose members together provide leadership",
      pis: [
        makePi("PI 5.1", "Contributes to team planning, execution, and shared decision making."),
        makePi("PI 5.2", "Supports team leadership and collaborative problem solving."),
      ],
    },
    {
      id: 6,
      description: "Students communicate effectively with a range of audiences",
      pis: [
        makePi("PI 6.1", "Produces clear written technical communication for the intended audience."),
        makePi("PI 6.2", "Delivers effective oral explanations of computing work."),
      ],
    },
  ]);

  const sixYearSchedule = reactive<Record<number, Record<string, string[]>>>({
    1: { "2024-2025": ["CS 350", "CS 400"], "2025-2026": ["CS 101", "CS 201", "CS 360"], "2026-2027": ["CS 320", "CS 350", "CS 400"], "2027-2028": ["CS 201", "CS 360", "CS 402"], "2028-2029": ["CS 101", "CS 320", "CS 350", "CS 400"], "2029-2030": ["CS 201", "CS 320", "CS 360", "CS 402"] },
    2: { "2024-2025": ["CS 320", "CS 330"], "2025-2026": ["CS 335", "CS 360", "CS 400"], "2026-2027": ["CS 201", "CS 330"], "2027-2028": ["CS 320", "CS 335", "CS 360", "CS 402"], "2028-2029": ["CS 330"], "2029-2030": ["CS 201", "CS 330", "CS 360", "CS 402"] },
    3: { "2024-2025": ["CS 400"], "2025-2026": ["CS 320"], "2026-2027": ["CS 320", "CS 400"], "2027-2028": ["CS 402"], "2028-2029": ["CS 400"], "2029-2030": ["CS 320", "CS 402"] },
    4: { "2024-2025": ["CS 330"], "2025-2026": ["CS 335", "CS 456"], "2026-2027": ["CS 330"], "2027-2028": ["CS 335", "CS 456"], "2028-2029": ["CS 330"], "2029-2030": ["CS 335", "CS 456"] },
    5: { "2024-2025": ["CS 400"], "2025-2026": ["CS 320"], "2026-2027": ["CS 320", "CS 400"], "2027-2028": ["CS 402"], "2028-2029": ["CS 400"], "2029-2030": ["CS 320", "CS 402"] },
    6: { "2024-2025": ["CS 201", "CS 420"], "2025-2026": ["CS 101", "CS 320", "CS 335", "CS 340", "CS 400"], "2026-2027": ["CS 201", "CS 420"], "2027-2028": ["CS 101", "CS 335", "CS 340", "CS 402"], "2028-2029": ["CS 101", "CS 320", "CS 420"], "2029-2030": ["CS 201", "CS 335", "CS 340", "CS 402"] }
  });

  const allAvailableCourses = ref([
    "CS 101", "CS 201", "CS 320", "CS 330", "CS 335",
    "CS 340", "CS 350", "CS 360", "CS 400", "CS 402",
    "CS 420", "CS 456", "CS 499", "MATH 210"
  ]);

  const courseOutcomeMap: Record<string, Record<number, string>> = reactive({
    "CS 101": { 1: "1, 5", 2: "", 3: "", 4: "", 5: "", 6: "1" },
    "CS 201": { 1: "1, 3, 5", 2: "5", 3: "", 4: "", 5: "", 6: "1, 2, 3" },
    "CS 320": { 1: "2, 4", 2: "1, 2, 3, 4", 3: "1, 2, 3, 4", 4: "", 5: "1, 2, 3", 6: "2, 4, 6" },
    "CS 330": { 1: "", 2: "6, 7", 3: "", 4: "1", 5: "", 6: "" },
    "CS 335": { 1: "", 2: "6, 7", 3: "", 4: "1", 5: "", 6: "5" },
    "CS 340": { 1: "", 2: "", 3: "", 4: "", 5: "", 6: "1" },
    "CS 350": { 1: "1, 3, 5", 2: "", 3: "", 4: "", 5: "", 6: "" },
    "CS 360": { 1: "", 2: "", 3: "", 4: "", 5: "", 6: "" },
    "CS 400": { 1: "1, 3, 5, 6", 2: "5", 3: "1, 2, 3, 4", 4: "1, 2, 3, 4", 5: "1, 2, 3", 6: "4, 6" },
    "CS 402": { 1: "2, 4", 2: "1, 2, 3, 4", 3: "1, 2, 3, 4", 4: "", 5: "1, 2, 3", 6: "4, 6" },
    "CS 420": { 1: "", 2: "", 3: "", 4: "", 5: "", 6: "1, 3, 5" }
  });

  const outcomes = computed(() => soPiMap.map(so => so.id));
  const courses = computed(() => [...allAvailableCourses.value].sort(compareCourseCodes));

  const unmappedCourses = computed(() =>
    courses.value.filter(course => {
      const row = courseOutcomeMap[course];
      if (!row) return true;
      return soPiMap.every(so => !(row[so.id] ?? '').trim());
    }),
  );

  function emptyYearRow(): Record<string, string[]> {
    return Object.fromEntries(academicYears.map(year => [year, []]));
  }

  function emptyOutcomeCells(): Record<number, string> {
    return Object.fromEntries(soPiMap.map(so => [so.id, '']));
  }

  function ensureMatrixRows() {
    allAvailableCourses.value.forEach(code => {
      if (!courseOutcomeMap[code]) {
        courseOutcomeMap[code] = emptyOutcomeCells();
      }
    });
  }

  function sortCourseCatalog() {
    allAvailableCourses.value.sort(compareCourseCodes);
  }

  ensureMatrixRows();
  sortCourseCatalog();
  scrubCoursePiCells();

  function piCountForOutcome(outcomeId: number): number {
    return soPiMap.find(so => so.id === outcomeId)?.pis.length ?? 0;
  }

  function scrubCoursePiCells() {
    Object.keys(courseOutcomeMap).forEach(course => {
      soPiMap.forEach(so => {
        const raw = courseOutcomeMap[course]?.[so.id] ?? '';
        const allowed = piCountForOutcome(so.id);
        const kept = parsePiIndexes(raw).filter(number => number >= 1 && number <= allowed);
        courseOutcomeMap[course][so.id] = kept.join(', ');
      });
    });
  }

  function renamePisForOutcome(so: OutcomeMapping) {
    so.pis.forEach((pi, index) => {
      pi.name = `PI ${so.id}.${index + 1}`;
    });
  }

  function remapScheduleAndMatrix(oldIds: number[]) {
    const nextSchedule: Record<number, Record<string, string[]>> = {};
    soPiMap.forEach((so, index) => {
      const oldId = oldIds[index];
      nextSchedule[so.id] = sixYearSchedule[oldId] ?? emptyYearRow();
    });
    Object.keys(sixYearSchedule).forEach(key => {
      delete sixYearSchedule[Number(key)];
    });
    Object.assign(sixYearSchedule, nextSchedule);

    Object.keys(courseOutcomeMap).forEach(course => {
      const previous = { ...courseOutcomeMap[course] };
      const nextRow: Record<number, string> = {};
      soPiMap.forEach((so, index) => {
        const oldId = oldIds[index];
        nextRow[so.id] = previous[oldId] ?? '';
      });
      Object.keys(courseOutcomeMap[course]).forEach(key => {
        delete courseOutcomeMap[course][Number(key)];
      });
      Object.assign(courseOutcomeMap[course], nextRow);
    });
  }

  function renumberStudentOutcomes() {
    const oldIds = soPiMap.map(so => so.id);
    soPiMap.forEach((so, index) => {
      so.id = index + 1;
      renamePisForOutcome(so);
    });
    remapScheduleAndMatrix(oldIds);
  }

  function findPi(outcomeId: number, piName: string): PerformanceIndicatorDetail | null {
    const outcome = soPiMap.find(so => so.id === outcomeId);
    return outcome?.pis.find(pi => pi.name === piName) ?? null;
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
    const nextId = soPiMap.length + 1;
    const so: OutcomeMapping = {
      id: nextId,
      description: '',
      pis: [makePi(`PI ${nextId}.1`, '')],
    };
    soPiMap.push(so);
    sixYearSchedule[nextId] = emptyYearRow();
    Object.keys(courseOutcomeMap).forEach(course => {
      courseOutcomeMap[course][nextId] = '';
    });
    return so;
  }

  function removeStudentOutcome(outcomeId: number): string | null {
    if (soPiMap.length <= 1) {
      return 'At least one student outcome is required.';
    }
    const index = soPiMap.findIndex(so => so.id === outcomeId);
    if (index === -1) return 'Student outcome was not found.';
    soPiMap.splice(index, 1);
    delete sixYearSchedule[outcomeId];
    Object.keys(courseOutcomeMap).forEach(course => {
      delete courseOutcomeMap[course][outcomeId];
    });
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

  function removePerformanceIndicator(outcomeId: number, piName: string): string | null {
    const so = soPiMap.find(item => item.id === outcomeId);
    if (!so) return 'Student outcome was not found.';
    if (so.pis.length <= 1) {
      return 'Each student outcome needs at least one performance indicator.';
    }
    const index = so.pis.findIndex(pi => pi.name === piName);
    if (index === -1) return 'Performance indicator was not found.';
    so.pis.splice(index, 1);
    renamePisForOutcome(so);
    scrubCoursePiCells();
    return null;
  }

  function updatePiDetails(
    outcomeId: number,
    piName: string,
    updates: { definition?: string; rubric?: PiRubric }
  ): string | null {
    const pi = findPi(outcomeId, piName);
    if (!pi) return 'Performance indicator was not found.';

    if (updates.definition !== undefined) {
      const definitionError = validateDescriptiveText(
        updates.definition,
        'PI definition',
        TEXT_LIMITS.piDefinition,
      );
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

  function findExistingCourse(courseName: string): string | undefined {
    return allAvailableCourses.value.find(course => courseCodesMatch(course, courseName));
  }

  function isCourseInCell(outcome: number, year: string, courseName: string): boolean {
    return (sixYearSchedule[outcome]?.[year] ?? []).some(course => courseCodesMatch(course, courseName));
  }

  function removeCourseFromCell(outcome: number, year: string, courseToRemove: string) {
    sixYearSchedule[outcome][year] = sixYearSchedule[outcome][year].filter(c => c !== courseToRemove);
  }

  function addCourseToCell(outcome: number, year: string, courseName: string): string | null {
    const parsed = parseCourseCode(courseName);
    if (!parsed.ok) return parsed.error;
    if (isCourseInCell(outcome, year, parsed.course.code)) {
      return `${parsed.course.code} is already scheduled in this cell.`;
    }
    const existing = findExistingCourse(parsed.course.code);
    if (!existing) {
      return `${parsed.course.code} is not in the course list. Create it as a new course first.`;
    }
    sixYearSchedule[outcome][year].push(existing);
    return null;
  }

  function registerNewCourse(courseName: string): { ok: true; code: string } | { ok: false; error: string } {
    const parsed = parseCourseCode(courseName);
    if (!parsed.ok) return parsed;

    const existing = findExistingCourse(parsed.course.code);
    if (existing) {
      return { ok: false, error: `${existing} already exists. Choose it from the list instead of creating a duplicate.` };
    }

    allAvailableCourses.value.push(parsed.course.code);
    sortCourseCatalog();
    courseOutcomeMap[parsed.course.code] = emptyOutcomeCells();
    return { ok: true, code: parsed.course.code };
  }

  return {
    academicYears,
    outcomes,
    highlightQuery,
    soPiMap,
    sixYearSchedule,
    allAvailableCourses,
    courses,
    unmappedCourses,
    courseOutcomeMap,
    piCountForOutcome,
    outcomeDescriptionError,
    updateOutcomeDescription,
    addStudentOutcome,
    removeStudentOutcome,
    addPerformanceIndicator,
    removePerformanceIndicator,
    findPi,
    updatePiDetails,
    findExistingCourse,
    isCourseInCell,
    removeCourseFromCell,
    addCourseToCell,
    registerNewCourse,
  };
});
