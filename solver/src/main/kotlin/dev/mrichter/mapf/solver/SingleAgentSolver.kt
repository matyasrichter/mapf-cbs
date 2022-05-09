package dev.mrichter.mapf.solver

import dev.mrichter.mapf.graph.Agent
import java.util.*

class NotSolvable(message: String) : Exception(message)

typealias VertexConstraint<CoordinatesType> = Pair<CoordinatesType, Int>
typealias EdgeConstraint<CoordinatesType> = Pair<Pair<CoordinatesType, CoordinatesType>, Int>

interface SingleAgentSolver<CoordinatesType> {
    fun solve(
        agent: Agent<CoordinatesType>,
        initialTimestep: Int,
        vertexConstraints: Set<VertexConstraint<CoordinatesType>>,
        edgeConstraints: Set<EdgeConstraint<CoordinatesType>>,
    ): Result<List<CoordinatesType>>
}
