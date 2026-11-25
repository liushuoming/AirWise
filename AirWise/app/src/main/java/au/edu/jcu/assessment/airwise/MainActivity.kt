package au.edu.jcu.assessment.airwise

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import au.edu.jcu.assessment.airwise.ui.AirWiseApp
import au.edu.jcu.assessment.airwise.ui.theme.AirWiseTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            AirWiseTheme {
                AirWiseApp()
            }
        }
    }
}
