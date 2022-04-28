package dev.mrichter.mapf.graph

enum class TileType {
    EMPTY,
    WALL,
}

fun TileType.isAccessible(): Boolean = when (this) {
    TileType.EMPTY -> true
    TileType.WALL -> false
}

data class Coordinates(val x: Int, val y: Int) {
    operator fun plus(offset: Coordinates) = Coordinates(x + offset.x, y + offset.y)
}

class GridGraph(private val tiles: Array<Array<TileType>>) : Graph<Coordinates, TileType> {
    init {
        check(tiles.isNotEmpty())
        check(tiles[0].isNotEmpty())
        // all rows have the same length
        check(tiles.all { it.size == tiles[0].size })
    }

    private val offsets =
        listOf(Coordinates(1, 0), Coordinates(0, 1), Coordinates(-1, 0), Coordinates(0, -1))

    override fun at(coordinates: Coordinates): TileType {
        if (coordinates.x < 0 || coordinates.y < 0 || coordinates.y > tiles.size || coordinates.x > tiles[0].size)
            return TileType.WALL
        return tiles[coordinates.y][coordinates.x]
    }

    override fun neighbours(coordinates: Coordinates): List<Coordinates> =
        offsets.map { offset -> coordinates + offset }.filter { c -> at(c).isAccessible() }


    override fun toString() = "Maze(tiles=${tiles[0].size}x${tiles.size})"
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GridGraph

        return tiles.contentDeepEquals(other.tiles)
    }

    override fun hashCode(): Int {
        return tiles.contentDeepHashCode()
    }
}
