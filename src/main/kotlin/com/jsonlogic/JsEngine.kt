@file:Suppress("unused")

package com.jsonlogic

import jdk.nashorn.api.scripting.NashornScriptEngineFactory
import java.io.InputStream
import java.io.SequenceInputStream
import java.util.*
import javax.script.Invocable
import javax.script.ScriptContext
import javax.script.ScriptEngine

private val engine: ScriptEngine = NashornScriptEngineFactory().getScriptEngine("-strict=true", "--optimistic-types=true", "--language=es6", "-timezone=UTC")
private val js: Invocable = engine as Invocable

object JsEngine : ScriptEngine by engine, Invocable by js {
    val GLOBAL_SCOPE = ScriptContext.GLOBAL_SCOPE
    val ENGINE_SCOPE = ScriptContext.ENGINE_SCOPE

    init {
        engine.eval(
                SequenceInputStream(Collections.enumeration(listOf<InputStream>(
                        javaClass.getResourceAsStream("/nashorn-polyfill.js"),
                        "\n//# sourceURL=src/main/resources/nashorn-polyfill.js".byteInputStream()
                ))).reader())
    }
}

