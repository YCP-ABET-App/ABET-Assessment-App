import { defineStore } from 'pinia';
import { computed, reactive, ref } from 'vue';
import { scheduleApi } from '@/api/scheduleApi';
import { useUserStore } from '@/stores/user-store';
import type {
  CourseEntity,
  OutcomeMapping,
  PerformanceIndicatorDetail,
  PiRubric,
  ScheduleEntryPayload,
  SemesterEntity,
} from '@/types/schedule';
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

function yearLabel(academicYear: number): string {
  return `${academicYear}-${academicYear + 1}`;
}

function asNumber(value: unknown): number | null {
  const n = Number(value);
  return Number.isFinite(n) ? n : null;
}

function makePi(
  name: string,
  definition: string,
  rubric?: PiRubric,
  extra?: Partial<PerformanceIndicatorDetail>,
): PerformanceIndicatorDetail {
  const indicatorNumber = extra?.indicatorNumber
    ?? Number(name.split('.').pop())
    ?? 1;
  return {
    id: extra?.id ?? 0,
    name,
    indicatorNumber,
    definition,
    rubric: rubric ? { ...rubric } : emptyRubric(),
    studentOutcomeId: extra?.studentOutcomeId ?? 0,
    programId: extra?.programId ?? null,
  };
}

export const useScheduleStore = defineStore('schedule', () => {
  const academicYears = ref<string[]>([]);
  const highlightQuery = ref('');
  const loading = ref(false);
  const error = ref<string | null>(null);
  const saving = ref(false);

  const soPiMap = reactive<OutcomeMapping[]>([]);
  const sixYearSchedule = reactive<Record<number, Record<string, string[]>>>({});
  const allAvailableCourses = ref<string[]>([]);
  const courseOutcomeMap = reactive<Record<string, Record<number, string>>>({});

  const catalog = ref<CourseEntity[]>([]);
  const semesters = ref<SemesterEntity[]>([]);
  const entries = ref<ScheduleEntryPayload[]>([]);
  const programId = ref<number | null>(null);
  const semesterId = ref<number | null>(null);

  const outcomes = computed(() => soPiMap.map(so => so.number));
  const courses = computed(() => [...allAvailableCourses.value].sort(compareCourseCodes));

  const unmappedCourses = computed(() =>
    courses.value.filter(course => {
      const row = courseOutcomeMap[course];
      if (!row) return true;
      return soPiMap.every(so => !(row[so.number] ?? '').trim());
    }),
  );

  function emptyYearRow(): Record<string, string[]> {
    return Object.fromEntries(academicYears.value.map(year => [year, []]));
  }

  function emptyOutcomeCells(): Record<number, string> {
    return Object.fromEntries(soPiMap.map(so => [so.number, '']));
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

  function piCountForOutcome(outcomeNumber: number): number {
    return soPiMap.find(so => so.number === outcomeNumber || so.id === outcomeNumber)?.pis.length ?? 0;
  }

  function findOutcome(outcomeId: number): OutcomeMapping | undefined {
    return soPiMap.find(so => so.id === outcomeId || so.number === outcomeId);
  }

  function scrubCoursePiCells() {
    Object.keys(courseOutcomeMap).forEach(course => {
      soPiMap.forEach(so => {
        const raw = courseOutcomeMap[course]?.[so.number] ?? '';
        const allowed = piCountForOutcome(so.number);
        const kept = parsePiIndexes(raw).filter(number => number >= 1 && number <= allowed);
        courseOutcomeMap[course][so.number] = kept.join(', ');
      });
    });
  }

  function renamePisForOutcome(so: OutcomeMapping) {
    so.pis.forEach((pi, index) => {
      pi.indicatorNumber = index + 1;
      pi.name = `PI ${so.number}.${index + 1}`;
    });
  }

  function remapScheduleAndMatrix(oldNumbers: number[]) {
    const nextSchedule: Record<number, Record<string, string[]>> = {};
    soPiMap.forEach((so, index) => {
      const oldNumber = oldNumbers[index];
      nextSchedule[so.number] = sixYearSchedule[oldNumber] ?? emptyYearRow();
    });
    Object.keys(sixYearSchedule).forEach(key => {
      delete sixYearSchedule[Number(key)];
    });
    Object.assign(sixYearSchedule, nextSchedule);

    Object.keys(courseOutcomeMap).forEach(course => {
      const previous = { ...courseOutcomeMap[course] };
      const nextRow: Record<number, string> = {};
      soPiMap.forEach((so, index) => {
        const oldNumber = oldNumbers[index];
        nextRow[so.number] = previous[oldNumber] ?? '';
      });
      Object.keys(courseOutcomeMap[course]).forEach(key => {
        delete courseOutcomeMap[course][Number(key)];
      });
      Object.assign(courseOutcomeMap[course], nextRow);
    });
  }

  function renumberStudentOutcomes() {
    const oldNumbers = soPiMap.map(so => so.number);
    soPiMap.forEach((so, index) => {
      so.number = index + 1;
      renamePisForOutcome(so);
    });
    remapScheduleAndMatrix(oldNumbers);
  }

  function courseByCode(code: string): CourseEntity | undefined {
    const needle = code.trim().toUpperCase();
    return catalog.value.find(c => (c.courseCode || '').toUpperCase() === needle);
  }

  function semestersForYear(year: string): SemesterEntity[] {
    return semesters.value.filter(s => yearLabel(s.academicYear) === year);
  }

  function outcomeByNumber(outcomeNumber: number): OutcomeMapping | undefined {
    return soPiMap.find(o => o.number === outcomeNumber || o.id === outcomeNumber);
  }

  function parsePiNumbers(courseCode: string, outcomeNumber: number): number[] {
    const raw = courseOutcomeMap[courseCode]?.[outcomeNumber] || '';
    return parsePiIndexes(raw).filter(n => n > 0);
  }

  function indicatorsForCourseOutcome(
    courseCode: string,
    outcomeNumber: number,
    emptyMeansAll = true,
  ): PerformanceIndicatorDetail[] {
    const outcome = outcomeByNumber(outcomeNumber);
    if (!outcome) return [];
    const selected = parsePiNumbers(courseCode, outcomeNumber);
    if (selected.length === 0) return emptyMeansAll ? outcome.pis : [];
    return outcome.pis.filter(pi => selected.includes(pi.indicatorNumber));
  }

  function resetUiCollections() {
    soPiMap.splice(0, soPiMap.length);
    Object.keys(sixYearSchedule).forEach(key => delete sixYearSchedule[Number(key)]);
    Object.keys(courseOutcomeMap).forEach(key => delete courseOutcomeMap[key]);
    academicYears.value = [];
  }

  function rebuildScheduleUi() {
    const years = academicYears.value;
    for (const outcome of soPiMap) {
      if (!sixYearSchedule[outcome.number]) {
        sixYearSchedule[outcome.number] = {};
      }
      for (const year of years) {
        sixYearSchedule[outcome.number][year] = [];
      }
    }

    const semesterById = new Map(semesters.value.map(s => [s.id, s]));
    const courseById = new Map(catalog.value.map(c => [c.id, c]));
    const piById = new Map<number, { outcomeNumber: number; indicatorNumber: number }>();
    for (const outcome of soPiMap) {
      for (const pi of outcome.pis) {
        piById.set(pi.id, { outcomeNumber: outcome.number, indicatorNumber: pi.indicatorNumber });
      }
    }

    const matrix: Record<string, Record<number, Set<number>>> = {};

    for (const entry of entries.value) {
      const semester = semesterById.get(entry.semesterId);
      const course = courseById.get(entry.courseId);
      const pi = piById.get(entry.indicatorId);
      if (!semester || !course || !pi) continue;
      const year = yearLabel(semester.academicYear);
      const code = course.courseCode;
      if (!sixYearSchedule[pi.outcomeNumber]) continue;
      if (!sixYearSchedule[pi.outcomeNumber][year]) {
        sixYearSchedule[pi.outcomeNumber][year] = [];
      }
      if (!sixYearSchedule[pi.outcomeNumber][year].includes(code)) {
        sixYearSchedule[pi.outcomeNumber][year].push(code);
      }
      if (!matrix[code]) matrix[code] = {};
      if (!matrix[code][pi.outcomeNumber]) matrix[code][pi.outcomeNumber] = new Set();
      matrix[code][pi.outcomeNumber].add(pi.indicatorNumber);
    }

    Object.keys(courseOutcomeMap).forEach(key => delete courseOutcomeMap[key]);
    for (const code of allAvailableCourses.value) {
      courseOutcomeMap[code] = emptyOutcomeCells();
      for (const outcome of soPiMap) {
        const nums = [...(matrix[code]?.[outcome.number] ?? [])].sort((a, b) => a - b);
        courseOutcomeMap[code][outcome.number] = nums.join(', ');
      }
    }
  }

  function mapIndicator(raw: any, outcomeNumber: number): PerformanceIndicatorDetail {
    const indicatorNumber = asNumber(raw.indicatorNumber ?? raw.indNumber ?? raw.ind_number) ?? 0;
    return {
      id: asNumber(raw.id) ?? 0,
      name: `PI ${outcomeNumber}.${indicatorNumber}`,
      indicatorNumber,
      definition: raw.description ?? raw.indDescription ?? '',
      rubric: {
        below: raw.belowRubric ?? '',
        met: raw.metRubric ?? '',
        exceeds: raw.exceedsRubric ?? '',
      },
      studentOutcomeId: asNumber(raw.studentOutcomeId) ?? 0,
      programId: asNumber(raw.programId),
    };
  }

  function outcomePayload(so: OutcomeMapping) {
    return {
      number: so.number,
      description: so.description || 'New student outcome. Add a full description.',
      evaluation: '',
      semesterId: semesterId.value,
      programId: programId.value,
      active: true,
    };
  }

  function indicatorPayload(so: OutcomeMapping, pi: PerformanceIndicatorDetail) {
    return {
      description: pi.definition || 'New performance indicator',
      indicatorNumber: pi.indicatorNumber,
      studentOutcomeId: so.id,
      programId: pi.programId ?? programId.value,
      belowRubric: pi.rubric.below,
      metRubric: pi.rubric.met,
      exceedsRubric: pi.rubric.exceeds,
      isActive: true,
    };
  }

  async function loadData() {
    const userStore = useUserStore();
    programId.value = userStore.currentProgramId;
    semesterId.value = userStore.currentSemesterId;
    if (!programId.value) {
      error.value = 'Select a program to load the assessment schedule.';
      return;
    }

    loading.value = true;
    error.value = null;
    try {
      const [courseRows, semesterRows, entryRows] = await Promise.all([
        scheduleApi.getCourses(),
        scheduleApi.getSemesters(),
        scheduleApi.getScheduleEntries(programId.value),
      ]);

      catalog.value = (courseRows || []).map((c: any) => ({
        id: asNumber(c.id) ?? 0,
        courseCode: c.courseCode ?? c.course_code ?? '',
        courseName: c.courseName ?? c.course_name ?? '',
      })).filter(c => c.id && c.courseCode);

      allAvailableCourses.value = catalog.value.map(c => c.courseCode);
      sortCourseCatalog();

      semesters.value = (semesterRows || []).map((s: any) => {
        const startYear = s.startDate ? new Date(s.startDate).getFullYear() : null;
        return {
          id: asNumber(s.id) ?? 0,
          name: s.name,
          code: s.code,
          academicYear: asNumber(s.academicYear ?? s.academic_year) ?? startYear ?? 0,
          type: s.type ?? '',
          startDate: s.startDate,
        };
      }).filter(s => s.id && s.academicYear);

      entries.value = (entryRows || []).map((e: any) => ({
        id: asNumber(e.id) ?? undefined,
        semesterId: asNumber(e.semesterId) ?? 0,
        courseId: asNumber(e.courseId) ?? 0,
        programId: asNumber(e.programId) ?? 0,
        indicatorId: asNumber(e.indicatorId) ?? 0,
      }));

      const yearSet = new Set(semesters.value.map(s => yearLabel(s.academicYear)));
      let years = [...yearSet].sort();
      if (years.length === 0) {
        const start = new Date().getFullYear();
        years = Array.from({ length: 6 }, (_, i) => yearLabel(start + i));
      }

      let outcomeRows: any[] = [];
      try {
        outcomeRows = await scheduleApi.getOutcomesByProgram(programId.value) || [];
      } catch {
        outcomeRows = [];
      }
      if (!outcomeRows.length && semesterId.value) {
        outcomeRows = await scheduleApi.getOutcomesBySemester(semesterId.value) || [];
        outcomeRows = outcomeRows.filter((o: any) => {
          const pid = asNumber(o.programId);
          return pid == null || pid === programId.value;
        });
      }

      resetUiCollections();
      academicYears.value = years;

      const mappedOutcomes: OutcomeMapping[] = [];
      for (const raw of outcomeRows) {
        const id = asNumber(raw.id);
        const number = asNumber(raw.number ?? raw.outNumber ?? raw.out_number);
        if (!id || !number) continue;
        let indicators: any[] = [];
        try {
          indicators = await scheduleApi.getActiveIndicatorsByOutcome(id) || [];
        } catch {
          indicators = [];
        }
        const pis = indicators
          .filter((pi: any) => {
            const pid = asNumber(pi.programId);
            return pid == null || pid === 0 || pid === programId.value;
          })
          .map((pi: any) => mapIndicator(pi, number))
          .sort((a, b) => a.indicatorNumber - b.indicatorNumber);
        mappedOutcomes.push({
          id,
          number,
          description: raw.description ?? raw.outDescription ?? '',
          pis,
        });
      }

      mappedOutcomes.sort((a, b) => a.number - b.number);
      soPiMap.push(...mappedOutcomes);
      rebuildScheduleUi();
      ensureMatrixRows();
    } catch (err) {
      console.error('Failed to load assessment schedule', err);
      error.value = 'Failed to load assessment schedule from the server.';
    } finally {
      loading.value = false;
    }
  }

  async function ensureEntriesLoaded() {
    if (!programId.value) return;
    entries.value = (await scheduleApi.getScheduleEntries(programId.value) || []).map((e: any) => ({
      id: asNumber(e.id) ?? undefined,
      semesterId: asNumber(e.semesterId) ?? 0,
      courseId: asNumber(e.courseId) ?? 0,
      programId: asNumber(e.programId) ?? 0,
      indicatorId: asNumber(e.indicatorId) ?? 0,
    }));
  }

  async function persistCourseOnCell(outcomeNumber: number, year: string, courseCode: string, add: boolean) {
    if (!programId.value) return;
    const course = courseByCode(courseCode);
    const yearSemesters = semestersForYear(year);
    const indicators = add
      ? indicatorsForCourseOutcome(courseCode, outcomeNumber, true)
      : (outcomeByNumber(outcomeNumber)?.pis || []);
    if (!course || yearSemesters.length === 0 || indicators.length === 0) {
      error.value = !course
        ? `Course ${courseCode} is not in the catalog.`
        : yearSemesters.length === 0
          ? `No semester is mapped to ${year}.`
          : `No performance indicators found for outcome ${outcomeNumber}.`;
      return;
    }

    saving.value = true;
    try {
      if (add) {
        for (const semester of yearSemesters) {
          for (const pi of indicators) {
            const exists = entries.value.some(e =>
              e.semesterId === semester.id &&
              e.courseId === course.id &&
              e.programId === programId.value &&
              e.indicatorId === pi.id,
            );
            if (exists) continue;
            try {
              await scheduleApi.createScheduleEntry({
                semesterId: semester.id,
                courseId: course.id,
                programId: programId.value,
                indicatorId: pi.id,
              });
            } catch (err) {
              console.warn('Schedule entry already exists or failed to create', err);
            }
          }
        }
      } else {
        const indicatorIds = new Set(indicators.map(pi => pi.id));
        const semesterIds = new Set(yearSemesters.map(s => s.id));
        const toDelete = entries.value.filter(e =>
          e.id != null &&
          e.courseId === course.id &&
          e.programId === programId.value &&
          semesterIds.has(e.semesterId) &&
          indicatorIds.has(e.indicatorId),
        );
        for (const entry of toDelete) {
          await scheduleApi.deleteScheduleEntry(entry.id as number);
        }
      }
      await ensureEntriesLoaded();
      rebuildScheduleUi();
    } finally {
      saving.value = false;
    }
  }

  function findPi(outcomeId: number, piName: string): PerformanceIndicatorDetail | null {
    const outcome = findOutcome(outcomeId);
    return outcome?.pis.find(pi => pi.name === piName) ?? null;
  }

  function outcomeDescriptionError(description: string): string | null {
    return validateDescriptiveText(description, 'Student outcome', TEXT_LIMITS.outcome);
  }

  async function updateOutcomeDescription(outcomeId: number, description: string): Promise<string | null> {
    const descriptionError = outcomeDescriptionError(description);
    if (descriptionError) return descriptionError;
    const so = findOutcome(outcomeId);
    if (!so) return 'Student outcome was not found.';
    so.description = description.replace(/\s+/g, ' ').trim();
    if (so.id) {
      await scheduleApi.updateOutcome(so.id, outcomePayload(so));
    }
    return null;
  }

  async function addStudentOutcome(): Promise<OutcomeMapping> {
    const nextNumber = (soPiMap[soPiMap.length - 1]?.number ?? soPiMap.length) + 1;
    const so: OutcomeMapping = {
      id: 0,
      number: nextNumber,
      description: '',
      pis: [makePi(`PI ${nextNumber}.1`, '', undefined, {
        indicatorNumber: 1,
        programId: programId.value,
      })],
    };
    if (programId.value && semesterId.value) {
      const created = await scheduleApi.createOutcome(outcomePayload(so));
      so.id = asNumber(created.id) ?? 0;
      so.pis[0].studentOutcomeId = so.id;
      const createdPi = await scheduleApi.createPerformanceIndicator(indicatorPayload(so, so.pis[0]));
      so.pis[0] = mapIndicator(createdPi, so.number);
    }
    soPiMap.push(so);
    sixYearSchedule[so.number] = emptyYearRow();
    Object.keys(courseOutcomeMap).forEach(course => {
      courseOutcomeMap[course][so.number] = '';
    });
    return so;
  }

  async function removeStudentOutcome(outcomeId: number): Promise<string | null> {
    if (soPiMap.length <= 1) {
      return 'At least one student outcome is required.';
    }
    const index = soPiMap.findIndex(so => so.id === outcomeId || so.number === outcomeId);
    if (index === -1) return 'Student outcome was not found.';
    const so = soPiMap[index];
    if (so.id) {
      await scheduleApi.deleteOutcome(so.id);
    }
    soPiMap.splice(index, 1);
    delete sixYearSchedule[so.number];
    Object.keys(courseOutcomeMap).forEach(course => {
      delete courseOutcomeMap[course][so.number];
    });
    renumberStudentOutcomes();
    return null;
  }

  async function addPerformanceIndicator(outcomeId: number): Promise<PerformanceIndicatorDetail | null> {
    const so = findOutcome(outcomeId);
    if (!so) return null;
    const pi = makePi(`PI ${so.number}.${so.pis.length + 1}`, '', undefined, {
      indicatorNumber: so.pis.length + 1,
      studentOutcomeId: so.id,
      programId: programId.value,
    });
    if (so.id && programId.value) {
      const created = await scheduleApi.createPerformanceIndicator(indicatorPayload(so, pi));
      const mapped = mapIndicator(created, so.number);
      so.pis.push(mapped);
      renamePisForOutcome(so);
      return mapped;
    }
    so.pis.push(pi);
    renamePisForOutcome(so);
    return pi;
  }

  async function removePerformanceIndicator(outcomeId: number, piName: string): Promise<string | null> {
    const so = findOutcome(outcomeId);
    if (!so) return 'Student outcome was not found.';
    if (so.pis.length <= 1) {
      return 'Each student outcome needs at least one performance indicator.';
    }
    const index = so.pis.findIndex(pi => pi.name === piName);
    if (index === -1) return 'Performance indicator was not found.';
    const pi = so.pis[index];
    if (pi.id) {
      await scheduleApi.deletePerformanceIndicator(pi.id);
    }
    so.pis.splice(index, 1);
    renamePisForOutcome(so);
    scrubCoursePiCells();
    return null;
  }

  async function updatePiDetails(
    outcomeId: number,
    piName: string,
    updates: { definition?: string; rubric?: PiRubric },
  ): Promise<string | null> {
    const outcome = findOutcome(outcomeId);
    const pi = outcome?.pis.find(item => item.name === piName);
    if (!pi || !outcome) return 'Performance indicator was not found.';

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

    if (pi.id) {
      await scheduleApi.updatePerformanceIndicator(pi.id, indicatorPayload(outcome, pi));
    }
    return null;
  }

  function findExistingCourse(courseName: string): string | undefined {
    return allAvailableCourses.value.find(course => courseCodesMatch(course, courseName));
  }

  function isCourseInCell(outcome: number, year: string, courseName: string): boolean {
    return (sixYearSchedule[outcome]?.[year] ?? []).some(course => courseCodesMatch(course, courseName));
  }

  async function removeCourseFromCell(outcome: number, year: string, courseToRemove: string) {
    const row = sixYearSchedule[outcome]?.[year];
    if (row) {
      sixYearSchedule[outcome][year] = row.filter(c => c !== courseToRemove);
    }
    await persistCourseOnCell(outcome, year, courseToRemove, false);
  }

  async function addCourseToCell(outcome: number, year: string, courseName: string): Promise<string | null> {
    const parsed = parseCourseCode(courseName);
    if (!parsed.ok) return parsed.error;
    if (isCourseInCell(outcome, year, parsed.course.code)) {
      return `${parsed.course.code} is already scheduled in this cell.`;
    }
    const existing = findExistingCourse(parsed.course.code);
    if (!existing) {
      return `${parsed.course.code} is not in the course list. Create it as a new course first.`;
    }
    if (!sixYearSchedule[outcome]) sixYearSchedule[outcome] = {};
    if (!sixYearSchedule[outcome][year]) sixYearSchedule[outcome][year] = [];
    sixYearSchedule[outcome][year].push(existing);
    await persistCourseOnCell(outcome, year, existing, true);
    return null;
  }

  async function registerNewCourse(
    courseName: string,
  ): Promise<{ ok: true; code: string } | { ok: false; error: string }> {
    const parsed = parseCourseCode(courseName);
    if (!parsed.ok) return parsed;

    const existing = findExistingCourse(parsed.course.code);
    if (existing) {
      return { ok: false, error: `${existing} already exists. Choose it from the list instead of creating a duplicate.` };
    }

    const created = await scheduleApi.createCourse(parsed.course.code);
    catalog.value.push({
      id: asNumber(created.id) ?? 0,
      courseCode: created.courseCode ?? parsed.course.code,
      courseName: created.courseName ?? parsed.course.code,
    });
    allAvailableCourses.value.push(parsed.course.code);
    sortCourseCatalog();
    courseOutcomeMap[parsed.course.code] = emptyOutcomeCells();
    return { ok: true, code: parsed.course.code };
  }

  async function syncCourseOutcomeIndicators(courseCode: string, outcomeNumber: number) {
    const years = academicYears.value.filter(year =>
      (sixYearSchedule[outcomeNumber]?.[year] || []).includes(courseCode),
    );
    for (const year of years) {
      await persistCourseOnCell(outcomeNumber, year, courseCode, false);
      const selected = indicatorsForCourseOutcome(courseCode, outcomeNumber, false);
      if (selected.length === 0) continue;
      if (!programId.value) continue;
      const course = courseByCode(courseCode);
      const yearSemesters = semestersForYear(year);
      if (!course || yearSemesters.length === 0) continue;
      saving.value = true;
      try {
        for (const semester of yearSemesters) {
          for (const pi of selected) {
            try {
              await scheduleApi.createScheduleEntry({
                semesterId: semester.id,
                courseId: course.id,
                programId: programId.value,
                indicatorId: pi.id,
              });
            } catch (err) {
              console.warn('Schedule entry already exists or failed to create', err);
            }
          }
        }
        await ensureEntriesLoaded();
        rebuildScheduleUi();
      } finally {
        saving.value = false;
      }
    }
  }

  return {
    academicYears,
    outcomes,
    highlightQuery,
    loading,
    error,
    saving,
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
    loadData,
    syncCourseOutcomeIndicators,
  };
});
