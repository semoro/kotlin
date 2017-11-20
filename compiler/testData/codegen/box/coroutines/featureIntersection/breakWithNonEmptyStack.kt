// WITH_RUNTIME
// WITH_COROUTINES
// IGNORE_BACKEND: NATIVE
import helpers.*
import kotlin.coroutines.experimental.*
import kotlin.coroutines.experimental.intrinsics.*

class A {
    var result = mutableListOf("O", "K", null)
    suspend fun foo(): String? = suspendCoroutineOrReturn { x ->
        x.resume(result.removeAt(0))
        COROUTINE_SUSPENDED
    }
}

var result = ""

suspend fun append(ignore: String, x: String) {
    result += x
}

fun builder(c: suspend () -> Unit) {
    c.startCoroutine(EmptyContinuation)
}

suspend fun bar() {
    val a = A()
    while (true) {
        append("ignore", a.foo() ?: break)
    }
}

fun box(): String {
    builder {
        bar()
    }

    return result
}

