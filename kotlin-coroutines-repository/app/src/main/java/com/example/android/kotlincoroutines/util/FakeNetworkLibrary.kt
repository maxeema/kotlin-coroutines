/*
 * Copyright 2018 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.kotlincoroutines.util

import android.os.Handler
import android.os.Looper
import java.util.*
import java.util.concurrent.Executors

private const val ONE_SECOND = 1_000L
private const val ERROR_RATE = 0.3

private val executor  = Executors.newCachedThreadPool()
private val uiHandler = Handler(Looper.getMainLooper())

/**
 * A completely fake network library that returns from a given list of strings or an error.
 */
fun fakeNetworkLibrary(from: List<String>): FakeNetworkCall<String> {
    assert(from.isNotEmpty()) { "You must pass at least one result string" }

    val result = FakeNetworkCall<String>()

    // Launch the "network request" in a new thread to avoid blocking the calling thread
    executor.submit {
        Thread.sleep(ONE_SECOND) // pretend we actually made a network request by sleeping

        // pretend we got a result from the passed list, or randomly an error
        if (DefaultErrorDecisionStrategy.shouldError()) {
            result.onError(FakeNetworkException("Error contacting the network"))
        } else {
            result.onSuccess(from[Random().nextInt(from.size)])
        }
    }
    return result
}

interface ErrorDecisionStrategy {
    fun shouldError(): Boolean
}

object DefaultErrorDecisionStrategy : ErrorDecisionStrategy {
    var delegate: ErrorDecisionStrategy = RandomErrorStrategy

    override fun shouldError() = delegate.shouldError()
}

object RandomErrorStrategy : ErrorDecisionStrategy {
    override fun shouldError() = Random().nextFloat() < ERROR_RATE
}

class FakeNetworkCall<T> {
    var result: FakeNetworkResult<T>? = null

    val listeners = mutableListOf<FakeNetworkListener<T>>()

    fun addOnResultListener(listener: (FakeNetworkResult<T>) -> Unit) {
        trySendResult(listener)
        listeners += listener
    }

    fun onSuccess(data: T) {
        result = FakeNetworkResult.FakeNetworkSuccess(data)
        sendResultToAllListeners()
    }

    fun onError(throwable: Throwable) {
        result = FakeNetworkResult.FakeNetworkError(throwable)
        sendResultToAllListeners()
    }

    private fun sendResultToAllListeners() = listeners.map { trySendResult(it) }

    private fun trySendResult(listener: FakeNetworkListener<T>) {
        val thisResult = result
        thisResult?.let {
            uiHandler.post {
                listener(thisResult)
            }
        }
    }
}

sealed class FakeNetworkResult<T> {

    class FakeNetworkSuccess<T>(val data: T) : FakeNetworkResult<T>()
    class FakeNetworkError<T>(val error: Throwable) : FakeNetworkResult<T>()

}

class FakeNetworkException(message: String) : Throwable(message)

typealias FakeNetworkListener<T> = (FakeNetworkResult<T>) -> Unit
