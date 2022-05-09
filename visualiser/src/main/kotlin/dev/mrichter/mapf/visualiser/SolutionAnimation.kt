package dev.mrichter.mapf.visualiser

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import kotlin.math.min

@Composable
fun SolutionAnimation(
    tileSize: MutableState<Int>,
    state: Solution,
) {
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