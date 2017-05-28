@file:Suppress("unused")

package com.jsonlogic

import com.github.salomonbrys.kotson.*
import com.google.gson.*
import jdk.nashorn.api.scripting.AbstractJSObject
import jdk.nashorn.api.scripting.JSObject
import jdk.nashorn.api.scripting.ScriptObjectMirror
import jdk.nashorn.api.scripting.ScriptUtils
import java.util.function.Function

/**
 * This is a fall back if the wrapped objects aren't working for a JsonElement
 */
fun JsonElement.convertWithJSON() = JSON.parse(gson.toJson(this))

fun Any?.unwrap(): Any? = when (this) {
    is ScriptObjectMirror -> when {
        this.isArray -> this.values
        else -> ScriptUtils.unwrap(this)
    }
    is JSObjectJsonObjectWrapper -> this.data
    is JSObjectJsonArrayWrapper -> this.data
    else -> this
}

fun JsonElement?.wrap(): Any? = when (this) {
    is JsonPrimitive -> this.wrap()
    is JsonArray -> this.wrap()
    is JsonObject -> this.wrap()
    is JsonNull -> this.wrap()
    else -> {
        if (this == null) null
        else throw UnsupportedOperationException("$this ${this.javaClass} is an unknown JsonElement type.")
    }
}

fun JsonPrimitive?.wrap(): Any? =
        if (this == null) null
        else if (this.isBoolean) this.bool
        else if (this.isNumber) this.number
        else this.string

fun JsonNull?.wrap(): Nothing? = null
fun JsonArray?.wrap(): JSObject? = this?.let { JSObjectJsonArrayWrapper(it) }
fun JsonObject?.wrap(): JSObject? = this?.let { JSObjectJsonObjectWrapper(it) }

private val gson = Gson()

class JSObjectJsonObjectWrapper(val data: JsonObject) : AbstractJSObject() {
    override fun setMember(name: String, value: Any?) {
        data[name] = gson.toJsonTree(value)
    }

    override fun getMember(name: String): Any? = data[name].wrap()

    override fun removeMember(name: String) {
        data.remove(name)
    }

    override fun keySet(): MutableSet<String> = data.keys().toMutableSet()

    override fun hasMember(name: String): Boolean = data.has(name)

}

class JSObjectFuncWrapper(val data: Function<Any?, Any?>) : AbstractJSObject() {
    override fun call(thiz: Any?, vararg args: Any?): Any? = data.apply(args)

    override fun isFunction(): Boolean = true

}

class JSObjectJsonArrayWrapper(val data: JsonArray) : AbstractJSObject() {
    override fun getSlot(index: Int): Any? = data[index].wrap()

    override fun hasSlot(slot: Int): Boolean = data[slot] != null

    override fun values(): MutableCollection<Any?> = data.map { it.wrap() }.toMutableList()

    override fun isArray(): Boolean = true

    override fun setSlot(index: Int, value: Any?) {
        data[index] = gson.toJsonTree(value)
    }

    override fun getDefaultValue(hint: Class<*>?): Any {
        println("${this::class}.getDefaultValue($hint) called.")
        return super.getDefaultValue(hint)
    }

    override fun setMember(name: String?, value: Any?) {
        throw UnsupportedOperationException("setMember($name, $value)")
    }

    override fun getMember(name: String?): Any? {
        if (name == "map") {
            return Function { callback: JSObject ->
                val newData = data.deepCopy()
                newData.map {
                    callback.call(null, it.wrap())
                }
                JSObjectJsonArrayWrapper(newData)
            }
        } else throw UnsupportedOperationException("getMember($name)")
    }

    override fun removeMember(name: String?) {
        throw UnsupportedOperationException("removeMember($name)")
    }

    override fun isInstance(instance: Any?): Boolean {
        throw UnsupportedOperationException("isInstance($instance)")
    }

    override fun keySet(): MutableSet<String> {
        throw UnsupportedOperationException("keySet()")
    }

    override fun hasMember(name: String?): Boolean {
        throw UnsupportedOperationException("hasMember($name)")
    }
}