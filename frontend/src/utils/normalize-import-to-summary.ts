import type { SummaryReportData } from "@/types/summary";
import type { SummaryImport, OutcomeImport, IndicatorImport, CourseImport, MeasureImport } from "@/utils/summary-parser.ts";

export function normalizeImportToSummary(parsed: SummaryImport): SummaryReportData {
  return {
    semesterId: 0,
    semesterName: "",
    academicYear: "",
    generatedDate: "",
    generatedBy: ["Imported manually"],
    outcomes: parsed.outcomes.map((outcome: OutcomeImport) => ({
      outcomeNumber: outcome.number,
      overallStatus: outcome.status,
      outcomeDescription: "",
      recommendedActions: [],

      indicators: outcome.indicators.flatMap((ind: IndicatorImport) => {
        return ind.courses.map((course: CourseImport) => ({
          id: Math.random(),
          indicatorNumber: String(ind.number),
          courseCode: course.courseCode,
          measures: course.measures.map((m: MeasureImport) => ({
            measureId: Math.random(),
            courseCode: course.courseCode,
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
