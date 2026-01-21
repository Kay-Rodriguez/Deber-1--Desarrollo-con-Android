package com.epn.miapp

import android.location.Location
import android.net.Uri

data class Accident(
    val accidentType: String,
    val accidentDate: String,
    val licensePlate: String,
    val driverName: String,
    val driverId: String,
    val observations: String,
    val photos: List<Uri>,
    val location: Location?
)
