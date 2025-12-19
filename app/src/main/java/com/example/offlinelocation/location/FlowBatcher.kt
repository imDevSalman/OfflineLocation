package com.example.offlinelocation.location

import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.ticker
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

fun <T> Flow<T>.batch(windowMs: Long): Flow<List<T>> = channelFlow {
    val buffer = mutableListOf<T>()
    val ticker = ticker(delayMillis = windowMs, initialDelayMillis = windowMs)

    launch {
        collect { value ->
            buffer += value
        }
    }

    launch {
        for (tick in ticker) {
            if (buffer.isNotEmpty()) {
                send(buffer.toList())
                buffer.clear()
            }
        }
    }

    awaitClose {
        ticker.cancel()
    }
}
