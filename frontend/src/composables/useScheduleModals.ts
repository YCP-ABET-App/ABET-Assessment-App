// Shared module-level state so every consumer sees the same modal open/close values.
import { ref, reactive, computed } from 'vue';
import type { TargetCell, SelectedPI, OutcomeMapping, PerformanceIndicatorDetail } from '@/types/schedule';
import { useScheduleStore } from '@/stores/scheduleStore';

const activeTargetCell = ref<TargetCell | null>(null);
const searchQuery = ref('');
const isCreatingCourse = ref(false);
const newCourseName = ref('');

const selectedPI = ref<SelectedPI | null>(null);
const rubricForm = reactive({ below: '', met: '', exceeds: '' });
const piDefinitionForm = ref('');

export function useScheduleModals() {
  const store = useScheduleStore();

  const filteredCourses = computed(() => {
    if (!searchQuery.value.trim()) return [];
    const query = searchQuery.value.toLowerCase().trim();

    return store.allAvailableCourses.filter(course => {
      const isMatch = course.toLowerCase().includes(query);
      const isAlreadyInCell = activeTargetCell.value
        && store.sixYearSchedule[activeTargetCell.value.outcome][activeTargetCell.value.year].includes(course);
      return isMatch && !isAlreadyInCell;
    });
  });

  /** Exact typed name already exists (case-insensitive) among available courses */
  const exactCourseMatch = computed(() => {
    const query = searchQuery.value.trim().toLowerCase();
    if (!query) return null;
    return store.allAvailableCourses.find(c => c.toLowerCase() === query) ?? null;
  });

  /** Show create-new affordance when there is typed text and it isn't already an exact known course */
  const canCreateFromQuery = computed(() => {
    const query = searchQuery.value.trim();
    if (!query) return false;
    return !exactCourseMatch.value;
  });

  function openCourseSearch(outcome: number, year: string) {
    activeTargetCell.value = { outcome, year };
    searchQuery.value = '';
    isCreatingCourse.value = false;
    newCourseName.value = '';
  }

  function closeCourseSearch() {
    activeTargetCell.value = null;
    searchQuery.value = '';
    isCreatingCourse.value = false;
    newCourseName.value = '';
  }

  function addCourseToActiveCell(courseName: string) {
    if (!activeTargetCell.value || !courseName.trim()) return;
    store.addCourseToCell(activeTargetCell.value.outcome, activeTargetCell.value.year, courseName.trim());
    closeCourseSearch();
  }

  function beginCreateCourse() {
    isCreatingCourse.value = true;
    if (!newCourseName.value.trim()) {
      newCourseName.value = searchQuery.value.trim();
    }
  }

  function createAndAddNewCourse() {
    const name = (newCourseName.value || searchQuery.value).trim();
    if (!name) return;
    const courseName = store.registerNewCourse(name);
    if (!courseName) return;
    addCourseToActiveCell(courseName);
  }

  /** One-click: register typed query as a new course and add it to the cell */
  function addTypedAsNewCourse() {
    const name = searchQuery.value.trim();
    if (!name) return;
    const courseName = store.registerNewCourse(name);
    if (!courseName) return;
    addCourseToActiveCell(courseName);
  }

  function openRubricModal(so: OutcomeMapping, pi: PerformanceIndicatorDetail) {
    selectedPI.value = {
      outcomeId: so.id,
      outcomeDesc: so.description,
      piName: pi.name,
      piDefinition: pi.definition,
    };
    piDefinitionForm.value = pi.definition;
    rubricForm.below = pi.rubric.below;
    rubricForm.met = pi.rubric.met;
    rubricForm.exceeds = pi.rubric.exceeds;
  }

  function closeRubricModal() {
    selectedPI.value = null;
    piDefinitionForm.value = '';
    rubricForm.below = '';
    rubricForm.met = '';
    rubricForm.exceeds = '';
  }

  function saveRubric() {
    if (!selectedPI.value) return;
    store.updatePiDetails(selectedPI.value.outcomeId, selectedPI.value.piName, {
      definition: piDefinitionForm.value.trim(),
      rubric: {
        below: rubricForm.below,
        met: rubricForm.met,
        exceeds: rubricForm.exceeds,
      },
    });
    closeRubricModal();
  }

  return {
    activeTargetCell,
    searchQuery,
    isCreatingCourse,
    newCourseName,
    filteredCourses,
    exactCourseMatch,
    canCreateFromQuery,
    selectedPI,
    rubricForm,
    piDefinitionForm,
    openCourseSearch,
    closeCourseSearch,
    addCourseToActiveCell,
    beginCreateCourse,
    createAndAddNewCourse,
    addTypedAsNewCourse,
    openRubricModal,
    closeRubricModal,
    saveRubric,
  };
}
