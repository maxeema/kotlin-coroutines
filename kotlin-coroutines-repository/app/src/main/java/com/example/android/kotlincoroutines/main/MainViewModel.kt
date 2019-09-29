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

import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android.kotlincoroutines.util.singleArgViewModelFactory
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class MainViewModel(private val repository: TitleRepository) : ViewModel() {

    companion object {
        val FACTORY = singleArgViewModelFactory(::MainViewModel)
    }

    val title = repository.title
    val spinner = MutableLiveData<Boolean>() as LiveData<Boolean>

    val snackEvent = MutableLiveData<String?>() as LiveData<String?>
    fun onSnackEvent() { snackEvent.asMutable().value = null }

    fun onMainViewClicked(v: View?) {
        launchDataLoad {
            repository.refreshTitle()
        }
    }

    private fun launchDataLoad(block: suspend () -> Unit) = viewModelScope.launch {
        spinner.asMutable().value = true
        try {
            block()
        } catch (error: Throwable) {
            snackEvent.asMutable().value = error.message
        } finally {
            spinner.asMutable().value = false
        }
    }

    private fun <T> LiveData<T>.asMutable() = this as MutableLiveData<T>

}
