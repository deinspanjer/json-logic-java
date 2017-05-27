package com.jsonlogic

import com.github.salomonbrys.kotson.*
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import org.junit.jupiter.api.Assertions.*

internal class JavaJsonLogicTest {
    @org.junit.jupiter.api.Test
    fun apply() {
        val logic = Gson().fromJson<JsonElement>("""
            [true]
          """)
        val data = Gson().fromJson<JsonObject>("""
            {
            }""")
        val result = JavaJsonLogic.apply(logic, data)
        assertEquals(jsonObject("0" to true), result)
    }

}