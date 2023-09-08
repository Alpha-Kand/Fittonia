package commandHandler.executeCommand

import commandHandler.ListDestinationsCommand
import settingsManager.SettingsManager

fun listDestinationsExecution(command: ListDestinationsCommand){
    val settingsManager = SettingsManager.settingsManager
    val printDestination: (SettingsManager.SettingsData.Destination) -> Unit = {
        println("Name: " + it.name)
        println("IP: " + it.ip)
        println()
    }
    command.getName()?.let { searchName ->
        settingsManager.settings.destinations.find { it.name == searchName }?.let {
            printDestination(it)
        } ?: run {
            println("No destination found with that name.")
            println()
        }
    } ?: run {
        if (settingsManager.settings.destinations.isEmpty()) {
            println("No destinations registered. Register with the 'add' command.")
        } else {
            settingsManager.settings.destinations.forEach {
                printDestination(it)
            }
        }
    }
}