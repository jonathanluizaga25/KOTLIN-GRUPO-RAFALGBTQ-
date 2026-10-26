package org.ucb.appp1

import android.app.Application
import com.google.firebase.FirebaseApp
import org.ucb.appp1.di.initKoinAndroid
import org.ucb.appp1.scheduler.LogScheduler
class MainApplication: Application(){
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        PushService.createChannel(this)
        initKoinAndroid(this)
        IntegratorSync.schedule(this)

        val logScheduler = LogScheduler(this)
        logScheduler.schedulePeriodicalUpload()
    }
}
