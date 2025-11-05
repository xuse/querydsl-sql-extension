package io.github.xuse.querydsl

import org.junit.jupiter.api.Test

class Hello {
    @Test
    fun main() {
        println("hello"+sum(1,2))
    }
    // 函数体只有一行代码:
    fun sum(a: Int, b: Int): Int = a + b
}