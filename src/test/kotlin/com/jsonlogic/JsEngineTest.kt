package com.jsonlogic

import jdk.nashorn.api.scripting.JSObject
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import java.util.function.Predicate

internal class JsEngineTest {
    @BeforeEach
    fun setUp() {
    }

    @Test
    fun eval() {
        assertEquals(1, JsEngine.eval("(function(){return 1})()"))
    }

    @Test
    fun putAndGet() {
        JsEngine.put("test", true)
        assertNotNull(JsEngine.get("test"))
    }

    interface TestConcat {
        fun testConcat(a: String, b: String): String
    }

    interface Console {
        fun log(vararg args: Any?)
        fun warn(vararg args: Any?)
        fun error(vararg args: Any?)
    }

    @Test
    fun getInterface() {
        JsEngine.eval("function testConcat(a,b){return ''+a+' and '+b}")
        val testConcat = JsEngine.getInterface(TestConcat::class.java)
        assertEquals("foo and bar", testConcat.testConcat("foo", "bar"), "getInterface using global function failed.")

        val console = JsEngine.getInterface(JsEngine.eval("console"), Console::class.java)
        console.log("log", 1, true)
        console.warn("warn", arrayOf(1, 2, 3))
        console.error("error", null)
    }

    @Test
    fun invokeFunction() {
        JsEngine.eval("function testInvokeFunction(obj,propName,propVal){obj[propName] = propVal; return obj;}")
        val result = JsEngine.invokeFunction("testInvokeFunction", JSON.parse("""{"a":"aaa"}"""), "b", "bbb") as JSObject
        assertTrue(result.getMember("a") == "aaa", "obj['a'] != 'aaa'")
        assertTrue(result.getMember("b") == "bbb", "obj['b'] != 'bbb'")
    }

    @Test
    fun invokeMethod() {
        val dateCls = JsEngine.eval("Date") as JSObject
        val dateNow = JsEngine.eval("new Date()") as JSObject
        val date1 = JsEngine.invokeMethod(dateCls, "now") as Long
        val date2 = JsEngine.invokeMethod(dateNow, "setFullYear", 2000) as Double
        assert(date1 > date2, { "Manipulation of date object didn't work: date1: $date1; date2: $date2" })
    }

}