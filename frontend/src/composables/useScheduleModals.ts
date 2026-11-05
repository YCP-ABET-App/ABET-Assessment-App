// Shared module-level state so every consumer sees the same modal open/close values.
import { ref, reactive, computed } from 'vue';
import type { TargetCell, SelectedPI, OutcomeMapping, PerformanceIndicatorDetail } from '@/types/schedule';
import { useScheduleStore } from '@/stores/scheduleStore';
import {
  parseCourseCode,
  parseCourseParts,
  TEXT_LIMITS,
  validateDescriptiveText,
} from '@/utils/scheduleValidation';

const activeTargetCell = ref<TargetCell | null>(null);
const searchQuery = ref('');
const isCreatingCourse = ref(false);
const newCoursePrefix = ref('');
const newCourseNumber = ref('');
const courseFormError = ref('');

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

  const filteredCourses = computed(() => {
    if (!searchQuery.value.trim()) return [];
    const query = searchQuery.value.toLowerCase().trim();
    const parsedQuery = parseCourseCode(searchQuery.value);

    return store.allAvailableCourses.filter(course => {
      const isMatch = parsedQuery.ok
        ? course.toLowerCase().includes(parsedQuery.course.code.toLowerCase())
          || course.toLowerCase().includes(query)
        : course.toLowerCase().includes(query);
      const isAlreadyInCell = activeTargetCell.value
        && store.isCourseInCell(activeTargetCell.value.outcome, activeTargetCell.value.year, course);
      return isMatch && !isAlreadyInCell;
    });
  });

  const exactCourseMatch = computed(() => {
    return store.findExistingCourse(searchQuery.value) ?? null;
  });

  const alreadyInActiveCell = computed(() => {
    if (!activeTargetCell.value || !exactCourseMatch.value) return false;
    return store.isCourseInCell(
      activeTargetCell.value.outcome,
      activeTargetCell.value.year,
      exactCourseMatch.value,
    );
  });

  const canCreateFromQuery = computed(() => {
    const query = searchQuery.value.trim();
    if (!query) return false;
    return !exactCourseMatch.value;
  });

  const composedNewCourse = computed(() => {
    const parsed = parseCourseParts(newCoursePrefix.value, newCourseNumber.value);
    return parsed.ok ? parsed.course.code : '';
  });

  const composedCourseExists = computed(() => {
    if (!composedNewCourse.value) return null;
    return store.findExistingCourse(composedNewCourse.value) ?? null;
  });

  const rubricErrors = computed(() => ({
    outcome: store.outcomeDescriptionError(outcomeDescriptionForm.value),
    definition: validateDescriptiveText(piDefinitionForm.value, 'PI definition', TEXT_LIMITS.piDefinition),
    below: validateDescriptiveText(rubricForm.below, 'Below expectations', TEXT_LIMITS.rubric),
    met: validateDescriptiveText(rubricForm.met, 'Meets expectations', TEXT_LIMITS.rubric),
    exceeds: validateDescriptiveText(rubricForm.exceeds, 'Exceeds expectations', TEXT_LIMITS.rubric),
  }));

  const rubricIsValid = computed(() => Object.values(rubricErrors.value).every(error => !error));

  function openCourseSearch(outcome: number, year: string) {
    activeTargetCell.value = { outcome, year };
    searchQuery.value = '';
    isCreatingCourse.value = false;
    newCoursePrefix.value = '';
    newCourseNumber.value = '';
    courseFormError.value = '';
  }

  function closeCourseSearch() {
    activeTargetCell.value = null;
    searchQuery.value = '';
    isCreatingCourse.value = false;
    newCoursePrefix.value = '';
    newCourseNumber.value = '';
    courseFormError.value = '';
  }

  async function addCourseToActiveCell(courseName: string) {
    if (!activeTargetCell.value || !courseName.trim()) return;
    const cell = activeTargetCell.value;
    const error = await store.addCourseToCell(cell.outcome, cell.year, courseName);
    if (error) {
      courseFormError.value = error;
      return;
    }
    closeCourseSearch();
  }

  async function applyParsedCourse(raw: string, asNew = false) {
    const parsed = parseCourseCode(raw);
    if (!parsed.ok) {
      courseFormError.value = parsed.error;
      return;
    }

    const existing = store.findExistingCourse(parsed.course.code);
    if (existing) {
      if (asNew) {
        courseFormError.value = `${existing} already exists. Choose it from the list instead of creating a duplicate.`;
        return;
      }
      await addCourseToActiveCell(existing);
      return;
    }

    const created = await store.registerNewCourse(parsed.course.code);
    if (!created.ok) {
      courseFormError.value = created.error;
      return;
    }
    await addCourseToActiveCell(created.code);
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

  async function createAndAddNewCourse() {
    const parsed = parseCourseParts(newCoursePrefix.value, newCourseNumber.value);
    if (!parsed.ok) {
      courseFormError.value = parsed.error;
      return;
    }
    await applyParsedCourse(parsed.course.code, true);
  }

  async function addTypedAsNewCourse() {
    await applyParsedCourse(searchQuery.value, true);
  }

  function openRubricModal(so: OutcomeMapping, pi: PerformanceIndicatorDetail) {
    selectedPI.value = {
      outcomeId: so.id,
      outcomeNumber: so.number,
      outcomeDesc: so.description,
      piId: pi.id,
      piName: pi.name,
      piDefinition: pi.definition,
    };
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
    if (saveStep.value === 'warn') {
      saveStep.value = 'choose';
      return;
    }
    if (saveStep.value === 'choose') {
      saveStep.value = 'edit';
    }
  }

  async function applyPiUpdates(piName: string) {
    if (!selectedPI.value) return 'Performance indicator was not found.';
    return store.updatePiDetails(selectedPI.value.outcomeId, piName, {
      definition: piDefinitionForm.value,
      rubric: {
        below: rubricForm.below,
        met: rubricForm.met,
        exceeds: rubricForm.exceeds,
      },
    });
  }

  async function saveRubric() {
    if (!selectedPI.value) return;

    if (pendingCreateAsNew.value) {
      const created = await store.addPerformanceIndicator(selectedPI.value.outcomeId);
      if (!created) {
        showRubricErrors.value = true;
        return;
      }
      const error = await applyPiUpdates(created.name);
      if (error) {
        showRubricErrors.value = true;
        return;
      }
      closeRubricModal();
      return;
    }

    const outcomeError = await store.updateOutcomeDescription(
      selectedPI.value.outcomeId,
      outcomeDescriptionForm.value,
    );
    const piError = await applyPiUpdates(selectedPI.value.piName);
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

  async function continueDeletePi(): Promise<string | null> {
    if (!pendingDelete.value) return 'Performance indicator was not found.';
    if (deleteStep.value < DELETE_PI_STEPS.length - 1) {
      deleteStep.value += 1;
      return null;
    }
    const error = await store.removePerformanceIndicator(
      pendingDelete.value.outcomeId,
      pendingDelete.value.piName,
    );
    if (!error) closeDeletePi();
    return error;
  }

  return {
    activeTargetCell,
    searchQuery,
    isCreatingCourse,
    newCoursePrefix,
    newCourseNumber,
    composedNewCourse,
    composedCourseExists,
    courseFormError,
    filteredCourses,
    exactCourseMatch,
    alreadyInActiveCell,
    canCreateFromQuery,
    selectedPI,
    rubricForm,
    piDefinitionForm,
    outcomeDescriptionForm,
    rubricErrors,
    rubricIsValid,
    showRubricErrors,
    saveStep,
    pendingCreateAsNew,
    pendingDelete,
    deleteStep,
    DELETE_PI_STEPS,
    TEXT_LIMITS,
    openCourseSearch,
    closeCourseSearch,
    addCourseToActiveCell,
    beginCreateCourse,
    createAndAddNewCourse,
    addTypedAsNewCourse,
    openRubricModal,
    closeRubricModal,
    beginSaveReview,
    chooseSaveMethod,
    backFromSaveFlow,
    saveRubric,
    beginDeletePi,
    closeDeletePi,
    backDeletePi,
    continueDeletePi,
  };
}
