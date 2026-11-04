import { httpClient } from './httpClient';
import type {
  ScheduleEntryPayload,
  CourseEntity,
  SemesterEntity,
  PerformanceIndicator
} from '@/types/schedule';

export const scheduleApi = {
  // Lookup data endpoints
  getCourses: () => httpClient.get<CourseEntity[]>('/courses'),
  getSemesters: () => httpClient.get<SemesterEntity[]>('/semesters'),
  getPerformanceIndicators: () => httpClient.get<PerformanceIndicator[]>('/performance-indicators'),

  // Schedule persistence
  getScheduleEntries: (programId: number) =>
    httpClient.get<ScheduleEntryPayload[]>(`/schedule-entries?programId=${programId}`),

  saveScheduleEntries: (entries: ScheduleEntryPayload[]) =>
    httpClient.post<void>('/schedule-entries/batch', entries),
};
