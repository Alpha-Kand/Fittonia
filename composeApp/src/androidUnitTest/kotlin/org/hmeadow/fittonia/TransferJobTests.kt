package org.hmeadow.fittonia

import SettingsManager
import UnitTest
import io.mockk.every
import io.mockk.mockk
import org.hmeadow.fittonia.models.IncomingJob
import org.hmeadow.fittonia.models.OutgoingJob
import org.hmeadow.fittonia.models.TransferJob
import org.hmeadow.fittonia.models.TransferStatus
import org.junit.jupiter.api.assertDoesNotThrow
import kotlin.test.assertEquals

internal class TransferJobTests : AndroidBaseMockkTest() {

    private fun mockOutgoingJob(items: List<TransferJob.Item>) = OutgoingJob(
        id = 0,
        status = TransferStatus.Sending,
        description = "",
        currentItem = 0,
        items = items,
        destination = SettingsManager.Destination(
            name = "Destination Name",
            ip = "123.456.789.012",
            password = "password",
        ),
        port = 12345,
        needDescription = false,
    )

    private fun mockIncomingJob(items: List<TransferJob.Item>) = IncomingJob(
        id = 0,
        status = TransferStatus.Sending,
        description = "",
        currentItem = 0,
        items = items,
        source = IncomingJob.Source(),
    )

    @UnitTest
    fun jobNotStartedProgress() {
        val items = listOf(
            mockk<TransferJob.Item> {
                every { sizeBytes } returns 100
                every { progressBytes } returns 0
            },
            mockk<TransferJob.Item> {
                every { sizeBytes } returns 100
                every { progressBytes } returns 0
            },
        )
        listOf(mockOutgoingJob(items), mockIncomingJob(items)).forEach {
            assertEquals(expected = 0.0, actual = it.progressPercentage)
        }
    }

    @UnitTest
    fun jobFinishedProgress() {
        val items = listOf(
            mockk<TransferJob.Item> {
                every { sizeBytes } returns 100
                every { progressBytes } returns 100
            },
            mockk<TransferJob.Item> {
                every { sizeBytes } returns 100
                every { progressBytes } returns 100
            },
        )
        listOf(mockOutgoingJob(items), mockIncomingJob(items)).forEach {
            assertEquals(expected = 1.0, actual = it.progressPercentage)
        }
    }

    @UnitTest
    fun jobInProgressProgress() {
        val items = listOf(
            mockk<TransferJob.Item> {
                every { sizeBytes } returns 100
                every { progressBytes } returns 50
            },
            mockk<TransferJob.Item> {
                every { sizeBytes } returns 100
                every { progressBytes } returns 100
            },
        )
        listOf(mockOutgoingJob(items), mockIncomingJob(items)).forEach {
            assertEquals(expected = 0.75, actual = it.progressPercentage)
        }
    }

    @UnitTest
    fun jobInProgressProgress2() {
        val items = listOf(
            mockk<TransferJob.Item> {
                every { sizeBytes } returns 100
                every { progressBytes } returns 25
            },
            mockk<TransferJob.Item> {
                every { sizeBytes } returns 100
                every { progressBytes } returns 25
            },
        )
        listOf(mockOutgoingJob(items), mockIncomingJob(items)).forEach {
            assertEquals(expected = 0.25, actual = it.progressPercentage)
        }
    }

    @UnitTest
    fun jobInProgressProgress3() {
        val items = (1..9).map {
            mockk<TransferJob.Item> {
                every { sizeBytes } returns 100
                every { progressBytes } returns 0
            }
        } + mockk<TransferJob.Item> {
            every { sizeBytes } returns 100
            every { progressBytes } returns 10
        }
        listOf(mockOutgoingJob(items), mockIncomingJob(items)).forEach {
            assertEquals(expected = 0.01, actual = it.progressPercentage)
        }
    }

    @UnitTest
    fun jobEmptyProgress() {
        assertEquals(expected = 0.0, actual = mockOutgoingJob(emptyList()).progressPercentage)
    }

    @UnitTest
    fun getUpdatedItemList() {
        val items = (0..2).map { index ->
            mockk<TransferJob.Item> {
                every { id } returns index
                every { progressBytes } returns 0
            }
        }
        val updatedItem = mockk<TransferJob.Item> {
            every { id } returns 1
            every { progressBytes } returns 100
        }
        listOf(
            mockOutgoingJob(items).updateItem(item = updatedItem),
            mockIncomingJob(items).updateItem(item = updatedItem),
        ).forEach { updatedJob ->
            assertEquals(expected = 0, actual = updatedJob.items[0].progressBytes)
            assertEquals(expected = 0, actual = updatedJob.items[0].id)

            assertEquals(expected = 0, actual = updatedJob.items[1].progressBytes)
            assertEquals(expected = 2, actual = updatedJob.items[1].id)

            assertEquals(expected = 100, actual = updatedJob.items[2].progressBytes)
            assertEquals(expected = 1, actual = updatedJob.items[2].id)
        }
    }

    @UnitTest
    fun updateEmptyJob() {
        val updatedJob = assertDoesNotThrow {
            mockOutgoingJob(emptyList()).updateItem(
                mockk<TransferJob.Item> {
                    every { id } returns 1
                    every { progressBytes } returns 100
                },
            )
        }
        assertEquals(expected = 0, actual = updatedJob.totalItems)
    }
}
