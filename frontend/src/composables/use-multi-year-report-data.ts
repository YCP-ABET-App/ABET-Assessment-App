import { ref, type Ref } from 'vue';
import api from '@/api';
import type { SummaryReportData } from './use-summary-report-data';

interface MultiYearReportProps {
  programId: number;
  startDate: string; // ISO: "YYYY-MM-DD"
  endDate: string;   // ISO: "YYYY-MM-DD"
}

export function useMultiYearReportData(props: Ref<MultiYearReportProps>) {
  const loading = ref(false);
  const error = ref<string | null>(null);
  const reportData = ref<SummaryReportData[] | null>(null);

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
      const reportRes = await api.get('/reports/multi-year', {
        params: {
          programId: props.value.programId,
          startDate: props.value.startDate,
          endDate: props.value.endDate
        }
      });

      const responseData: SummaryReportData[] = reportRes.data.data;

      if (!responseData || !Array.isArray(responseData) || responseData.length === 0) {
        error.value = 'No data found for the selected semester range';
        loading.value = false;
        return;
      }

      reportData.value = responseData;

    } catch (err: any) {
      error.value = err.response?.data?.message ?? err.message ?? 'Failed to load multi-year report data';
    } finally {
      loading.value = false;
    }
  }

  return { loading, error, reportData, loadReportData };
}
