package com.epn.miapp

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.location.Location
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.widget.DatePicker
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.epn.miapp.ui.theme.MiappTheme
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.location.*
import java.util.*

class AccidentRegistryActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MiappTheme {
                AccidentRegistryScreen()
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AccidentRegistryScreen() {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // Form fields states
    var accidentType by remember { mutableStateOf("") }
    var accidentDate by remember { mutableStateOf("Seleccionar fecha") }
    var licensePlate by remember { mutableStateOf("") }
    var driverName by remember { mutableStateOf("") }
    var driverId by remember { mutableStateOf("") }
    var observations by remember { mutableStateOf("") }
    var photos by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var location by remember { mutableStateOf<Location?>(null) }

    // Dropdown state
    val accidentTypes = listOf("Choque", "Colisión", "Atropello")
    var expanded by remember { mutableStateOf(false) }

    // Date picker
    val calendar = Calendar.getInstance()
    val datePickerDialog = DatePickerDialog(
        context,
        { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
            accidentDate = "$dayOfMonth/${month + 1}/$year"
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                photos = photos + uri
            }
        }
    }
    
    // Location state and permissions
    val locationPermissions = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    
    @SuppressLint("MissingPermission")
    fun requestLocation() {
        if (locationPermissions.allPermissionsGranted) {
            val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000L).build()
            fusedLocationClient.requestLocationUpdates(locationRequest, object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    locationResult.lastLocation?.let {
                        location = it
                        fusedLocationClient.removeLocationUpdates(this)
                    }
                }
            }, Looper.getMainLooper())
        } else {
            locationPermissions.launchMultiplePermissionRequest()
        }
    }


    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Registro de Accidente") })
        },
        modifier = Modifier.fillMaxSize()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // Accident Type
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                TextField(
                    value = accidentType,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Tipo de accidente") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    accidentTypes.forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type) },
                            onClick = {
                                accidentType = type
                                expanded = false
                            }
                        )
                    }
                }
            }
            
            // Accident Date
            OutlinedButton(onClick = { datePickerDialog.show() }, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.DateRange, contentDescription = "Date icon", modifier = Modifier.padding(end = 8.dp))
                Text(accidentDate)
            }

            // TextFields
            OutlinedTextField(value = licensePlate, onValueChange = { licensePlate = it }, label = { Text("Matrícula del auto") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = driverName, onValueChange = { driverName = it }, label = { Text("Nombre conductor") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = driverId, onValueChange = { driverId = it }, label = { Text("Cédula conductor") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = observations, onValueChange = { observations = it }, label = { Text("Observaciones") }, modifier = Modifier.fillMaxWidth(), maxLines = 4)

            // Photos
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text("Fotografías", modifier = Modifier.weight(1f))
                IconButton(onClick = { 
                    val intent = Intent(context, CameraActivity::class.java)
                    cameraLauncher.launch(intent)
                }) {
                    Icon(Icons.Default.CameraAlt, contentDescription = "Take Photo")
                }
            }
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(photos) { photoUri ->
                    Image(
                        painter = rememberAsyncImagePainter(photoUri),
                        contentDescription = "Captured photo",
                        modifier = Modifier.size(100.dp),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            // Location
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                 location?.let {
                    Text("Ubicación: ${it.latitude}, ${it.longitude}", modifier = Modifier.weight(1f))
                } ?: Text("Ubicación: No obtenida", modifier = Modifier.weight(1f))
                IconButton(onClick = { requestLocation() }) {
                    Icon(Icons.Default.MyLocation, contentDescription = "Get Location")
                }
            }


            // Save Button
            Button(
                onClick = {
                    val newAccident = Accident(
                        accidentType = accidentType,
                        accidentDate = accidentDate,
                        licensePlate = licensePlate,
                        driverName = driverName,
                        driverId = driverId,
                        observations = observations,
                        photos = photos,
                        location = location
                    )
                    AccidentRepository.addAccident(newAccident)

                    // Vibrate for 5 seconds
                    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator.vibrate(VibrationEffect.createOneShot(5000, VibrationEffect.DEFAULT_AMPLITUDE))
                    } else {
                        @Suppress("DEPRECATION")
                        vibrator.vibrate(5000)
                    }
                    Toast.makeText(context, "Registro de accidente guardado", Toast.LENGTH_LONG).show()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar")
            }
        }
    }
}
