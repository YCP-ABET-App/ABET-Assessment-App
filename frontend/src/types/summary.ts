// ==============================
// SUMMARY REPORT TYPE DEFINITIONS
// ==============================

// A single measure attached to an indicator for a specific course
export interface IndicatorMeasure {
  measureId: number;
  courseIndicatorId?: number;
  courseCode: string;
  description: string;

  studentsMet?: number;
  studentsExceeded?: number;
  studentsBelow?: number;

  met?: number;
  exceeded?: number;
  below?: number;

  metPercentage: number;
  status: string;
  note?: string | null;
  recommendedAction?: string | null;
}

// A performance indicator + course pairing in the summary report
export interface IndicatorData {
  id: number;
  indicatorNumber: string; // e.g., "3.1"
  courseCode: string;
  studentCount?: number; // Number of students in the course
  measures: IndicatorMeasure[];
}

// Outcome with nested indicator data
export interface OutcomeData {
  outcomeNumber: number;
  outcomeDescription: string;
  overallStatus: string;
  indicators: IndicatorData[];
  recommendedActions: string[];
}

// Final report root object
export interface SummaryReportData {
  semesterId: number;
  semesterName: string;
  academicYear: string;
  generatedDate: string;
  generatedBy: string[];
  outcomes: OutcomeData[];
}
