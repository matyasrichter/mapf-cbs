package dev.mrichter.mapf.parser

import dev.mrichter.mapf.graph.Agent

abstract class InputParser<InputType, CoordinatesType>(protected val data: InputType) {
    abstract fun parse(): Result<List<Agent<CoordinatesType>>>
}