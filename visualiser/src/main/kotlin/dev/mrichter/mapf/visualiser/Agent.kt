package dev.mrichter.mapf.visualiser

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.mrichter.mapf.graph.Coordinates
import kotlin.math.ceil
import kotlin.math.floor

@Composable
fun AgentC(tileSize: Int, agentData: AgentData) {
    val position = agentData.position()
    Box(
        Modifier
            .offset((tileSize * position.first).dp, (tileSize * position.second).dp)
    ){
        Box(
            Modifier
                .size(tileSize.dp, tileSize.dp)
                .background(agentData.color)
        )
    }
}

data class AgentData(
    val color: Color,
    val path: List<Coordinates>,
) {
    var finished by mutableStateOf(false)
    private var tilesSinceStart by mutableStateOf(0.0)

    fun position(): Pair<Float, Float> {
        val fromIndex = floor(tilesSinceStart).toInt()
        if (fromIndex >= path.size -1) {
            finished = true
            return Pair(path.last().x.toFloat(), path.last().y.toFloat())
        }
        val from = path[fromIndex]
        val to = path[ceil(tilesSinceStart).toInt()]
        val progress = (tilesSinceStart % 1).toFloat()
        return Pair(
            from.x + (to.x - from.x) * progress,
            from.y + (to.y - from.y) * progress,
        )
    }

    // speed in tiles per nanosecond
    fun update(delta: Long, speed: Float) {
        tilesSinceStart += (delta / speed)
    }
}