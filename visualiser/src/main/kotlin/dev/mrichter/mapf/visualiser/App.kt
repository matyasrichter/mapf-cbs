package dev.mrichter.mapf.visualiser

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp
import dev.mrichter.mapf.graph.Agent
import dev.mrichter.mapf.graph.Coordinates
import dev.mrichter.mapf.graph.GridGraph
import dev.mrichter.mapf.graph.manhattanDistance
import dev.mrichter.mapf.parser.MovingAITextGridParser
import dev.mrichter.mapf.solver.CTSolver
import dev.mrichter.mapf.solver.SingleAgentAStarSolver
import kotlinx.coroutines.launch
import java.util.*
import kotlin.math.min



@Composable
@Preview
fun MapfVisualiser(maps: List<Choice>) {
    val snackbarHostState = remember { SnackbarHostState() }
    var appState by remember { mutableStateOf<AppState>(ChoosingMap(snackbarHostState)) }

    val tileSize = remember { mutableStateOf(0) }
    val coroutineScope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxWidth().fillMaxHeight().padding(5.dp)) {
        val headerHeight = 80.dp
        when (val state = appState) {
            is ChoosingMap -> {
                Row(
                    modifier = Modifier.height(headerHeight).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        MapSelectMenu(maps) {
                            coroutineScope.launch {
                                state.setMap(it)
                            }
                        }
                    }
                    Column(
                        modifier = Modifier.align(Alignment.CenterVertically)

                    ) {
                        Button(
                            onClick = {
                                appState = state.proceed()
                            }, enabled = state.graph.value != null
                        ) {
                            Text("Continue")
                        }
                    }
                }
                Row {
                    state.graph.value?.let { graph ->
                        Box(modifier = Modifier.fillMaxWidth().fillMaxHeight().onSizeChanged {
                            tileSize.value =
                                min((it.width / graph.tiles.first().size), (it.height / graph.tiles.size))
                        }) {
                            Grid(tileSize.value, graph)
                        }
                    }
                }
            }
            is ChoosingAgents -> {
                Row(
                    modifier = Modifier.height(headerHeight).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Row {
                            NumericField("Start X", state.tmpStartX){
                                state.tmpStartX = it
                            }
                            NumericField("Start Y", state.tmpStartY) {
                                state.tmpStartY = it
                            }
                            NumericField("Target X", state.tmpTargetX) {
                                state.tmpTargetX = it
                            }
                            NumericField("Target Y", state.tmpTargetY) {
                                state.tmpTargetY = it
                            }
                            Button(onClick = {
                                state.addAgent()
                            }) {
                                Text("Add")
                            }
                        }
                    }
                    Column(
                        modifier = Modifier.align(Alignment.CenterVertically)

                    ) {
                        Button(
                            onClick = {
                                appState = state.proceed()
                            }, enabled = state.agents.isNotEmpty()
                        ) {
                            Text("Continue")
                        }
                    }
                }
                Row {
                    Box(modifier = Modifier.fillMaxWidth().fillMaxHeight().onSizeChanged {
                        tileSize.value =
                            min((it.width / state.graph.tiles.first().size), (it.height / state.graph.tiles.size))
                    }) {
                        Grid(tileSize.value, state.graph)
                        state.agents.map { (agent, color) ->
                            AgentBox(tileSize.value, agent.start, color)
                            AgentBox(tileSize.value, agent.target, color)
                        }
                        state.tmpStart()?.let {
                            AgentBox(tileSize.value, it, state.peekNextColor())
                        }
                        state.tmpTarget()?.let {
                            AgentBox(tileSize.value, it, state.peekNextColor())
                        }
                    }
                }
            }
            is Solving -> {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxHeight().fillMaxWidth()
                ) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterVertically))
                }
                appState = state.proceed()
            }
            is NotSolvable -> {
                Text("This MAPF instance isn't solvable.")
                Button(onClick = {
                    appState = state.proceed()
                }) {
                    Text("Restart")
                }
            }

            is Solution -> {
                Row(modifier = Modifier.height(headerHeight).fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Column(
                        modifier = Modifier.align(Alignment.CenterVertically)

                    ) {
                        Row {
                            Button(onClick = {
                                state.restart()
                            }, modifier = Modifier.padding(horizontal = 5.dp)) {
                                Text("Restart")
                            }
                            Button(onClick = {
                                appState = state.proceed()
                            }) {
                                Text("Choose a new map")
                            }
                        }
                    }
                }
                Box(modifier = Modifier.fillMaxWidth().fillMaxHeight().onSizeChanged {
                    tileSize.value =
                        min((it.width / state.graph.tiles.first().size), (it.height / state.graph.tiles.size))
                }) {
                    Grid(tileSize.value, state.graph)
                    state.solution.value.map { AgentC(tileSize.value, it) }
                }
                state.start()
                LaunchedEffect(Unit) {
                    while (true) {
                        withFrameNanos {
                            if (!state.paused && !state.finished) state.update(it)
                        }
                    }
                }
            }
        }
        SnackbarHost(hostState = snackbarHostState)
    }
}