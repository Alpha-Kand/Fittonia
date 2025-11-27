package org.hmeadow.fittonia

import BaseMockkTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach

abstract class AndroidBaseMockkTest : BaseMockkTest() {

    @BeforeEach
    override fun beforeEachBaseMockk() {
        super.beforeEachBaseMockk()
    }

    @AfterEach
    override fun afterEachBaseMockk() {
        super.afterEachBaseMockk()
    }
}
