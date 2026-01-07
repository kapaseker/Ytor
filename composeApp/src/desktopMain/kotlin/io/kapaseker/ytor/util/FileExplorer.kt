package io.kapaseker.ytor.util

import java.awt.Desktop
import java.io.File

/**
 * Opens the file explorer at the specified path.
 * On Windows, Linux, and macOS, this will open the folder containing the file,
 * or the folder itself if the path is a directory.
 * 
 * @param path The file or directory path to open in the file explorer
 * @return true if the operation was successful, false otherwise
 */
fun openFileExplorer(path: String): Boolean {
    return try {
        val file = File(path)
        
        // If the file doesn't exist, try to get the parent directory
        val targetFile = if (file.exists()) {
            file
        } else {
            // If it's a file path that doesn't exist, try to open the parent directory
            file.parentFile ?: file
        }
        
        // Ensure we have a directory to open
        val directory = if (targetFile.isDirectory) {
            targetFile
        } else {
            targetFile.parentFile
        }
        
        if (directory != null && directory.exists()) {
            Desktop.getDesktop().open(directory)
            true
        } else {
            false
        }
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}

