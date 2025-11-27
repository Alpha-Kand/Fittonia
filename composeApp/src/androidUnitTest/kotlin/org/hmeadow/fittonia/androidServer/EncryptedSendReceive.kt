package org.hmeadow.fittonia.androidServer

import UnitTest
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import org.hmeadow.fittonia.AndroidBaseMockkTest
import org.hmeadow.fittonia.PuPrKeyCipher
import org.hmeadow.fittonia.PuPrKeyCipher.ENCRYPT_MAX_BYTES_ALLOWED
import org.hmeadow.fittonia.TestInputStream
import org.hmeadow.fittonia.TestOutputStream
import org.hmeadow.fittonia.TestSocket
import org.hmeadow.fittonia.hmeadowSocket.HMeadowSocketClient
import org.hmeadow.fittonia.hmeadowSocket.HMeadowSocketHandler
import org.hmeadow.fittonia.utility.byteArray
import org.hmeadow.fittonia.utility.toJSONByteArray
import org.junit.jupiter.api.Assertions
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.Socket
import kotlin.math.min
import kotlin.test.assertEquals

internal class EncryptedSendReceive : AndroidBaseMockkTest() {

    open class TestSocketHandlerBase : HMeadowSocketHandler() {
        override fun bindToSocket(block: () -> Socket): Socket {
            mDataInput = DataInputStream(TestInputStream())
            mDataOutput = DataOutputStream(TestOutputStream())
            return TestSocket()
        }
    }

    private val lorem = "Repudiandae aliquid nisi nihil laborum iste. Eius sunt quia animi. Quia illum sed non " +
        "exercitationem aspernatur laborum ratione omnis. Dolores harum maiores a dolorem. Incidunt officia " +
        "error et aut eaque. Ab voluptatem nisi beatae. Ea voluptas libero est ut illo amet optio. Praesentium " +
        "voluptate blanditiis illum possimus et. Culpa voluptatum provident quod. Maxime voluptatem numquam est. " +
        "Est ad alias laudantium est id necessitatibus facilis enim. Nulla illo quia neque qui. Dolorem " +
        "repudiandae quia ea atque non. Vel autem rerum nulla praesentium est qui id ad. Repellendus quis in " +
        "quas est dicta accusamus magni labore. Quidem quo sequi dicta dolores reprehenderit molestiae. Minima " +
        "sit cupiditate corrupti ut. Ex harum perspiciatis cumque ut perferendis. Ut praesentium eum et ratione. " +
        "Debitis et alias harum aut sit odit voluptatem ut. Molestiae facilis laborum voluptatem beatae. Quos " +
        "perspiciatis quia quos debitis corrupti quas repellat. Qui aut quia ut. Numquam beatae optio soluta rem " +
        "sunt."

    @UnitTest
    fun encryptAndSendTest() {
        val loremSize = lorem.toJSONByteArray.size
        val mockkTestSocketHandlerBase = mockk<TestSocketHandlerBase>(relaxed = true)
        every { mockkTestSocketHandlerBase.sendByteArrayRaw(any()) } just Runs

        val expectedOutput = mutableListOf<ByteArray>().apply {
            repeat(5) {
                add(
                    lorem
                        .toJSONByteArray
                        .sliceArray(
                            it * ENCRYPT_MAX_BYTES_ALLOWED..<min(
                                (it + 1) * ENCRYPT_MAX_BYTES_ALLOWED,
                                loremSize,
                            ),
                        ),
                )
            }
            add(loremSize.byteArray)
        }

        mockkObject(PuPrKeyCipher)
        every { PuPrKeyCipher.encrypt(any(), any()) } answers {
            val input = it.invocation.args.first() as ByteArray
            Assertions.assertArrayEquals(expectedOutput.first(), it.invocation.args.first() as ByteArray)
            expectedOutput.removeFirst()
            input
        }

        val theirKey = PuPrKeyCipher.HMPublicKey(ByteArray(0))
        val testClient = HMeadowSocketClient(
            port = 1234,
            ipAddress = "",
            socketInterface = mockkTestSocketHandlerBase,
        )

        testClient.encryptAndSend(
            data = lorem,
            theirPublicKey = theirKey,
        )
    }

    @UnitTest
    fun receiveAndDecryptTest() {
        mockkObject(PuPrKeyCipher)
        every { PuPrKeyCipher.decrypt(any()) } answers { it.invocation.args.first() as ByteArray }
        val incomingData = mutableListOf(
            (256 * 4).byteArray,
            lorem.toJSONByteArray,
        )

        val testServer = HMeadowSocketClient(
            ipAddress = "",
            port = 1234,
            socketInterface = object : TestSocketHandlerBase() {
                override fun receiveByteArrayRaw(size: Int): ByteArray {
                    return incomingData.first().also {
                        incomingData.removeFirst()
                    }
                }
            },
        )
        assertEquals(lorem, testServer.receiveAndDecrypt<String>())
    }
}
