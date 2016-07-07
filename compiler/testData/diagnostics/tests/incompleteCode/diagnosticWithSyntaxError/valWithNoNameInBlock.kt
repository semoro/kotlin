// !DIAGNOSTICS: -UNUSED_PARAMETER -UNUSED_VARIABLE

fun println(x: String) {
}

fun run(block: () -> Unit) {}

val propertyNameOnTheNextLine = 1

fun foo() {
    val<!SYNTAX!><!>
    println("abc")

    val<!SYNTAX!><!>
    run {
        println("abc")
    }

    val<!SYNTAX!><!>
    if (1 == 1) {

    }

    val<!SYNTAX!><!>
    (1 + 2)

    val<!SYNTAX!><!>
    // Parsed as simple name expression
    propertyNameOnTheNextLine

    // Correct properties
    val
    property1 = 1

    val
    propertyWithBy by <!UNRESOLVED_REFERENCE!>lazy<!> { 1 }

    val
    propertyWithType: Int

    val
    (a, b) = <!COMPONENT_FUNCTION_MISSING, COMPONENT_FUNCTION_MISSING!>1<!>
}
