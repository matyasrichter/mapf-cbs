package dev.mrichter.mapf.parser

import dev.mrichter.mapf.graph.Graph


abstract class Parser<InputType, out GraphType>(protected val data: InputType)
        where GraphType : Graph<*, *> {
    @Throws(ParseError::class)
    abstract fun parse(): GraphType
}