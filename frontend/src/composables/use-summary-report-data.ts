import { ref, computed } from 'vue';
import type { Ref, ComputedRef } from 'vue';
import api from '@/api';

// ==============================
// INTERFACES
// ==============================

// A single measure attached to an indicator for a specific course
export interface IndicatorMeasure {
  measureId: number;
  courseIndicatorId?: number;
  scheduleEntryId: number;
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
  indicatorDescription?: string;
  courseCode: string;
  studentCount?: number;                    // Students in the course (all program sections combined)
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
  // UTILITY FUNCTIONS
  // ==============================

  function calculateMetPercentage(met: number, exceeded: number, below: number): number {
    const total = met + exceeded + below;
    if (total === 0) return 0;
    return Math.round(((met + exceeded) / total) * 1000) / 10; // 1 decimal place
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
      // console.log('Loading summary report for program:', programId.value, 'semester:', semesterId.value);

      // ------------------------------
      // Load Semester
      // ------------------------------
      const semesterRes = await api.get('/semesters', {
        params: { id: semesterId.value }
      });

      const semester = Array.isArray(semesterRes.data.data)
        ? semesterRes.data.data[0]
        : semesterRes.data.data;
      console.log('SEMESTER KEYS:', Object.keys(semester));
      console.log('FULL SEMESTER:', JSON.stringify(semester, null, 2));

      if (!semester) throw new Error('Semester not found');

      // ------------------------------
      // Load Outcomes
      // ------------------------------
      const outcomesRes = await api.get(`/outcome/bySemester/${semesterId.value}`);
      console.log('========== SINGLE REPORT DEBUG ==========');
      console.log('SEMESTER ID:', semesterId.value);
      console.log(
        'OUTCOME RESPONSE:',
        JSON.stringify(outcomesRes.data, null, 2)
      );
      console.log('=========================================');

      const outcomes = outcomesRes.data.data ?? [];
      console.log('Loaded outcomes:', outcomes.length, outcomes);

      if (outcomes.length === 0) {
        error.value = 'No student outcomes found for this semester';
        loading.value = false;
        return;
      }

      // ------------------------------
      // Load Admin Names ("Generated by")
      // ------------------------------
      const adminsRes = await api.get(`/program/${programId.value}/admins`);
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
        } catch (err) {
          console.warn(`Failed to load admin user ${admin.userId}`);
        }
      }

      // ------------------------------
      // Load All Active Courses (once)
      // ------------------------------
      let allCourses: any[] = [];
      try {
        const coursesRes = await api.get('/courses/active', {
          params: {
            semesterId: semesterId.value,
            page: 0,
            size: 1000
          }
        });

        allCourses = coursesRes.data.content ?? coursesRes.data.data ?? [];
        // console.log('Loaded active courses:', allCourses.length, allCourses);
      } catch (err: any) {
        throw new Error('Failed to load courses');
      }

      // ==============================
      // BUILD OUTCOME DATA
      // ==============================

      const outcomeDataArray: OutcomeData[] = [];

      for (const outcome of outcomes) {
        const indicatorsRes = await api.get('/performance-indicators/by-outcome/active', {
          params: { studentOutcomeId: outcome.id }
        });

        const indicators = indicatorsRes.data.data ?? [];
        // console.log(`Outcome ${outcome.number}: found ${indicators.length} indicators`);
        const indicatorDataArray: IndicatorData[] = [];
        const recommendedActions: string[] = [];

        const outcomeDescription =
          outcome.description ??
          outcome.outDescription ??
          '';

        for (const indicator of indicators) {
          const indicatorNumber =
            indicator.indNumber ??
            indicator.indicatorNumber ??
            indicator.ind_number ??
            indicator.indicator_number;
          const indicatorDescription = indicator.description ?? '';


          for (const course of allCourses) {
            console.log('SECTION REQUEST:', {
              course: course.courseCode,
              courseId: course.id,
              semesterId: semesterId.value,
              programId: programId.value
            });

            const sectionsRes = await api.get('/section', {
              params: {
                courseId: course.id,
                semesterId: semesterId.value,
                programId: programId.value
              }
            });


            // console.log("COURSE:", course.courseCode);
            // console.log("COURSE OBJECT:", course);
            // console.log("COURSE ID:", course.id);
            // console.log("SECTIONS API RESPONSE:", sectionsRes.data);

            const sectionData = sectionsRes.data?.data;

            const sections = Array.isArray(sectionData)
              ? sectionData
              : Array.isArray(sectionData?.sections)
                ? sectionData.sections
                : Array.isArray(sectionData?.content)
                  ? sectionData.content
                  : Array.isArray(sectionsRes.data?.content)
                    ? sectionsRes.data.content
                    : [];

            console.log(
              `Found ${sections.length} sections for ${course.courseCode}`
            );

            if (sections.length === 0) {
              console.warn(
                `No sections found for course ${course.courseCode}`,
                {
                  courseId: course.id,
                  semesterId: semesterId.value,
                  response: sectionsRes.data
                }
              );
              continue;
            }

            const sectionPrograms: any[] = [];

            for (const section of sections) {
              const sectionProgramRes = await api.get('/section-program', {
                params: {
                  sectionId: section.id,
                  programId: programId.value
                }
              });

              const results = sectionProgramRes.data.data ?? [];
              sectionPrograms.push(...results);
            }

            if (sectionPrograms.length === 0) {
              continue;
            }

            const programSectionProgramIds = new Set(
              sectionPrograms.map((sp: any) => Number(sp.id))
            );

            const scheduleRes = await api.get('/schedule-entry', {
              params: {
                programId: programId.value,
                semesterId: semesterId.value,
                courseId: course.id,
                indicatorId: indicator.id
              }
            });
            const scheduleEntries = scheduleRes.data.data ?? [];

            if (scheduleEntries.length === 0) {
              continue;
            }

            const formattedMeasures: IndicatorMeasure[] = [];

            for (const entry of scheduleEntries) {
              const measuresRes = await api.get('/measure', {
                params: { scheduleEntryId: entry.id, active: true }
              });
              const measures = measuresRes.data.data ?? [];

              for (const measure of measures) {
                const resultsRes = await api.get('/measure-result', {
                  params: { measureId: measure.id }
                });
                const results = (resultsRes.data.data ?? []).filter((r: any) =>
                  programSectionProgramIds.has(Number(r.sectionProgramId))
                );

                let met = 0;
                let exceeded = 0;
                let below = 0;
                const notes: string[] = [];

                for (const result of results) {
                  met += Number(result.studentsMet) || 0;
                  exceeded += Number(result.studentsExceeded) || 0;
                  below += Number(result.studentsBelow) || 0;
                  if (result.observation) {
                    notes.push(result.observation);
                  }
                }

                const pct = calculateMetPercentage(met, exceeded, below);
                const status = determineStatus(pct);
                const recAction = measure.recommendedAction ?? null;

                formattedMeasures.push({
                  measureId: measure.id,
                  scheduleEntryId: entry.id,
                  courseCode: course.courseCode,
                  description: measure.description ?? 'No description',
                  studentsMet: met,
                  studentsExceeded: exceeded,
                  studentsBelow: below,
                  met,
                  exceeded,
                  below,
                  metPercentage: pct,
                  status,
                  note: notes.length ? [...new Set(notes)].join(' ') : undefined,
                  recommendedAction: recAction
                });

                if (recAction) {
                  recommendedActions.push(recAction);
                }
              }
            }

            if (formattedMeasures.length > 0) {
              const studentCount = formattedMeasures.reduce((max, m) => {
                const total = (m.studentsMet ?? 0) + (m.studentsExceeded ?? 0) + (m.studentsBelow ?? 0);
                return Math.max(max, total);
              }, 0);

              indicatorDataArray.push({
                id: indicator.id,
                indicatorNumber: `${outcome.number}.${indicatorNumber}`,
                indicatorDescription,
                courseCode: course.courseCode,
                studentCount,
                measures: formattedMeasures
              });
            }
          }
        }

        if (indicatorDataArray.length > 0) {
          outcomeDataArray.push({
            outcomeNumber: outcome.number,
            outcomeDescription,
            overallStatus: determineOutcomeStatus(indicatorDataArray),
            indicators: indicatorDataArray,
            recommendedActions: [...new Set(recommendedActions)]
          });
        }
      }

      // ==============================
      // FINAL REPORT OBJECT
      // ==============================

      const academicYear = String(semester.academicYear);

      reportData.value = {
        semesterId: semesterId.value!,
        semesterName: semester.name,
        academicYear,
        generatedDate: new Date().toLocaleDateString('en-US'),
        generatedBy: adminNames.length ? adminNames : ['System Administrator'],
        outcomes: outcomeDataArray
      };

      console.log('FINAL REPORT DATA:', reportData.value);

    } catch (err: any) {
      error.value = err.message ?? 'Failed to load summary report data';
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
