package commandHandler.executeCommand.sendExecution

import com.varabyte.kotter.foundation.input.input
import com.varabyte.kotter.foundation.text.Color
import com.varabyte.kotter.foundation.text.color
import com.varabyte.kotter.foundation.text.red
import com.varabyte.kotter.foundation.text.text
import com.varabyte.kotter.foundation.text.textLine
import com.varabyte.kotter.runtime.MainRenderScope
import commandHandler.FileTransfer

fun MainRenderScope.clientEnginePrintLineRenderBlock(
    colourIndex: Int,
    message: String,
) {
    color(Color.entries[colourIndex])
    textLine(text = message)
}

fun MainRenderScope.sendFilesCollectingRenderBlock(fileCount: Int) {
    text(text = "Total files found: ")
    text(text = fileCount.toString())
}

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
            FileTransfer.CANCEL -> textLine(text = "$action. Cancel sending files.")
            FileTransfer.SKIP_INVALID -> textLine(text = "$action. Skip invalid files.")
            FileTransfer.COMPRESS_EVERYTHING -> textLine(
                text = "$action. Compress all files and send as a single file.",
            )

            FileTransfer.COMPRESS_INVALID -> textLine(
                text = "$action. Compress invalid files only and send as a single file (relative file paths will be preserved).",
            )

            FileTransfer.SHOW_ALL -> textLine(text = "$action. Show all files and ask again.")
        }
    }
    text(text = "> "); input()
}

fun MainRenderScope.renderCutoffPath(index: Int, path: String, cutoff: Int) {
    text(text = "${index + 1} ")
    text(text = path.subSequence(0, cutoff).toString())
    red { textLine(text = path.substring(startIndex = cutoff)) }
}
