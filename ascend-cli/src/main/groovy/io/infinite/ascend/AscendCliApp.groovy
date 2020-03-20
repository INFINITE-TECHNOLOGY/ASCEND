package io.infinite.ascend

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import groovy.util.logging.Slf4j
import io.infinite.ascend.client.App2AppAuthorizationHelperFile
import io.infinite.ascend.granting.model.Authorization
import io.infinite.blackbox.BlackBox
import picocli.CommandLine

@BlackBox
@Slf4j
@CommandLine.Command(name = "ASCEND-CLI", mixinStandardHelpOptions = true, description = "App to app authorization command line client.", version = "1.0.0")
class AscendCliApp {

    @CommandLine.Parameters(index = "0", description = "Ascend Client Application Name, as trusted in Ascend Granting Server.")
    String ascendClientAppName

    @CommandLine.Parameters(index = "1", description = "Ascend Granting Server URL")
    String ascendGrantingUrl

    @CommandLine.Parameters(index = "2", description = "Desired authorization scope name.")
    String scopeName

    @CommandLine.Parameters(index = "3", description = "File containing HEX string with the private key.")
    File privateKeyFile

    static void main(String[] args) {
        log.info("Welcome to Infinite Technology Ascend app to app authorization command line client.")
        log.info("This is Open Source software, free for usage and modification.")
        log.info("By using this software you accept License and User Agreement.")
        log.info("Visit our web site: https://i-t.io/Ascend")
        int exitCode = new CommandLine(new AscendCliApp()).execute(args)
        System.exit(exitCode)
    }

    Integer call() throws Exception {
        Authorization orbitAuthorization = new App2AppAuthorizationHelperFile(privateKeyFile, ascendGrantingUrl).createApp2AppAuthorization(
                ascendClientAppName,
                scopeName
        )
        new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT).writeValue(System.out, orbitAuthorization)
        return 0
    }

}
