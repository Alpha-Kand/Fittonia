package commandHandler.executeCommand.sendExecution

import com.varabyte.kotter.foundation.input.input
import com.varabyte.kotter.foundation.text.red
import com.varabyte.kotter.foundation.text.text
import com.varabyte.kotter.foundation.text.textLine
import com.varabyte.kotter.runtime.MainRenderScope
import commandHandler.FileTransfer
import sendFilesUserOptionCancel
import sendFilesUserOptionCompressAll
import sendFilesUserOptionCompressInvalid
import sendFilesUserOptionShowAll
import sendFilesUserOptionSkipInvalid

fun MainRenderScope.fileNamesTooLongRenderBlock(actionList: List<Int>) {
    textLine()
    val sb = StringBuilder("What would you like to do? (")
    actionList.forEach {
        sb.append("$it,")
    }
    sb.dropLast(2)
    sb.append(')')
    textLine(text = sb.toString())

    actionList.forEach { action ->
        when (action) {
            FileTransfer.CANCEL -> textLine(text = sendFilesUserOptionCancel.format(action))
            FileTransfer.SKIP_INVALID -> textLine(text = sendFilesUserOptionSkipInvalid.format(action))
            FileTransfer.COMPRESS_EVERYTHING -> textLine(text = sendFilesUserOptionCompressAll.format(action))
            FileTransfer.COMPRESS_INVALID -> textLine(text = sendFilesUserOptionCompressInvalid.format(action))
            FileTransfer.SHOW_ALL -> textLine(text = sendFilesUserOptionShowAll.format(action))
        }
    }
    text(text = "? "); input()
}

fun MainRenderScope.renderCutoffPath(index: Int, path: String, cutoff: Int) {
    text(text = "${index + 1} ")
    text(text = path.subSequence(0, cutoff).toString())
    red { textLine(text = path.substring(startIndex = cutoff)) }
}
