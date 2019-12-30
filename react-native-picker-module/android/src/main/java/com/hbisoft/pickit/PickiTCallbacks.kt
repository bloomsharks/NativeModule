package com.hbisoft.pickit

interface PickiTCallbacks {
    open fun PickiTonStartListener() {}
    open fun PickiTonProgressUpdate(progress: Int) {}
    fun PickiTonCompleteListener(
        path: String?,
        wasDriveFile: Boolean,
        wasUnknownProvider: Boolean,
        wasSuccessful: Boolean,
        originalFileName: String,
        originalFileSize: Int,
        Reason: String?
    )
}