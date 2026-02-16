package com.digia.digiaui.sdk

import com.digia.digiaui.framework.DUIFactory
import com.digia.digiaui.framework.appstate.DUIAppState
import com.digia.digiaui.init.DigiaUI
import com.digia.digiaui.init.DigiaUIManager
import com.digia.digiaui.init.DigiaUIOptions
import com.digia.digiaui.sdk.api.DigiaHost
import com.digia.digiaui.sdk.internal.DefaultDigiaAppState
import com.digia.digiaui.sdk.internal.DefaultDigiaHost
import com.digia.digiaui.sdk.internal.DefaultDigiaSdk
import java.util.concurrent.atomic.AtomicReference
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout

/**
 * Public initializer entrypoint for host apps.
 *
 * Returns a small [DigiaSdk] handle (host + appState) to keep the public surface minimal.
 */
object DigiaSDK {


        // Always-available handle (safe to access before initialization finishes).
        private val sdkInstance =(DefaultDigiaSdk(host = DefaultDigiaHost(), appState = DefaultDigiaAppState()))

        private val initScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

        @Volatile private var initDeferred: CompletableDeferred<Unit> = CompletableDeferred()
        @Volatile private var internalJob: Job? = null
        @Volatile private var lastInitError: Throwable? = null

        // Tracks async initialization completion (success or failure).
        internal val initJob: Deferred<Unit>
                get() = initDeferred

        fun ins(): DefaultDigiaHost = getInstance().host

        @JvmStatic
        fun initialize(options: DigiaUIOptions) {
                synchronized(this) {
                        if (isInitialized()) return

                        // If initialization is already running, don't start it again.
                        if (internalJob?.isActive == true) return

                        // Allow retry if a previous attempt failed.
                        if (lastInitError != null) {
                                initDeferred = CompletableDeferred()
                                lastInitError = null
                        }

                        val appContext = options.context.applicationContext

                        internalJob = initScope.launch {
                                try {
                                        // Fully initialize DigiaUI (suspending).
                                        val digiaUI =
                                                DigiaUI.initialize(
                                                        options.copy(context = appContext)
                                                )

                                        // Align with DigiaUIApp init sequence.
                                        DigiaUIManager.initialize(digiaUI)
                                        DigiaUIManager.getInstance().bottomSheetManager =
                                                com.digia.digiaui.framework.bottomsheet
                                                        .BottomSheetManager()
                                        DigiaUIManager.getInstance().dialogManager =
                                                com.digia.digiaui.framework.dialog.DialogManager()

                                        DUIAppState.instance.init(
                                                digiaUI.dslConfig.appState ?: emptyList()
                                        )

                                        com.digia.digiaui.framework.DUIFactory.getInstance()
                                                .initialize()


                                        // Signal that the SDK is "Fully Ready".
                                        initDeferred.complete(Unit)
                                } catch (t: Throwable) {
                                        lastInitError = t
                                        initDeferred.completeExceptionally(t)
                                }
                        }
                }
        }

        @JvmStatic
        suspend fun ensureInitialized() {
                if (!isInitialized()) {
                      initJob.await()
                }
        }


        @JvmStatic
        fun waitForInitialization(timeoutMs: Long = 0): Boolean {
                return runBlocking {
                        try {
                                if (timeoutMs > 0) {
                                        withTimeout(timeoutMs) { initDeferred.await(); true }
                                } else {
                                        initDeferred.await(); true
                                }
                        } catch (e: Exception) {
                                false // Timeout or Error
                        }
                }
        }

        @JvmStatic
        fun getInstance(): DefaultDigiaSdk = sdkInstance

        @JvmStatic
        fun isInitialized(): Boolean = initDeferred.isCompleted && lastInitError == null

        @JvmStatic fun lastErrorOrNull(): Throwable? = lastInitError
}
