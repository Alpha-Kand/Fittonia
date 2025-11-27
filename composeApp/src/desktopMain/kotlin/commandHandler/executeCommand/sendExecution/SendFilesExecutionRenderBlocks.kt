package commandHandler.executeCommand.sendExecution

import com.varabyte.kotter.foundation.input.input
import com.varabyte.kotter.foundation.text.red
import com.varabyte.kotter.foundation.text.text
import com.varabyte.kotter.foundation.text.textLine
import com.varabyte.kotter.runtime.MainRenderScope

fun MainRenderScope.fileNamesTooLongRenderBlock(
    actionList: List<Int>,
    optionStringMap: Map<Int, String>,
) {
    textLine()
    val sb = StringBuilder("What would you like to do? (")
    actionList.forEach {
        sb.append("$it,")
    }
    sb.dropLast(2)
    sb.append(')')
    textLine(text = sb.toString())

    actionList.forEach { action ->
        optionStringMap[action]?.format(action)
    }
    text(text = "? ")
    input()
}

fun MainRenderScope.renderCutoffPath(index: Int, path: String, cutoff: Int) {
    text(text = "${index + 1} ")
    text(text = path.subSequence(0, cutoff).toString())
    red { textLine(text = path.substring(startIndex = cutoff)) }
}
