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
      // Fetch multi-year report from backend
      const reportRes = await api.get('/reports/multi-year', {
        params: {
          programId: props.value.programId,
          startDate: props.value.startDate,
          endDate: props.value.endDate
        }
      });

      const responseData = reportRes.data.data;

      if (!responseData || responseData.outcomes.length === 0) {
        error.value = 'No measures found for the selected date range';
        loading.value = false;
        return;
      }

      reportData.value = responseData;

    } catch (err: any) {
      error.value = err.message ?? 'Failed to load multi-year report data';
    } finally {
      loading.value = false;
    }
  }

  return { loading, error, reportData, loadReportData };
}