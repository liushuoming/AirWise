package au.edu.jcu.assessment.airwise

import android.app.Application
import au.edu.jcu.assessment.airwise.di.AppContainer
import au.edu.jcu.assessment.airwise.di.DefaultAppContainer

class AirWiseApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
    }
}
