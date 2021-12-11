package com.axiearena.energycalculator.data.models

data class EnergyCalcData(
    var energy: Int = 3,
    var round: Int = 1,
    var history: ArrayList<EnergyHistory> = ArrayList()
)