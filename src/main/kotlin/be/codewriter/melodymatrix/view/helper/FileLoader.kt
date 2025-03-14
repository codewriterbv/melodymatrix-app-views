package be.codewriter.melodymatrix.view.helper

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.io.InputStream

class FileLoader {
    companion object {
        private val logger: Logger = LogManager.getLogger(FileLoader::class.java.name)

        fun getResource(filePath: String): InputStream? {
            val inputStream: InputStream? = this::class.java.getResourceAsStream(filePath);

            if (inputStream == null) {
                logger.error("Can't find the file, sorry can't create the screen...")
            }

            return inputStream
        }
    }
}