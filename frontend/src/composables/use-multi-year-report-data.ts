
import { ref, type ComputedRef } from 'vue';
import api from '@/api';

import type { SummaryReportData } from './use-summary-report-data';

interface MultiYearReportProps {
  programId: number;
  startDate: string;
  endDate: string;
}

export function useMultiYearReportData(
  props: ComputedRef<MultiYearReportProps>
) {
  const loading = ref(false);
  const error = ref<string | null>(null);
  const reportData = ref<SummaryReportData | null>(null);

  async function loadReportData() {
    // Clear previous results
    error.value = null;
    reportData.value = null;

    const { programId, startDate, endDate } = props.value;

    // Validate parameters
    if (!programId || programId <= 0) {
      error.value = 'Invalid program ID';
      return;
    }

    if (!startDate || !endDate) {
      error.value = 'Please select both a start and end date';
      return;
    }

    if (endDate < startDate) {
      error.value = 'End date cannot be before start date';
      return;
    }

    loading.value = true;

    try {
      // Request multi-year report from backend
      const reportRes = await api.get('/reports/multi-year', {
        params: {
          programId,
          startDate,
          endDate
        }
      });

      const responseData = reportRes.data?.data;

      console.log('Full multi-year response:', reportRes.data);
      console.log('Multi-year report data:', responseData);

      console.log('Selected date range:', {
        programId,
        startDate,
        endDate
      });

      // Validate response
      if (
        !responseData ||
        !Array.isArray(responseData.outcomes) ||
        responseData.outcomes.length === 0
      ) {
        error.value = 'No measures found for the selected date range';
        return;
      }

      reportData.value = responseData as SummaryReportData;

    } catch (err: unknown) {
      console.error('Multi-Year Report Error:', err);

      if (typeof err === 'object' && err !== null && 'response' in err) {
        const axiosError = err as {
          response?: {
            data?: {
              message?: string;
              error?: string;
            };
            status?: number;
          };
          config?: {
            url?: string;
            params?: unknown;
          };
        };

        console.error(
          'Backend Response:',
          axiosError.response?.data
        );

        console.error(
          'HTTP Status:',
          axiosError.response?.status
        );

        console.error(
          'Request URL:',
          axiosError.config?.url
        );

        console.error(
          'Request Parameters:',
          axiosError.config?.params
        );

        error.value =
          axiosError.response?.data?.message ??
          axiosError.response?.data?.error ??
          'Failed to load multi-year report data';

      } else {
        error.value =
          err instanceof Error
            ? err.message
            : 'Failed to load multi-year report data';
      }

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




// import { ref, type Ref } from 'vue';
// import api from '@/api';
// import type { SummaryReportData, IndicatorData, IndicatorMeasure, OutcomeData } from './use-summary-report-data';
//
// interface MultiYearReportProps {
//   programId: number;
//   startDate: string; // ISO: "YYYY-MM-DD"
//   endDate: string;   // ISO: "YYYY-MM-DD"
// }
//
// export function useMultiYearReportData(props: Ref<MultiYearReportProps>) {
//   const loading = ref(false);
//   const error = ref<string | null>(null);
//   const reportData = ref<SummaryReportData | null>(null);
//
//   // ==============================
//   // UTILITY FUNCTIONS
//   // ==============================
//
//   function calculateMetPercentage(met: number, exceeded: number, below: number): number {
//     const total = met + exceeded + below;
//     if (total === 0) return 0;
//     return Math.round(((met + exceeded) / total) * 1000) / 10;
//   }
//
//   function determineStatus(percentage: number): string {
//     if (percentage >= 80) return 'Met comfortably';
//     if (percentage >= 70) return 'Met';
//     if (percentage >= 65) return 'Barely not met';
//     return 'Not met';
//   }
//
//   function determineOutcomeStatus(indicators: IndicatorData[]): string {
//     const allMeasures = indicators.flatMap(i => i.measures);
//     if (allMeasures.length === 0) return 'No Data';
//     const metCount = allMeasures.filter(
//       m => m.status === 'Met comfortably' || m.status === 'Met'
//     ).length;
//     const percentage = (metCount / allMeasures.length) * 100;
//     if (percentage >= 80) return 'MET';
//     if (percentage >= 50) return 'Partially Met';
//     return 'Not Met';
//   }
//
//   // ==============================
//   // MAIN LOAD FUNCTION
//   // ==============================
//
//   async function loadReportData() {
//     if (!props.value.programId || props.value.programId <= 0) {
//       error.value = 'Invalid program ID';
//       return;
//     }
//     if (!props.value.startDate || !props.value.endDate) {
//       error.value = 'Please select both a start and end date';
//       return;
//     }
//     if (props.value.endDate < props.value.startDate) {
//       error.value = 'End date cannot be before start date';
//       return;
//     }
//
//     loading.value = true;
//     error.value = null;
//     reportData.value = null;
// //comment
//     //ckedlkfjsdkljghskljfg
//     try {
//       // Fetch multi-year report from backend
//       const reportRes = await api.get('/reports/multi-year', {
//         params: {
//           programId: props.value.programId,
//           startDate: props.value.startDate,
//           endDate: props.value.endDate
//         }
//       });
//
//       const responseData = reportRes.data.data;
//
//       console.log('Full multi-year response:', reportRes.data);
//       console.log('Report data:', responseData);
//       console.log('Selected date range:', {
//         programId: props.value.programId,
//         startDate: props.value.startDate,
//         endDate: props.value.endDate
//       });
//       console.log('Outcomes:', responseData?.outcomes);
//
//       if (!responseData || responseData.outcomes.length === 0) {
//         error.value = 'No measures found for the selected date range';
//         loading.value = false;
//         return;
//       }
//
//       reportData.value = responseData;
//
//     } catch (err: unknown) {
//       console.error('Multi-Year Report Error:', err);
//
//       if (err instanceof Error) {
//         console.error('Error Message:', err.message);
//       }
//
//       if (typeof err === 'object' && err !== null && 'response' in err) {
//         const axiosError = err as {
//           response?: {
//             data?: { message?: string; error?: string };
//             status?: number;
//           };
//           config?: {
//             url?: string;
//             params?: unknown;
//           };
//         };
//
//         console.error('Backend Response:', axiosError.response?.data);
//         console.error('HTTP Status:', axiosError.response?.status);
//         console.error('Request URL:', axiosError.config?.url);
//         console.error('Request Parameters:', axiosError.config?.params);
//
//         error.value =
//           axiosError.response?.data?.message ??
//           axiosError.response?.data?.error ??
//           'Failed to load multi-year report data';
//       } else {
//         error.value = 'Failed to load multi-year report data';
//       }
//     } finally {
//       loading.value = false;
//     }
//   }
//
//   return { loading, error, reportData, loadReportData };
// }
