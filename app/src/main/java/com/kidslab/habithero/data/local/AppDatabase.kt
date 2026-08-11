package com.kidslab.habithero.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.kidslab.habithero.data.local.dao.BadgeDao
import com.kidslab.habithero.data.local.dao.DesafioDiarioDao
import com.kidslab.habithero.data.local.dao.HabitCompletionDao
import com.kidslab.habithero.data.local.dao.HabitDao
import com.kidslab.habithero.data.local.dao.UserProfileDao
import com.kidslab.habithero.data.local.dao.UserUnlockDao
import com.kidslab.habithero.data.local.entity.Badge
import com.kidslab.habithero.data.local.entity.DesafioDiario
import com.kidslab.habithero.data.local.entity.Habit
import com.kidslab.habithero.data.local.entity.HabitCompletion
import com.kidslab.habithero.data.local.entity.UserBadge
import com.kidslab.habithero.data.local.entity.UserProfile
import com.kidslab.habithero.data.local.entity.UserUnlock

@Database(
    entities = [
        UserProfile::class,
        Habit::class,
        HabitCompletion::class,
        Badge::class,
        UserBadge::class,
        UserUnlock::class,
        DesafioDiario::class
    ],
    version = 2,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userProfileDao(): UserProfileDao
    abstract fun habitDao(): HabitDao
    abstract fun habitCompletionDao(): HabitCompletionDao
    abstract fun badgeDao(): BadgeDao
    abstract fun userUnlockDao(): UserUnlockDao
    abstract fun desafioDiarioDao(): DesafioDiarioDao

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
                .addMigrations(MIGRACION_1_2)
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
