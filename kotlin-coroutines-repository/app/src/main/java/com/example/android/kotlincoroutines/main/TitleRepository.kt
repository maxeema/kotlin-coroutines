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

package com.example.android.kotlincoroutines.main

import androidx.lifecycle.map
import com.example.android.kotlincoroutines.util.FakeNetworkCall
import com.example.android.kotlincoroutines.util.FakeNetworkResult.FakeNetworkError
import com.example.android.kotlincoroutines.util.FakeNetworkResult.FakeNetworkSuccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.LazyThreadSafetyMode.NONE
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class TitleRepository(private val network: MainNetwork, private val titleDao: TitleDao) {

    val title by lazy(NONE) { titleDao.loadTitle().map { it?.title } }

    suspend fun refreshTitle() = withContext(Dispatchers.IO) {
        val result = network.fetchNewWelcome().await()
        titleDao.insertTitle(Title(result))
    }

}

suspend fun <T> FakeNetworkCall<T>.await() : T = suspendCoroutine { continuation ->
    addOnResultListener { when (it) {
        is FakeNetworkSuccess<T> -> continuation.resume(it.data)
        is FakeNetworkError -> continuation.resumeWithException(it.error)
    }}
}
