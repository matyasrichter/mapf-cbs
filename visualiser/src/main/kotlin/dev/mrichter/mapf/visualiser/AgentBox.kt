package dev.mrichter.mapf.visualiser

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.mrichter.mapf.graph.Coordinates

@Composable
fun AgentBox(tileSize: Int, coordinates: Coordinates, color: Color) {
    Box(
        modifier = Modifier.offset(
            (tileSize * coordinates.x).dp, (tileSize * coordinates.y).dp
        )
    ) {
        Box(
            modifier = Modifier.size(tileSize.dp, tileSize.dp).background(color)
        )
    }
}