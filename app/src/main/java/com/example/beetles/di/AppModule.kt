package com.example.beetles.di

import com.example.beetles.data.BeetleSkinStore
import com.example.beetles.data.CurrentPlayerStore
import com.example.beetles.data.GameSettingsStore
import com.example.beetles.data.GoldRateRepository
import com.example.beetles.data.GoldRateStore
import com.example.beetles.db.BeetlesDatabase
import com.example.beetles.ui.GameViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single { GameSettingsStore(androidContext()) }
    single { CurrentPlayerStore(androidContext()) }
    single { GoldRateStore(androidContext()) }
    single { GoldRateRepository(get()) }
    single { BeetleSkinStore(androidContext()) }
    single { BeetlesDatabase.get(androidContext()) }

    viewModel {
        GameViewModel(
            settingsStore = get(),
            currentPlayerStore = get(),
            goldRateStore = get(),
            goldRateRepository = get(),
            beetleSkinStore = get(),
            database = get()
        )
    }
}
