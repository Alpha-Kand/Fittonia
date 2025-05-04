import commandHandler.decodeIpCodeCommand
import commandHandler.dumpCommand
import commandHandler.exitCommand
import commandHandler.helpCommand
import commandHandler.ipArguments
import commandHandler.ipCodeArguments
import commandHandler.ipCodeCommand
import commandHandler.listDestinationsCommand
import commandHandler.nameArguments
import commandHandler.newArguments
import commandHandler.oldArguments
import commandHandler.pathArguments
import commandHandler.portArguments
import commandHandler.quitCommand
import commandHandler.removeCommand
import commandHandler.searchArguments
import commandHandler.sendFilesCommand
import commandHandler.sendMessageCommand
import commandHandler.serverAccessCodeCommand
import commandHandler.serverCommand
import commandHandler.setDefaultPortCommand
import fittonia.composeapp.generated.resources.Res
import fittonia.composeapp.generated.resources.decode_ip_code_help_arguments_ip
import fittonia.composeapp.generated.resources.decode_ip_code_help_description
import fittonia.composeapp.generated.resources.decode_ip_code_help_format
import fittonia.composeapp.generated.resources.dump_path_help_arguments_path
import fittonia.composeapp.generated.resources.dump_path_help_description
import fittonia.composeapp.generated.resources.dump_path_help_format
import fittonia.composeapp.generated.resources.encode_ip_code_help_description
import fittonia.composeapp.generated.resources.exit_help_description
import fittonia.composeapp.generated.resources.exit_help_title
import fittonia.composeapp.generated.resources.help_help_arguments_search
import fittonia.composeapp.generated.resources.help_help_description
import fittonia.composeapp.generated.resources.help_help_format
import fittonia.composeapp.generated.resources.list_help_arguments_name
import fittonia.composeapp.generated.resources.list_help_arguments_search
import fittonia.composeapp.generated.resources.list_help_description
import fittonia.composeapp.generated.resources.list_help_format
import fittonia.composeapp.generated.resources.remove_help_arguments_name
import fittonia.composeapp.generated.resources.remove_help_description
import fittonia.composeapp.generated.resources.remove_help_format
import fittonia.composeapp.generated.resources.send_files_help_arguments_ip
import fittonia.composeapp.generated.resources.send_files_help_description
import fittonia.composeapp.generated.resources.send_files_help_format
import fittonia.composeapp.generated.resources.send_message_help_arguments_ip
import fittonia.composeapp.generated.resources.send_message_help_description
import fittonia.composeapp.generated.resources.send_message_help_format
import fittonia.composeapp.generated.resources.server_access_code_help_arguments_new
import fittonia.composeapp.generated.resources.server_access_code_help_arguments_old
import fittonia.composeapp.generated.resources.server_access_code_help_description
import fittonia.composeapp.generated.resources.server_access_code_help_format
import fittonia.composeapp.generated.resources.server_init_help_arguments_port
import fittonia.composeapp.generated.resources.server_init_help_description
import fittonia.composeapp.generated.resources.server_init_help_format
import fittonia.composeapp.generated.resources.set_default_port_help_arguments_port
import fittonia.composeapp.generated.resources.set_default_port_help_description
import fittonia.composeapp.generated.resources.set_default_port_help_format
import org.jetbrains.compose.resources.getString

data class HelpDoc(
    val title: String,
    val description: String,
    val format: String,
    val arguments: List<Pair<List<String>, String>>?,
) {
    fun search(term: String) = listOf(title, description, format).any { it.contains(term, ignoreCase = true) }
}

object HelpDocLoader {
    private var initialized = false
    private val mHelpDocs = mutableListOf<HelpDoc>()
    val helpDocs: List<HelpDoc>
        get() {
            return if (initialized) {
                mHelpDocs
            } else {
                throw Exception() // TODO - After release
            }
        }

    suspend fun init() {
        mHelpDocs.addAll(
            listOf(
                createEncodeIpHelpDoc(),
                createDecodeIpCodeHelpDoc(),
                createDumpHelpDoc(),
                createListHelpDoc(),
                createRemoveHelpDoc(),
                createSendFilesHelpDoc(),
                createSendMessageHelpDoc(),
                createSetDefaultPortHelpDoc(),
                createServerHelpDoc(),
                createServerAccessCodeHelpDoc(),
                createHelpHelpDoc(),
                createExitHelpDoc(),
            ),
        )
        initialized = true
    }

    private suspend fun createDecodeIpCodeHelpDoc() = HelpDoc(
        title = decodeIpCodeCommand,
        description = getString(Res.string.decode_ip_code_help_description),
        format = getString(Res.string.decode_ip_code_help_format, decodeIpCodeCommand),
        arguments = listOf(
            ipCodeArguments to getString(Res.string.decode_ip_code_help_arguments_ip),
        ),
    )

    private suspend fun createDumpHelpDoc() = HelpDoc(
        title = dumpCommand,
        description = getString(Res.string.dump_path_help_description),
        format = getString(Res.string.dump_path_help_format, dumpCommand),
        arguments = listOf(
            pathArguments to getString(Res.string.dump_path_help_arguments_path),
        ),
    )

    private suspend fun createEncodeIpHelpDoc() = HelpDoc(
        title = ipCodeCommand,
        description = getString(Res.string.encode_ip_code_help_description),
        format = ipCodeCommand,
        arguments = null,
    )

    private suspend fun createExitHelpDoc() = HelpDoc(
        title = getString(Res.string.exit_help_title, exitCommand, quitCommand),
        description = getString(Res.string.exit_help_description),
        format = exitCommand,
        arguments = null,
    )

    private suspend fun createListHelpDoc() = HelpDoc(
        title = listDestinationsCommand,
        description = getString(Res.string.list_help_description),
        format = getString(Res.string.list_help_format, listDestinationsCommand),
        arguments = listOf(
            nameArguments to getString(Res.string.list_help_arguments_name),
            searchArguments to getString(Res.string.list_help_arguments_search),
        ),
    )

    private suspend fun createRemoveHelpDoc() = HelpDoc(
        title = removeCommand,
        description = getString(Res.string.remove_help_description),
        format = getString(Res.string.remove_help_format, removeCommand),
        arguments = listOf(
            nameArguments to getString(Res.string.remove_help_arguments_name),
        ),
    )

    private suspend fun createSetDefaultPortHelpDoc() = HelpDoc(
        title = setDefaultPortCommand,
        description = getString(Res.string.set_default_port_help_description),
        format = getString(Res.string.set_default_port_help_format, setDefaultPortCommand),
        arguments = listOf(
            portArguments to getString(Res.string.set_default_port_help_arguments_port),
        ),
    )

    private suspend fun createServerAccessCodeHelpDoc() = HelpDoc(
        title = serverAccessCodeCommand,
        description = getString(Res.string.server_access_code_help_description),
        format = getString(Res.string.server_access_code_help_format, serverAccessCodeCommand),
        arguments = listOf(
            newArguments to getString(Res.string.server_access_code_help_arguments_new),
            oldArguments to getString(Res.string.server_access_code_help_arguments_old),
        ),
    )

    private suspend fun createServerHelpDoc() = HelpDoc(
        title = serverCommand,
        description = getString(Res.string.server_init_help_description),
        format = getString(Res.string.server_init_help_format, serverCommand),
        arguments = listOf(
            portArguments to getString(Res.string.server_init_help_arguments_port),
        ),
    )

    private suspend fun createSendFilesHelpDoc() = HelpDoc(
        title = sendFilesCommand,
        description = getString(Res.string.send_files_help_description),
        format = getString(Res.string.send_files_help_format, sendFilesCommand),
        arguments = listOf(
            ipArguments to getString(Res.string.send_files_help_arguments_ip),
        ),
    )

    private suspend fun createSendMessageHelpDoc() = HelpDoc(
        title = sendMessageCommand,
        description = getString(Res.string.send_message_help_description),
        format = getString(Res.string.send_message_help_format, sendMessageCommand),
        arguments = listOf(
            ipArguments to getString(Res.string.send_message_help_arguments_ip),
        ),
    )

    private suspend fun createHelpHelpDoc() = HelpDoc(
        title = helpCommand,
        description = getString(Res.string.help_help_description),
        format = getString(Res.string.help_help_format, helpCommand),
        arguments = listOf(
            searchArguments to getString(Res.string.help_help_arguments_search),
        ),
    )
}
