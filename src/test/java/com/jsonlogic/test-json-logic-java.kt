package com.jsonlogic

import com.github.salomonbrys.kotson.jsonArray
import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.toJson
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonNull
import com.google.gson.JsonParser
import io.kotlintest.matchers.shouldBe
import io.kotlintest.properties.*
import io.kotlintest.specs.StringSpec

fun <A : JsonElement, B : JsonElement?, C : JsonElement?> mytable(headers: Headers3, vararg rows: Row3<A, B, C>) = Table3(headers, rows.asList())
fun <A : JsonElement, B : JsonElement?, C : JsonElement?> myrow(a: A, b: B, c: C) = Row3(a as JsonElement, b as JsonElement?, c as JsonElement?)

class TestCasesFromJsonLogicJs : StringSpec() {
    val gson = Gson()
    val parser = JsonParser()
    fun String.parseJSON(): JsonElement = parser.parse(this)

    interface TestFunc {
        fun helloWorld(name: String): String
    }

    interface Adder {
        fun plus(a: Int, b: Int): Int
    }

    init {
        "should return" {
            val testCases = mytable(
                    headers("logic", "data", "result")
                    , myrow(jsonArray(1, 2, true, false, "red", "blue", jsonObject("var" to "foo")), jsonObject("foo" to "bar"),
                    """{"0":1,"1":2,"2":true,"3":false,"4":"red","5":"blue","6":"bar"}""".parseJSON())
                    , myrow(jsonArray(1, 2, true, false, "red", "blue", jsonObject("var" to "foo")), null,
                    """{"0":1,"1":2,"2":true,"3":false,"4":"red","5":"blue"}""".parseJSON())
                    , myrow(jsonObject("var" to "b"), jsonObject("a" to 1, "b" to 2), 2.toJson())
                    , myrow(1.toJson(), null, 1.toJson())
                    , myrow(true.toJson(), null, true.toJson())
                    , myrow(false.toJson(), null, false.toJson())
                    , myrow("Hi".toJson(), null, "Hi".toJson())
                    , myrow("""{ "==" : [1, 1] }""".parseJSON(), JsonNull.INSTANCE, true.toJson())
                    , myrow("""{ "var" : ["a"] }""".parseJSON(), """{ a : 1, b : 2 }""".parseJSON(), 1.toJson())
                    , myrow("""{ "var" : "a" }""".parseJSON(), """{ a : 1, b : 2 }""".parseJSON(), 1.toJson())
                    , myrow("""{"var" : 1 }""".parseJSON(), """[ "apple", "banana", "carrot" ]""".parseJSON(), "banana".toJson())
                    , myrow("""
                            { "and" : [
                                {"<" : [ { "var" : "temp" }, 110 ]},
                                {"==" : [ { "var" : "pie.filling" }, "apple" ] }
                              ] }
                          """.parseJSON(), """
                            { "temp" : 100, "pie" : { "filling" : "apple" } }
                          """.parseJSON(), true.toJson())
                    , myrow(true.toJson(), JsonNull.INSTANCE, true.toJson())
                    , myrow(false.toJson(), JsonNull.INSTANCE, false.toJson())
                    , myrow("""{"and" : [ { ">" : [3,1] }, { "<" : [1,3] } ] }""".parseJSON(), JsonNull.INSTANCE, true.toJson())
                    , myrow("""{ "==": [ 2, { "+": [ 1, { "var": "one" } ] } ] }""".parseJSON(), """{ "one": 1 }""".parseJSON(), true.toJson())
            )

            forAll(testCases) { a, b, result -> JavaJsonLogic.apply(gson.toJsonTree(a), gson.toJsonTree(b)) shouldBe gson.toJsonTree(result) }
        }

        "can create a new interface" {
            val testFunc = JavaJsonLogic.getInterface("{ helloWorld: function(name) { return 'Hello '+name; } }", TestFunc::class.java)
            "Hello Daniel" shouldBe testFunc.helloWorld("Daniel")
        }

        "can create a new json-logic operator" {
            JavaJsonLogic.addOperation("minus", """function(a,b) { return a - b; }""")

            true.toJson() shouldBe JavaJsonLogic.apply("""{ "==": [ 1, { "minus": [ 2, { "var": "one" } ] } ] }""".parseJSON(), """{ "one": 1 }""".parseJSON())
        }

//        "can create a new json-logic operator2" {
//            JavaJsonLogic.add_operation("Adder", JavaJsonLogic.getInterface("""{ plus: function(a,b) { return a+b; } }""", Adder::class.java))
//
//            true.toJson() shouldBe JavaJsonLogic.apply("""{ "==": [ 2, { "Adder.plus": [ 1, { "var": "one" } ] } ] }""".parseJSON(), """{ "one": 1 }"""
//                    .parseJSON())
//        }
    }

}

/*
QUnit.test( "logging", function( assert ) {
    var last_console;
    console.log = function(logged) {
        last_console = logged;
    };
    assert.equal( jsonLogic.apply({"log": [1]}), 1 );
    assert.equal( last_console, 1 );
});

QUnit.test( "edge cases", function( assert ) {
    assert.equal( jsonLogic.apply(), undefined, "Called with no arguments" );
});

QUnit.test( "Expanding functionality with add_operator", function( assert) {
    // Operator is not yet defined
    assert.throws(
            function() {
                jsonLogic.apply({"add_to_a": []});
            },
            /Unrecognized operation/
    );

    // Set up some outside data, and build a basic function operator
    var a = 0;
    var add_to_a = function(b) {
        if(b === undefined) {
            b=1;
        } return a += b;
    };
    jsonLogic.add_operation("add_to_a", add_to_a);
    // New operation executes, returns desired result
    // No args
    assert.equal( jsonLogic.apply({"add_to_a": []}), 1 );
    // Unary syntactic sugar
    assert.equal( jsonLogic.apply({"add_to_a": 41}), 42 );
    // New operation had side effects.
    assert.equal(a, 42);

    var fives = {
        add: function(i) {
        return i + 5;
    },
        subtract: function(i) {
        return i - 5;
    },
    };

    jsonLogic.add_operation("fives", fives);
    assert.equal( jsonLogic.apply({"fives.add": 37}), 42 );
    assert.equal( jsonLogic.apply({"fives.subtract": [47]}), 42 );

    // Calling a method with multiple var as arguments.
    jsonLogic.add_operation("times", function(a, b) {
        return a*b;
    });
    assert.equal(
            jsonLogic.apply(
                    {"times": [{"var": "a"}, {"var": "b"}]},
                    {a: 6, b: 7}
            ),
            42
    );

    // Calling a method that takes an array, but the inside of the array has rules, too
    jsonLogic.add_operation("array_times", function(a) {
        return a[0]*a[1];
    });
    assert.equal(
            jsonLogic.apply(
                    {"array_times": [[{"var": "a"}, {"var": "b"}]]},
                    {a: 6, b: 7}
            ),
            42
    );
});

QUnit.test( "Expanding functionality with method", function( assert) {
    // Data contains a real object with methods and local state
    var a = {
        count: 0,
        increment: function() {
        return this.count += 1;
    },
        add: function(b) {
        return this.count += b;
    },
    };

    // Look up "a" in data, and run the increment method on it with no args.
    assert.equal(
            jsonLogic.apply(
                    {"method": [{"var": "a"}, "increment"]},
                    {"a": a}
            ),
            1 // Happy return value
    );
    assert.equal(a.count, 1); // Happy state change

    // Run the add method with an argument
    assert.equal(
            jsonLogic.apply(
                    {"method": [{"var": "a"}, "add", [41]]},
                    {"a": a}
            ),
            42 // Happy return value
    );
    assert.equal(a.count, 42); // Happy state change
});


QUnit.test("Control structures don't eval depth-first", function(assert) {
    // Depth-first recursion was wasteful but not harmful until we added custom operations that could have side-effects.

    // If operations run the condition, if truthy, it runs and returns that consequent.
    // Consequents of falsy conditions should not run.
    // After one truthy condition, no other condition should run
    var conditions = [];
    var consequents = [];
    jsonLogic.add_operation("push.if", function(v) {
        conditions.push(v); return v;
    });
    jsonLogic.add_operation("push.then", function(v) {
        consequents.push(v); return v;
    });
    jsonLogic.add_operation("push.else", function(v) {
        consequents.push(v); return v;
    });

    jsonLogic.apply({"if": [
        {"push.if": [true]},
        {"push.then": ["first"]},
        {"push.if": [false]},
        {"push.then": ["second"]},
        {"push.else": ["third"]},
        ]});
    assert.deepEqual(conditions, [true]);
    assert.deepEqual(consequents, ["first"]);

    conditions = [];
    consequents = [];
    jsonLogic.apply({"if": [
        {"push.if": [false]},
        {"push.then": ["first"]},
        {"push.if": [true]},
        {"push.then": ["second"]},
        {"push.else": ["third"]},
        ]});
    assert.deepEqual(conditions, [false, true]);
    assert.deepEqual(consequents, ["second"]);

    conditions = [];
    consequents = [];
    jsonLogic.apply({"if": [
        {"push.if": [false]},
        {"push.then": ["first"]},
        {"push.if": [false]},
        {"push.then": ["second"]},
        {"push.else": ["third"]},
        ]});
    assert.deepEqual(conditions, [false, false]);
    assert.deepEqual(consequents, ["third"]);


    jsonLogic.add_operation("push", function(arg) {
        i.push(arg); return arg;
    });
    var i = [];

    i = [];
    jsonLogic.apply({"and": [{"push": [false]}, {"push": [false]}]});
    assert.deepEqual(i, [false]);
    i = [];
    jsonLogic.apply({"and": [{"push": [false]}, {"push": [true]}]});
    assert.deepEqual(i, [false]);
    i = [];
    jsonLogic.apply({"and": [{"push": [true]}, {"push": [false]}]});
    assert.deepEqual(i, [true, false]);
    i = [];
    jsonLogic.apply({"and": [{"push": [true]}, {"push": [true]}]});
    assert.deepEqual(i, [true, true]);


    i = [];
    jsonLogic.apply({"or": [{"push": [false]}, {"push": [false]}]});
    assert.deepEqual(i, [false, false]);
    i = [];
    jsonLogic.apply({"or": [{"push": [false]}, {"push": [true]}]});
    assert.deepEqual(i, [false, true]);
    i = [];
    jsonLogic.apply({"or": [{"push": [true]}, {"push": [false]}]});
    assert.deepEqual(i, [true]);
    i = [];
    jsonLogic.apply({"or": [{"push": [true]}, {"push": [true]}]});
    assert.deepEqual(i, [true]);
});
*/
