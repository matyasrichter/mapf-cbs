package dev.mrichter.mapf.cli

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.types.file
import dev.mrichter.mapf.graph.manhattanDistance
import dev.mrichter.mapf.parser.MovingAIGridBenchmarkParser
import dev.mrichter.mapf.parser.MovingAITextGridParser
import dev.mrichter.mapf.solver.CTSolver
import dev.mrichter.mapf.solver.SingleAgentAStarSolver
import java.util.Calendar

class Run : CliktCommand(help = "Run a MovingAI.com benchmark") {
    private val map by argument("map").file(mustBeReadable = true)
    private val benchmark by argument("benchmark").file(mustBeReadable = true)

    override fun run() {
        echo("Parsing map...")
        val graph = MovingAITextGridParser(map.bufferedReader()).parse().onFailure {
            echo("❌ Could not parse the map file.")
        }.onSuccess {
            echo("✅ Map parsed successfully.")
        }
        echo("Parsing benchmark...")
        val benchmark = MovingAIGridBenchmarkParser(benchmark.bufferedReader()).parse().onFailure {
            echo("❌ Could not parse the benchmark file.")
        }.onSuccess {
            echo("✅ Benchmark parsed successfully.")
        }
        if (graph.isFailure || benchmark.isFailure) return
        echo("Solving...")
        val start = Calendar.getInstance().timeInMillis
        CTSolver(SingleAgentAStarSolver(graph.getOrThrow(), ::manhattanDistance)).solve(benchmark.getOrThrow())
            .onSuccess { node ->
                val end = Calendar.getInstance().timeInMillis
                echo("✅ Found a solution in ${((end - start) / 10E6)} s.")
                if (confirm("Do you want to see the solution?") == true) {
                    echo("Cost: ${node.cost}")
                    echo("Paths:")
                    val iterators = node.solution.map { path -> path.iterator() }
                    while (iterators.any { it.hasNext() }) {
                        val line = iterators.map { if (it.hasNext()) it.next() else null }
                            .map { if (it == null) "-" else "${it.x},${it.y}" }
                            .joinTo(StringBuilder(), " | ") { it.padStart(6) }
                        echo(line)
                    }
                }
            }
            .onFailure { echo("❌ Could not find a solution.") }
    }
}

fun main(args: Array<String>) = Run().main(args)