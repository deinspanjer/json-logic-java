package com.jsonlogic

import jdk.nashorn.api.scripting.JSObject

interface JsonTransformer {
    fun parse(input: String): JSObject
    @Suppress("unused")
    fun stringify(input: Any?): Any
}