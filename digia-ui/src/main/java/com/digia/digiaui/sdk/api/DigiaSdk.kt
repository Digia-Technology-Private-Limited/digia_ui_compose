package com.digia.digiaui.sdk.api

import com.digia.digiaui.sdk.internal.DefaultDigiaHost

/** SDK handle returned after successful initialization. */
interface DigiaSdk {
    val host: DigiaHost
    val appState: DigiaAppState
}
