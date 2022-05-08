package dev.mrichter.mapf.visualiser

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.singleWindowApplication
import java.io.File

fun main() = singleWindowApplication(
    title = "MAPF-CBS", state = WindowState(size = DpSize(800.dp, 800.dp))
) {
    val maps = File(object {}.javaClass.getResource("/maps").file)
        .walk()
        .filter { it.isFile }
        .filter { it.extension == "map" }
        .map { Choice(it.name, "/maps/${it.name}") }.toList()
    MapfVisualiser(maps)
}