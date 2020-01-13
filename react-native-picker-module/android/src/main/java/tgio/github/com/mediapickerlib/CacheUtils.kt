package tgio.github.com.mediapickerlib

import org.apache.commons.io.FileUtils
import org.apache.commons.io.filefilter.AgeFileFilter
import org.apache.commons.io.filefilter.TrueFileFilter
import java.io.File
import java.util.*

object CacheUtils {
    fun deleteFiles(folder: File, thresholdDate: Date) {
        val filesToDelete =
            FileUtils.iterateFiles(
                folder,
                AgeFileFilter(thresholdDate),
                TrueFileFilter.TRUE
            )
        for (aFile in filesToDelete) {
            aFile.delete()
        }
    }
}