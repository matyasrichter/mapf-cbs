package dev.mrichter.mapf.solver

import dev.mrichter.mapf.graph.*
import org.junit.Test
import java.util.*
import kotlin.test.*
import kotlin.test.assertTrue

class SingleAgentAStarSolverTest {
    @Test
    fun `Finds basic path`() {
        val graph = GridGraph(
            arrayOf(
                arrayOf(TileType.WALL, TileType.WALL, TileType.WALL, TileType.WALL, TileType.WALL),
                arrayOf(TileType.WALL, TileType.EMPTY, TileType.EMPTY, TileType.EMPTY, TileType.WALL),
                arrayOf(TileType.WALL, TileType.WALL, TileType.WALL, TileType.WALL, TileType.WALL),
            )
        )
        val solver = SingleAgentAStarSolver(graph, ::manhattanDistance)
        val result = solver.solve(Agent(UUID.randomUUID(), Coordinates(1, 1), Coordinates(3, 1)), 0, setOf(), setOf())
        assertTrue { result.isSuccess }
        assertContentEquals(result.getOrThrow(), listOf(Coordinates(1, 1), Coordinates(2, 1), Coordinates(3, 1)))
    }

    @Test
    fun `Fails for basic unsolvable path`() {
        val graph = GridGraph(
            arrayOf(
                arrayOf(TileType.WALL, TileType.WALL, TileType.WALL, TileType.WALL, TileType.WALL),
                arrayOf(TileType.WALL, TileType.EMPTY, TileType.WALL, TileType.EMPTY, TileType.WALL),
                arrayOf(TileType.WALL, TileType.WALL, TileType.WALL, TileType.WALL, TileType.WALL),
            )
        )
        val solver = SingleAgentAStarSolver(graph, ::manhattanDistance)
        val result = solver.solve(Agent(UUID.randomUUID(), Coordinates(1, 1), Coordinates(3, 1)), 0, setOf(), setOf())
        assertTrue { result.isFailure }
    }

    @Test
    fun `Finds optimal path`() {
        val graph = GridGraph(
            arrayOf(
                arrayOf(TileType.WALL, TileType.WALL, TileType.WALL, TileType.WALL, TileType.WALL, TileType.WALL),
                arrayOf(TileType.WALL, TileType.EMPTY, TileType.EMPTY, TileType.EMPTY, TileType.EMPTY, TileType.WALL),
                arrayOf(TileType.WALL, TileType.WALL, TileType.EMPTY, TileType.WALL, TileType.EMPTY, TileType.WALL),
                arrayOf(TileType.WALL, TileType.EMPTY, TileType.EMPTY, TileType.EMPTY, TileType.EMPTY, TileType.WALL),
                arrayOf(TileType.WALL, TileType.WALL, TileType.WALL, TileType.WALL, TileType.WALL, TileType.WALL),
            )
        )
        val solver = SingleAgentAStarSolver(graph, ::manhattanDistance)
        val result = solver.solve(Agent(UUID.randomUUID(), Coordinates(1, 1), Coordinates(3, 3)), 0, setOf(), setOf())
        assertTrue { result.isSuccess }
        assertContentEquals(
            result.getOrThrow(), listOf(
                Coordinates(1, 1), Coordinates(2, 1), Coordinates(2, 2), Coordinates(2, 3), Coordinates(3, 3)
            )
        )
    }

    @Test
    fun `Finds path with vertex constraints for other agents`() {
        val graph = GridGraph(
            arrayOf(
                arrayOf(TileType.WALL, TileType.WALL, TileType.WALL, TileType.WALL, TileType.WALL, TileType.WALL),
                arrayOf(TileType.WALL, TileType.EMPTY, TileType.EMPTY, TileType.EMPTY, TileType.EMPTY, TileType.WALL),
                arrayOf(TileType.WALL, TileType.WALL, TileType.EMPTY, TileType.WALL, TileType.EMPTY, TileType.WALL),
                arrayOf(TileType.WALL, TileType.EMPTY, TileType.EMPTY, TileType.EMPTY, TileType.EMPTY, TileType.WALL),
                arrayOf(TileType.WALL, TileType.WALL, TileType.WALL, TileType.WALL, TileType.WALL, TileType.WALL),
            )
        )
        val uuid = UUID.randomUUID()
        val constraints = setOf(
            Triple(Coordinates(1, 3), 0, uuid),
            Triple(Coordinates(2, 3), 1, uuid),
            Triple(Coordinates(2, 2), 2, uuid),
            Triple(Coordinates(2, 1), 3, uuid),
            Triple(Coordinates(3, 1), 4, uuid),
        )
        val solver = SingleAgentAStarSolver(graph, ::manhattanDistance)
        val result =
            solver.solve(Agent(UUID.randomUUID(), Coordinates(1, 1), Coordinates(3, 3)), 0, constraints, setOf())
        assertTrue { result.isSuccess }
        assertContentEquals(
            result.getOrThrow(), listOf(
                Coordinates(1, 1), Coordinates(2, 1), Coordinates(2, 2), Coordinates(2, 3), Coordinates(3, 3)
            )
        )
    }

    @Test
    fun `Finds path with vertex self-constraints`() {
        val graph = GridGraph(
            arrayOf(
                arrayOf(TileType.WALL, TileType.WALL, TileType.WALL, TileType.WALL, TileType.WALL, TileType.WALL),
                arrayOf(TileType.WALL, TileType.EMPTY, TileType.EMPTY, TileType.EMPTY, TileType.EMPTY, TileType.WALL),
                arrayOf(TileType.WALL, TileType.WALL, TileType.EMPTY, TileType.WALL, TileType.EMPTY, TileType.WALL),
                arrayOf(TileType.WALL, TileType.EMPTY, TileType.EMPTY, TileType.EMPTY, TileType.EMPTY, TileType.WALL),
                arrayOf(TileType.WALL, TileType.WALL, TileType.WALL, TileType.WALL, TileType.WALL, TileType.WALL),
            )
        )
        val uuid = UUID.randomUUID()
        val vConstraints = setOf(
            Triple(Coordinates(1, 3), 0, uuid),
            Triple(Coordinates(2, 3), 1, uuid),
            Triple(Coordinates(2, 2), 2, uuid),
            Triple(Coordinates(2, 1), 3, uuid),
            Triple(Coordinates(3, 1), 4, uuid),
        )

        val solver = SingleAgentAStarSolver(graph, ::manhattanDistance)
        val result = solver.solve(Agent(uuid, Coordinates(1, 1), Coordinates(3, 3)), 0, vConstraints, setOf())
        assertTrue { result.isSuccess }
        assertEquals(7, result.getOrThrow().size)
    }

    @Test
    fun `Finds path with edge self-constraints`() {
        val graph = GridGraph(
            arrayOf(
                arrayOf(TileType.WALL, TileType.WALL, TileType.WALL, TileType.WALL, TileType.WALL, TileType.WALL),
                arrayOf(TileType.WALL, TileType.EMPTY, TileType.EMPTY, TileType.EMPTY, TileType.EMPTY, TileType.WALL),
                arrayOf(TileType.WALL, TileType.WALL, TileType.EMPTY, TileType.WALL, TileType.EMPTY, TileType.WALL),
                arrayOf(TileType.WALL, TileType.EMPTY, TileType.EMPTY, TileType.EMPTY, TileType.EMPTY, TileType.WALL),
                arrayOf(TileType.WALL, TileType.WALL, TileType.WALL, TileType.WALL, TileType.WALL, TileType.WALL),
            )
        )
        val uuid = UUID.randomUUID()
        val eConstraints = setOf(
            Triple(Pair(Coordinates(2, 1), Coordinates(2, 2)), 1, uuid)
        )
        val solver = SingleAgentAStarSolver(graph, ::manhattanDistance)
        val result = solver.solve(Agent(uuid, Coordinates(1, 1), Coordinates(3, 3)), 0, setOf(), eConstraints)
        assertTrue { result.isSuccess }
        assertEquals(7, result.getOrThrow().size)
    }
}