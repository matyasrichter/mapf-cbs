package dev.mrichter.mapf.visualiser

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import dev.mrichter.mapf.graph.GridGraph

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

class Solution(
    val graph: GridGraph,
    val solution: MutableState<List<AgentData>>,
) {
    private var previousTimeNanos: Long = Long.MAX_VALUE
    private var startTime = 0L
    var paused = true
    var finished = false
    var speed by mutableStateOf(1.0)

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