package com.digia.digiaui.sdk.internal

import com.digia.digiaui.sdk.api.DigiaAppState
import com.digia.digiaui.sdk.api.DigiaHost
import com.digia.digiaui.sdk.api.DigiaSdk

data class DefaultDigiaSdk(
         val host: DefaultDigiaHost,
         val appState: DefaultDigiaAppState,
)
