package com.epn.miapp

object AccidentRepository {
    private val accidents = mutableListOf<Accident>()

    fun addAccident(accident: Accident) {
        accidents.add(accident)
    }

    fun getAccidents(): List<Accident> = accidents
}
