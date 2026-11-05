import api from '@/api';
import type { ScheduleEntryPayload } from '@/types/schedule';

function unwrap<T>(response: { data?: { data?: T; content?: T } }): T {
  const payload = response.data as { data?: T; content?: T } | T;
  if (payload && typeof payload === 'object' && 'data' in (payload as object) && (payload as { data?: T }).data !== undefined) {
    return (payload as { data: T }).data;
  }
  if (payload && typeof payload === 'object' && 'content' in (payload as object) && (payload as { content?: T }).content !== undefined) {
    return (payload as { content: T }).content;
  }
  return payload as T;
}

export const scheduleApi = {
  getCourses: async () => unwrap<any[]>(await api.get('/courses/searchCourse', { params: { isActive: true } })),

  getSemesters: async () => unwrap<any[]>(await api.get('/semesters')),

  getOutcomesByProgram: async (programId: number) =>
    unwrap<any[]>(await api.get(`/outcome/byProgram/${programId}`)),

  getOutcomesBySemester: async (semesterId: number) =>
    unwrap<any[]>(await api.get(`/outcome/bySemester/${semesterId}`)),

  getOutcome: async (id: number) => unwrap<any>(await api.get(`/outcome/${id}`)),

  getActiveIndicatorsByProgram: async (programId: number) =>
    unwrap<any[]>(await api.get('/performance-indicators/by-program/active', { params: { programId } })),

  getActiveIndicatorsByOutcome: async (studentOutcomeId: number) =>
    unwrap<any[]>(await api.get('/performance-indicators/by-outcome/active', { params: { studentOutcomeId } })),

  createOutcome: async (body: Record<string, unknown>) =>
    unwrap<any>(await api.post('/outcome', body)),

  updateOutcome: async (id: number, body: Record<string, unknown>) =>
    unwrap<any>(await api.put(`/outcome/${id}`, body)),

  deleteOutcome: async (id: number) => {
    await api.delete(`/outcome/${id}`);
  },

  createPerformanceIndicator: async (body: Record<string, unknown>) =>
    unwrap<any>(await api.post('/performance-indicators', body)),

  updatePerformanceIndicator: async (id: number, body: Record<string, unknown>) =>
    unwrap<any>(await api.put(`/performance-indicators/${id}`, body)),

  deletePerformanceIndicator: async (id: number) => {
    await api.delete(`/performance-indicators/${id}`);
  },

  getScheduleEntries: async (programId: number) =>
    unwrap<ScheduleEntryPayload[]>(await api.get('/schedule-entry', { params: { programId } })),

  createScheduleEntry: async (entry: ScheduleEntryPayload) =>
    unwrap<ScheduleEntryPayload>(await api.post('/schedule-entry', entry)),

  deleteScheduleEntry: async (id: number) => {
    await api.delete(`/schedule-entry/${id}`);
  },

  createCourse: async (courseCode: string) =>
    unwrap<any>(await api.post('/courses', {
      courseCode,
      courseName: courseCode,
      courseDescription: `Added from assessment schedule (${courseCode})`,
      studentCount: 0,
      isActive: true,
    })),
};
