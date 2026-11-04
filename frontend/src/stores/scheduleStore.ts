import { defineStore } from 'pinia';
import { ref, reactive } from 'vue';
import type { OutcomeMapping, PerformanceIndicatorDetail, PiRubric } from '@/types/schedule';

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
  // Config & Metadata
  const academicYears = ["2024-2025", "2025-2026", "2026-2027", "2027-2028", "2028-2029", "2029-2030"];
  const outcomes = [1, 2, 3, 4, 5, 6];
  const highlightQuery = ref('');

  // SO-PI Dropdown Data (local mock — backend later)
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
  ]);

  // 6-Year Outcome Schedule Grid State
  const sixYearSchedule = reactive<Record<number, Record<string, string[]>>>({
    1: { "2024-2025": ["CS 350", "CS 400"], "2025-2026": ["CS 101", "CS 201", "CS 360"], "2026-2027": ["CS 320", "CS 350", "CS 400"], "2027-2028": ["CS 201", "CS 360", "CS 402"], "2028-2029": ["CS 101", "CS 320", "CS 350", "CS 400"], "2029-2030": ["CS 201", "CS 320", "CS 360", "CS 402"] },
    2: { "2024-2025": ["CS 320", "CS 330"], "2025-2026": ["CS 335", "CS 360", "CS 400"], "2026-2027": ["CS 201", "CS 330"], "2027-2028": ["CS 320", "CS 335", "CS 360", "CS 402"], "2028-2029": ["CS 330"], "2029-2030": ["CS 201", "CS 330", "CS 360", "CS 402"] },
    3: { "2024-2025": ["CS 400"], "2025-2026": ["CS 320"], "2026-2027": ["CS 320", "CS 400"], "2027-2028": ["CS 402"], "2028-2029": ["CS 400"], "2029-2030": ["CS 320", "CS 402"] },
    4: { "2024-2025": ["CS 330"], "2025-2026": ["CS 335", "CS 456"], "2026-2027": ["CS 330"], "2027-2028": ["CS 335", "CS 456"], "2028-2029": ["CS 330"], "2029-2030": ["CS 335", "CS 456"] },
    5: { "2024-2025": ["CS 400"], "2025-2026": ["CS 320"], "2026-2027": ["CS 320", "CS 400"], "2027-2028": ["CS 402"], "2028-2029": ["CS 400"], "2029-2030": ["CS 320", "CS 402"] },
    6: { "2024-2025": ["CS 201", "CS 420"], "2025-2026": ["CS 101", "CS 320", "CS 335", "CS 340", "CS 400"], "2026-2027": ["CS 201", "CS 420"], "2027-2028": ["CS 101", "CS 335", "CS 340", "CS 402"], "2028-2029": ["CS 101", "CS 320", "CS 420"], "2029-2030": ["CS 201", "CS 335", "CS 340", "CS 402"] }
  });

  // Global Available Course Options
  const allAvailableCourses = ref([
    "CS 101", "CS 201", "CS 320", "CS 330", "CS 335",
    "CS 340", "CS 350", "CS 360", "CS 400", "CS 402",
    "CS 420", "CS 456", "CS 499", "MATH 210"
  ]);

  // Course / Outcome Mapping Matrix
  const courses = ["CS 101", "CS 201", "CS 320", "CS 330", "CS 335", "CS 340", "CS 350", "CS 360", "CS 400", "CS 402", "CS 420"];
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

  // Actions
  function removeCourseFromCell(outcome: number, year: string, courseToRemove: string) {
    sixYearSchedule[outcome][year] = sixYearSchedule[outcome][year].filter(c => c !== courseToRemove);
  }

  function addCourseToCell(outcome: number, year: string, courseName: string) {
    if (!sixYearSchedule[outcome][year].includes(courseName)) {
      sixYearSchedule[outcome][year].push(courseName);
    }
  }

  function registerNewCourse(courseName: string): string {
    const formattedName = courseName.trim().toUpperCase();
    if (!formattedName) return formattedName;
    if (!allAvailableCourses.value.includes(formattedName)) {
      allAvailableCourses.value.push(formattedName);
    }
    return formattedName;
  }

  function findPi(outcomeId: number, piName: string): PerformanceIndicatorDetail | null {
    const outcome = soPiMap.find(so => so.id === outcomeId);
    return outcome?.pis.find(pi => pi.name === piName) ?? null;
  }

  function updatePiDetails(
    outcomeId: number,
    piName: string,
    updates: { definition?: string; rubric?: PiRubric }
  ) {
    const pi = findPi(outcomeId, piName);
    if (!pi) return;
    if (updates.definition !== undefined) {
      pi.definition = updates.definition;
    }
    if (updates.rubric) {
      pi.rubric.below = updates.rubric.below;
      pi.rubric.met = updates.rubric.met;
      pi.rubric.exceeds = updates.rubric.exceeds;
    }
  }

  return {
    academicYears,
    outcomes,
    highlightQuery,
    soPiMap,
    sixYearSchedule,
    allAvailableCourses,
    courses,
    courseOutcomeMap,
    removeCourseFromCell,
    addCourseToCell,
    registerNewCourse,
    findPi,
    updatePiDetails,
  };
});
