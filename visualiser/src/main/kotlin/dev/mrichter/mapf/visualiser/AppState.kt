package dev.mrichter.mapf.visualiser

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
        Color(230, 25, 75),
        Color(60, 180, 75),
        Color(255, 225, 25),
        Color(0, 130, 200),
        Color(245, 130, 48),
        Color(145, 30, 180),
        Color(70, 240, 240),
        Color(240, 50, 230),
        Color(210, 245, 60),
        Color(250, 190, 212),
        Color(0, 128, 128),
        Color(220, 190, 255),
        Color(170, 110, 40),
        Color(255, 250, 200),
        Color(128, 0, 0),
        Color(170, 255, 195),
        Color(128, 128, 0),
        Color(255, 215, 180),
        Color(0, 0, 128),
    ).asSequence().repeat()
}

sealed class AppState() {
    abstract fun proceed(): AppState
}

class ChoosingMap() : AppState() {
    val graph = mutableStateOf<GridGraph?>(null)

    override fun proceed(): AppState {
        val g = graph.value
        return g?.let { ChoosingAgents(g) } ?: this
    }

    fun setMap(path: String) = getGraph(path).fold(onSuccess = { graph.value = it },
        onFailure = { })

    private fun getGraph(resourceName: String): Result<GridGraph> =
        object {}.javaClass.getResourceAsStream(resourceName).asResult("Could not open map file")
            .map { it.bufferedReader() }.map { MovingAITextGridParser(it).parse() }.unwrap()
}

class ChoosingAgents(
    val graph: GridGraph,
) : AppState() {
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
        if (agents.isNotEmpty()) return Solving(graph, agents.map { it.first })
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
    val graph: GridGraph,
    val agents: List<Agent<Coordinates>>,
) : AppState() {
    override fun proceed(): AppState = CTSolver(SingleAgentAStarSolver(graph, ::manhattanDistance)).solve(agents).map {
        Solution(
            graph,
            mutableStateOf(it.solution.zip(getColorsSequence().asIterable()).map { (path, color) ->
                AgentData(color, path)
            })
        )
    }.getOrDefault(NotSolvable())
}

class NotSolvable(

) : AppState() {
    override fun proceed(): AppState = ChoosingMap()
}

class Solution(
    val graph: GridGraph,
    val solution: MutableState<List<AgentData>>,
) : AppState() {
    private var previousTimeNanos: Long = Long.MAX_VALUE
    private var startTime = 0L
    var paused = true
    var finished = false
    var speed by mutableStateOf(1.0)

    override fun proceed(): AppState = ChoosingMap()

    fun speedUp() {
        if (speed > 0.25) speed -= 0.25
    }

    fun slowDown() {
        if (speed < 2.0) speed += 0.25
    }

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
        solution.value.forEach { it.update(dt, (speed / 10E-9).toFloat()) }
        finished = (solution.value.all { it.finished })
    }
}