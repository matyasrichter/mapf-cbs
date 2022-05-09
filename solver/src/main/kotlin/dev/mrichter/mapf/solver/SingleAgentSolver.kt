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
