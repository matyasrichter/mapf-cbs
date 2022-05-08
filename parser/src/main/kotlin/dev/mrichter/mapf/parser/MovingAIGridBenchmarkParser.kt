package dev.mrichter.mapf.parser

import dev.mrichter.mapf.graph.Agent
import dev.mrichter.mapf.graph.Coordinates
import java.io.BufferedReader
import java.util.*
import java.util.regex.Pattern

class MovingAIGridBenchmarkParser(data: BufferedReader) : InputParser<BufferedReader, Coordinates>(data) {
    override fun parse(): Result<List<Agent<Coordinates>>> {
        if (data.readLine().trim() != "version 1")
            return Result.failure(ParseError("Invalid version"))
        val whitespace = Pattern.compile("\\s+")
        return Result.runCatching {
            data.lineSequence().map { line ->
                val fields = line.split(whitespace)
                if (fields.size != 9)
                    throw ParseError("Invalid line")
                Agent(
                    UUID.randomUUID(),
                    Coordinates(fields[4].toInt(), fields[5].toInt()),
                    Coordinates(fields[6].toInt(), fields[7].toInt()),
                )
            }.toList()
        }.fold(
            onSuccess = { Result.success(it) },
            onFailure = { Result.failure(ParseError("Invalid benchmark line")) }
        )
    }
}