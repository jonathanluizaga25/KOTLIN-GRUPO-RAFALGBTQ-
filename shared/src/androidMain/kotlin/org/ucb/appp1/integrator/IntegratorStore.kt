package org.ucb.appp1.integrator

import android.content.Context
import androidx.room3.Room

object IntegratorStore {
    @Volatile private var instance: IntegratorDatabase? = null

    fun database(context: Context): IntegratorDatabase = instance ?: synchronized(this) {
        instance ?: Room.databaseBuilder<IntegratorDatabase>(
            context.applicationContext,
            context.applicationContext.getDatabasePath("integrator.db").absolutePath
        ).build().also { instance = it }
    }
}
