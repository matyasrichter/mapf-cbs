package dev.mrichter.mapf.parser

import dev.mrichter.mapf.graph.Graph


abstract class MapParser<InputType, out GraphType>(protected val data: InputType)
        where GraphType : Graph<*, *> {

    abstract fun parse(): Result<GraphType>
}