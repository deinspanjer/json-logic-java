@file:Suppress("unused")

package com.jsonlogic

import com.github.salomonbrys.kotson.bool
import com.github.salomonbrys.kotson.number
import com.github.salomonbrys.kotson.string
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import jdk.nashorn.api.scripting.JSObject
import jdk.nashorn.api.scripting.NashornScriptEngineFactory
import javax.script.Invocable
import javax.script.ScriptEngine

private val gson = Gson()

object JavaJsonLogic {
    interface JsonLogic {
        fun apply(logic: Any, data: Any?): Any
        fun add_operation(operatorName: String, operatorDefinition: Any)
    }

    private val jsEngine: ScriptEngine = NashornScriptEngineFactory()
            .getScriptEngine("-strict=true", "--optimistic-types=true", "--language=es6", "-timezone=UTC")

    private val js: Invocable = jsEngine as Invocable

    val jsonLogic: JsonLogic = run {
        jsEngine.eval(javaClass.getResourceAsStream("/json-logic-js/logic.js").reader())
        jsEngine.eval(javaClass.getResourceAsStream("/nashorn-polyfill.js").reader())
        js.getInterface(jsEngine.eval("jsonLogic"), JsonLogic::class.java)
    }
    val JSON: JsonTransformer = js.getInterface(jsEngine.eval("JSON"), JsonTransformer::class.java)

    fun apply(logicStr: String, data: Any?): JsonElement {
        return gson.toJsonTree(jsonLogic.apply(JSON.parse(logicStr), data))
    }

    fun apply(logic: JsonElement, data: JsonElement?): JsonElement {
        return gson.toJsonTree(jsonLogic.apply(logic.wrap()!!, data.wrap()))
    }

    fun apply(logic: JsonElement, data: Any?): JsonElement {
        return gson.toJsonTree(jsonLogic.apply(logic.wrap()!!, data))
    }

    fun <I> getInterface(definition: String, ifaceClass: Class<I>): I {
        val encapsulated = """(function() { return $definition; }())"""
        return js.getInterface(jsEngine.eval(encapsulated), ifaceClass)
    }

            // XXX Almost got this version working, but need a bit more magic.
            // Maybe here? http://stackoverflow.com/questions/31459192/java-nashorn-store-function
//    fun add_operation(operatorName: String, operatorDefinition: Any) {
//        jsonLogic.add_operation(operatorName, operatorDefinition)
//    }
    fun addOperation(operatorName: String, operatorDefinition: String) {
        jsEngine.eval("""jsonLogic.add_operation("$operatorName", $operatorDefinition);""")
    }

    /**
     * This is a fall back if the wrapped objects aren't working for a JsonElement
     */
    fun JsonElement.bruteForceConvert() : JSObject = JSON.parse(gson.toJson(this))
}