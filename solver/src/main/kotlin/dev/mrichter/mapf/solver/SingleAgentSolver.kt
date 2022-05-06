package dev.mrichter.mapf.solver

import dev.mrichter.mapf.graph.Agent
import java.util.*

class NotSolvable(message: String) : Exception(message)

typealias VertexConstraint<CoordinatesType> = Triple<CoordinatesType, Int, UUID>
typealias EdgeConstraint<CoordinatesType> = Triple<Pair<CoordinatesType, CoordinatesType>, Int, UUID>

interface SingleAgentSolver<CoordinatesType> {
    fun solve(
        agent: Agent<CoordinatesType>,
        initialTimestep: Int,
        vertexConstraints: Set<VertexConstraint<CoordinatesType>>,
        edgeConstraints: Set<EdgeConstraint<CoordinatesType>>,
    ): Result<List<CoordinatesType>>
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