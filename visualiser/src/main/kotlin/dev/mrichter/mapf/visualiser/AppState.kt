package dev.mrichter.mapf.visualiser

import androidx.compose.material.SnackbarHostState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import dev.mrichter.mapf.graph.*
import dev.mrichter.mapf.parser.MovingAITextGridParser
import dev.mrichter.mapf.solver.CTSolver
import dev.mrichter.mapf.solver.SingleAgentAStarSolver
import java.util.*

fun <T> Result<Result<T>>.unwrap() = fold(onFailure = { Result.failure(it) }, onSuccess = { it.map { graph -> graph } })

fun <T> T?.asResult(error: String) = if (this != null) Result.success(this) else Result.failure(Exception(error))
fun <T> Sequence<T>.repeat() = sequence { while (true) yieldAll(this@repeat) }

fun getColorsSequence(): Sequence<Color> {
    return arrayOf(
        Color.Red, Color.Blue, Color.Cyan,
        Color.Magenta, Color.Yellow,
    ).asSequence().repeat()
}

sealed class AppState(val snackbarHostState: SnackbarHostState) {
    abstract fun proceed(): AppState
}

class ChoosingMap(snackbarHostState: SnackbarHostState) : AppState(snackbarHostState) {
    val graph = mutableStateOf<GridGraph?>(null)

    override fun proceed(): AppState {
        val g = graph.value
        return g?.let { ChoosingAgents(snackbarHostState, g) } ?: this
    }

    suspend fun setMap(path: String) = getGraph(path).fold(onSuccess = { graph.value = it },
        onFailure = { snackbarHostState.showSnackbar(it.message.orEmpty()) })

    private fun getGraph(resourceName: String): Result<GridGraph> =
        object {}.javaClass.getResourceAsStream(resourceName).asResult("Could not open map file")
            .map { it.bufferedReader() }.map { MovingAITextGridParser(it).parse() }.unwrap()
}

class ChoosingAgents(
    snackbarHostState: SnackbarHostState,
    val graph: GridGraph,
) : AppState(snackbarHostState) {
    private val colors by mutableStateOf(getColorsSequence().iterator())
    private var nextColor by mutableStateOf(colors.next())
    var tmpStartX by mutableStateOf<Int?>(null)
    var tmpStartY by mutableStateOf<Int?>(null)
    var tmpTargetX by mutableStateOf<Int?>(null)
    var tmpTargetY by mutableStateOf<Int?>(null)

    var agents by mutableStateOf(
        listOf<Pair<Agent<Coordinates>, Color>>()
    )

    fun peekNextColor() = nextColor
    fun takeNextColor() = nextColor.also { nextColor = colors.next() }

    override fun proceed(): AppState {
        if (agents.isNotEmpty()) return Solving(snackbarHostState, graph, agents.map { it.first })
        return this
    }

    fun tmpStart(): Coordinates? = buildCoordinates(tmpStartX, tmpStartY)

    fun tmpTarget(): Coordinates? = buildCoordinates(tmpTargetX, tmpTargetY)

    fun addAgent(): Result<Unit> {
        val start =
            tmpStart()?.let { if (graph.at(it) == TileType.EMPTY && !agents.any { a -> a.first.start == it }) it else null }
        val target =
            tmpTarget()?.let { if (graph.at(it) == TileType.EMPTY && !agents.any { a -> a.first.target == it }) it else null }
        start?.let {
            target?.let {
                agents = agents + Pair(Agent(UUID.randomUUID(), start, target), takeNextColor())
                return Result.success(Unit)
            }
        }
        return Result.failure(Exception("Invalid coordinates"))
    }

    private fun clear() {
        tmpStartX = null
        tmpStartY = null
        tmpTargetX = null
        tmpTargetX = null
    }

    fun removeAgent(index: Int) {
        agents = agents.take(index - 1) + agents.drop(index)
    }

    private fun buildCoordinates(x: Int?, y: Int?): Coordinates? {
        if (x != null && 0 <= x && x < graph.tiles.first().size) {
            if (y != null && 0 <= y && y < graph.tiles.size) {
                return Coordinates(x, y)
            }
        }
        return null
    }
}

class Solving(
    snackbarHostState: SnackbarHostState,
    val graph: GridGraph,
    val agents: List<Agent<Coordinates>>,
) : AppState(snackbarHostState) {
    override fun proceed(): AppState = CTSolver(SingleAgentAStarSolver(graph, ::manhattanDistance)).solve(agents).map {
        Solution(
            snackbarHostState,
            graph,
            mutableStateOf(it.solution.zip(getColorsSequence().asIterable()).map { (path, color) ->
                AgentData(color, path)
            })
        )
    }.getOrDefault(NotSolvable(snackbarHostState))
}

class NotSolvable(
    snackbarHostState: SnackbarHostState
) : AppState(snackbarHostState) {
    override fun proceed(): AppState = ChoosingMap(snackbarHostState)
}

class Solution(
    snackbarHostState: SnackbarHostState,
    val graph: GridGraph,
    val solution: MutableState<List<AgentData>>,
) : AppState(snackbarHostState) {
    private var previousTimeNanos: Long = Long.MAX_VALUE
    private var startTime = 0L
    var paused = true
    var finished = false

    override fun proceed(): AppState = ChoosingMap(snackbarHostState)

    fun restart() {
        paused = true
        solution.value.forEach { it.restart() }
        start()
    }

    fun start() {
        previousTimeNanos = System.nanoTime()
        startTime = previousTimeNanos
        paused = false
        finished = false
    }

    fun update(nanos: Long) {
        val dt = (nanos - previousTimeNanos).coerceAtLeast(0)
        previousTimeNanos = nanos
        solution.value.forEach { it.update(dt, (1 / 10E-9).toFloat()) }
        finished = (solution.value.all { it.finished })
    }
}