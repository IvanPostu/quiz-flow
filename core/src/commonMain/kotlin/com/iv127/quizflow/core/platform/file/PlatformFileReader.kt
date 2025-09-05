package com.iv127.quizflow.core.platform.file

expect class PlatformFileReader(filePath: String) {
    /**
     * Sets the file-pointer offset, measured from the beginning of this
     * file, at which the next read or write occurs.
     */
    fun setPosition(start: Long)

    fun getLength(): Long

    /**
     * Reads up to {@code buffer.length} bytes of data from this file
     * into an array of bytes.
     * @param      buffer   the buffer into which the data is read.
     * @return     the total number of bytes read into the buffer, or
     *             {@code -1} if there is no more data because the end of
     *             this file has been reached.
     */
    fun readIntoBuffer(buffer: ByteArray): Int

    fun close()

}
