import { ref, computed } from 'vue';
import type { Ref, ComputedRef } from 'vue';
import api from '@/api';

// ==============================
// INTERFACES
// ==============================

// A single measure attached to an indicator for a specific course
export interface IndicatorMeasure {
  measureId: number;
  courseIndicatorId: number;
  courseCode: string;
  description: string;

  studentsMet: number;
  studentsExceeded: number;
  studentsBelow: number;

  met: number;
  exceeded: number;
  below: number;

  metPercentage: number;
  status: string;
  note?: string;
  recommendedAction?: string | null;
}


// A performance indicator + course pairing in the summary report
export interface IndicatorData {
  id: number;
  indicatorNumber: string;                  // e.g., "3.1"
  courseCode: string;
  studentCount?: number;                    // ADDED: Number of students in the course
  measures: IndicatorMeasure[];
}

// Outcome with nested indicator data
export interface OutcomeData {
  outcomeId: number;
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

interface SummaryReportProps {
  programId: number | Ref<number> | ComputedRef<number>;
  semesterId:
    | number
    | Ref<number | null>
    | ComputedRef<number | null>
    | null
    | undefined;
}

// ==============================
// COMPOSABLE
// ==============================

export function useSummaryReportData(props: SummaryReportProps) {
  const loading = ref(false);
  const error = ref<string | null>(null);
  const reportData = ref<SummaryReportData | null>(null);

  // Normalized props – always numbers
  const programId = computed(() =>
    typeof props.programId === 'number'
      ? props.programId
      : (props.programId as Ref<number>).value
  );

  const semesterId = computed(() => {
    if (typeof props.semesterId === 'number') return props.semesterId;
    if (!props.semesterId) return null;
    return (props.semesterId as Ref<number | null>).value;
  });

  // ==============================
  // MAIN LOAD FUNCTION
  // ==============================

  async function loadReportData() {
    if (!programId.value || programId.value <= 0) {
      error.value = 'Invalid program ID';
      return;
    }

    if (!semesterId.value || semesterId.value <= 0) {
      error.value = 'Please select a semester';
      return;
    }

    loading.value = true;
    error.value = null;
    reportData.value = null;

    try {
      const res = await api.get('/reports/summary', {
        params: { programId: programId.value, semesterId: semesterId.value }
      });

      const raw = res.data.data;
      if (!raw) throw new Error('No report data returned');

      reportData.value = {
        semesterId: raw.semesterId,
        semesterName: raw.semesterName,
        academicYear: raw.academicYear,
        generatedDate: raw.generatedDate,
        generatedBy: raw.generatedBy?.length ? raw.generatedBy : ['System Administrator'],
        outcomes: (raw.outcomes ?? []).map((o: any) => ({
          outcomeId: o.outcomeId,
          outcomeNumber: o.outcomeNumber,
          outcomeDescription: o.description,
          overallStatus: o.overallStatus,
          indicators: (o.indicators ?? []).map((ind: any) => ({
            id: ind.indicatorId,
            indicatorNumber: ind.indicatorNumber,
            courseCode: ind.courseCode,
            studentCount: ind.studentCount,
            measures: (ind.measures ?? []).map((m: any) => ({
              measureId: m.measureId,
              courseIndicatorId: m.courseIndicatorId,
              courseCode: m.courseCode,
              description: m.description,
              studentsMet: m.studentsMet,
              studentsExceeded: m.studentsExceeded,
              studentsBelow: m.studentsBelow,
              met: m.studentsMet,
              exceeded: m.studentsExceeded,
              below: m.studentsBelow,
              metPercentage: m.metPercentage,
              status: m.status,
              note: m.note ?? undefined,
              recommendedAction: m.recommendedAction ?? null,
            }))
          })),
          recommendedActions: o.recommendedActions ?? []
        }))
      };

    } catch (err: any) {
      error.value = err.response?.data?.message ?? err.message ?? 'Failed to load summary report data';
    } finally {
      loading.value = false;
    }
  }

  return {
    loading,
    error,
    reportData,
    loadReportData
  };
}
