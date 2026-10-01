package com.example.data.profile

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.curriculum.CurriculumDepartment
import com.example.data.studytools.AcademicContext
import com.squareup.moshi.JsonClass

/**
 * Domain model representing a student's persistent Academic Profile.
 */
data class AcademicProfile(
    val departmentId: String,       // e.g. "it", "cse_aiml", "ece", "ee", "me", "ce", "bme", "cst", "bba", "mca"
    val departmentName: String,     // e.g. "Information Technology"
    val programmeId: String = departmentId,
    val programmeName: String,     // e.g. "B.Tech. in Information Technology"
    val regulationId: String = "r25",
    val regulation: String = "R25",
    val semester: Int,             // 1..8
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun toEntity(): AcademicProfileEntity = AcademicProfileEntity(
        id = PRIMARY_PROFILE_ID,
        departmentId = departmentId,
        departmentName = departmentName,
        programmeId = programmeId,
        programmeName = programmeName,
        regulationId = regulationId,
        regulation = regulation,
        semester = semester,
        updatedAt = updatedAt
    )

    fun toAcademicContext(courseCode: String = "", courseName: String = "", module: String = "", topic: String = "", syllabus: String = ""): AcademicContext =
        AcademicContext(
            department = departmentName,
            programme = programmeName,
            regulation = regulation,
            semester = semester,
            courseCode = courseCode,
            courseName = courseName,
            module = module,
            topic = topic,
            officialSyllabusContent = syllabus
        )

    companion object {
        const val PRIMARY_PROFILE_ID = "primary"

        fun fromDepartment(dept: CurriculumDepartment, semester: Int): AcademicProfile =
            AcademicProfile(
                departmentId = dept.id,
                departmentName = dept.name,
                programmeId = dept.id,
                programmeName = dept.programme,
                regulationId = dept.regulation.lowercase(),
                regulation = dept.regulation,
                semester = semester,
                updatedAt = System.currentTimeMillis()
            )
    }
}

/**
 * Room Database Entity for local persistent storage of the student's Academic Profile.
 */
@Entity(tableName = "academic_profile")
data class AcademicProfileEntity(
    @PrimaryKey
    val id: String = AcademicProfile.PRIMARY_PROFILE_ID,
    val departmentId: String,
    val departmentName: String,
    val programmeId: String,
    val programmeName: String,
    val regulationId: String,
    val regulation: String,
    val semester: Int,
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun toDomain(): AcademicProfile = AcademicProfile(
        departmentId = departmentId,
        departmentName = departmentName,
        programmeId = programmeId,
        programmeName = programmeName,
        regulationId = regulationId,
        regulation = regulation,
        semester = semester,
        updatedAt = updatedAt
    )
}
