package com.example.plantme_grupo8.data

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore

val Context.plantsDataStore by preferencesDataStore(name = "plants")

