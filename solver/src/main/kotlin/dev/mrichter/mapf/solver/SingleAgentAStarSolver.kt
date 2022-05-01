package dev.mrichter.mapf.solver

import dev.mrichter.mapf.graph.Graph
import java.util.*

class SingleAgentAStarSolver<CoordinatesType>(
    val graph: Graph<CoordinatesType, *>,
    val constraints: ConstraintTable<CoordinatesType>,
    val distanceMetric: (CoordinatesType, CoordinatesType) -> Double,
) : SingleAgentSolver<CoordinatesType> {
    override fun solve(
        agentId: UUID,
        initialTimestep: Int,
        start: CoordinatesType,
        target: CoordinatesType
    ): Result<List<CoordinatesType>> {
        val pathMap = HashMap<CoordinatesType, CoordinatesType>()
        // shortest path currently known from start to node
        val gScore = mutableMapOf(Pair(start, 0.0)).withDefault { Double.POSITIVE_INFINITY }
        // current best estimate through a node
        val fScore = mutableMapOf(Pair(start, distanceMetric(start, target))).withDefault { Double.POSITIVE_INFINITY }
        val queue =
            PriorityQueue { l: CoordinatesType, r: CoordinatesType ->
                if (fScore.getValue(l) > fScore.getValue(r)) 1 else -1
            }
        queue.add(start)
        val queueSteps = mutableMapOf(Pair(start, initialTimestep))
        while (!queue.isEmpty()) {
            val curr = queue.remove()
            if (curr == target)
                return Result.success(reconstructPath(pathMap, target))
            val currStep = queueSteps.remove(curr)!!
            graph.neighbours(curr).filter {
                constraints.canMoveTo(it, currStep + 1, agentId) && constraints.canMoveTo(it, currStep, agentId)
            }.forEach {
                val possibleScore = gScore.getValue(curr) + distanceMetric(curr, it)
                if (possibleScore <= gScore.getValue(it)) {
                    queue.remove(it)
                    pathMap[it] = curr
                    gScore[it] = possibleScore
                    fScore[it] = possibleScore + distanceMetric(it, target)
                    queue.add(it)
                    queueSteps[it] = currStep + 1
                }
            }
        }
        return Result.failure(NotSolvable(""))
    }
}