package dev.mrichter.mapf.solver

import dev.mrichter.mapf.graph.*
import org.junit.Test
import java.util.UUID
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CTSolverTest {
    @Test
    fun `Solves basic instance without conflicts`() {
        val graph = GridGraph(
            arrayOf(
                arrayOf(TileType.WALL, TileType.WALL, TileType.WALL, TileType.WALL, TileType.WALL),
                arrayOf(TileType.WALL, TileType.EMPTY, TileType.EMPTY, TileType.EMPTY, TileType.WALL),
                arrayOf(TileType.WALL, TileType.WALL, TileType.WALL, TileType.WALL, TileType.WALL),
                arrayOf(TileType.WALL, TileType.EMPTY, TileType.EMPTY, TileType.EMPTY, TileType.WALL),
                arrayOf(TileType.WALL, TileType.WALL, TileType.WALL, TileType.WALL, TileType.WALL),
            )
        )
        val solver = CTSolver(
            SingleAgentAStarSolver(graph, ::manhattanDistance)
        )
        val solution = solver.solve(
            listOf(
                Agent(UUID.randomUUID(), Coordinates(1, 1), Coordinates(3, 1)),
                Agent(UUID.randomUUID(), Coordinates(1, 3), Coordinates(3, 3)),
            )
        )
        assertTrue { solution.isSuccess }
        solution.onSuccess {
            assertEquals(6, it.cost)
            assertTrue { it.vertexConstraints.isEmpty() }
            assertContentEquals(
                listOf(
                    listOf(Coordinates(1, 1), Coordinates(2, 1), Coordinates(3, 1)),
                    listOf(Coordinates(1, 3), Coordinates(2, 3), Coordinates(3, 3))
                ),
                it.solution
            )
        }
    }

    @Test
    fun `Fails for an unsolvable instance`() {
        val graph = GridGraph(
            arrayOf(
                arrayOf(TileType.WALL, TileType.WALL, TileType.WALL, TileType.WALL, TileType.WALL),
                arrayOf(TileType.WALL, TileType.EMPTY, TileType.EMPTY, TileType.EMPTY, TileType.WALL),
                arrayOf(TileType.WALL, TileType.WALL, TileType.WALL, TileType.WALL, TileType.WALL),
                arrayOf(TileType.WALL, TileType.EMPTY, TileType.EMPTY, TileType.EMPTY, TileType.WALL),
                arrayOf(TileType.WALL, TileType.WALL, TileType.WALL, TileType.WALL, TileType.WALL),
            )
        )
        val solver = CTSolver(
            SingleAgentAStarSolver(graph, ::manhattanDistance)
        )
        val solution = solver.solve(
            listOf(
                Agent(UUID.randomUUID(), Coordinates(1, 1), Coordinates(3, 1)),
                Agent(UUID.randomUUID(), Coordinates(1, 3), Coordinates(1, 1)),
            )
        )
        assertTrue { solution.isFailure }
    }

    @Test
    fun `Solves a basic instance with conflicts`() {
        val graph = GridGraph(
            arrayOf(
                arrayOf(TileType.WALL, TileType.WALL, TileType.WALL, TileType.WALL, TileType.WALL),
                arrayOf(TileType.WALL, TileType.EMPTY, TileType.EMPTY, TileType.EMPTY, TileType.WALL),
                arrayOf(TileType.WALL, TileType.EMPTY, TileType.EMPTY, TileType.EMPTY, TileType.WALL),
                arrayOf(TileType.WALL, TileType.EMPTY, TileType.EMPTY, TileType.EMPTY, TileType.WALL),
                arrayOf(TileType.WALL, TileType.WALL, TileType.WALL, TileType.WALL, TileType.WALL),
            )
        )
        val solver = CTSolver(
            SingleAgentAStarSolver(graph, ::manhattanDistance)
        )
        val solution = solver.solve(
            listOf(
                Agent(UUID.randomUUID(), Coordinates(1, 1), Coordinates(3, 3)),
                Agent(UUID.randomUUID(), Coordinates(1, 3), Coordinates(3, 1)),
            )
        )
        assertTrue { solution.isSuccess }
        solution.onSuccess {
            assertEquals(10, it.cost)
        }
    }

    @Test
    fun `Solves an instance with forced waiting`() {
        val graph = GridGraph(
            arrayOf(
                arrayOf(TileType.WALL, TileType.WALL, TileType.WALL, TileType.WALL, TileType.WALL, TileType.WALL, TileType.WALL),
                arrayOf(TileType.WALL, TileType.EMPTY, TileType.EMPTY, TileType.WALL, TileType.EMPTY, TileType.EMPTY, TileType.WALL),
                arrayOf(TileType.WALL, TileType.EMPTY, TileType.EMPTY, TileType.EMPTY, TileType.EMPTY, TileType.EMPTY, TileType.WALL),
                arrayOf(TileType.WALL, TileType.EMPTY, TileType.EMPTY, TileType.WALL, TileType.EMPTY, TileType.EMPTY, TileType.WALL),
                arrayOf(TileType.WALL, TileType.WALL, TileType.WALL, TileType.WALL, TileType.WALL, TileType.WALL, TileType.WALL),
            )
        )
        val solver = CTSolver(
            SingleAgentAStarSolver(graph, ::manhattanDistance)
        )
        val solution = solver.solve(
            listOf(
                Agent(UUID.randomUUID(), Coordinates(1, 1), Coordinates(5, 3)),
                Agent(UUID.randomUUID(), Coordinates(5, 1), Coordinates(1, 3)),
            )
        )
        assertTrue { solution.isSuccess }
        solution.onSuccess {
            assertEquals(14, it.cost)
        }
    }

    @Test
    fun `Solves an instance with multiple agents`() {
        val graph = GridGraph(
            arrayOf(
                arrayOf(TileType.WALL, TileType.WALL, TileType.WALL, TileType.WALL, TileType.WALL, TileType.WALL),
                arrayOf(TileType.WALL, TileType.EMPTY, TileType.EMPTY, TileType.EMPTY, TileType.EMPTY, TileType.WALL),
                arrayOf(TileType.WALL, TileType.WALL, TileType.EMPTY, TileType.EMPTY, TileType.WALL, TileType.WALL),
                arrayOf(TileType.WALL, TileType.EMPTY, TileType.EMPTY, TileType.EMPTY, TileType.EMPTY, TileType.WALL),
                arrayOf(TileType.WALL, TileType.EMPTY, TileType.EMPTY, TileType.EMPTY, TileType.EMPTY, TileType.WALL),
                arrayOf(TileType.WALL, TileType.EMPTY, TileType.WALL, TileType.WALL, TileType.EMPTY, TileType.WALL),
                arrayOf(TileType.WALL, TileType.EMPTY, TileType.EMPTY, TileType.EMPTY, TileType.EMPTY, TileType.WALL),
                arrayOf(TileType.WALL, TileType.WALL, TileType.EMPTY, TileType.EMPTY, TileType.EMPTY, TileType.WALL),
                arrayOf(TileType.WALL, TileType.EMPTY, TileType.EMPTY, TileType.WALL, TileType.EMPTY, TileType.WALL),
                arrayOf(TileType.WALL, TileType.WALL, TileType.WALL, TileType.WALL, TileType.WALL, TileType.WALL),
            )
        )
        val solver = CTSolver(
            SingleAgentAStarSolver(graph, ::manhattanDistance)
        )
        val solution = solver.solve(
            listOf(
                Agent(UUID.randomUUID(), Coordinates(1, 1), Coordinates(4, 1)),
                Agent(UUID.randomUUID(), Coordinates(4, 8), Coordinates(1,3)),
                Agent(UUID.randomUUID(), Coordinates(1,8), Coordinates(2,1)),
                Agent(UUID.randomUUID(), Coordinates(1,4), Coordinates(4,3)),
                Agent(UUID.randomUUID(), Coordinates(2,1), Coordinates(2,7)),
            )
        )
        assertTrue { solution.isSuccess }
    }
}