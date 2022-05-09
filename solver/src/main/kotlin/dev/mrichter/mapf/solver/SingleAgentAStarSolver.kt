package dev.mrichter.mapf.solver

import dev.mrichter.mapf.graph.Agent
import dev.mrichter.mapf.graph.Graph
import java.util.*

data class State<CT>(
    val coordinates: CT,
    val timestep: Int,
    // path length from start
    val gScore: Int,
    // total path estimate
    val fScore: Int
)

class SingleAgentAStarSolver<CT>(
    val graph: Graph<CT, *>,
    val distanceMetric: (CT, CT) -> Int,
) : SingleAgentSolver<CT> {
    override fun solve(
        agent: Agent<CT>,
        initialTimestep: Int,
        vertexConstraints: Set<VertexConstraint<CT>>,
        edgeConstraints: Set<EdgeConstraint<CT>>,
    ): Result<List<CT>> {
        val pathMap = HashMap<State<CT>, State<CT>>()
        val closed = hashSetOf<State<CT>>()
        val open = hashSetOf<State<CT>>()
        val queue = PriorityQueue(compareBy<State<CT>> { it.fScore }.thenBy { it.timestep })
        queue.add(State(agent.start, initialTimestep, 0, distanceMetric(agent.start, agent.target)))
        while (!queue.isEmpty()) {
            val curr = queue.remove()
            if (curr.coordinates == agent.target)
                return Result.success(reconstructPath(pathMap, curr))
            closed.add(curr)
            open.remove(curr)
            graph.neighbours(curr.coordinates).filter {
                !vertexConstraints.contains(Pair(it, curr.timestep + 1))
                        && !edgeConstraints.contains(Pair(Pair(curr.coordinates, it), curr.timestep))
            }.forEach {
                val next = State(
                    it,
                    curr.timestep + 1,
                    curr.gScore + 1,
                    curr.gScore + 1 + distanceMetric(it, agent.target)
                )
                if (next.timestep < graph.size() && !closed.contains(next) && !open.contains(next)) {
                    pathMap[next] = curr
                    queue.add(next)
                    open.add(next)
                }
            }
        }
        return Result.failure(NotSolvable(""))
    }

    private fun <CT> reconstructPath(
        pathMap: Map<State<CT>, State<CT>>,
        target: State<CT>,
    ): List<CT> {
        var path = listOf(target.coordinates)
        var current = target
        while (pathMap.containsKey(current)) {
            current = pathMap[current]!!
            path = listOf(current.coordinates) + path
        }
        return path
    }
}