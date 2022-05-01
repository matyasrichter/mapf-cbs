package dev.mrichter.mapf.solver

import java.util.*

class ConstraintTable<CoordinatesType> {
    private val vertexConstraints = mutableMapOf<Pair<CoordinatesType, Int>, UUID>()
    private val finalConstraints = mutableMapOf<CoordinatesType, Pair<Int, UUID>>()

    fun addPath(path: Iterable<CoordinatesType>, agentId: UUID) {
        var maxIndex = 0;
        for ((index, coordinate) in path.withIndex()) {
            vertexConstraints[Pair(coordinate, index)] = agentId
            maxIndex++;
        }
        finalConstraints[path.last()] = Pair(maxIndex, agentId)
    }

    fun canMoveTo(coordinate: CoordinatesType, timeStep: Int, selfId: UUID): Boolean {
        val pair = Pair(coordinate, timeStep)
        if (vertexConstraints.containsKey(pair) && vertexConstraints[pair] != selfId)
            return false
        if (finalConstraints.containsKey(coordinate) && finalConstraints[coordinate]!!.first <= timeStep && finalConstraints[coordinate]!!.second != selfId)
            return false
        return true
    }
}