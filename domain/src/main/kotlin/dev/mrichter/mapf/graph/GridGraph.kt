package dev.mrichter.mapf.graph

enum class TileType {
    EMPTY,
    WALL,
}

data class Coordinates(val x: Int, val y: Int)

class GridGraph(private val tiles: Array<Array<TileType>>) : Graph<Coordinates, TileType> {
    init {
        check(tiles.isNotEmpty())
        check(tiles[0].isNotEmpty())
        // all rows have the same length
        check(tiles.all { it.size == tiles[0].size })
    }

    override fun at(coordinates: Coordinates): TileType = tiles[coordinates.y][coordinates.x]

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
