package dev.mrichter.mapf.visualiser

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.mrichter.mapf.graph.*
import dev.mrichter.mapf.parser.MovingAITextGridParser
import dev.mrichter.mapf.solver.CTSolver
import dev.mrichter.mapf.solver.SingleAgentAStarSolver
import java.util.*

class Instance {
    private var previousTimeNanos: Long = Long.MAX_VALUE
    private val colors = arrayOf(
        Color.Red, Color.Blue, Color.Cyan,
        Color.Magenta, Color.Yellow,
    )
    private var startTime = 0L
    var size by mutableStateOf(Pair(0.dp, 0.dp))
    var agents = mutableStateListOf<AgentData>()
        private set
    var elapsed by mutableStateOf(0L)
    var started by mutableStateOf(false)
    var finished by mutableStateOf(false)

    fun start(paths: List<List<Coordinates>>) {
        previousTimeNanos = System.nanoTime()
        startTime = previousTimeNanos
        started = true
        agents.clear()
        paths.map {
            agents.add(
                AgentData(colors.random(), it)
            )
        }
    }

    fun update(nanos: Long) {
        val dt = (nanos - previousTimeNanos).coerceAtLeast(0)
        previousTimeNanos = nanos
        elapsed = nanos - startTime
        agents.forEach { it.update(dt, (1 / 10E-9).toFloat()) }
    }
}

@Composable
fun Grid(tileSize: Int, graph: GridGraph) {
    graph.tiles.forEachIndexed { y, row ->
        row.forEachIndexed { x, tile ->
            Box(
                Modifier
                    .offset((tileSize * x).dp, (tileSize * y).dp)
            ) {
                Box(
                    Modifier
                        .size(tileSize.dp, tileSize.dp)
                        .background(
                            when (tile) {
                                TileType.EMPTY -> Color.Gray
                                TileType.WALL -> Color.Black
                            }
                        )
                )
            }
        }
    }
}

@Composable
@Preview
fun MapfVisualiser() {
    val instance = remember { Instance() }
    val density = LocalDensity.current
    val tileSize = 12
    val graph = getGraph()
    val solution = graph?.let { CTSolver(SingleAgentAStarSolver(it, ::manhattanDistance)) }?.solve(
        listOf(
            Agent(UUID.randomUUID(), Coordinates(18, 4), Coordinates(20, 42)),
            Agent(UUID.randomUUID(), Coordinates(20, 4), Coordinates(20, 35)),
            Agent(UUID.randomUUID(), Coordinates(10, 42), Coordinates(30, 40)),
            Agent(UUID.randomUUID(), Coordinates(10, 41), Coordinates(8,22)),
            )
    )

    Column {
        Row {
            Button(
                onClick = {
                    instance.started = !instance.started
                    if (instance.started) {
                        solution?.onSuccess {
                            instance.start(it.solution)
                        }
                    }
                }, modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (instance.started) "Stop" else "Start", fontSize = 20.sp)
            }
        }
        if (instance.started) {
            Box(modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .onSizeChanged {
                    with(density) {
                        instance.size = it.width.toDp() to it.height.toDp()
                    }
                }
            ) {
                graph?.let {
                    Grid(tileSize, graph)
                }
                instance.agents.map { AgentC(tileSize, it) }
            }
        }

        LaunchedEffect(Unit) {
            while (true) {
                withFrameNanos {
                    if (instance.started && !instance.finished)
                        instance.update(it)
                }
            }
        }
    }
}

fun getGraph(): GridGraph? =
    object {}.javaClass.getResourceAsStream("/den207d.map")
        ?.let {
            MovingAITextGridParser(it.bufferedReader()).parse()
        }
