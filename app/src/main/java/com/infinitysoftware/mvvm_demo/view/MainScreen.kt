package com.infinitysoftware.mvvm_demo.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.infinitysoftware.mvvm_demo.ui.theme.BeautifulGreen
import com.infinitysoftware.mvvm_demo.ui.theme.BeautifulTomatoRed
import com.infinitysoftware.mvvm_demo.viewmodel.myViewModel

// Custom Made Composable Functions Section.
// Main Screen Representation Composable Function.
@Composable
fun MainScreen(viewModel: myViewModel, defaultFontSize: TextUnit = 24.sp) {

    // Converts LiveData To Compose State To Observe Data Changes.
    val myData = viewModel.myRepository.observeAsState()

    // Observes LiveData From ViewModel And Converts It To Compose State.
    val isLoading = viewModel.isLoading.observeAsState()

    // Creates A Centered Column With A Button To Trigger Data Fetch.
    Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Button(onClick = { viewModel.getMyData() }) {
            Text(text = "Get Data")
        }

        if (isLoading.value == true) {
            CircularProgressIndicator(modifier = Modifier.padding(30.dp), color = BeautifulTomatoRed)

        } else {

            // Displays Username Text If It Is Not Null.
            myData.value?.userName?.let {
                Row {
                    Text(text = "Username: ", fontSize = defaultFontSize, color = BeautifulTomatoRed)
                    Text(text = it, fontSize = defaultFontSize, color = BeautifulGreen)
                }
            }

            // Displays First Name Text If It Is Not Null.
            myData.value?.firstName?.let {
                Row {
                    Text(text = "First Name: ", fontSize = defaultFontSize, color = BeautifulTomatoRed)
                    Text(text = it, fontSize = defaultFontSize, color = BeautifulGreen)
                }
            }

            // Displays Last Name Text If It Is Not Null.
            myData.value?.lastName?.let {
                Row {
                    Text(text = "Last Name: ", fontSize = defaultFontSize, color = BeautifulTomatoRed)
                    Text(text = it, fontSize = defaultFontSize, color = BeautifulGreen)
                }
            }

            // Displays Birth Year Text If It Is Not Null.
            myData.value?.birthYear?.let {
                Row {
                    Text(text = "Birth Year: ", fontSize = defaultFontSize, color = BeautifulTomatoRed)
                    Text(text = "$it", fontSize = defaultFontSize, color = BeautifulGreen)
                }
            }
        }
    }
}