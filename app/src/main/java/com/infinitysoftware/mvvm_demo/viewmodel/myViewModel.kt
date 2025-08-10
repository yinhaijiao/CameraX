package com.infinitysoftware.mvvm_demo.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.infinitysoftware.mvvm_demo.model.Data
import com.infinitysoftware.mvvm_demo.model.Repository
import kotlinx.coroutines.launch

// ViewModel Class That Holds And Manages UI Data.
class myViewModel : ViewModel() {

    // Repository Instance That Handles Data Fetching.
    val Repository : Repository = Repository()

    // Encapsulated LiveData Pattern Providing Read-Only Access To UI.
    private val _myRepository = MutableLiveData<Data>()
    val myRepository : LiveData<Data> = _myRepository

    // Encapsulated LiveData Pattern For Tracking Loading State.
    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading : LiveData<Boolean> = _isLoading

    // Fetches Data From Repository And Updates LiveData States.
    fun getMyData() {
        _isLoading.postValue(true)
        viewModelScope.launch {
            val myResult = Repository.fetchData()
            _myRepository.postValue(myResult)
            _isLoading.postValue(false)
        }
    }
}