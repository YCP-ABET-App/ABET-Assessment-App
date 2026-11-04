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
  semesterType: 'FALL' | 'SPRING' | 'SUMMER' | 'BOTH';
  indicatorIds: number[];
}
