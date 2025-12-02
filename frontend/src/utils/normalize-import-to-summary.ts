import type { SummaryReportData } from "@/types/summary";
import type { SummaryImport } from "@/utils/summaryParser";

export function normalizeImportToSummary(parsed: SummaryImport): SummaryReportData {
  return {
    semesterId: 0,
    semesterName: "",
    academicYear: "",
    generatedDate: "",
    generatedBy: ["Imported manually"],
    outcomes: parsed.outcomes.map(outcome => ({
      outcomeNumber: outcome.number,
      overallStatus: outcome.status,
      outcomeDescription: "",
      recommendedActions: [],

      indicators: outcome.indicators.flatMap(ind => {
        return ind.courses.map(course => ({
          id: Math.random(),
          indicatorNumber: ind.number,
          courseCode: course.courseCode,
          measures: course.measures.map(m => ({
            measureId: Math.random(),
            description: m.description,
            status: m.status,
            metPercentage: m.metPercentage,
            note: null,
            recommendedAction: m.recommendedActions.join("\n")
          }))
        }));
      })
    }))
  };
}
