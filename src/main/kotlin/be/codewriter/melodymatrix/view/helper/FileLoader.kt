package be.codewriter.melodymatrix.view.helper

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.io.InputStream

/**
 * Utility class for loading resource files from the classpath.
 *
 * This class provides methods to load files packaged as resources in the application JAR.
 * If a resource cannot be found, an error is logged.
 */
class FileLoader {
    companion object {
        private val logger: Logger = LogManager.getLogger(FileLoader::class.java.name)

        /**
         * Loads a resource file from the classpath.
         *
         * @param filePath The path to the resource file relative to the classpath root
         * @return An InputStream for the resource, or null if the resource is not found
         */
        fun getResource(filePath: String): InputStream? {
            val inputStream: InputStream? = this::class.java.getResourceAsStream(filePath);

            if (inputStream == null) {
                logger.error("Can't find the file, sorry can't create the screen...")
            }

            return inputStream
        }
    }
}