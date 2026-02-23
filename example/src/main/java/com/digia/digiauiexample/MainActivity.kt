package com.digia.digiauiexample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material.Text
import com.digia.digiaui.framework.DUIFactory
import com.digia.digiaui.init.DigiaUIOptions
import com.digia.digiaui.init.Flavor
//import com.digia.digiaui.sdk.CreateComponent
import com.digia.digiaui.sdk.DigiaSDK
import kotlinx.coroutines.ExperimentalCoroutinesApi

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DigiaSDK.initialize(
                options =
                        DigiaUIOptions(
                                context = this@MainActivity.applicationContext,
                                flavor = Flavor.Debug(),
                                accessKey = "69786962fe19ceddd06eade6",
                        )
        )
        enableEdgeToEdge()

            val success = DigiaSDK.waitForInitialization(timeoutMs = 5000)

            if (success) {
//                val data = DigiaSDK.getInstance().appState.get("sd")
                setContent {             DUIFactory.getInstance().CreateNavHost(null)
                }
            } else {
                setContent { Text("Initialization Timeout") }
            }
//            DigiaSDK.ins().CreatePage(startPageId = null,
//                null, null, null, null, null, null
//                )

    }
}
