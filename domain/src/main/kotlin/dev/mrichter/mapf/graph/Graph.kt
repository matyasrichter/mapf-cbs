package dev.mrichter.mapf.graph


interface Graph<CoordinatesType, VertexType> {
    fun at(coordinates: CoordinatesType): VertexType
    fun neighbours(coordinates: CoordinatesType): List<CoordinatesType>
}