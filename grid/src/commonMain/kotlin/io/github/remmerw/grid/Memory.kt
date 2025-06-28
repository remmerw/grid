package io.github.remmerw.grid

import kotlinx.io.RawSink
import kotlinx.io.RawSource
import kotlinx.io.buffered

interface Memory {
    fun size(): Int
    fun readBytes(offset: Int, length: Int): ByteArray
    fun writeBytes(bytes: ByteArray, offset: Int)
    fun transferTo(sink: RawSink) {
        rawSource().buffered().transferTo(sink)
    }
    fun rawSource(): RawSource
}

expect fun allocateMemory(size: Int): Memory