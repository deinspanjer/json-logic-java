@file:Suppress("unused")

package com.jsonlogic

import com.google.gson.Gson
import com.google.gson.JsonElement
import jdk.nashorn.api.scripting.NashornScriptEngineFactory
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.io.SequenceInputStream
import java.io.StringReader
import java.util.*
import javax.script.Invocable
import javax.script.ScriptEngine

private val gson = Gson()

object JavaJsonLogic {
    interface JsonLogic {
        fun apply(logic: Any, data: Any?): Any
        fun add_operation(operatorName: String, operatorDefinition: Any)
    }

    val jsonLogic: JsonLogic = run {
        JsEngine.eval(
                SequenceInputStream(Collections.enumeration(listOf<InputStream>(
                        javaClass.getResourceAsStream("/json-logic-js/logic.js")
                        , "\n//# sourceURL=src/main/resources/json-logic-js/logic.js".byteInputStream()
                ))).reader())
        JsEngine.getInterface(JsEngine.eval("jsonLogic"), JsonLogic::class.java)
    }

    fun apply(logicStr: String, data: Any?): Any? {
        return jsonLogic.apply(JSON.parse(logicStr), data).unwrap()
    }

    fun apply(logic: JsonElement, data: JsonElement?): JsonElement {
        return gson.toJsonTree(jsonLogic.apply(logic.wrap()!!, data.wrap()).unwrap())
    }

    fun apply(logic: JsonElement, data: Any?): JsonElement {
        return gson.toJsonTree(jsonLogic.apply(logic.wrap()!!, data).unwrap())
    }

    fun addOperation(operatorName: String, operatorDefinition: String) {
        JsEngine.eval("""jsonLogic.add_operation("$operatorName", $operatorDefinition);""")
    }
}