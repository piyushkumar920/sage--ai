package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        TopicEntity::class,
        MessageEntity::class,
        RoadmapProgressEntity::class,
        TopicProgressEntity::class,
        QuizResultEntity::class,
        DailyQuizRecordEntity::class,
        WeakConceptEntity::class,
        com.example.data.studytools.SavedStudyToolEntity::class,
        FocusSessionEntity::class,
        com.example.data.mission.DailyMissionEntity::class,
        com.example.data.profile.AcademicProfileEntity::class,
        PersonalRoadmapEntity::class,
        com.example.data.documents.UploadedDocumentEntity::class,
        com.example.data.documents.DocumentExtractionEntity::class,
        com.example.data.documents.DocumentGroundedRoadmapEntity::class,
        com.example.data.attachments.StudyAttachmentEntity::class
    ],
    version = 10,
    exportSchema = false
)
abstract class SageDatabase : RoomDatabase() {
    abstract fun sageDao(): SageDao

    companion object {
        @Volatile
        private var INSTANCE: SageDatabase? = null

        val MIGRATION_8_9 = object : androidx.room.migration.Migration(8, 9) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `uploaded_documents` (
                        `documentId` TEXT NOT NULL PRIMARY KEY,
                        `userId` TEXT NOT NULL,
                        `fileName` TEXT NOT NULL,
                        `mimeType` TEXT NOT NULL,
                        `fileSizeBytes` INTEGER NOT NULL,
                        `fileSignature` TEXT NOT NULL,
                        `uploadedAt` INTEGER NOT NULL,
                        `validationStatus` TEXT NOT NULL,
                        `syncStatus` TEXT NOT NULL
                    )
                """.trimIndent())

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `document_extractions` (
                        `extractionId` TEXT NOT NULL PRIMARY KEY,
                        `documentId` TEXT NOT NULL,
                        `confidence` TEXT NOT NULL,
                        `structuredCurriculumJson` TEXT NOT NULL,
                        `extractedAt` INTEGER NOT NULL,
                        `verificationStatus` TEXT NOT NULL
                    )
                """.trimIndent())

                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `document_grounded_roadmaps` (
                        `roadmapId` TEXT NOT NULL PRIMARY KEY,
                        `documentId` TEXT NOT NULL,
                        `extractionId` TEXT NOT NULL,
                        `status` TEXT NOT NULL,
                        `groundedConfidence` TEXT NOT NULL,
                        `createdAt` INTEGER NOT NULL,
                        `updatedAt` INTEGER NOT NULL
                    )
                """.trimIndent())
            }
        }

        val MIGRATION_9_10 = object : androidx.room.migration.Migration(9, 10) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `study_attachments` (
                        `attachmentId` TEXT NOT NULL PRIMARY KEY,
                        `ownerUid` TEXT NOT NULL,
                        `documentId` TEXT NOT NULL,
                        `contextType` TEXT NOT NULL,
                        `contextId` TEXT NOT NULL,
                        `originalFileName` TEXT NOT NULL,
                        `mimeType` TEXT NOT NULL,
                        `fileSize` INTEGER NOT NULL,
                        `uploadStatus` TEXT NOT NULL,
                        `processingStatus` TEXT NOT NULL,
                        `extractionId` TEXT,
                        `extractedSummary` TEXT,
                        `localUri` TEXT,
                        `isDeleted` INTEGER NOT NULL DEFAULT 0,
                        `syncStatus` TEXT NOT NULL,
                        `createdAt` INTEGER NOT NULL,
                        `updatedAt` INTEGER NOT NULL
                    )
                """.trimIndent())

                db.execSQL("CREATE INDEX IF NOT EXISTS `index_study_attachments_contextType_contextId_isDeleted` ON `study_attachments` (`contextType`, `contextId`, `isDeleted`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_study_attachments_documentId` ON `study_attachments` (`documentId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_study_attachments_ownerUid` ON `study_attachments` (`ownerUid`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_study_attachments_ownerUid_contextType_contextId` ON `study_attachments` (`ownerUid`, `contextType`, `contextId`)")
            }
        }

        fun getInstance(context: Context): SageDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SageDatabase::class.java,
                    "sage_ai_learning.db"
                )
                    .addMigrations(MIGRATION_8_9, MIGRATION_9_10)
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
