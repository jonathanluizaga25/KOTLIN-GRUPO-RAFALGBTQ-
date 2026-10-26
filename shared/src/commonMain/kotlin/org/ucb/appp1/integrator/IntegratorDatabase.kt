package org.ucb.appp1.integrator

import androidx.room3.ConstructedBy
import androidx.room3.Dao
import androidx.room3.Database
import androidx.room3.Entity
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.PrimaryKey
import androidx.room3.Query
import androidx.room3.RoomDatabase
import androidx.room3.RoomDatabaseConstructor
import androidx.room3.Transaction
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "remote_config")
data class CachedConfig(
    @PrimaryKey val id: Int = 1,
    val welcomeMessage: String,
    val notesEnabled: Boolean,
    val maintenanceMode: Boolean,
    val syncedAt: Long
)

@Entity(tableName = "config_history")
data class ConfigHistory(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val description: String,
    val changedAt: Long
)

@Dao
interface IntegratorDao {
    @Query("SELECT * FROM remote_config WHERE id = 1")
    fun observeConfig(): Flow<CachedConfig?>

    @Query("SELECT * FROM remote_config WHERE id = 1")
    suspend fun getConfig(): CachedConfig?

    @Query("SELECT * FROM config_history ORDER BY id DESC LIMIT 50")
    fun observeHistory(): Flow<List<ConfigHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun putConfig(config: CachedConfig)

    @Insert
    suspend fun addHistory(history: ConfigHistory)

    @Transaction
    suspend fun saveAndRecord(config: CachedConfig): Boolean {
        val previous = getConfig()
        val changed = previous != null && (
            previous.welcomeMessage != config.welcomeMessage ||
                previous.notesEnabled != config.notesEnabled ||
                previous.maintenanceMode != config.maintenanceMode
            )
        putConfig(config)
        if (changed) {
            addHistory(ConfigHistory(
                description = "Bienvenida: ${config.welcomeMessage}; notas: ${config.notesEnabled}; mantenimiento: ${config.maintenanceMode}",
                changedAt = config.syncedAt
            ))
        }
        return changed
    }
}

@Database(entities = [CachedConfig::class, ConfigHistory::class], version = 1)
@ConstructedBy(IntegratorDatabaseConstructor::class)
abstract class IntegratorDatabase : RoomDatabase() {
    abstract fun integratorDao(): IntegratorDao
}

@Suppress("KotlinNoActualForExpect")
expect object IntegratorDatabaseConstructor : RoomDatabaseConstructor<IntegratorDatabase> {
    override fun initialize(): IntegratorDatabase
}
