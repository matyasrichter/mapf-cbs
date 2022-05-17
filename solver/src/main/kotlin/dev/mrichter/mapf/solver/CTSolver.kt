package dev.mrichter.mapf.solver

import dev.mrichter.mapf.graph.Agent
import java.util.*

class ConstraintTreeNode<CT>(
    val parent: ConstraintTreeNode<CT>?,
    val vertexConstraint: VertexConstraint<CT>?,
    val edgeConstraint: EdgeConstraint<CT>?,
    val agentId: UUID,
    val solution: List<List<CT>>,
    val cost: Int,
) {
    /**
     * Traverse the tree and collect vertex constraints for an agent.
     */
    fun getVertexConstraints(id: UUID): Sequence<VertexConstraint<CT>> = sequence {
        if (agentId == id && vertexConstraint != null) {
            yield(vertexConstraint)
        }
        parent?.let {
            yieldAll(it.getVertexConstraints(id))
        }
    }

    /**
     * Traverse the tree and collect edge constraints for an agent.
     */
    fun getEdgeConstraints(id: UUID): Sequence<EdgeConstraint<CT>> = sequence {
        if (agentId == id && edgeConstraint != null) {
            yield(edgeConstraint)
        }
        parent?.let {
            yieldAll(it.getEdgeConstraints(id))
        }
    }

    val depth: Int = parent?.depth?.inc() ?: 0
}

fun <E> Iterable<E>.updated(index: Int, elem: E) = mapIndexed { i, existing -> if (i == index) elem else existing }

class CTSolver<CT>(
    private val singleAgentSolver: SingleAgentSolver<CT>,
) {
    fun solve(agents: List<Agent<CT>>): Result<ConstraintTreeNode<CT>> {
        val open = PriorityQueue(compareBy<ConstraintTreeNode<CT>> { it.cost }.thenBy { it.depth })
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

    private fun createNode(
        agents: List<Agent<CT>>,
        parent: ConstraintTreeNode<CT>,
        index: Int,
        vertexConstraint: VertexConstraint<CT>?,
        edgeConstraint: EdgeConstraint<CT>?,
    ): Result<ConstraintTreeNode<CT>> {
        return singleAgentSolver.solve(
            agents[index],
            0,
            parent.getVertexConstraints(agents[index].id).toHashSet()
                .also { set -> vertexConstraint?.let { set.add(it) } },
            parent.getEdgeConstraints(agents[index].id).toHashSet().also { set -> edgeConstraint?.let { set.add(it) } },
        ).map {
            val newSolutionSet = parent.solution.updated(index, it)
            ConstraintTreeNode(parent,
                vertexConstraint,
                edgeConstraint,
                agents[index].id,
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
            ConstraintTreeNode(parent = null,
                vertexConstraint = null,
                edgeConstraint = null,
                agentId = agents[0].id,
                solution = solution,
                cost = solution.fold(0) { prev, path -> prev + path.size })
        )
    }

    /**
     * Find conflicts by iterating through all paths and create child nodes for them.
     */
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
                    // check for vertex conflict
                    if (n0[indexA] == n0[indexB]) {
                        return sequence {
                            // check if we're not creating a constraint for a finished agent
                            // because the child node would never reach it
                            if (step < node.solution[indexA].size) {
                                createNode(agents, node, indexA, Pair(n0[indexA], step), null)
                                    .onSuccess {
                                        yield(it)
                                    }
                            }
                            if (step < node.solution[indexB].size) {
                                createNode(agents, node, indexB, Pair(n0[indexB], step), null)
                                    .onSuccess {
                                        yield(it)
                                    }
                            }
                        }
                    }
                    // check for edge conflict
                    if (n0[indexA] == n1[indexB] && n0[indexB] == n1[indexA]) {
                        return sequence {
                            if (step < node.solution[indexA].size) {
                                createNode(
                                    agents,
                                    node,
                                    indexA,
                                    null,
                                    Pair(Pair(n0[indexA], n1[indexA]), step),
                                ).onSuccess {
                                    yield(it)
                                }
                            }
                            if (step < node.solution[indexB].size) {
                                createNode(
                                    agents,
                                    node,
                                    indexB,
                                    null,
                                    Pair(Pair(n0[indexB], n1[indexB]), step),
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
}