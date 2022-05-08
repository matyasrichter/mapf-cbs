package dev.mrichter.mapf.visualiser

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import dev.mrichter.mapf.graph.GridGraph
import dev.mrichter.mapf.graph.TileType

@Composable
fun Grid(tileSize: Int, graph: GridGraph) {
    Canvas(
        modifier = Modifier
            .width((graph.tiles.first().size * tileSize).dp)
            .height((graph.tiles.size * tileSize).dp)
    ) {
        graph.tiles.forEachIndexed { y, row ->
            row.forEachIndexed { x, tile ->
                drawRect(
                    color = when (tile) {
                        TileType.EMPTY -> Color.Gray
                        TileType.WALL -> Color.Black
                    },
                    topLeft = Offset((tileSize * x).toFloat(), (tileSize * y).toFloat()),
                    size = Size(tileSize.toFloat(), tileSize.toFloat()),
                )
            }
        }
    }
}