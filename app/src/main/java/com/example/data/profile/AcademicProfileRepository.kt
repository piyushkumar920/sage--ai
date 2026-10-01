package com.example.data.profile

import android.content.Context
import com.example.data.curriculum.CurriculumCourse
import com.example.data.curriculum.CurriculumDepartment
import com.example.data.curriculum.CurriculumRepository
import com.example.data.local.PreferencesManager
import com.example.data.local.SageDao
import com.example.data.studytools.AcademicContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * Repository responsible for the single source of truth for the student's Academic Profile.
 * Persists locally via Room & Preferences, and coordinates synchronization with Firestore.
 */
class AcademicProfileRepository(
    private val context: Context,
    private val dao: SageDao,
    private val preferencesManager: PreferencesManager,
    private val curriculumRepository: CurriculumRepository
) {
    /**
     * Observable flow of the student's current Academic Profile.
     */
    val academicProfileFlow: Flow<AcademicProfile?> = dao.getAcademicProfileFlow().map { entity ->
        entity?.toDomain() ?: preferencesManager.getAcademicProfile()
    }

    /**
     * Synchronously returns the currently cached Academic Profile from preferences.
     */
    fun getProfileSync(): AcademicProfile? {
        return preferencesManager.getAcademicProfile()
    }

    /**
     * Asynchronously loads the persistent Academic Profile from Room, falling back to Preferences.
     */
    suspend fun getProfile(): AcademicProfile? = withContext(Dispatchers.IO) {
        dao.getAcademicProfileOnce()?.toDomain() ?: preferencesManager.getAcademicProfile()
    }

    /**
     * Checks whether an established academic profile exists.
     */
    fun hasProfile(): Boolean {
        return preferencesManager.hasAcademicProfile
    }

    /**
     * Persists a new or updated Academic Profile.
     */
    suspend fun saveProfile(profile: AcademicProfile): AcademicProfile = withContext(Dispatchers.IO) {
        val updatedProfile = profile.copy(updatedAt = System.currentTimeMillis())
        preferencesManager.saveAcademicProfile(updatedProfile)
        dao.saveAcademicProfile(updatedProfile.toEntity())
        updatedProfile
    }

    /**
     * Saves an Academic Profile from a selected department and semester.
     */
    suspend fun saveProfile(department: CurriculumDepartment, semester: Int): AcademicProfile {
        val profile = AcademicProfile.fromDepartment(department, semester)
        return saveProfile(profile)
    }

    /**
     * Clears local profile (for testing or complete account reset).
     */
    suspend fun clearProfile() = withContext(Dispatchers.IO) {
        preferencesManager.clearAcademicProfile()
        dao.clearAcademicProfile()
    }

    /**
     * Retrieves the primary or introductory course for the given academic profile and semester.
     */
    fun getInitialCourseForProfile(profile: AcademicProfile): CurriculumCourse? {
        val semesterCourses = curriculumRepository.getCoursesForSemester(profile.departmentId, profile.semester)
        return semesterCourses.firstOrNull() ?: curriculumRepository.getCoursesForDepartment(profile.departmentId).firstOrNull()
    }

    /**
     * Builds the default AcademicContext for a given profile.
     */
    fun buildDefaultAcademicContext(profile: AcademicProfile): AcademicContext {
        val course = getInitialCourseForProfile(profile)
        val firstModule = course?.modules?.firstOrNull()
        return AcademicContext(
            department = profile.departmentName,
            programme = profile.programmeName,
            regulation = profile.regulation,
            semester = profile.semester,
            courseCode = course?.code ?: "",
            courseName = course?.title ?: "",
            module = firstModule?.moduleNumber ?: "",
            topic = firstModule?.title ?: "",
            officialSyllabusContent = firstModule?.content ?: ""
        )
    }
}
