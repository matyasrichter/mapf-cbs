package dev.mrichter.mapf.graph

import java.util.*

data class Agent<CoordinatesType>(
    val id: UUID,
    val start: CoordinatesType,
    val target: CoordinatesType,
)