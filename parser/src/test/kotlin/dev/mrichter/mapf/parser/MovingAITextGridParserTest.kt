package dev.mrichter.mapf.parser

import dev.mrichter.mapf.graph.GridGraph
import dev.mrichter.mapf.graph.TileType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class MovingAITextGridParserTest {
    @Test
    fun `Parses a valid map`() {
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
    fun `Fails for invalid map type`() {
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
    fun `Fails for invalid height header value`() {
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
    fun `Fails for invalid width header value`() {
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
    fun `Fails when map start is missing`() {
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
    fun `Fails for invalid tile type`() {
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
    fun `Fails when not all lines are of the same width`() {
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
    fun `Fails when height header and actual height differ (less lines)`() {
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
    fun `Fails when height header and actual height differ (more lines)`() {
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