import api from '@/api';

export interface SectionPiProgress {
  sectionId: number;
  indicatorStatus: boolean;
}

export function isMeasureResultSubmitted(result: { status?: string } | null | undefined): boolean {
  return String(result?.status ?? '').toLowerCase() === 'complete';
}

export async function loadSectionPiProgress(
  section: { id: number; courseId?: number },
  programId: number,
  scheduleEntries: any[],
  measuresByEntryId: Map<number, any[]>
): Promise<SectionPiProgress[]> {
  const courseEntries = scheduleEntries.filter(
    (entry: any) => Number(entry.courseId) === Number(section.courseId)
  );
  const indicatorIds = [...new Set(
    courseEntries.map((entry: any) => Number(entry.indicatorId))
  )].filter((id) => Number.isFinite(id));

  if (indicatorIds.length === 0) {
    return [];
  }

  const sectionProgramRes = await api.get('/section-program', {
    params: { sectionId: section.id, programId }
  });
  const sectionProgramId = (sectionProgramRes.data?.data ?? [])[0]?.id;

  const progress: SectionPiProgress[] = [];

  for (const indicatorId of indicatorIds) {
    const entries = courseEntries.filter(
      (entry: any) => Number(entry.indicatorId) === indicatorId
    );
    let measureCount = 0;
    let submittedCount = 0;

    if (sectionProgramId != null) {
      for (const entry of entries) {
        const entryId = Number(entry.id);
        if (!measuresByEntryId.has(entryId)) {
          const measuresRes = await api.get('/measure', {
            params: { scheduleEntryId: entryId, active: true }
          });
          measuresByEntryId.set(entryId, measuresRes.data?.data ?? []);
        }

        for (const measure of measuresByEntryId.get(entryId) ?? []) {
          measureCount += 1;
          const resultsRes = await api.get('/measure-result', {
            params: { measureId: measure.id, sectionProgramId }
          });
          const results = resultsRes.data?.data ?? [];
          if (results.length > 0 && results.every(isMeasureResultSubmitted)) {
            submittedCount += 1;
          }
        }
      }
    }

    progress.push({
      sectionId: Number(section.id),
      indicatorStatus: measureCount > 0 && submittedCount === measureCount
    });
  }

  return progress;
}
