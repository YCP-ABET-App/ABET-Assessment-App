// src/types/schedule.ts
export interface PiRubric {
  below: string;
  met: string;
  exceeds: string;
}

export interface PerformanceIndicatorDetail {
  name: string;
  definition: string;
  rubric: PiRubric;
}

export interface OutcomeMapping {
  id: number;
  description: string;
  pis: PerformanceIndicatorDetail[];
}

export interface SelectedPI {
  outcomeId: number;
  outcomeDesc: string;
  piName: string;
  piDefinition: string;
}

/* `year` is an academic year ("2025-2026") or TBD for the TBD column. */
export interface TargetCell {
  outcome: number;
  year: string;
}

// ---- Schedule mappings (new) ----
export const TBD = 'TBD' as const;

export type RealSemester = 'FALL' | 'SPRING' | 'BOTH';
export type SemesterSlot = RealSemester | typeof TBD;

export const SEMESTER_OPTIONS: { value: RealSemester; label: string }[] = [
  { value: 'FALL', label: 'Fall' },
  { value: 'SPRING', label: 'Spring' },
  { value: 'BOTH', label: 'Both' },
];

/*
 * One course assessed for one outcome in one year + semester.
 * year === TBD  -> semester === TBD, piNumbers === [] (outcome-level placeholder)
 * otherwise     -> semester is a real semester, piNumbers has >= 1 entry (1-based index within the outcome)
 */
export interface ScheduleMapping {
  id: string;
  course: string;
  outcome: number;
  year: string;
  semester: SemesterSlot;
  piNumbers: number[];
}

export interface NewMapping {
  course: string;
  outcome: number;
  year: string;
  semester: SemesterSlot;
  piNumbers: number[];
}

/* Flattened junction-table rows. outcomeLevel rows = TBD: the outcome is sent in place of a PI. */
export interface BackendScheduleRow {
  course: string;
  academicYear: string | null;
  semester: SemesterSlot;
  outcomeId: number;
  piNumber: number | null;
  outcomeLevel: boolean;
}

export interface ScheduleEntryPayload {
  id?: number;
  semesterId: number;
  courseId: number;
  programId: number;
  indicatorId: number;
}

export interface CourseEntity {
  id: number;
  courseCode: string;
}

export interface SemesterEntity {
  id: number;
  academicYear: number;
  type: string;
}

export interface PerformanceIndicator {
  id: number;
  indNumber: number;
  outcomeId: number;
  description: string;
}

export interface ScheduledCourse {
  code: string;
  semesterType: 'FALL' | 'SPRING' | 'BOTH' | 'TBD';
  indicatorIds: number[];
}
