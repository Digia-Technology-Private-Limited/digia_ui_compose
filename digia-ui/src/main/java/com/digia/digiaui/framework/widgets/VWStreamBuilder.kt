package com.digia.digiaui.framework.widgets

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow


sealed class AsyncSnapshot<out T> {
    data class Data<T>(val value: T) : AsyncSnapshot<T>()
    data class Error(val throwable: Throwable) : AsyncSnapshot<Nothing>()
    object Loading : AsyncSnapshot<Nothing>()
}



@Composable
fun <T> InternalStreamBuilder(
    flow: Flow<T>,
    initialData: T? = null,
    onSuccess: ((T) -> Unit)? = null,
    onError: ((Throwable) -> Unit)? = null,
    builder: @Composable (AsyncSnapshot<T>) -> Unit
) {
    var snapshot by remember {
        mutableStateOf<AsyncSnapshot<T>>(
            initialData?.let { AsyncSnapshot.Data(it) }
                ?: AsyncSnapshot.Loading
        )
    }

    LaunchedEffect(flow) {
        try {
            flow.collect { value ->
                snapshot = AsyncSnapshot.Data(value)
                onSuccess?.invoke(value)
            }
        } catch (t: Throwable) {
            snapshot = AsyncSnapshot.Error(t)
            onError?.invoke(t)
        }
    }

    builder(snapshot)
}


class VWStreamBuilder {
}