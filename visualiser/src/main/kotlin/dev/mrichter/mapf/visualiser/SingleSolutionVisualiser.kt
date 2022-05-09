package dev.mrichter.mapf.visualiser

import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.singleWindowApplication
import dev.mrichter.mapf.graph.Coordinates
import dev.mrichter.mapf.graph.GridGraph

fun showSingleSolution(graph: GridGraph, solution: List<List<Coordinates>>) = singleWindowApplication(
    title = "MAPF-CBS", state = WindowState(size = DpSize(800.dp, 800.dp))
) {
    SingleSolutionVisualiser(graph, solution)
}

@Composable
fun SingleSolutionVisualiser(graph: GridGraph, solution: List<List<Coordinates>>) {
    val tileSize = remember { mutableStateOf(0) }
    val state = remember {
        mutableStateOf(
            Solution(
                graph = graph,
                solution = mutableStateOf(solution.zip(getColorsSequence().asIterable()).map { (path, color) ->
                    AgentData(color, path)
                })
            )
        )
    }
    Column {
        Row(modifier = Modifier.height(50.dp).fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
            Column(
                modifier = Modifier.align(Alignment.CenterVertically)

            ) {
                Row {
                    Text("Speed: ", modifier = Modifier.align(Alignment.CenterVertically))
                    Button(onClick = {
                        state.value.slowDown()
                    }, modifier = Modifier.padding(horizontal = 5.dp)) {
                        Text("-")
                    }
                    Button(onClick = {
                        state.value.speedUp()
                    }, modifier = Modifier.padding(horizontal = 5.dp)) {
                        Text("+")
                    }
                    Button(onClick = {
                        state.value.restart()
                    }, modifier = Modifier.padding(horizontal = 10.dp)) {
                        Text("Restart")
                    }
                }
            }
        }
        Row {
            SolutionAnimation(tileSize, state.value)
        }
    }
}