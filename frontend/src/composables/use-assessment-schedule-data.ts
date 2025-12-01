import { ref, computed } from 'vue';
import api from '@/api';

export interface Course {
  id: number;
  courseCode: string;
  courseName: string;
}

export interface StudentOutcome {
  id: number;
  number?: number;  // from DTO
  outNumber?: number;  // camelCase from entity
  out_number?: number; // snake_case from DB
  description?: string; // from DTO
  outDescription?: string; // camelCase from entity
  out_description?: string; // snake_case from DB
}

export interface OutcomeScheduleRow {
  outcomeId: number;
  outcomeNumber: number;
  outcomeDescription: string;
  assignments: Map<string, number[]>; // year -> course IDs
}

export interface CourseOutcomeMapping {
  courseId: number;
  courseCode: string;
  courseName: string;
  outcomeMappings: {
    [outcomeNumber: number]: number[]; // performance indicator numbers
  };
}

interface AssessmentScheduleProps {
  programId: number;
  startYear?: number;
  yearCount?: number;
}

export function useAssessmentScheduleData(props: AssessmentScheduleProps) {
  const loading = ref(false);
  const error = ref<string | null>(null);
  const courses = ref<Course[]>([]);
  const outcomes = ref<StudentOutcome[]>([]);
  const scheduleData = ref<OutcomeScheduleRow[]>([]);
  const courseOutcomeMappings = ref<CourseOutcomeMapping[]>([]);

  // Generate academic years
  const academicYears = computed(() => {
    const start = props.startYear || new Date().getFullYear();
    const count = props.yearCount || 6;
    const years: string[] = [];

    for (let i = 0; i < count; i++) {
      const yearStart = start + i;
      const yearEnd = yearStart + 1;
      years.push(`${yearStart}-${yearEnd}`);
    }

    return years;
  });

  // Get course codes for a cell
  function getCourseCodesForCell(outcomeId: number, year: string): string {
    const row = scheduleData.value.find(r => r.outcomeId === outcomeId);
    if (!row) return '';

    const courseIds = row.assignments.get(year) || [];
    if (courseIds.length === 0) return '';

    const courseCodes = courseIds
      .map(id => {
        const course = courses.value.find(c => c.id === id);
        if (!course) return null;
        // Remove "CS " prefix for brevity
        return course.courseCode.replace(/^CS\s*/i, '').trim();
      })
      .filter(code => code !== null)
      .sort((a, b) => {
        const numA = parseInt(a!) || 0;
        const numB = parseInt(b!) || 0;
        return numA - numB;
      });

    return courseCodes.length > 0 ? `(${courseCodes.join(', ')})` : '';
  }

  // Get full course names for tooltip
  function getCourseNamesForCell(outcomeId: number, year: string): string {
    const row = scheduleData.value.find(r => r.outcomeId === outcomeId);
    if (!row) return '';

    const courseIds = row.assignments.get(year) || [];
    if (courseIds.length === 0) return 'No courses assigned';

    const courseNames = courseIds
      .map(id => {
        const course = courses.value.find(c => c.id === id);
        return course ? `${course.courseCode} - ${course.courseName}` : null;
      })
      .filter(name => name !== null);

    return courseNames.join('\n');
  }

  // Format performance indicator list
  function formatIndicatorList(indicators: number[]): string {
    if (!indicators || indicators.length === 0) return '';
    return indicators.sort((a, b) => a - b).join(', ');
  }

  // Load data from backend
  async function loadData() {
    loading.value = true;
    error.value = null;

    try {
      // Load courses
      const coursesRes = await api.get(`/program/${props.programId}/courses/active`);
      courses.value = coursesRes.data.data || [];

      // Load outcomes
      const semestersRes = await api.get('/semesters', {
        params: { programId: props.programId, page: 0, size: 100 }
      });

      const semesters = semestersRes.data.content || [];

      if (semesters.length > 0) {
        const latestSemester = semesters[0];
        const outcomesRes = await api.get(`/outcome/bySemester/${latestSemester.id}`);
        outcomes.value = outcomesRes.data.data || [];
        console.log('Loaded outcomes:', outcomes.value);
      }

      // Initialize schedule data
      await buildScheduleData();

    } catch (err) {
      console.error('Error loading data:', err);
      error.value = 'Failed to load assessment schedule';
    } finally {
      loading.value = false;
    }
  }

  // Build schedule data structure
  async function buildScheduleData() {
    const data: OutcomeScheduleRow[] = [];
    const courseMappingMap = new Map<number, CourseOutcomeMapping>();

    // Initialize course mappings
    courses.value.forEach(course => {
      courseMappingMap.set(course.id, {
        courseId: course.id,
        courseCode: course.courseCode,
        courseName: course.courseName,
        outcomeMappings: {}
      });
    });

    for (const outcome of outcomes.value) {
      const assignments = new Map<string, number[]>();

      // Initialize all years with empty arrays
      academicYears.value.forEach(year => {
        assignments.set(year, []);
      });

      // TODO: Load actual assignments from backend
      // For now, initializing with empty data

      // Handle all possible field name variations (DTO, camelCase, snake_case)
      const outcomeNumber = outcome.number ?? outcome.outNumber ?? outcome.out_number ?? 0;
      const outcomeDescription = outcome.description ?? outcome.outDescription ?? outcome.out_description ?? '';

      data.push({
        outcomeId: outcome.id,
        outcomeNumber: outcomeNumber,
        outcomeDescription: outcomeDescription,
        assignments
      });
    }

    // Build course-outcome mappings from course-indicator relationships
    for (const course of courses.value) {
      try {
        const indicatorsRes = await api.get(`/courses/${course.id}/indicators`);
        const indicatorIds = indicatorsRes.data || [];

        // For each indicator, get its details including which outcome it belongs to
        for (const indicatorId of indicatorIds) {
          try {
            const indicatorRes = await api.get(`/performance-indicators/${indicatorId}`);
            const indicator = indicatorRes.data.data;

            if (indicator && indicator.studentOutcomeId) {
              const outcome = outcomes.value.find(o => o.id === indicator.studentOutcomeId);
              if (outcome) {
                const outcomeNumber = outcome.number ?? outcome.outNumber ?? outcome.out_number ?? 0;

                // Add to course mapping
                const courseMapping = courseMappingMap.get(course.id);
                if (courseMapping) {
                  if (!courseMapping.outcomeMappings[outcomeNumber]) {
                    courseMapping.outcomeMappings[outcomeNumber] = [];
                  }
                  courseMapping.outcomeMappings[outcomeNumber].push(indicator.indicatorNumber);
                }
              }
            }
          } catch (err) {
            console.error(`Error loading indicator ${indicatorId}:`, err);
          }
        }
      } catch (err) {
        console.error(`Error loading indicators for course ${course.id}:`, err);
      }
    }

    scheduleData.value = data;
    courseOutcomeMappings.value = Array.from(courseMappingMap.values());
  }

  return {
    loading,
    error,
    courses,
    outcomes,
    scheduleData,
    courseOutcomeMappings,
    academicYears,
    loadData,
    getCourseCodesForCell,
    getCourseNamesForCell,
    formatIndicatorList
  };
}
