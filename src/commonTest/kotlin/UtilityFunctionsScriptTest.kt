import io.mockk.mockk
import io.mockk.verify

private class UtilityFunctionsScriptTest : BaseSocketScriptTest() {

    private val onConfirm: () -> Boolean = mockk(relaxed = true)
    private val onDeny: () -> Boolean = mockk(relaxed = true)

    @UnitTest
    fun sendReceiveApprovalConfirm() = runSocketScriptTest2(
        setupBlock = {},
        {
            generateClient().receiveApproval(
                onConfirm = onConfirm,
                onDeny = onDeny,
            )
            verify(exactly = 1) { onConfirm() }
            verify(exactly = 0) { onDeny() }
        },
        {
            generateServer().sendApproval(choice = true)
        },
    )

    @UnitTest
    fun sendReceiveApprovalDeny() = runSocketScriptTest2(
        setupBlock = {},
        {
            generateClient().receiveApproval(
                onConfirm = onConfirm,
                onDeny = onDeny,
            )
            verify(exactly = 0) { onConfirm() }
            verify(exactly = 1) { onDeny() }
        },
        {
            generateServer().sendApproval(choice = false)
        },
    )
}
