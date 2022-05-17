package dev.mrichter.mapf.parser

import dev.mrichter.mapf.graph.Coordinates
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MovingAIBenchmarkParserTest {
    @Test
    fun `Parses a valid benchmark`() {
        val data = """
            version 1
            11	Berlin_1_256.map	256	256	220	92	194	65	45.38477631
            4	Berlin_1_256.map	256	256	146	202	132	191	18.55634918
            33	Berlin_1_256.map	256	256	254	112	222	219	135.32590179
            42	Berlin_1_256.map	256	256	87	83	107	246	171.28427124
        """.trimIndent()
        val parser = MovingAIGridBenchmarkParser(data.reader().buffered())
        val benchmark = parser.parse()
        val expected = listOf(
            Pair(Coordinates(220, 92), Coordinates(194, 65)),
            Pair(Coordinates(146, 202), Coordinates(132, 191)),
            Pair(Coordinates(254, 112), Coordinates(222, 219)),
            Pair(Coordinates(87, 83), Coordinates(107, 246)),
        )
        assertTrue { benchmark.isSuccess }
        assertEquals(expected.size, benchmark.getOrNull()?.size)
        expected.zip(benchmark.getOrNull()!!).forEach { (expected, actual) ->
            assertEquals(expected.first, actual.start)
            assertEquals(expected.second, actual.target)
        }
    }

    @Test
    fun `Fails with missing header`() {
        val data = """
            11	Berlin_1_256.map	256	256	220	92	194	65	45.38477631
            4	Berlin_1_256.map	256	256	146	202	132	191	18.55634918
        """.trimIndent()
        val parser = MovingAIGridBenchmarkParser(data.reader().buffered())
        assertTrue { parser.parse().isFailure }
    }

    @Test
    fun `Fails with invalid header`() {
        val data = """
            version 2
            11	Berlin_1_256.map	256	256	220	92	194	65	45.38477631
            4	Berlin_1_256.map	256	256	146	202	132	191	18.55634918
        """.trimIndent()
        val parser = MovingAIGridBenchmarkParser(data.reader().buffered())
        assertTrue { parser.parse().isFailure }
    }

    @Test
    fun `Fails with invalid coordinates`() {
        val data = """
            version 1
            11	Berlin_1_256.map	256	256	aaa	92	194	65	45.38477631
            4	Berlin_1_256.map	256	256	146	202	132	191	18.55634918
        """.trimIndent()
        val parser = MovingAIGridBenchmarkParser(data.reader().buffered())
        assertTrue { parser.parse().isFailure }
    }

    @Test
    fun `Fails with missing fields`() {
        val data = """
            version 1
            Berlin_1_256.map	256	256	220	92	194	65	45.38477631
            4	Berlin_1_256.map	256	256	146	202	132	191	18.55634918
        """.trimIndent()
        val parser = MovingAIGridBenchmarkParser(data.reader().buffered())
        assertTrue { parser.parse().isFailure }
    }

    @Test
    fun `Succeeds with extra whitespace`() {
        val data = """
            version 1
            11	Berlin_1_256.map	256	    256	220	92	194	65	45.38477631
            4	Berlin_1_256.map	256	256	146     202	132	191	18.55634918
        """.trimIndent()
        val parser = MovingAIGridBenchmarkParser(data.reader().buffered())
        assertTrue { parser.parse().isSuccess }
    }
}