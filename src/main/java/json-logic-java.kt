import com.github.salomonbrys.kotson.jsonArray
import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.toJson
import com.google.gson.Gson
import com.google.gson.JsonElement
import javaJsonLogic.global
import javaJsonLogic.gson
import javaJsonLogic.jsonTransformer
import jdk.nashorn.api.scripting.NashornScriptEngineFactory
import jdk.nashorn.api.scripting.ScriptObjectMirror
import javax.script.Invocable
import javax.script.ScriptEngine

interface JsonLogic {
    fun apply(logic: Any, data: Any?): Any
}

interface JSON {
    fun parse(input: String): Any
    @Suppress("unused")
    fun stringify(input: Any): Any
}

object javaJsonLogic {
    internal val gson = Gson()
    private val jsEngine: ScriptEngine = NashornScriptEngineFactory().scriptEngine
    val js: Invocable = jsEngine as Invocable
    internal val global = jsEngine.context

    val jsonLogic: JsonLogic = run {
        jsEngine.eval(javaClass.getResourceAsStream("json-logic-js/logic.js").reader())
        js.getInterface(jsEngine.eval("jsonLogic"), JsonLogic::class.java)
    }
    val jsonTransformer: JSON = js.getInterface(jsEngine.eval("JSON"), JSON::class.java)

    fun apply(logic: JsonElement, data: JsonElement?): JsonElement {
        return gson.toJsonTree(jsonLogic.apply(logic.unwrap(), data?.unwrap()))
    }
}

fun JsonElement?.unwrap(): Any {
    when {
        this == null || this.isJsonNull -> return ScriptObjectMirror.wrapAsJSONCompatible(null, global)
        else -> {
            val input = gson.toJson(this)
            val parse = jsonTransformer.parse(input)
            return parse
        }
    }
}

fun main(args: Array<String>) {
    arrayOf(
            Pair(jsonArray(1, 2, true, false, "red", "blue", jsonObject("var" to "foo")), jsonObject("foo" to "bar"))
            , Pair(jsonArray(1, 2, true, false, "red", "blue", jsonObject("var" to "foo")),null)
            , Pair(jsonObject("var" to "b"), jsonObject("a" to 1, "b" to 2))
            , Pair(1.toJson(), null)
            , Pair(true.toJson(), null)
            , Pair(false.toJson(), null)
            , Pair("Hi".toJson(), null)

    ).forEach { println(javaJsonLogic.apply(it.first, it.second)) }
}
