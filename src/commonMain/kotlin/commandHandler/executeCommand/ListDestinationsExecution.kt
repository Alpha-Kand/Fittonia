package commandHandler.executeCommand

import com.varabyte.kotter.foundation.text.textLine
import com.varabyte.kotter.runtime.Session
import commandHandler.ListDestinationsCommand
import settingsManager.SettingsManager

fun Session.listDestinationsExecution(command: ListDestinationsCommand) = section {
    val settingsManager = SettingsManager.settingsManager
    val printDestination: (SettingsManager.SettingsData.Destination) -> Unit = {
        textLine(text = "Name: " + it.name)
        textLine(text = "IP: " + it.ip)
        textLine()
    }
    command.getName()?.let { searchName ->
        settingsManager.settings.destinations.find { it.name == searchName }?.let {
            printDestination(it)
        } ?: run {
            textLine(text = "No destination found with that name.")
            textLine()
        }
    } ?: run {
        if (settingsManager.settings.destinations.isEmpty()) {
            textLine(text = "No destinations registered. Register with the 'add' command.")
        } else {
            settingsManager.settings.destinations.forEach {
                printDestination(it)
            }
        }
    }
}.run()
