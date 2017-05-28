package com.jsonlogic

import com.github.salomonbrys.kotson.*
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import jdk.nashorn.api.scripting.ScriptObjectMirror
import jdk.nashorn.api.scripting.ScriptUtils
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class JavaJsonLogicTest {
    @Test
    fun simpleApplyJEJO() {
        val gson = Gson()
        val logic = gson.fromJson<JsonElement>(""" [true] """)
        val data = gson.fromJson<JsonObject>(""" {} """)
        val result = JavaJsonLogic.apply(logic, data)
        assertEquals(jsonArray(true), result)
    }
    @Test
    fun simpleApplyStringAny() {
        val gson = Gson()
        val logic = """ [true] """
        val data = gson.fromJson<JsonObject>(""" {} """)
        val result = JavaJsonLogic.apply(logic, data)
        assertEquals(listOf(true), result.unwrap())
    }
    @Test
    fun simpleApplyJEAny() {
        val gson = Gson()
        val logic = gson.fromJson<JsonElement>(""" [true] """)
        val data = object { val foo = "bar"}
        val result = JavaJsonLogic.apply(logic, data)
        assertEquals(jsonArray(true), result)
    }
}