import { ref, type Ref } from 'vue';
import api from '@/api';
import type { SummaryReportData, IndicatorData, IndicatorMeasure, OutcomeData } from './use-summary-report-data';

interface MultiYearReportProps {
  programId: number;
  startDate: string; // ISO: "YYYY-MM-DD"
  endDate: string;   // ISO: "YYYY-MM-DD"
}

export function useMultiYearReportData(props: Ref<MultiYearReportProps>) {
  const loading = ref(false);
  const error = ref<string | null>(null);
  const reportData = ref<SummaryReportData | null>(null);

  // ==============================
  // UTILITY FUNCTIONS
  // ==============================

  function calculateMetPercentage(met: number, exceeded: number, below: number): number {
    const total = met + exceeded + below;
    if (total === 0) return 0;
    return Math.round(((met + exceeded) / total) * 1000) / 10;
  }

  function determineStatus(percentage: number): string {
    if (percentage >= 80) return 'Met comfortably';
    if (percentage >= 70) return 'Met';
    if (percentage >= 65) return 'Barely not met';
    return 'Not met';
  }

  function determineOutcomeStatus(indicators: IndicatorData[]): string {
    const allMeasures = indicators.flatMap(i => i.measures);
    if (allMeasures.length === 0) return 'No Data';
    const metCount = allMeasures.filter(
      m => m.status === 'Met comfortably' || m.status === 'Met'
    ).length;
    const percentage = (metCount / allMeasures.length) * 100;
    if (percentage >= 80) return 'MET';
    if (percentage >= 50) return 'Partially Met';
    return 'Not Met';
  }

  // ==============================
  // MAIN LOAD FUNCTION
  // ==============================

  async function loadReportData() {
    if (!props.value.programId || props.value.programId <= 0) {
      error.value = 'Invalid program ID';
      return;
    }
    if (!props.value.startDate || !props.value.endDate) {
      error.value = 'Please select both a start and end date';
      return;
    }
    if (props.value.endDate < props.value.startDate) {
      error.value = 'End date cannot be before start date';
      return;
    }

    loading.value = true;
    error.value = null;
    reportData.value = null;

    try {
      // ------------------------------
      // Fetch raw measures + semesters from backend
      // ------------------------------
      const reportRes = await api.get('/reports/multi-year', {
        params: {
          programId: props.value.programId,
          startDate: props.value.startDate,
          endDate: props.value.endDate
        }
      });

      const payload = reportRes.data.data;
      const rawMeasures: any[] = payload.measures ?? [];
      const semesters: any[] = payload.semesters ?? [];

      if (rawMeasures.length === 0) {
        error.value = 'No measures found for the selected date range';
        loading.value = false;
        return;
      }

      // ------------------------------
      // Load Outcomes for the program
      // ------------------------------
      const firstSemesterId = semesters[0]?.id;
      if (!firstSemesterId) {
        error.value = 'No semesters found in the selected date range';
        loading.value = false;
        return;
      }

      const outcomesRes = await api.get(`/outcome/bySemester/${firstSemesterId}`);
      const outcomes = outcomesRes.data.data ?? [];

      if (outcomes.length === 0) {
        error.value = 'No student outcomes found for this program';
        loading.value = false;
        return;
      }

      // ------------------------------
      // Load Admin Names
      // ------------------------------
      const adminsRes = await api.get(`/program/${props.value.programId}/admins`);
      const programAdmins = adminsRes.data.data ?? [];
      const adminNames: string[] = [];

      for (const admin of programAdmins) {
        try {
          const userRes = await api.get(`/users/${admin.userId}`);
          const user = userRes.data.data;
          const title = user.nameTitle || user.name_title || '';
          const first = user.nameFirst || user.firstName || '';
          const last = user.nameLast || user.lastName || '';
          adminNames.push([title, first, last].filter(Boolean).join(' '));
        } catch {
          console.warn(`Failed to load admin user ${admin.userId}`);
        }
      }

      // ------------------------------
      // Load all active courses for the program 
      // ------------------------------
      const coursesRes = await api.get('/courses/active', {
        params: { programId: props.value.programId }
      });
      const allCourses: any[] = coursesRes.data.data ?? [];

      // ==============================
      // BUILD OUTCOME DATA
      // ==============================

      const outcomeDataArray: OutcomeData[] = [];

      for (const outcome of outcomes) {
        const indicatorsRes = await api.get('/performance-indicators/by-outcome/active', {
          params: { studentOutcomeId: outcome.id }
        });
        const indicators = indicatorsRes.data.data ?? [];
        const indicatorDataArray: IndicatorData[] = [];
        const recommendedActions: string[] = [];

        for (const indicator of indicators) {
          for (const course of allCourses) {
            const indicatorIdsRes = await api.get(`/courses/${course.id}/indicators`);
            const indicatorIds = indicatorIdsRes.data ?? [];
            if (!indicatorIds.includes(indicator.id)) continue;

            // Filter rawMeasures to those belonging to this indicator and course
            const indicatorMeasuresRes = await api.get(`/measure/byIndicator/${indicator.id}`);
            const indicatorMeasures = indicatorMeasuresRes.data.data ?? [];

            const courseMeasuresRes = await api.get(`/measure/byCourse/${course.id}`);
            const courseMeasures = courseMeasuresRes.data.data ?? [];

            const matched = indicatorMeasures.filter((m: any) =>
              courseMeasures.some((cm: any) => cm.id === m.id)
            );

            const formattedMeasures: IndicatorMeasure[] = [];

            for (const measure of matched) {
              const met = measure.met ?? measure.studentsMet;
              const exceeded = measure.exceeded ?? measure.studentsExceeded;
              const below = measure.below ?? measure.studentsBelow;
              if (met == null || exceeded == null || below == null) continue;

              const pct = calculateMetPercentage(met, exceeded, below);
              const status = determineStatus(pct);
              const desc = measure.measureDescription || measure.description || 'No description';

              formattedMeasures.push({
                measureId: measure.id,
                courseIndicatorId: measure.courseIndicatorId,
                courseCode: course.courseCode,
                description: desc,
                studentsMet: met,
                studentsExceeded: exceeded,
                studentsBelow: below,
                met,
                exceeded,
                below,
                metPercentage: pct,
                status,
                note: measure.observation ?? undefined,
                recommendedAction: measure.recommendedAction ?? null
              });

              if (measure.recommendedAction) {
                recommendedActions.push(measure.recommendedAction);
              }
            }

            if (formattedMeasures.length > 0) {
              const indicatorNumber =
                indicator.indNumber ?? indicator.indicatorNumber ?? indicator.ind_number;

              indicatorDataArray.push({
                id: indicator.id,
                indicatorNumber: `${outcome.number}.${indicatorNumber}`,
                courseCode: course.courseCode,
                studentCount: course.studentCount ?? 0,
                measures: formattedMeasures
              });
            }
          }
        }

        if (indicatorDataArray.length > 0) {
          outcomeDataArray.push({
            outcomeNumber: outcome.number,
            outcomeDescription: outcome.outDescription,
            overallStatus: determineOutcomeStatus(indicatorDataArray),
            indicators: indicatorDataArray,
            recommendedActions: [...new Set(recommendedActions)]
          });
        }
      }

      // ==============================
      // FINAL REPORT OBJECT
      // ==============================

      reportData.value = {
        semesterId: firstSemesterId,
        semesterName: `${props.value.startDate} – ${props.value.endDate}`,
        academicYear: `${props.value.startDate.slice(0, 4)}–${props.value.endDate.slice(0, 4)}`,
        generatedDate: new Date().toLocaleDateString('en-US'),
        generatedBy: adminNames.length ? adminNames : ['System Administrator'],
        outcomes: outcomeDataArray
      };

    } catch (err: any) {
      error.value = err.message ?? 'Failed to load multi-year report data';
    } finally {
      loading.value = false;
    }
  }

  return { loading, error, reportData, loadReportData };
}