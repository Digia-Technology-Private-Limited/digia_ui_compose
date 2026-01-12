package com.digia.digiaui.framework.appstate

import com.digia.digiaui.framework.models.Variable
import com.digia.digiaui.framework.utils.JsonLike

class StateDescriptor<T>(
    val key: String,
    val initialValue: T,
    val shouldPersist: Boolean = true,
    val deserialize: (String) -> T,
    val serialize: (T) -> String,
    val description: String? = null,
    val streamName: String
)

