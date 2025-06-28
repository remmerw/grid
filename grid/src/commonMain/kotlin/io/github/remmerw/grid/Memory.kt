package io.github.remmerw.grid

import kotlinx.io.RawSink
import kotlinx.io.RawSource
import kotlinx.io.buffered

interface Memory : ReadOnlyMemory {
    fun writeBytes(bytes: ByteArray, offset: Int)

}
interface ReadOnlyMemory {
    fun size(): Int
    fun readBytes(offset: Int, length: Int): ByteArray
    fun transferTo(sink: RawSink) {
        rawSource().buffered().transferTo(sink)
    }
    fun rawSource(): RawSource
}

expect fun allocateMemory(size: Int): Memory

fun allocateReadOnlyMemory(bytes: ByteArray): ReadOnlyMemory {
    val memory = allocateMemory(bytes.size)
    memory.writeBytes(bytes, 0)
    return memory
}
