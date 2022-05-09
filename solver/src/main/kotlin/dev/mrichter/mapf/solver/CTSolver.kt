package dev.mrichter.mapf.solver

import dev.mrichter.mapf.graph.Agent
import java.util.*

class ConstraintTreeNode<CT>(
    val vertexConstraints: Set<VertexConstraint<CT>>,
    val edgeConstraints: Set<EdgeConstraint<CT>>,
    val solution: List<List<CT>>,
    val cost: Int
)

fun <E> Iterable<E>.updated(index: Int, elem: E) = mapIndexed { i, existing -> if (i == index) elem else existing }
fun <E> Iterable<E>.zipLongest(other: Iterable<E>) = sequence {
    val a = iterator()
    val b = other.iterator()
    while (a.hasNext() && b.hasNext()) {
        yield(Pair(a.next(), b.next()))
    }
    while (a.hasNext()) {
        yield(Pair(a.next(), other.last()))
    }
    while (b.hasNext()) {
        yield(Pair(last(), b.next()))
    }
}
//todo 4.2.3. Resolving a conflict

class CTSolver<CT>(
    val singleAgentSolver: SingleAgentSolver<CT>,
) {
    fun solve(agents: List<Agent<CT>>): Result<ConstraintTreeNode<CT>> {
        val open =
            PriorityQueue(compareBy<ConstraintTreeNode<CT>> { it.cost }.thenBy { it.vertexConstraints.size + it.edgeConstraints.size })
        val root = createRootNode(agents)
        root.onSuccess { open.add(it) }
        var totalNodes = 0
        while (!open.isEmpty()) {
            val curr = open.remove()
            totalNodes++
            var final = true
            createChildren(curr, agents).forEach { open.add(it); final = false }
            if (final) return Result.success(curr)
        }
        return Result.failure(NotSolvable("Instance is not solvable"))
    }

    private fun makePathPairs(node: ConstraintTreeNode<CT>) = sequence {
        node.solution.forEachIndexed { indexA, a ->
            node.solution.withIndex().drop(indexA + 1).forEach { (indexB, b) ->
                yield(Pair(Pair(indexA, a), Pair(indexB, b)))
            }
        }
    }

    private fun createChildren(
        node: ConstraintTreeNode<CT>, agents: List<Agent<CT>>
    ): Sequence<ConstraintTreeNode<CT>> {
        val iterators = node.solution.map { it.iterator() }.withIndex()
        val mapNextOrLast = { its: Iterable<IndexedValue<Iterator<CT>>> ->
            its.map { (index, it) -> if (it.hasNext()) it.next() else node.solution[index].last() }
        }
        var n0 = mapNextOrLast(iterators)
        var step = 0

        while (iterators.any { (_, it) -> it.hasNext() }) {
            val n1 = mapNextOrLast(iterators)
            for (indexA in n0.indices) {
                for (indexB in indexA + 1 until node.solution.size) {
                    if (n0[indexA] == n0[indexB]) {
                        return sequence {
                            if (step < node.solution[indexA].size) {
                                createNode(
                                    agents,
                                    indexA,
                                    node.vertexConstraints + Triple(n0[indexA], step, agents[indexA].id),
                                    node.edgeConstraints,
                                    node.solution
                                ).onSuccess {
                                    yield(it)
                                }
                            }
                            if (step < node.solution[indexB].size) {
                                createNode(
                                    agents,
                                    indexB,
                                    node.vertexConstraints + Triple(n0[indexB], step, agents[indexB].id),
                                    node.edgeConstraints,
                                    node.solution
                                ).onSuccess {
                                    yield(it)
                                }
                            }
                        }
                    }
                    if (n0[indexA] == n1[indexB] && n0[indexB] == n1[indexA]) {
                        return sequence {
                            if (step < node.solution[indexA].size) {

                                createNode(
                                    agents,
                                    indexA,
                                    node.vertexConstraints,
                                    node.edgeConstraints + Triple(
                                        Pair(n0[indexA], n1[indexA]),
                                        step,
                                        agents[indexA].id
                                    ),
                                    node.solution,
                                ).onSuccess {
                                    yield(it)
                                }
                            }
                            if (step < node.solution[indexB].size) {
                                createNode(
                                    agents,
                                    indexB,
                                    node.vertexConstraints,
                                    node.edgeConstraints + Triple(
                                        Pair(n0[indexB], n1[indexB]),
                                        step,
                                        agents[indexB].id
                                    ),
                                    node.solution,
                                ).onSuccess {
                                    yield(it)
                                }
                            }
                        }
                    }
                }
            }
            n0 = n1
            step++
        }
        return emptySequence()
    }

    private fun createNode(
        agents: List<Agent<CT>>,
        index: Int,
        vertexConstraints: Set<VertexConstraint<CT>>,
        edgeConstraints: Set<EdgeConstraint<CT>>,
        previousSolution: List<List<CT>>
    ): Result<ConstraintTreeNode<CT>> {
        return singleAgentSolver.solve(agents[index], 0, vertexConstraints, edgeConstraints).map {
            val newSolutionSet = previousSolution.updated(index, it)
            ConstraintTreeNode(vertexConstraints,
                edgeConstraints,
                newSolutionSet,
                newSolutionSet.fold(0) { prev, path -> prev + path.size })
        }
    }

    private fun createRootNode(agents: List<Agent<CT>>): Result<ConstraintTreeNode<CT>> {
        val solution: List<List<CT>>
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