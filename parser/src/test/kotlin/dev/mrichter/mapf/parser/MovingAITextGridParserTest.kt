package dev.mrichter.mapf.parser

import dev.mrichter.mapf.graph.GridGraph
import dev.mrichter.mapf.graph.TileType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class MovingAITextGridParserTest {
    @Test
    fun testParserOk() {
        val data = """
            type octile
            height 4
            width 4
            map
            ..@@
            @.@.
            ....
            @@@@
        """.trimIndent()
        val expected = GridGraph(
            arrayOf(
                arrayOf(TileType.EMPTY, TileType.EMPTY, TileType.WALL, TileType.WALL),
                arrayOf(TileType.WALL, TileType.EMPTY, TileType.WALL, TileType.EMPTY),
                arrayOf(TileType.EMPTY, TileType.EMPTY, TileType.EMPTY, TileType.EMPTY),
                arrayOf(TileType.WALL, TileType.WALL, TileType.WALL, TileType.WALL)
            )
        )
        val parser = MovingAITextGridParser(data.reader().buffered())
        assertEquals(expected, parser.parse())
    }

    @Test
    fun testParserInvalidType() {
        val data = """
            type someothertype
            height 4
            width 4
            map
            ..@@
            @.@.
            ....
            @@@@
        """.trimIndent()
        val parser = MovingAITextGridParser(data.reader().buffered())
        assertFailsWith<ParseError> { parser.parse() }
    }
    @Test
    fun testParserInvalidHeightHeader() {
        val data = """
            type octile
            height a
            width 4
            map
            ..@@
            @.@.
            ....
            @@@@
        """.trimIndent()
        val parser = MovingAITextGridParser(data.reader().buffered())
        assertFailsWith<ParseError> { parser.parse() }
    }
    @Test
    fun testParserInvalidWidthHeader() {
        val data = """
            type octile
            height 4
            width b
            map
            ..@@
            @.@.
            ....
            @@@@
        """.trimIndent()
        val parser = MovingAITextGridParser(data.reader().buffered())
        assertFailsWith<ParseError> { parser.parse() }
    }
    @Test
    fun testParserMissingMapStart() {
        val data = """
            type octile
            height 4
            width 4
            ..@@
            @.@.
            ....
            @@@@
        """.trimIndent()
        val parser = MovingAITextGridParser(data.reader().buffered())
        assertFailsWith<ParseError> { parser.parse() }
    }
    @Test
    fun testParserInvalidTile() {
        val data = """
            type octile
            height 4
            width 4
            map
            ..@@
            @+@.
            ....
            @@@@
        """.trimIndent()
        val parser = MovingAITextGridParser(data.reader().buffered())
        assertFailsWith<ParseError> { parser.parse() }
    }
    @Test
    fun testParserInvalidLineWidth() {
        val data = """
            type octile
            height 4
            width 4
            map
            ..@@
            @.@
            ....
            @@@@
        """.trimIndent()
        val parser = MovingAITextGridParser(data.reader().buffered())
        assertFailsWith<ParseError> { parser.parse() }
    }
    @Test
    fun testParserNotEnoughLines() {
        val data = """
            type octile
            height 4
            width 4
            map
            ..@@
            @.@.
            ....
        """.trimIndent()
        val parser = MovingAITextGridParser(data.reader().buffered())
        assertFailsWith<ParseError> { parser.parse() }
    }
    @Test
    fun testParserTooManyLines() {
        val data = """
            type octile
            height 4
            width 4
            map
            ..@@
            @.@.
            ....
            @@@@
            ....
        """.trimIndent()
        val parser = MovingAITextGridParser(data.reader().buffered())
        assertFailsWith<ParseError> { parser.parse() }
    }
}