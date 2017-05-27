package com.jsonlogic

interface JsonTransformer {
    fun parse(input: String): Any
    fun stringify(input: Any?): String
}

private val jsonTransformer = JsEngine.getInterface(JsEngine.eval("JSON"), JsonTransformer::class.java)
object JSON: JsonTransformer by jsonTransformer
