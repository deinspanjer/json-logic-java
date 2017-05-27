package com.jsonlogic

import com.github.salomonbrys.kotson.*
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import org.junit.jupiter.api.Assertions.*

internal class JavaJsonLogicTest {
    @org.junit.jupiter.api.Test
    fun simpleApplyJEJO() {
        val gson = Gson()
        val logic = gson.fromJson<JsonElement>(""" [true] """)
        val data = gson.fromJson<JsonObject>(""" {} """)
        val result = JavaJsonLogic.apply(logic, data)
        print(result)
        assertEquals(jsonObject("0" to true), result)
    }
}