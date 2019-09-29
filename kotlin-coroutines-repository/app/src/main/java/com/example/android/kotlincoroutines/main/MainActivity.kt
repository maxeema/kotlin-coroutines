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

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.observe
import com.example.android.kotlincoroutines.R
import com.example.android.kotlincoroutines.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Layout binding
        val binding = DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main)

        // Get MainViewModel by passing a database to the factory
        val repository = TitleRepository(MainNetworkImpl, getDatabase(this).titleDao)
        val model by viewModels<MainViewModel>(factoryProducer = { MainViewModel.FACTORY(repository) })

        // Tie view with model
        binding.model = model
        binding.lifecycleOwner = this

        model.snackEvent.observe(this) { text ->
            text ?: return@observe
            Snackbar.make(binding.root, text, Snackbar.LENGTH_SHORT).show()
            model.onSnackEvent()
        }
    }

}
