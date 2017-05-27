package com.jsonlogic

import com.google.gson.*
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    val jsonParser = JsonParser()

    println("Test JSON Logic by entering rules and data. A blank line ends input. 'quit' exits.")

    while (true) {
        println("\n\nInput the JSON Logic rule:")
        val jsonLogicInput = readInput()

        val parsedJsonLogic: JsonElement
        try {
            parsedJsonLogic = jsonParser.parse(jsonLogicInput)
        } catch(e: Exception) {
            println("The input did not parse as valid JSON: $e")
            continue
        }

        println("JSON Logic rule parsed as a ${getJsonType(parsedJsonLogic)}")
        println("Input the JSON or JavaScript data object:")
        val data = readInput()

        val parsedData = JsEngine.eval(data)

        println("JSON Logic result: ${JavaJsonLogic.apply(parsedJsonLogic, parsedData)}")
    }
}

private fun getJsonType(parsedJsonLogic: JsonElement): String {
    return when (parsedJsonLogic){
        is JsonObject -> "JSON Object"
        is JsonArray -> "JSON Array"
        is JsonNull -> "JSON Null"
        else -> {
            val prim = parsedJsonLogic.asJsonPrimitive
            return when {
                prim.isNumber -> "Primitive Number"
                prim.isBoolean -> "Primitive Boolean"
                prim.isString -> "Primitive String"
                else -> "Unknown!"
            }

        }
    }
}

private fun readInput(): String {
    val jsonLogicInput = StringBuilder()
    do {
        val line = readLine()
        if (line!!.toLowerCase() == "quit") exitProcess(0)
        jsonLogicInput.append(line)
    } while (line!!.isNotEmpty())
    return jsonLogicInput.toString()
}