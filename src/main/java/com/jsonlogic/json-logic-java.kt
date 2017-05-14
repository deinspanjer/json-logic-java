package com.jsonlogic

import com.google.gson.Gson
import com.google.gson.JsonElement
import jdk.nashorn.api.scripting.NashornScriptEngineFactory
import javax.script.Invocable
import javax.script.ScriptEngine


interface JsonTransformer {
    fun parse(input: String): Any
    @Suppress("unused")
    fun stringify(input: Any): Any
}

object JavaJsonLogic {
    interface JsonLogic {
        fun apply(logic: Any, data: Any?): Any
        fun add_operation(operatorName: String, operatorDefinition: Any)
    }

    private val gson = Gson()
    private val jsEngine: ScriptEngine = NashornScriptEngineFactory()
            .getScriptEngine("-strict=true", "--optimistic-types=true", "--language=es6", "-timezone=UTC")

    private val js: Invocable = jsEngine as Invocable

    val jsonLogic: JsonLogic = run {
        jsEngine.eval(javaClass.getResourceAsStream("/json-logic-js/logic.js").reader())
        js.getInterface(jsEngine.eval("jsonLogic"), JsonLogic::class.java)
    }
    val JSON: JsonTransformer = js.getInterface(jsEngine.eval("JSON"), JsonTransformer::class.java)

    fun apply(logic: JsonElement, data: JsonElement?): JsonElement {
        return gson.toJsonTree(jsonLogic.apply(logic.convert()!!, data?.convert()))
    }

    @Suppress("unused")
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

    fun JsonElement?.convert(): Any? {
        when {
            this == null || this.isJsonNull -> return null
            else -> return JSON.parse(gson.toJson(this))
        }
    }
}
