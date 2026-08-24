package com.matelab.islas.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.matelab.islas.data.local.dao.CatalogDao
import com.matelab.islas.data.local.dao.PlayerDao
import com.matelab.islas.data.local.dao.ProgressDao
import com.matelab.islas.data.local.dao.RewardDao
import com.matelab.islas.data.local.entity.AttemptEntity
import com.matelab.islas.data.local.entity.BadgeEntity
import com.matelab.islas.data.local.entity.BadgeUnlockEntity
import com.matelab.islas.data.local.entity.ChallengeEntity
import com.matelab.islas.data.local.entity.CollectibleEntity
import com.matelab.islas.data.local.entity.CollectibleUnlockEntity
import com.matelab.islas.data.local.entity.MetaEntity
import com.matelab.islas.data.local.entity.MissionEntity
import com.matelab.islas.data.local.entity.MissionProgressEntity
import com.matelab.islas.data.local.entity.ProfileEntity
import com.matelab.islas.data.local.entity.ReviewItemEntity
import com.matelab.islas.data.local.entity.SettingsEntity
import com.matelab.islas.data.local.entity.WorldEntity
import com.matelab.islas.data.local.seed.DatabaseSeeder

@Database(
    entities = [
        WorldEntity::class,
        MissionEntity::class,
        ChallengeEntity::class,
        BadgeEntity::class,
        CollectibleEntity::class,
        ProfileEntity::class,
        SettingsEntity::class,
        MissionProgressEntity::class,
        AttemptEntity::class,
        BadgeUnlockEntity::class,
        CollectibleUnlockEntity::class,
        ReviewItemEntity::class,
        MetaEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class MateLabDatabase : RoomDatabase() {

    abstract fun catalogDao(): CatalogDao
    abstract fun playerDao(): PlayerDao
    abstract fun progressDao(): ProgressDao
    abstract fun rewardDao(): RewardDao

    companion object {
        const val NAME = "matelab.db"

        @Volatile
        private var instance: MateLabDatabase? = null

        fun get(context: Context): MateLabDatabase =
            instance ?: synchronized(this) {
                instance ?: build(context.applicationContext).also { instance = it }
            }

        private fun build(context: Context): MateLabDatabase =
            Room.databaseBuilder(context, MateLabDatabase::class.java, NAME)
                .addCallback(SeedCallback)
                // El contenido se puede regenerar, asi que una migracion fallida
                // nunca debe dejar la app inservible en el movil de un nino.
                .fallbackToDestructiveMigration()
                .build()

        private object SeedCallback : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                DatabaseSeeder.seedCatalog(db)
                DatabaseSeeder.seedPlayerDefaults(db)
            }

            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                if (DatabaseSeeder.needsCatalogSeed(db)) {
                    DatabaseSeeder.seedCatalog(db)
                }
                DatabaseSeeder.seedPlayerDefaults(db)
            }
        }
    }
}
