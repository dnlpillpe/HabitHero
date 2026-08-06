package com.kidslab.habithero.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.kidslab.habithero.data.local.dao.BadgeDao
import com.kidslab.habithero.data.local.dao.HabitCompletionDao
import com.kidslab.habithero.data.local.dao.HabitDao
import com.kidslab.habithero.data.local.dao.UserProfileDao
import com.kidslab.habithero.data.local.entity.Badge
import com.kidslab.habithero.data.local.entity.Habit
import com.kidslab.habithero.data.local.entity.HabitCompletion
import com.kidslab.habithero.data.local.entity.UserBadge
import com.kidslab.habithero.data.local.entity.UserProfile

@Database(
    entities = [
        UserProfile::class,
        Habit::class,
        HabitCompletion::class,
        Badge::class,
        UserBadge::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userProfileDao(): UserProfileDao
    abstract fun habitDao(): HabitDao
    abstract fun habitCompletionDao(): HabitCompletionDao
    abstract fun badgeDao(): BadgeDao

    companion object {
        const val NOMBRE = "habithero.db"

        @Volatile
        private var instancia: AppDatabase? = null

        fun obtener(context: Context): AppDatabase =
            instancia ?: synchronized(this) {
                instancia ?: construir(context.applicationContext).also { instancia = it }
            }

        private fun construir(context: Context): AppDatabase =
            Room.databaseBuilder(context, AppDatabase::class.java, NOMBRE)
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        DatabaseSeeder.sembrar(db)
                    }
                })
                // Version 1: aún no hay migraciones. Si en el futuro cambia el
                // esquema sin migración escrita, se recrea la base en lugar de
                // dejar la app inutilizable.
                .fallbackToDestructiveMigration()
                .build()

        /** Solo para el borrado total desde Configuración. */
        fun cerrarYOlvidar() {
            synchronized(this) {
                instancia?.close()
                instancia = null
            }
        }
    }
}
