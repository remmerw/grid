package io.github.remmerw.grid

import kotlinx.io.RawSink

interface Memory {
    fun	length() : Int
    fun readBytes(offset :Int, length : Int) : ByteArray
    fun writeBytes(bytes: ByteArray, offset :Int)
    fun transferTo(sink : RawSink)
}
expect fun allocateMemory(size: Int) : Memory