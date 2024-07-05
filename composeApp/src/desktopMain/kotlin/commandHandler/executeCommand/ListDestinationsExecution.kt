package commandHandler.executeCommand

import OutputIO.printlnIO
import SettingsManager
import SettingsManagerDesktop
import commandHandler.ListDestinationsCommand
import fittonia.composeapp.generated.resources.Res
import fittonia.composeapp.generated.resources.destination_not_found
import fittonia.composeapp.generated.resources.ip_output
import fittonia.composeapp.generated.resources.name_output
import fittonia.composeapp.generated.resources.no_destinations_registered

suspend fun listDestinationsExecution(command: ListDestinationsCommand) {
    val settingsManager = SettingsManagerDesktop.settingsManager
    val printDestination: suspend (SettingsManager.Destination) -> Unit = {
        printlnIO(Res.string.name_output, it.name)
        printlnIO(Res.string.ip_output, it.ip)
    }
    if (settingsManager.settings.destinations.isEmpty()) {
        printlnIO(Res.string.no_destinations_registered)
    } else {
        command.getName()?.let { searchName ->
            settingsManager.settings.destinations.find { it.name == searchName }?.let {
                printDestination(it)
            } ?: printlnIO(Res.string.destination_not_found)
        } ?: command.getSearch()?.let { searchText ->
            settingsManager.settings.destinations.find { it.name.contains(searchText) }?.let {
                printDestination(it)
            } ?: printlnIO(Res.string.destination_not_found)
        } ?: run {
            settingsManager.settings.destinations.forEach {
                printDestination(it)
            }
        }
    }
}
