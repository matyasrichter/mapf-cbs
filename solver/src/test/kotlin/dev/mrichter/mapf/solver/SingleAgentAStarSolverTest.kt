package dev.mrichter.mapf.solver

import dev.mrichter.mapf.graph.Coordinates
import dev.mrichter.mapf.graph.GridGraph
import dev.mrichter.mapf.graph.TileType
import dev.mrichter.mapf.graph.manhattanDistance
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
        val solver = SingleAgentAStarSolver(graph, ConstraintTable(), ::manhattanDistance)
        val result = solver.solve(UUID.randomUUID(), 0, Coordinates(1, 1), Coordinates(3, 1))
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
        val solver = SingleAgentAStarSolver(graph, ConstraintTable(), ::manhattanDistance)
        val result = solver.solve(UUID.randomUUID(), 0, Coordinates(1, 1), Coordinates(3, 1))
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
        val solver = SingleAgentAStarSolver(graph, ConstraintTable(), ::manhattanDistance)
        val result = solver.solve(UUID.randomUUID(), 0, Coordinates(1, 1), Coordinates(3, 3))
        assertTrue { result.isSuccess }
        assertContentEquals(
            result.getOrThrow(), listOf(
                Coordinates(1, 1),
                Coordinates(2, 1),
                Coordinates(2, 2),
                Coordinates(2, 3),
                Coordinates(3, 3)
            )
        )
    }

    @Test
    fun `Finds path with constraints`() {
        val graph = GridGraph(
            arrayOf(
                arrayOf(TileType.WALL, TileType.WALL, TileType.WALL, TileType.WALL, TileType.WALL, TileType.WALL),
                arrayOf(TileType.WALL, TileType.EMPTY, TileType.EMPTY, TileType.EMPTY, TileType.EMPTY, TileType.WALL),
                arrayOf(TileType.WALL, TileType.WALL, TileType.EMPTY, TileType.WALL, TileType.EMPTY, TileType.WALL),
                arrayOf(TileType.WALL, TileType.EMPTY, TileType.EMPTY, TileType.EMPTY, TileType.EMPTY, TileType.WALL),
                arrayOf(TileType.WALL, TileType.WALL, TileType.WALL, TileType.WALL, TileType.WALL, TileType.WALL),
            )
        )
        val constraintTable = ConstraintTable<Coordinates>()
        constraintTable.addPath(
            listOf(
                Coordinates(1, 3),
                Coordinates(2, 3),
                Coordinates(2, 2),
                Coordinates(2, 1),
                Coordinates(3, 1),
            ), UUID.randomUUID()
        )
        val solver = SingleAgentAStarSolver(graph, constraintTable, ::manhattanDistance)
        val result = solver.solve(UUID.randomUUID(), 0, Coordinates(1, 1), Coordinates(3, 3))
        assertTrue { result.isSuccess }
        assertContentEquals(
            result.getOrThrow(), listOf(
                Coordinates(1, 1),
                Coordinates(2, 1),
                Coordinates(3, 1),
                Coordinates(4, 1),
                Coordinates(4, 2),
                Coordinates(4, 3),
                Coordinates(3, 3),
            )
        )
    }

    @Test
    fun `Finds path with self-constraints`() {
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
        val constraintTable = ConstraintTable<Coordinates>()
        constraintTable.addPath(
            listOf(
                Coordinates(1, 3),
                Coordinates(2, 3),
                Coordinates(2, 2),
                Coordinates(2, 1),
                Coordinates(3, 1),
            ), uuid
        )
        val solver = SingleAgentAStarSolver(graph, constraintTable, ::manhattanDistance)
        val result = solver.solve(uuid, 0, Coordinates(1, 1), Coordinates(3, 3))
        assertTrue { result.isSuccess }
        assertContentEquals(
            result.getOrThrow(), listOf(
                Coordinates(1, 1),
                Coordinates(2, 1),
                Coordinates(2, 2),
                Coordinates(2, 3),
                Coordinates(3, 3)
            )
        )
    }

    @Test
    fun `Fails when unsolvable due to constraints`() {
        val graph = GridGraph(
            arrayOf(
                arrayOf(TileType.WALL, TileType.WALL, TileType.WALL, TileType.WALL, TileType.WALL, TileType.WALL),
                arrayOf(TileType.WALL, TileType.EMPTY, TileType.EMPTY, TileType.EMPTY, TileType.EMPTY, TileType.WALL),
                arrayOf(TileType.WALL, TileType.WALL, TileType.WALL, TileType.WALL, TileType.WALL, TileType.WALL),
            )
        )
        val constraintTable = ConstraintTable<Coordinates>()
        constraintTable.addPath(
            listOf(
                Coordinates(4, 1),
                Coordinates(3, 1),
                Coordinates(2, 1),
            ), UUID.randomUUID()
        )
        val solver = SingleAgentAStarSolver(
            graph,
            constraintTable,
            ::manhattanDistance
        )
        val result = solver.solve(UUID.randomUUID(), 0, Coordinates(1, 1), Coordinates(4, 1))
        assertTrue { result.isFailure }
    }
}