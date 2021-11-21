package com.axiearena.energycalculator.data.models

data class Session(
    val lastUpdated: Long,
    val isPcMode: Boolean = false,
    val isBasicMode: Boolean = false,
    val energyCalcData: EnergyCalcData,
    val cardCounterData: CardCounterData?,
    val arenaTrackerData: ArenaTrackerData?,
    val slpCalculatorData: SlpCalculatorData
)