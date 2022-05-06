package dev.mrichter.mapf.solver

import dev.mrichter.mapf.graph.Agent
import java.util.*

class ConstraintTreeNode<CoordinatesType>(
    val vertexConstraints: Set<VertexConstraint<CoordinatesType>>,
    val edgeConstraints: Set<EdgeConstraint<CoordinatesType>>,
    val solution: List<List<CoordinatesType>>,
    val cost: Int
)

data class Conflict<CoordinatesType>(
    val aIndex: Int,
    val bIndex: Int,
    val timestep: Int,
    val aCoordinates: CoordinatesType,
    val bCoordinates: CoordinatesType,
)

fun <E> Iterable<E>.updated(index: Int, elem: E) = mapIndexed { i, existing -> if (i == index) elem else existing }


class CTSolver<CoordinatesType>(
    val singleAgentSolver: SingleAgentSolver<CoordinatesType>,
) {
    fun solve(agents: List<Agent<CoordinatesType>>): Result<ConstraintTreeNode<CoordinatesType>> {
        val open = PriorityQueue<ConstraintTreeNode<CoordinatesType>> { l, r -> l.cost.compareTo(r.cost) }
        val root = createRootNode(agents)
        root.onSuccess { open.add(it) }
        while (!open.isEmpty()) {
            val curr = open.remove()
            var final = true
            val paths = sequence {
                curr.solution.forEachIndexed { indexA, a ->
                    curr.solution.withIndex().drop(indexA + 1).forEach { (indexB, b) ->
                        yield(Pair(Pair(indexA, a), Pair(indexB, b)))
                    }
                }
            }
            // first, look for vertex conflicts
            val vertex = paths.map { (aPair, bPair) ->
                val (aIndex, a) = aPair
                val (bIndex, b) = bPair
                sequence {
                    // check for vertex conflicts first
                    a.zip(b).withIndex().forEach {
                        val (coordA, coordB) = it.value
                        if (coordA == coordB) {
                            yield(Pair(Triple(coordA, it.index, agents[aIndex].id), aIndex))
                            yield(Pair(Triple(coordB, it.index, agents[bIndex].id), bIndex))
                        }
                    }
                }
            }.flatten().iterator()
            if (vertex.hasNext()) {
                // if a vertex conflict exists, branch into two leaves
                val (constraint, index) = vertex.next()
                createNode(
                    agents,
                    index,
                    curr.vertexConstraints + constraint,
                    curr.edgeConstraints,
                    curr.solution
                ).onSuccess {
                    open.add(it)
                    final = false
                }
            }
            // now check for edge conflicts
            val edge = paths.map { (aPair, bPair) ->
                val (aIndex, a) = aPair
                val (bIndex, b) = bPair
                sequence {
                    // zip two paths with the same paths offset by 1
                    a.zip(b).withIndex().zip(a.zip(b).drop(1)).forEach { (it_n0, it_n1) ->
                        // timestep n
                        val (a0, b0) = it_n0.value
                        // timestep n+1
                        val (a1, b1) = it_n1
                        if (a0 == b1 && b0 == a1) {
                            yield(Pair(Triple(Pair(a0, a1), it_n0.index, agents[aIndex].id), aIndex))
                            yield(Pair(Triple(Pair(b0, b1), it_n0.index, agents[bIndex].id), bIndex))
                        }
                    }
                }
            }.flatten().iterator()
            if (edge.hasNext()) {
                // if an edge conflict exists, branch into two leaves
                val (constraint, index) = edge.next()
                createNode(
                    agents,
                    index,
                    curr.vertexConstraints,
                    curr.edgeConstraints + constraint,
                    curr.solution
                ).onSuccess {
                    open.add(it)
                    final = false
                }
            }
            if (final) return Result.success(curr)
        }
        return Result.failure(NotSolvable("Instance is not solvable"))
    }

    private fun createNode(
        agents: List<Agent<CoordinatesType>>,
        index: Int,
        vertexConstraints: Set<VertexConstraint<CoordinatesType>>,
        edgeConstraints: Set<EdgeConstraint<CoordinatesType>>,
        previousSolution: List<List<CoordinatesType>>
    ): Result<ConstraintTreeNode<CoordinatesType>> {
        return singleAgentSolver.solve(agents[index], 0, vertexConstraints, edgeConstraints).map {
            val newSolutionSet = previousSolution.updated(index, it)
            ConstraintTreeNode(
                vertexConstraints,
                edgeConstraints,
                newSolutionSet,
                newSolutionSet.fold(0) { prev, path -> prev + path.size }
            )
        }
    }

    private fun createRootNode(agents: List<Agent<CoordinatesType>>): Result<ConstraintTreeNode<CoordinatesType>> {
        val solution: List<List<CoordinatesType>>
        try {
            val raw = agents.map {
                singleAgentSolver.solve(it, 0, setOf(), setOf()).getOrThrow()
            }
            val solutionLength = raw.maxOf { it.size }
            solution = raw.map {
                it + generateSequence { it.last() }.take(solutionLength - it.size)
            }
        } catch (e: NotSolvable) {
            return Result.failure(NotSolvable("Not solvable"))
        }
        return Result.success(
            ConstraintTreeNode(setOf(), setOf(), solution, solution.fold(0) { prev, path -> prev + path.size })
        )
    }
}