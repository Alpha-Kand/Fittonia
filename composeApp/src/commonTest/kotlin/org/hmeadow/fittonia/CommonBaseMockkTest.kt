package org.hmeadow.fittonia

import BaseMockkTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach

abstract class CommonBaseMockkTest : BaseMockkTest() {

    @BeforeEach
    override fun beforeEachBaseMockk() {
        super.beforeEachBaseMockk()
    }

    @AfterEach
    override fun afterEachBaseMockk() {
        super.afterEachBaseMockk()
    }

    inline fun <reified T> T.printTest(prefix: String = ""): T {
        if (prefix.isNotEmpty()) {
            print(prefix)
        }
        println(this)
        return this
    }
}
