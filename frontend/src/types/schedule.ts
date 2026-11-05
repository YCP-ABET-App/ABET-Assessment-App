export interface PiRubric {
  below: string;
  met: string;
  exceeds: string;
}

export interface PerformanceIndicatorDetail {
  id: number;
  name: string;
  indicatorNumber: number;
  definition: string;
  rubric: PiRubric;
  studentOutcomeId: number;
  programId: number | null;
}

export interface OutcomeMapping {
  id: number;
  number: number;
  description: string;
  pis: PerformanceIndicatorDetail[];
}

export interface SelectedPI {
  outcomeId: number;
  outcomeNumber: number;
  outcomeDesc: string;
  piId: number;
  piName: string;
  piDefinition: string;
}

export interface TargetCell {
  outcome: number;
  year: string;
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
  courseName?: string;
}

export interface SemesterEntity {
  id: number;
  name?: string;
  code?: string;
  academicYear: number;
  type: string;
  startDate?: string;
}
