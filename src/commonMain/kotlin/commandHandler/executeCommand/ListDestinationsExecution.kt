package commandHandler.executeCommand

import OutputIO.printlnIO
import commandHandler.ListDestinationsCommand
import destinationNotFound
import ipOutput
import nameOutput
import noDestinationsRegistered
import settingsManager.SettingsManager

fun listDestinationsExecution(command: ListDestinationsCommand) {
    val settingsManager = SettingsManager.settingsManager
    val printDestination: (SettingsManager.SettingsData.Destination) -> Unit = {
        printlnIO(nameOutput.format(it.name))
        printlnIO(ipOutput.format(it.ip))
    }
    if (settingsManager.settings.destinations.isEmpty()) {
        printlnIO(noDestinationsRegistered)
    } else {
        command.getName()?.let { searchName ->
            settingsManager.settings.destinations.find { it.name == searchName }?.let {
                printDestination(it)
            } ?: printlnIO(destinationNotFound)
        } ?: command.getSearch()?.let { searchText ->
            settingsManager.settings.destinations.find { it.name.contains(searchText) }?.let {
                printDestination(it)
            } ?: printlnIO(destinationNotFound)
        } ?: run {
            settingsManager.settings.destinations.forEach {
                printDestination(it)
            }
        }
    }
}
