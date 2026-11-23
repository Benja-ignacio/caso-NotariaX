package com.duoc.notix.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        UsuarioEntity::class,
        TramiteEntity::class,
        DocumentoEntity::class,
        EventoHistorialEntity::class,
        AvisoEntity::class
    ],
    version = 3,
    exportSchema = false
)
@TypeConverters(NotixTypeConverters::class)
abstract class NotixDatabase : RoomDatabase() {

    abstract fun tramiteDao(): TramiteDao
    abstract fun usuarioDao(): UsuarioDao
    abstract fun avisoDao(): AvisoDao

    companion object {
        @Volatile
        private var INSTANCE: NotixDatabase? = null

        /**
         * Room sigue siendo la fuente de verdad mientras no exista backend.
         * La migración conserva usuarios, trámites, documentos, historial y avisos.
         */
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE tramites ADD COLUMN firmaEvidencia TEXT")
            }
        }

        fun getDatabase(context: Context): NotixDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    NotixDatabase::class.java,
                    "notix_database"
                )
                .addMigrations(MIGRATION_2_3)
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
