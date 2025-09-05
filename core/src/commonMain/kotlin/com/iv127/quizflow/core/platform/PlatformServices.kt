package com.iv127.quizflow.core.platform

import com.iv127.quizflow.core.platform.file.FileIO
import com.iv127.quizflow.core.platform.proc.ProcessUtils

interface PlatformServices {

    fun getProcessUtils(): ProcessUtils

    fun getFileIO(): FileIO

}
