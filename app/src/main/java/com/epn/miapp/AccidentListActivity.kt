package com.epn.miapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.epn.miapp.ui.theme.MiappTheme

class AccidentListActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MiappTheme {
                AccidentListScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccidentListScreen() {
    val accidents = AccidentRepository.getAccidents()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Listado de Accidentes") })
        }
    ) {
        LazyColumn(modifier = Modifier.padding(it).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            items(accidents) { accident ->
                AccidentItem(accident = accident)
            }
        }
    }
}

@Composable
fun AccidentItem(accident: Accident) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Tipo: ${accident.accidentType}")
            Text("Fecha: ${accident.accidentDate}")
            Text("Matrícula: ${accident.licensePlate}")
            Text("Conductor: ${accident.driverName}")
            Text("Cédula: ${accident.driverId}")
            Text("Observaciones: ${accident.observations}")
            accident.location?.let {
                Text("Ubicación: ${it.latitude}, ${it.longitude}")
            }
        }
    }
}