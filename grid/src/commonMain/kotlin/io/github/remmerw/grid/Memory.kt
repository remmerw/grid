package io.github.remmerw.grid

import kotlinx.io.Buffer
import kotlinx.io.RawSink
import kotlinx.io.RawSource
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readByteArray

const val SPLITTER = 4096L

interface RandomAccessFile : AutoCloseable {
    fun readBytes(offset: Long, length: Int): ByteArray
    fun writeBytes(bytes: ByteArray, offset: Long)
    fun writeMemory(memory: Memory, offset: Long)
    override fun close()
}

interface Memory {
    fun writeBytes(bytes: ByteArray, offset: Int)
    fun size(): Int
    fun readBytes(offset: Int, length: Int): ByteArray
    fun transferTo(sink: RawSink) {
        rawSource().buffered().transferTo(sink)
    }
    fun rawSource(): RawSource
}
expect fun allocateMemory(size: Int): Memory
expect fun randomAccessFile(path:Path): RandomAccessFile

fun allocateMemory(bytes: ByteArray): Memory {
    val memory = allocateMemory(bytes.size)
    memory.writeBytes(bytes, 0)
    return memory
}

fun allocateMemory(path: Path): Memory {
    require(SystemFileSystem.exists(path)) { "path does not exists" }
    val size = SystemFileSystem.metadataOrNull(path)!!.size.toInt()
    val memory = allocateMemory(size)
    val sink = Buffer()
    var offset = 0
    SystemFileSystem.source(path).use { source ->
        do {
            val read = source.readAtMostTo(sink, UShort.MAX_VALUE.toLong())
            if (read > 0) {
                memory.writeBytes(sink.readByteArray(), offset)
                offset += read.toInt()
            }
        } while(read > 0)
    }
    return memory
}
