<script setup lang="ts">
import { ref, onMounted, watch } from "vue";
import { useUserStore } from "@/stores/user-store";
import { storeToRefs } from "pinia";
import api from "@/api";
import MeasureListing from "@/components/MeasureListing.vue";
import { BaseCard, BaseButton } from "@/components/ui";

// ------------------------------
// TYPES
// ------------------------------
interface ProgramUser {
  id: number;
  userId: number;
  programId: number;
  role: string;
}

interface RawSection {
  id: number;
  courseCode: string;
}

interface MeasureResults {
  id: number;
  met: number;
  exceeded: number;
  below: number;
  observation: string;
  status: string;

  measureId: number;
  measure: {
    id: number;
    measureDescription: string;
    metDescription: string;
    belowDescription: string;
    exceededDescription: string;
  }
}

interface InstructorDashboardSection {
  id: number;
  courseCode: string;
  instructorName: string;
  measuresCompleted: number;
  measuresTotal: number;
  measures: MeasureResults[];
  expanded: boolean;
}

// ------------------------------
// STORE REFERENCES
// ------------------------------
const userStore = useUserStore();
const { currentProgramId: programId, currentSemesterId: semesterId } = storeToRefs(userStore);

// ------------------------------
// STATE
// ------------------------------
const sections = ref<InstructorDashboardSection[]>([]);
const programUserId = ref<number | null>(null);
const isLoading = ref<boolean>(false);
const errorMessage = ref<string | null>(null);

// ------------------------------
// LOAD PROGRAM USER FOR CURRENT PROGRAM
// ------------------------------
async function loadProgramUserId() {
  if (!programId.value) return;

  const res = await api.get(`/program/${programId.value}/users`);
  const programUsers = res.data.data as ProgramUser[];

  const me = programUsers.find((pu) => pu.userId === userStore.userId);
  if (!me) throw new Error("User is not assigned to this program");

  programUserId.value = me.id;
}

// ------------------------------
// LOAD MEASURE RESULTS FOR A SECTION
// ------------------------------
async function loadSectionMeasureResults(sectionId: number): Promise<MeasureResults[]> {
  try {
    const res = await api.get(`/measure-result`, { params: {
      sectionId: sectionId
    }});

    console.log(res)

    return res.data.data as MeasureResults[];
  } catch (err) {
    console.error(`Failed to load measures for course`);
    return [];
  }
}

// ------------------------------
// LOAD INSTRUCTOR COURSE LIST
// ------------------------------
async function loadInstructorSections() {
  if (!programUserId.value || !semesterId.value || !userStore.userId) return;

  // First, query the section user table to find what sections are assigned to the user
  console.log("Loading sections for user id: ", userStore.userId)
  let sectionUserRes = null;
  try{
    sectionUserRes = await api.get("/section-user", { params:
        {
          userId: userStore.userId
        }
    });
  } finally {
    console.log("Section User Results: ", sectionUserRes)
  }

  // Extract section IDs from the section user results
  const sectionUserData = sectionUserRes?.data?.data ?? [];

  if(sectionUserData.length === 0) {
    console.warn("No sections found for user");
    return;
  }

  let sectionIds: any[] = [];

  sectionUserData.forEach((su: any) => {
    console.log(`SectionUser - id: ${su.id}, sectionId: ${su.sectionId}, userId: ${su.userId}`)

    sectionIds.push(su.sectionId);
  })


  const sectionRes = await api.get("/section", {
    params: {
      ids: sectionIds
    },
  });

  console.log("Section Results: ", sectionRes)

  // Parse out sections and courses from the response, assemble section titles

  const responseData = sectionRes.data.data;

  const rawSections = responseData.sections;
  const rawCourses = responseData.courses;


  const results: InstructorDashboardSection[] = [];

  rawSections.forEach((section: any) => {

    const course = rawCourses.find((c: any) => c.id === section.courseId);
    if (!course) {
      console.warn(`No course found for section ${section.id}`);
      return;
    }

    results.push({
      id: section.id,
      courseCode: `${course.courseCode} ${course.courseName} ${section.sectionNumber}`,
      instructorName: `${userStore.user?.firstName} ${userStore.user?.lastName}`,
      measuresCompleted: section.completedMeasures,
      measuresTotal: section.totalMeasures,
      measures: [],
      expanded: false,
    });
  });

  sections.value = results;
}

// ------------------------------
// TOGGLE SECTION EXPANSION
// ------------------------------
async function toggleSection(section: InstructorDashboardSection) {
  section.expanded = !section.expanded;

  // Load measures if expanding and not already loaded
  if (section.expanded && section.measures.length === 0) {
    section.measures = await loadSectionMeasureResults(section.id);
  }
}

// ------------------------------
// REFRESH MEASURE RESULTS FOR A SECTION
// ------------------------------
async function refreshSectionMeasureResults(sectionId: number) {
  const section = sections.value.find(s => s.id === sectionId);
  if (!section) return;

  section.measures = await loadSectionMeasureResults(sectionId);

}

// ------------------------------
// COMBINED LOADER
// ------------------------------
async function reload() {
  if (!programId.value) {
    errorMessage.value = "No program selected. Please select a program from the navigation menu.";
    return;
  }

  if (!semesterId.value) {
    errorMessage.value = "No semester selected. Please select a semester from the navigation menu.";
    return;
  }

  isLoading.value = true;
  errorMessage.value = null;

  try {
    await loadProgramUserId();
    await loadInstructorSections();
  } catch (err) {
    console.error(err);
    errorMessage.value = "Failed to load instructor dashboard";
  } finally {
    isLoading.value = false;
  }
}

onMounted(reload);

// Reload when program ID or semester ID or course ID changes
watch([programId, semesterId], () => {
  // Reset sections when semester changes to avoid showing stale data
  sections.value = [];
  reload();
});
</script>

<template>
  <section v-if="!isLoading" class="instructor-dashboard">
    <h1>Instructor Dashboard</h1>

    <h3>Your Assigned Sections</h3>

    <div v-if="errorMessage" class="error">
      {{ errorMessage }}
    </div>

    <div v-else-if="!programId || !semesterId ">
      <p class="info-message">
        Please select a program and semester from the navigation menu to view your sections.
      </p>
    </div>

    <div v-else-if="sections.length === 0">
      <p>No sections created for the current course.</p>
    </div>

    <div v-else class="section-grid">
      <BaseCard
        v-for="section in sections"
        :key="section.id"
        class="section-card"
        variant="elevated"
        hoverable
      >
        <div class="section-card-content">
          <div class="section-course-code">
            <h3>{{ section.courseCode }}</h3>
          </div>
        </div>
        <!-- TODO: Add here the aggregate data for how many indicators for a section have been completed-->
      </BaseCard>
    </div>
  </section>

  <section v-else class="loading-screen">
    Loading instructor dashboard...
  </section>
</template>

<style scoped>
.section-course-code {
  align-items: start;
  text-align: center;
}

.section-card-content {
  display: flex;
  flex-direction: row;
  gap: 0.5rem;
  align-items: center;
  height: 100%;
}

.section-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(330px, 1fr));
  gap: 1.25rem;
}

.section-card :deep(.base-card) {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: var(--color-bg-secondary);
}

/* Make card-body expand to fill available space */
.section-card :deep(.card-body) {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.section-card {
  cursor: pointer;
  transition: all 0.2s ease;
  height: 100%;
}

.instructor-dashboard {
  margin: 2rem;
}

.error {
  color: red;
  margin-bottom: 1rem;
  padding: 1rem;
  background-color: #fee;
  border-radius: 0.5rem;
}

.info-message {
  color: #9ca3af;
  padding: 1rem;
  background-color: rgb(36, 36, 36);
  border-radius: 0.5rem;
  text-align: center;
}

.sections-container {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.section-card {
  background-color: rgb(36, 36, 36);
  border-radius: 0.75rem;
  overflow: hidden;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0.75rem 1rem;
  cursor: pointer;
  transition: background-color 0.2s;
}

.section-header:hover {
  background-color: rgb(43, 43, 43);
}

.section-info h3 {
  margin: 0 0 0.25rem 0;
  font-size: 1.125rem;
  color: white;
}

.instructor-name {
  margin: 0;
  color: #9ca3af;
  font-size: 0.8125rem;
}

.section-stats {
  display: flex;
  text-align: center;
  align-items: center;
  gap: 0.75rem;
}

.measures-count {
  color: #9ca3af;
  font-size: 0.8125rem;
}

.expand-button {
  background: none;
  border: none;
  color: white;
  font-size: 0.875rem;
  cursor: pointer;
  transition: transform 0.2s;
  padding: 0.25rem;
}

.expand-button.expanded {
  transform: rotate(0deg);
}

.measures-container {
  padding: 0 1rem 0.75rem;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.no-measures {
  padding: 1.5rem;
  text-align: center;
  color: #9ca3af;
  font-style: italic;
}

.loading-screen {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 50vh;
  font-size: 1.25rem;
  color: #9ca3af;
}
</style>
