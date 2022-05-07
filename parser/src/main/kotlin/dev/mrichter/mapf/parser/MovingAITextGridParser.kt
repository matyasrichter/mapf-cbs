package dev.mrichter.mapf.parser

import dev.mrichter.mapf.graph.GridGraph
import dev.mrichter.mapf.graph.TileType
import java.io.BufferedReader
import java.io.IOException

class MovingAITextGridParser(data: BufferedReader) : Parser<BufferedReader, GridGraph>(data) {
    override fun parse(): GridGraph {
        val (expWidth, expHeight) = parseHeader()
        return GridGraph(parseMap(expWidth, expHeight))
    }

    /**
     * Process the header, returning a (width x height) pair
     */
    private fun parseHeader(): Pair<Int, Int> {
        try {

            if (data.readLine().trim() != "type octile") throw ParseError("Invalid grid type")
            val expHeight: Int
            val expWidth: Int
            try {
                expHeight = data.readLine().trim().removePrefix("height ").toInt()
                expWidth = data.readLine().trim().removePrefix("width ").toInt()
            } catch (e: NumberFormatException) {
                throw ParseError("Invalid grid dimensions header")
            }
            if (data.readLine().trim() != "map") throw ParseError("Missing map start")
            return Pair(expWidth, expHeight)
        } catch (e: IOException) {
            // iterator exhausted
            throw ParseError("Unexpected end-of-file when parsing grid header.")
        }
    }

    private fun parseMap(expWidth: Int, expHeight: Int): Array<Array<TileType>> {
        try {
            val lines = Array(size = expHeight) { Array(expWidth) { TileType.EMPTY } }
            var lineCount = 0
            data.forEachLine { line ->
                if (lineCount >= expHeight) throw ParseError("Invalid grid dimensions: too many lines (expected $expHeight)")
                lines[lineCount] = line.trim().map { char ->
                    when (char) {
                        '.' -> TileType.EMPTY
                        'T' -> TileType.WALL
                        '@' -> TileType.WALL
                        else -> throw ParseError("Invalid tile")
                    }
                }.toTypedArray()
                if (lines[lineCount].size != expWidth) throw ParseError("Invalid grid dimensions (line $lineCount length must be $expWidth)")
                ++lineCount
            }
            if (lineCount < expHeight) throw ParseError("Invalid grid dimensions: not enough lines (expected $expHeight)")
            return lines
        } catch (e: IOException) {
            // iterator exhausted
            throw ParseError("Unexpected end-of-file. Map height should be $expHeight")
        }
    }
}