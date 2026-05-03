package com.abetappteam.abetapp.service;

import com.abetappteam.abetapp.entity.Course;
import com.abetappteam.abetapp.entity.Program;
import com.abetappteam.abetapp.entity.ProgramUser;
import com.abetappteam.abetapp.dto.ProgramDTO;
import com.abetappteam.abetapp.BaseServiceTest;
import com.abetappteam.abetapp.exception.ConflictException;
import com.abetappteam.abetapp.exception.ResourceNotFoundException;
import com.abetappteam.abetapp.repository.CourseRepository;
import com.abetappteam.abetapp.repository.ProgramRepository;
import com.abetappteam.abetapp.repository.ProgramUserRepository;
import com.abetappteam.abetapp.util.TestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class ProgramServiceTest extends BaseServiceTest {

    @Mock
    private ProgramRepository programRepository;
    @Mock
    private ProgramUserRepository programUserRepository;
    @Mock
    private CourseRepository courseRepository;

    @InjectMocks
    private ProgramService programService;

    private Program testProgram;
    private ProgramDTO testDTO;
    private ProgramUser adminProgramUser;
    private ProgramUser instructorProgramUser;

    private static final Long USER_ID = 10L;
    private static final Long PROGRAM_ID = 1L;

    @BeforeEach
    void setUp() {
        testProgram = TestDataBuilder.createProgramWithId(PROGRAM_ID, "EU Testing", "Example University", true);
        testDTO = TestDataBuilder.createProgramDTO();

        adminProgramUser = TestDataBuilder.createProgramUserWithId(100L, true, PROGRAM_ID, USER_ID, true);
        instructorProgramUser = TestDataBuilder.createProgramUserWithId(101L, false, PROGRAM_ID, USER_ID, true);
    }

    // ─── findById / findAll (inherited from BaseService) ─────────────────────────

    @Test
    void findById_returnsProgram() {
        when(programRepository.findById(PROGRAM_ID)).thenReturn(Optional.of(testProgram));

        Program found = programService.findById(PROGRAM_ID);

        assertThat(found).isNotNull();
        assertThat(found.getId()).isEqualTo(PROGRAM_ID);
        assertThat(found.getName()).isEqualTo("EU Testing");
        assertThat(found.getInstitution()).isEqualTo("Example University");
        assertThat(found.getActive()).isTrue();
        verify(programRepository).findById(PROGRAM_ID);
    }

    @Test
    void findById_throwsWhenNotFound() {
        when(programRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> programService.findById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Program not found with id: 999");
    }

    @Test
    void findAll_returnsAllPrograms() {
        List<Program> programs = TestDataBuilder.createProgramList(3);
        when(programRepository.findAll()).thenReturn(programs);

        List<Program> found = programService.findAll();

        assertThat(found).hasSize(3);
        verify(programRepository).findAll();
    }

    @Test
    void findAll_paginatedReturnsPage() {
        Pageable pageable = PageRequest.of(0, 10);
        List<Program> programs = TestDataBuilder.createProgramList(3);
        Page<Program> page = new PageImpl<>(programs, pageable, 3);
        when(programRepository.findAll(pageable)).thenReturn(page);

        Page<Program> found = programService.findAll(pageable);

        assertThat(found.getContent()).hasSize(3);
        assertThat(found.getTotalElements()).isEqualTo(3);
        verify(programRepository).findAll(pageable);
    }

    // ─── create ───────────────────────────────────────────────────────────────────

    @Test
    void create_savesAndReturnsProgram() {
        when(programRepository.save(any(Program.class))).thenReturn(testProgram);

        Program created = programService.create(testDTO);

        assertThat(created).isNotNull();
        verify(programRepository).save(any(Program.class));
    }

    @Test
    void create_defaultsActiveToTrueWhenDtoActiveIsNull() {
        ProgramDTO dtoWithNullActive = new ProgramDTO("New Program", "New Institution", null);
        Program expected = TestDataBuilder.createProgramWithId(2L, "New Program", "New Institution", true);
        when(programRepository.save(any(Program.class))).thenReturn(expected);

        Program created = programService.create(dtoWithNullActive);

        assertThat(created.getActive()).isTrue();
    }

    // ─── update ───────────────────────────────────────────────────────────────────

    @Test
    void update_modifiesAndSavesProgram() {
        when(programRepository.findById(PROGRAM_ID)).thenReturn(Optional.of(testProgram));
        when(programRepository.save(any(Program.class))).thenReturn(testProgram);

        Program updated = programService.update(PROGRAM_ID, testDTO);

        assertThat(updated).isNotNull();
        verify(programRepository).findById(PROGRAM_ID);
        verify(programRepository).save(any(Program.class));
    }

    @Test
    void update_throwsWhenProgramNotFound() {
        when(programRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> programService.update(999L, testDTO))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void update_doesNotChangeActiveWhenDtoActiveIsNull() {
        ProgramDTO dtoWithNullActive = new ProgramDTO("Updated", "Updated Inst", null);
        testProgram.setActive(true);
        when(programRepository.findById(PROGRAM_ID)).thenReturn(Optional.of(testProgram));
        when(programRepository.save(any(Program.class))).thenReturn(testProgram);

        programService.update(PROGRAM_ID, dtoWithNullActive);

        assertThat(testProgram.getActive()).isTrue();
    }

    // ─── delete ───────────────────────────────────────────────────────────────────

    @Test
    void delete_removesProgram() {
        when(programRepository.findById(PROGRAM_ID)).thenReturn(Optional.of(testProgram));
        doNothing().when(programRepository).delete(testProgram);

        programService.delete(PROGRAM_ID);

        verify(programRepository).findById(PROGRAM_ID);
        verify(programRepository).delete(testProgram);
    }

    // ─── activate / deactivate ────────────────────────────────────────────────────

    @Test
    void activate_setsActiveTrueAndSaves() {
        testProgram.setActive(false);
        when(programRepository.findById(PROGRAM_ID)).thenReturn(Optional.of(testProgram));
        when(programRepository.save(any(Program.class))).thenReturn(testProgram);

        Program activated = programService.activate(PROGRAM_ID);

        assertThat(activated.getActive()).isTrue();
        verify(programRepository).save(testProgram);
    }

    @Test
    void deactivate_setsActiveFalseAndSaves() {
        when(programRepository.findById(PROGRAM_ID)).thenReturn(Optional.of(testProgram));
        when(programRepository.save(any(Program.class))).thenReturn(testProgram);

        Program deactivated = programService.deactivate(PROGRAM_ID);

        assertThat(deactivated.getActive()).isFalse();
        verify(programRepository).save(testProgram);
    }

    // ─── findAllActive / findAllInactive ─────────────────────────────────────────

    @Test
    void findAllActive_returnsOnlyActivePrograms() {
        List<Program> activePrograms = List.of(
                TestDataBuilder.createProgramWithId(1L, "Active 1", "Example University", true),
                TestDataBuilder.createProgramWithId(2L, "Active 2", "Example University", true));
        when(programRepository.findByActiveTrue()).thenReturn(activePrograms);

        List<Program> found = programService.findAllActive();

        assertThat(found).hasSize(2);
        assertThat(found).allMatch(Program::getActive);
    }

    @Test
    void findAllInactive_returnsOnlyInactivePrograms() {
        List<Program> inactivePrograms = List.of(
                TestDataBuilder.createProgramWithId(3L, "Inactive 1", "Example University", false));
        when(programRepository.findByActiveFalse()).thenReturn(inactivePrograms);

        List<Program> found = programService.findAllInactive();

        assertThat(found).hasSize(1);
        assertThat(found).noneMatch(Program::getActive);
    }

    // ─── searchActiveByNameFragment ───────────────────────────────────────────────

    @Test
    void searchActiveByNameFragment_returnsMatchingPrograms() {
        when(programRepository.findActiveProgramsByNameContaining("EU"))
                .thenReturn(List.of(testProgram));

        List<Program> results = programService.searchActiveByNameFragment("EU");

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getName()).isEqualTo("EU Testing");
        verify(programRepository).findActiveProgramsByNameContaining("EU");
    }

    @Test
    void searchActiveByNameFragment_returnsEmptyListWhenNoMatches() {
        when(programRepository.findActiveProgramsByNameContaining("XYZ")).thenReturn(List.of());

        List<Program> results = programService.searchActiveByNameFragment("XYZ");

        assertThat(results).isEmpty();
    }

    // ─── getActiveProgramsForUser / getAllProgramsForUser ─────────────────────────

    @Test
    void getActiveProgramsForUser_returnsActiveMemberships() {
        when(programUserRepository.findByUserIdAndIsActive(USER_ID, true))
                .thenReturn(List.of(adminProgramUser));

        List<ProgramUser> results = programService.getActiveProgramsForUser(USER_ID);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getUserId()).isEqualTo(USER_ID);
    }

    @Test
    void getAllProgramsForUser_returnsAllMembershipsIncludingInactive() {
        ProgramUser inactive = TestDataBuilder.createProgramUserWithId(102L, false, PROGRAM_ID, USER_ID, false);
        when(programUserRepository.findByUserId(USER_ID))
                .thenReturn(List.of(adminProgramUser, inactive));

        List<ProgramUser> results = programService.getAllProgramsForUser(USER_ID);

        assertThat(results).hasSize(2);
    }

    // ─── isAdminInProgram / hasAccessToProgram ────────────────────────────────────

    @Test
    void isAdminInProgram_returnsTrueWhenUserIsAdmin() {
        when(programUserRepository.existsByProgramIdAndUserIdAndIsAdmin(PROGRAM_ID, USER_ID, true))
                .thenReturn(true);

        assertThat(programService.isAdminInProgram(USER_ID, PROGRAM_ID)).isTrue();
    }

    @Test
    void isAdminInProgram_returnsFalseWhenUserIsNotAdmin() {
        when(programUserRepository.existsByProgramIdAndUserIdAndIsAdmin(PROGRAM_ID, USER_ID, true))
                .thenReturn(false);

        assertThat(programService.isAdminInProgram(USER_ID, PROGRAM_ID)).isFalse();
    }

    @Test
    void hasAccessToProgram_returnsTrueWhenUserIsMember() {
        when(programUserRepository.existsByProgramIdAndUserIdAndIsActive(PROGRAM_ID, USER_ID, true))
                .thenReturn(true);

        assertThat(programService.hasAccessToProgram(USER_ID, PROGRAM_ID)).isTrue();
    }

    @Test
    void hasAccessToProgram_returnsFalseWhenUserIsNotMember() {
        when(programUserRepository.existsByProgramIdAndUserIdAndIsActive(PROGRAM_ID, USER_ID, true))
                .thenReturn(false);

        assertThat(programService.hasAccessToProgram(USER_ID, PROGRAM_ID)).isFalse();
    }

    // ─── getRoleInProgram ─────────────────────────────────────────────────────────

    @Test
    void getRoleInProgram_returnsADMINWhenUserIsAdmin() {
        when(programUserRepository.findByProgramIdAndUserIdAndIsActive(PROGRAM_ID, USER_ID, true))
                .thenReturn(Optional.of(adminProgramUser));

        assertThat(programService.getRoleInProgram(USER_ID, PROGRAM_ID)).isEqualTo("ADMIN");
    }

    @Test
    void getRoleInProgram_returnsINSTRUCTORWhenUserIsNotAdmin() {
        when(programUserRepository.findByProgramIdAndUserIdAndIsActive(PROGRAM_ID, USER_ID, true))
                .thenReturn(Optional.of(instructorProgramUser));

        assertThat(programService.getRoleInProgram(USER_ID, PROGRAM_ID)).isEqualTo("INSTRUCTOR");
    }

    @Test
    void getRoleInProgram_returnsNullWhenUserNotInProgram() {
        when(programUserRepository.findByProgramIdAndUserIdAndIsActive(PROGRAM_ID, USER_ID, true))
                .thenReturn(Optional.empty());

        assertThat(programService.getRoleInProgram(USER_ID, PROGRAM_ID)).isNull();
    }

    // ─── getProgramUser ───────────────────────────────────────────────────────────

    @Test
    void getProgramUser_returnsProgramUserWhenFound() {
        when(programUserRepository.findByProgramIdAndUserIdAndIsActive(PROGRAM_ID, USER_ID, true))
                .thenReturn(Optional.of(adminProgramUser));

        ProgramUser result = programService.getProgramUser(USER_ID, PROGRAM_ID);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(100L);
    }

    @Test
    void getProgramUser_throwsWhenUserNotInProgram() {
        when(programUserRepository.findByProgramIdAndUserIdAndIsActive(PROGRAM_ID, USER_ID, true))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> programService.getProgramUser(USER_ID, PROGRAM_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User is not associated with this program");
    }

    // ─── getDefaultProgramForUser ─────────────────────────────────────────────────

    @Test
    void getDefaultProgramForUser_prefersAdminProgramOverInstructor() {
        when(programUserRepository.findByUserIdAndIsActive(USER_ID, true))
                .thenReturn(List.of(instructorProgramUser, adminProgramUser));

        ProgramUser result = programService.getDefaultProgramForUser(USER_ID);

        assertThat(result.getAdminStatus()).isTrue();
    }

    @Test
    void getDefaultProgramForUser_returnsFirstProgramWhenNoAdminRole() {
        when(programUserRepository.findByUserIdAndIsActive(USER_ID, true))
                .thenReturn(List.of(instructorProgramUser));

        ProgramUser result = programService.getDefaultProgramForUser(USER_ID);

        assertThat(result).isEqualTo(instructorProgramUser);
    }

    @Test
    void getDefaultProgramForUser_throwsWhenUserHasNoPrograms() {
        when(programUserRepository.findByUserIdAndIsActive(USER_ID, true)).thenReturn(List.of());

        assertThatThrownBy(() -> programService.getDefaultProgramForUser(USER_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("not associated with any programs");
    }

    // ─── getUsersInProgram / getAdminsInProgram / getInstructorsInProgram ─────────

    @Test
    void getUsersInProgram_returnsActiveMembersOfProgram() {
        when(programUserRepository.findByProgramIdAndIsActive(PROGRAM_ID, true))
                .thenReturn(List.of(adminProgramUser, instructorProgramUser));

        List<ProgramUser> results = programService.getUsersInProgram(PROGRAM_ID);

        assertThat(results).hasSize(2);
    }

    @Test
    void getAdminsInProgram_returnsOnlyAdmins() {
        when(programUserRepository.findByProgramIdAndIsAdminAndIsActive(PROGRAM_ID, true, true))
                .thenReturn(List.of(adminProgramUser));

        List<ProgramUser> results = programService.getAdminsInProgram(PROGRAM_ID);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getAdminStatus()).isTrue();
    }

    @Test
    void getInstructorsInProgram_returnsOnlyNonAdmins() {
        when(programUserRepository.findByProgramIdAndIsAdminAndIsActive(PROGRAM_ID, false, true))
                .thenReturn(List.of(instructorProgramUser));

        List<ProgramUser> results = programService.getInstructorsInProgram(PROGRAM_ID);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getAdminStatus()).isFalse();
    }

    // ─── getCoursesInProgram ──────────────────────────────────────────────────────

    @Test
    void getCoursesInProgram_returnsActiveCoursesForProgram() {
        List<Course> courses = TestDataBuilder.createCourseList(3);
        when(courseRepository.findActiveCoursesByProgramId(PROGRAM_ID)).thenReturn(courses);

        List<Course> results = programService.getCoursesInProgram(PROGRAM_ID);

        assertThat(results).hasSize(3);
        verify(courseRepository).findActiveCoursesByProgramId(PROGRAM_ID);
    }

    // ─── addUserToProgram ─────────────────────────────────────────────────────────

    @Test
    void addUserToProgram_savesAndReturnsProgramUser() {
        when(programUserRepository.existsByProgramIdAndUserId(PROGRAM_ID, USER_ID)).thenReturn(false);
        when(programRepository.findById(PROGRAM_ID)).thenReturn(Optional.of(testProgram));
        when(programUserRepository.save(any(ProgramUser.class))).thenReturn(adminProgramUser);

        ProgramUser result = programService.addUserToProgram(USER_ID, PROGRAM_ID, true);

        assertThat(result).isNotNull();
        verify(programUserRepository).save(any(ProgramUser.class));
    }

    @Test
    void addUserToProgram_throwsConflictWhenUserAlreadyInProgram() {
        when(programUserRepository.existsByProgramIdAndUserId(PROGRAM_ID, USER_ID)).thenReturn(true);

        assertThatThrownBy(() -> programService.addUserToProgram(USER_ID, PROGRAM_ID, true))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("already associated");

        verify(programUserRepository, never()).save(any());
    }

    @Test
    void addUserToProgram_throwsWhenProgramNotFound() {
        when(programUserRepository.existsByProgramIdAndUserId(PROGRAM_ID, USER_ID)).thenReturn(false);
        when(programRepository.findById(PROGRAM_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> programService.addUserToProgram(USER_ID, PROGRAM_ID, true))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(programUserRepository, never()).save(any());
    }

    // ─── updateUserRole ───────────────────────────────────────────────────────────

    @Test
    void updateUserRole_updatesAdminStatusAndSaves() {
        when(programUserRepository.findByProgramIdAndUserIdAndIsActive(PROGRAM_ID, USER_ID, true))
                .thenReturn(Optional.of(instructorProgramUser));
        when(programUserRepository.save(instructorProgramUser)).thenReturn(instructorProgramUser);

        ProgramUser result = programService.updateUserRole(USER_ID, PROGRAM_ID, true);

        assertThat(instructorProgramUser.getAdminStatus()).isTrue();
        verify(programUserRepository).save(instructorProgramUser);
    }

    @Test
    void updateUserRole_throwsWhenUserNotInProgram() {
        when(programUserRepository.findByProgramIdAndUserIdAndIsActive(PROGRAM_ID, USER_ID, true))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> programService.updateUserRole(USER_ID, PROGRAM_ID, true))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ─── removeUserFromProgram ────────────────────────────────────────────────────

    @Test
    void removeUserFromProgram_setsInactiveAndSaves() {
        when(programUserRepository.findByProgramIdAndUserIdAndIsActive(PROGRAM_ID, USER_ID, true))
                .thenReturn(Optional.of(adminProgramUser));
        when(programUserRepository.save(adminProgramUser)).thenReturn(adminProgramUser);

        programService.removeUserFromProgram(USER_ID, PROGRAM_ID);

        assertThat(adminProgramUser.getIsActive()).isFalse();
        verify(programUserRepository).save(adminProgramUser);
    }

    @Test
    void removeUserFromProgram_throwsWhenUserNotInProgram() {
        when(programUserRepository.findByProgramIdAndUserIdAndIsActive(PROGRAM_ID, USER_ID, true))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> programService.removeUserFromProgram(USER_ID, PROGRAM_ID))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ─── deleteUserFromProgram ────────────────────────────────────────────────────

    @Test
    void deleteUserFromProgram_callsRepositoryDelete() {
        doNothing().when(programUserRepository).deleteByProgramIdAndUserId(PROGRAM_ID, USER_ID);

        programService.deleteUserFromProgram(USER_ID, PROGRAM_ID);

        verify(programUserRepository).deleteByProgramIdAndUserId(PROGRAM_ID, USER_ID);
    }

    // ─── findProgramUserById / saveProgramUser ────────────────────────────────────

    @Test
    void findProgramUserById_returnsProgramUserWhenFound() {
        when(programUserRepository.findById(100L)).thenReturn(Optional.of(adminProgramUser));

        ProgramUser result = programService.findProgramUserById(100L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(100L);
    }

    @Test
    void findProgramUserById_throwsWhenNotFound() {
        when(programUserRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> programService.findProgramUserById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("ProgramUser not found with ID: 999");
    }

    @Test
    void saveProgramUser_delegatesToRepository() {
        when(programUserRepository.save(adminProgramUser)).thenReturn(adminProgramUser);

        ProgramUser result = programService.saveProgramUser(adminProgramUser);

        assertThat(result).isEqualTo(adminProgramUser);
        verify(programUserRepository).save(adminProgramUser);
    }
}
