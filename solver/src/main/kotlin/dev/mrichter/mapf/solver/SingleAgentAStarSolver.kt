package dev.mrichter.mapf.solver

import dev.mrichter.mapf.graph.Agent
import dev.mrichter.mapf.graph.Graph
import java.util.*

class SingleAgentAStarSolver<CoordinatesType>(
    val graph: Graph<CoordinatesType, *>,
    val distanceMetric: (CoordinatesType, CoordinatesType) -> Int,
) : SingleAgentSolver<CoordinatesType> {
    override fun solve(
        agent: Agent<CoordinatesType>,
        initialTimestep: Int,
        vertexConstraints: Set<VertexConstraint<CoordinatesType>>,
        edgeConstraints: Set<EdgeConstraint<CoordinatesType>>,
    ): Result<List<CoordinatesType>> {
        val pathMap = HashMap<Pair<CoordinatesType, Int>, CoordinatesType>()
        // shortest path currently known from start to node
        val gScore = mutableMapOf(Pair(agent.start, 0)).withDefault { Int.MAX_VALUE }
        // current best estimate through a node
        val fScore =
            mutableMapOf(Pair(agent.start, distanceMetric(agent.start, agent.target))).withDefault { Int.MAX_VALUE }
        val queue =
            PriorityQueue { l: CoordinatesType, r: CoordinatesType ->
                if (fScore.getValue(l) > fScore.getValue(r)) 1 else -1
            }
        queue.add(agent.start)
        val queueSteps = mutableMapOf(Pair(agent.start, initialTimestep))
        while (!queue.isEmpty()) {
            val curr = queue.remove()
            val currStep = queueSteps.remove(curr)!!
            if (curr == agent.target)
                return Result.success(reconstructPath(pathMap, Pair(agent.target, currStep)))
            graph.neighbours(curr).filter {
                !vertexConstraints.contains(Triple(it, currStep + 1, agent.id))
                        && !edgeConstraints.contains(Triple(Pair(curr, it), currStep, agent.id))
            }.forEach {
                val possibleScore = gScore.getValue(curr) + distanceMetric(curr, it)
                if (possibleScore <= gScore.getValue(it)) {
                    queue.remove(it)
                    pathMap[Pair(it, currStep + 1)] = curr
                    gScore[it] = possibleScore
                    fScore[it] = possibleScore + distanceMetric(it, agent.target)
                    queue.add(it)
                    queueSteps[it] = currStep + 1
                }
            }
        }
        return Result.failure(NotSolvable(""))
    }
}