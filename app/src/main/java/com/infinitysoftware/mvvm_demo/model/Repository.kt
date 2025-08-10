package com.infinitysoftware.mvvm_demo.model

import kotlinx.coroutines.delay

class Repository {

    // Has To Be 'suspend fun' For Proper Working Of: 'delay(5000)'
    suspend fun fetchData() : Data{

        // Mocking API
        delay(5000)

        // Returning Hardcoded Value.
        return Data("Dusan", "Rosic", "@dusanrsc", 1997)
    }
}