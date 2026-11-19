// src/composables/useScheduleModals.ts
// Shared module-level state so every consumer sees the same modal open/close values.
import { ref, reactive, computed } from 'vue';
import type { TargetCell, SelectedPI, OutcomeMapping, PerformanceIndicatorDetail, SemesterSlot } from '@/types/schedule';
import { TBD } from '@/types/schedule';
import { useScheduleStore } from '@/stores/scheduleStore';
import {
  parseCourseCode, parseCourseParts, TEXT_LIMITS, validateDescriptiveText,
} from '@/utils/scheduleValidation';

// ---- Add-course flow ----
const activeTargetCell = ref<TargetCell | null>(null);
const addStep = ref<'course' | 'details'>('course');
const searchQuery = ref('');
const isCreatingCourse = ref(false);
const newCoursePrefix = ref('');
const newCourseNumber = ref('');
const courseFormError = ref('');
const chosenCourse = ref<{ code: string; isNew: boolean } | null>(null);
const chosenSemester = ref<SemesterSlot>('FALL');
const chosenPis = ref<number[]>([]);

// ---- Course detail (click a chip) ----
const detailTarget = ref<{ course: string; outcome: number } | null>(null);
const drafts = reactive<Record<string, { year: string; semester: SemesterSlot; pis: number[] }>>({});
const rowErrors = reactive<Record<string, string>>({});

// ---- Rubric / PI delete (unchanged) ----
const selectedPI = ref<SelectedPI | null>(null);
const rubricForm = reactive({ below: '', met: '', exceeds: '' });
const piDefinitionForm = ref('');
const outcomeDescriptionForm = ref('');
const showRubricErrors = ref(false);
const saveStep = ref<'edit' | 'choose' | 'warn'>('edit');
const pendingCreateAsNew = ref(false);

const pendingDelete = ref<{ outcomeId: number; piName: string } | null>(null);
const deleteStep = ref(0);
const DELETE_PI_STEPS = [
  {
    title: 'Remove this performance indicator?',
    body: 'This PI will be removed from the student outcome and any course mappings that use it.',
  },
  {
    title: 'This can affect assessment data',
    body: 'Course / Outcome PI numbers and schedule mappings that relied on this indicator will change. Historic reports may no longer match this PI.',
  },
  {
    title: 'Final confirmation',
    body: 'This cannot be undone. Permanently delete this performance indicator?',
  },
] as const;

export function useScheduleModals() {
  const store = useScheduleStore();

  const isTbdTarget = computed(() => activeTargetCell.value?.year === TBD);

  // PIs belonging ONLY to the target outcome
  const outcomePis = computed(() => {
    const so = store.soPiMap.find(item => item.id === activeTargetCell.value?.outcome);
    return (so?.pis ?? []).map((pi, i) => ({ number: i + 1, name: pi.name, definition: pi.definition }));
  });

  const filteredCourses = computed(() => {
    if (!searchQuery.value.trim()) return [];
    const query = searchQuery.value.toLowerCase().trim();
    const parsedQuery = parseCourseCode(searchQuery.value);
    return store.allAvailableCourses.filter(course =>
      parsedQuery.ok
        ? course.toLowerCase().includes(parsedQuery.course.code.toLowerCase()) || course.toLowerCase().includes(query)
        : course.toLowerCase().includes(query));
  });

  const exactCourseMatch = computed(() => store.findExistingCourse(searchQuery.value) ?? null);
  const canCreateFromQuery = computed(() => !!searchQuery.value.trim() && !exactCourseMatch.value);

  const composedNewCourse = computed(() => {
    const parsed = parseCourseParts(newCoursePrefix.value, newCourseNumber.value);
    return parsed.ok ? parsed.course.code : '';
  });
  const composedCourseExists = computed(() =>
    composedNewCourse.value ? store.findExistingCourse(composedNewCourse.value) ?? null : null);

  /** What the chosen course already has for the target outcome. */
  const chosenExistingEntries = computed(() =>
    chosenCourse.value && activeTargetCell.value && !chosenCourse.value.isNew
      ? store.courseOutcomeEntries(chosenCourse.value.code, activeTargetCell.value.outcome)
      : []);
  const chosenPiUsage = computed(() =>
    chosenCourse.value && activeTargetCell.value && !chosenCourse.value.isNew
      ? store.piUsage(chosenCourse.value.code, activeTargetCell.value.outcome)
      : {});

  function courseNoteForTarget(course: string): string {
    return activeTargetCell.value ? store.courseSummary(course, activeTargetCell.value.outcome) : '';
  }

  // ---------- add-course flow ----------
  function resetAddState() {
    searchQuery.value = '';
    isCreatingCourse.value = false;
    newCoursePrefix.value = '';
    newCourseNumber.value = '';
    courseFormError.value = '';
    chosenCourse.value = null;
    chosenPis.value = [];
    addStep.value = 'course';
  }

  function openCourseSearch(outcome: number, year: string) {
    resetAddState();
    activeTargetCell.value = { outcome, year };
    chosenSemester.value = year === TBD ? TBD : 'FALL';
  }

  function closeCourseSearch() {
    activeTargetCell.value = null;
    resetAddState();
  }

  function chooseCourse(code: string, isNew: boolean) {
    chosenCourse.value = { code, isNew };
    chosenPis.value = [];
    courseFormError.value = '';
    addStep.value = 'details';
  }

  function chooseExistingCourse(code: string) {
    chooseCourse(code, false);
  }

  function applyParsedCourse(raw: string) {
    const parsed = parseCourseCode(raw);
    if (!parsed.ok) {
      courseFormError.value = parsed.error;
      return;
    }
    const existing = store.findExistingCourse(parsed.course.code);
    chooseCourse(existing ?? parsed.course.code, !existing);
  }

  function beginCreateCourse() {
    isCreatingCourse.value = true;
    courseFormError.value = '';
    const parsed = parseCourseCode(searchQuery.value);
    if (parsed.ok) {
      newCoursePrefix.value = parsed.course.prefix;
      newCourseNumber.value = parsed.course.number;
    } else {
      const bits = searchQuery.value.trim().split(/\s+|-/);
      newCoursePrefix.value = bits[0]?.replace(/[^A-Za-z]/g, '') ?? '';
      newCourseNumber.value = bits.slice(1).join('') || searchQuery.value.replace(/[^0-9A-Za-z]/g, '').replace(/^[A-Za-z]+/, '');
    }
  }

  function createAndAddNewCourse() {
    const parsed = parseCourseParts(newCoursePrefix.value, newCourseNumber.value);
    if (!parsed.ok) {
      courseFormError.value = parsed.error;
      return;
    }
    if (store.findExistingCourse(parsed.course.code)) {
      courseFormError.value = `${parsed.course.code} already exists. Choose it from the list instead of creating a duplicate.`;
      return;
    }
    chooseCourse(parsed.course.code, true);
  }

  function addTypedAsNewCourse() {
    applyParsedCourse(searchQuery.value);
  }

  function backToCourseStep() {
    addStep.value = 'course';
    courseFormError.value = '';
  }

  function togglePi(n: number) {
    chosenPis.value = chosenPis.value.includes(n)
      ? chosenPis.value.filter(x => x !== n)
      : [...chosenPis.value, n].sort((a, b) => a - b);
  }

  function confirmAddMapping() {
    const target = activeTargetCell.value;
    const chosen = chosenCourse.value;
    if (!target || !chosen) return;

    if (chosen.isNew) {
      const created = store.registerNewCourse(chosen.code);
      if (!created.ok) {
        courseFormError.value = created.error;
        return;
      }
      chosenCourse.value = { code: created.code, isNew: false };
    }

    const error = store.addMapping({
      course: chosenCourse.value!.code,
      outcome: target.outcome,
      year: target.year,
      semester: target.year === TBD ? TBD : chosenSemester.value,
      piNumbers: target.year === TBD ? [] : chosenPis.value,
    });
    if (error) {
      courseFormError.value = error;
      return;
    }
    closeCourseSearch();
  }

  // ---------- course detail ----------
  const detailRows = computed(() =>
    detailTarget.value ? store.courseOutcomeEntries(detailTarget.value.course, detailTarget.value.outcome) : []);

  const detailOutcomePis = computed(() => {
    const so = store.soPiMap.find(item => item.id === detailTarget.value?.outcome);
    return (so?.pis ?? []).map((pi, i) => ({ number: i + 1, name: pi.name }));
  });

  function syncDrafts() {
    Object.keys(drafts).forEach(k => delete drafts[k]);
    detailRows.value.forEach(m => {
      drafts[m.id] = { year: m.year, semester: m.semester, pis: [...m.piNumbers] };
    });
  }

  function openCourseDetail(course: string, outcome: number) {
    detailTarget.value = { course, outcome };
    Object.keys(rowErrors).forEach(k => delete rowErrors[k]);
    syncDrafts();
  }

  function closeCourseDetail() {
    detailTarget.value = null;
    Object.keys(drafts).forEach(k => delete drafts[k]);
    Object.keys(rowErrors).forEach(k => delete rowErrors[k]);
  }

  function setDraftYear(id: string, year: string) {
    const d = drafts[id];
    if (!d) return;
    d.year = year;
    if (year === TBD) {
      d.semester = TBD;
      d.pis = [];
    } else if (d.semester === TBD) {
      d.semester = 'FALL';
    }
  }

  function toggleDraftPi(id: string, n: number) {
    const d = drafts[id];
    if (!d) return;
    d.pis = d.pis.includes(n) ? d.pis.filter(x => x !== n) : [...d.pis, n].sort((a, b) => a - b);
  }

  function saveDetailRow(id: string) {
    const d = drafts[id];
    if (!d) return;
    const error = store.updateMapping(id, { year: d.year, semester: d.semester, piNumbers: d.pis });
    rowErrors[id] = error ?? '';
    if (!error) syncDrafts();
  }

  function removeDetailRow(id: string) {
    store.removeMapping(id);
    delete rowErrors[id];
    if (!detailRows.value.length) closeCourseDetail();
    else syncDrafts();
  }

  // ---------- rubric flow (unchanged) ----------
  const rubricErrors = computed(() => ({
    outcome: store.outcomeDescriptionError(outcomeDescriptionForm.value),
    definition: validateDescriptiveText(piDefinitionForm.value, 'PI definition', TEXT_LIMITS.piDefinition),
    below: validateDescriptiveText(rubricForm.below, 'Below expectations', TEXT_LIMITS.rubric),
    met: validateDescriptiveText(rubricForm.met, 'Meets expectations', TEXT_LIMITS.rubric),
    exceeds: validateDescriptiveText(rubricForm.exceeds, 'Exceeds expectations', TEXT_LIMITS.rubric),
  }));
  const rubricIsValid = computed(() => Object.values(rubricErrors.value).every(error => !error));

  function openRubricModal(so: OutcomeMapping, pi: PerformanceIndicatorDetail) {
    selectedPI.value = { outcomeId: so.id, outcomeDesc: so.description, piName: pi.name, piDefinition: pi.definition };
    piDefinitionForm.value = pi.definition;
    outcomeDescriptionForm.value = so.description;
    rubricForm.below = pi.rubric.below;
    rubricForm.met = pi.rubric.met;
    rubricForm.exceeds = pi.rubric.exceeds;
    showRubricErrors.value = false;
    saveStep.value = 'edit';
    pendingCreateAsNew.value = false;
  }

  function closeRubricModal() {
    selectedPI.value = null;
    piDefinitionForm.value = '';
    outcomeDescriptionForm.value = '';
    rubricForm.below = '';
    rubricForm.met = '';
    rubricForm.exceeds = '';
    showRubricErrors.value = false;
    saveStep.value = 'edit';
    pendingCreateAsNew.value = false;
  }

  function beginSaveReview() {
    if (!selectedPI.value) return;
    if (!rubricIsValid.value) {
      showRubricErrors.value = true;
      return;
    }
    saveStep.value = 'choose';
  }

  function chooseSaveMethod(createAsNew: boolean) {
    pendingCreateAsNew.value = createAsNew;
    saveStep.value = 'warn';
  }

  function backFromSaveFlow() {
    if (saveStep.value === 'warn') saveStep.value = 'choose';
    else if (saveStep.value === 'choose') saveStep.value = 'edit';
  }

  function applyPiUpdates(piName: string) {
    if (!selectedPI.value) return 'Performance indicator was not found.';
    return store.updatePiDetails(selectedPI.value.outcomeId, piName, {
      definition: piDefinitionForm.value,
      rubric: { below: rubricForm.below, met: rubricForm.met, exceeds: rubricForm.exceeds },
    });
  }

  function saveRubric() {
    if (!selectedPI.value) return;

    if (pendingCreateAsNew.value) {
      const created = store.addPerformanceIndicator(selectedPI.value.outcomeId);
      if (!created) {
        showRubricErrors.value = true;
        return;
      }
      if (applyPiUpdates(created.name)) {
        showRubricErrors.value = true;
        return;
      }
      closeRubricModal();
      return;
    }

    const outcomeError = store.updateOutcomeDescription(selectedPI.value.outcomeId, outcomeDescriptionForm.value);
    const piError = applyPiUpdates(selectedPI.value.piName);
    if (outcomeError || piError) {
      showRubricErrors.value = true;
      saveStep.value = 'edit';
      return;
    }
    closeRubricModal();
  }

  function beginDeletePi(outcomeId: number, piName: string) {
    pendingDelete.value = { outcomeId, piName };
    deleteStep.value = 0;
  }

  function closeDeletePi() {
    pendingDelete.value = null;
    deleteStep.value = 0;
  }

  function backDeletePi() {
    if (deleteStep.value <= 0) {
      closeDeletePi();
      return;
    }
    deleteStep.value -= 1;
  }

  function continueDeletePi(): string | null {
    if (!pendingDelete.value) return 'Performance indicator was not found.';
    if (deleteStep.value < DELETE_PI_STEPS.length - 1) {
      deleteStep.value += 1;
      return null;
    }
    const error = store.removePerformanceIndicator(pendingDelete.value.outcomeId, pendingDelete.value.piName);
    if (!error) closeDeletePi();
    return error;
  }

  return {
    // add flow
    activeTargetCell, addStep, searchQuery, isCreatingCourse, newCoursePrefix, newCourseNumber,
    composedNewCourse, composedCourseExists, courseFormError, filteredCourses, exactCourseMatch,
    canCreateFromQuery, isTbdTarget, outcomePis, chosenCourse, chosenSemester, chosenPis,
    chosenExistingEntries, chosenPiUsage, courseNoteForTarget,
    openCourseSearch, closeCourseSearch, chooseExistingCourse, beginCreateCourse,
    createAndAddNewCourse, addTypedAsNewCourse, backToCourseStep, togglePi, confirmAddMapping,
    // detail
    detailTarget, detailRows, detailOutcomePis, drafts, rowErrors,
    openCourseDetail, closeCourseDetail, setDraftYear, toggleDraftPi, saveDetailRow, removeDetailRow,
    // rubric / delete
    selectedPI, rubricForm, piDefinitionForm, outcomeDescriptionForm, rubricErrors, rubricIsValid,
    showRubricErrors, saveStep, pendingCreateAsNew, pendingDelete, deleteStep, DELETE_PI_STEPS, TEXT_LIMITS,
    openRubricModal, closeRubricModal, beginSaveReview, chooseSaveMethod, backFromSaveFlow, saveRubric,
    beginDeletePi, closeDeletePi, backDeletePi, continueDeletePi,
  };
}
