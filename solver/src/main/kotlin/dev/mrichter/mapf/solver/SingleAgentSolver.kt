package dev.mrichter.mapf.solver

import java.util.*

class NotSolvable(message: String) : Exception(message)
interface SingleAgentSolver<CoordinatesType> {
    fun solve(
        agentId: UUID,
        initialTimestep: Int,
        start: CoordinatesType,
        target: CoordinatesType
    ): Result<Iterable<CoordinatesType>>
}

internal fun <CoordinatesType> reconstructPath(
    pathMap: Map<CoordinatesType, CoordinatesType>,
    node: CoordinatesType
): List<CoordinatesType> {
    var path = listOf(node)
    var current = node;
    while (pathMap.containsKey(current)) {
        current = pathMap[current]!!
        path = listOf(current) + path
    }
    return path
}