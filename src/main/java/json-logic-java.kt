import com.google.gson.Gson
import com.google.gson.JsonElement
import javaJsonLogic.gson
import javaJsonLogic.jsonTransformer
import jdk.nashorn.api.scripting.NashornScriptEngineFactory
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

    val jsonLogic: JsonLogic = run {
        jsEngine.eval(javaClass.getResourceAsStream("json-logic-js/logic.js").reader())
        js.getInterface(jsEngine.eval("jsonLogic"), JsonLogic::class.java)
    }
    val jsonTransformer: JSON = js.getInterface(jsEngine.eval("JSON"), JSON::class.java)

    fun apply(logic: JsonElement, data: JsonElement?): JsonElement {
        return gson.toJsonTree(jsonLogic.apply(logic.unwrap()!!, data?.unwrap()))
    }
}

fun JsonElement?.unwrap(): Any? {
    when {
        this == null || this.isJsonNull -> return null
        else -> return jsonTransformer.parse(gson.toJson(this))
    }
}
