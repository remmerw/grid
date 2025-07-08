package io.github.remmerw.grid

import kotlinx.io.Buffer
import kotlinx.io.RawSource
import kotlinx.io.Sink
import kotlinx.io.bytestring.getByteString
import kotlinx.io.files.Path
import kotlinx.io.readByteArray
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import kotlin.math.min
import kotlin.uuid.ExperimentalUuidApi

internal class AndroidRandomAccessFile(val raf: RandomAccessFile) : io.github.remmerw.grid.RandomAccessFile {
    override fun readBytes(position: Long, bytes: ByteArray, offset: Int, length: Int) {
        raf.seek(position)
        raf.read(bytes, offset, length)
    }

    override fun writeBytes(position: Long, bytes: ByteArray, offset: Int, length: Int) {
        raf.seek(position)
        raf.write(bytes, offset, length)
    }

    override fun writeMemory(position: Long, memory: Memory) {
        raf.seek(position)
        val buffer = Buffer()
        memory.rawSource().use { source ->
            do {
                val written = source.readAtMostTo(buffer, SPLITTER)
                if (written > 0) {
                    raf.write(buffer.readByteArray())
                }
            } while (written > 0)
        }
    }

    override fun transferTo(position: Long, sink: Sink, length: Long) {
        raf.seek(position)
        val data = ByteArray(SPLITTER.toInt())
        var stillToRead = length
        var read: Int
        var todo: Boolean
        var min: Int
        do {
            read = raf.read(data)
            todo = read > 0 && stillToRead > 0
            if (todo) {
                min = min(read.toLong(), stillToRead).toInt()
                sink.write(data, 0, min)
                stillToRead -= read
            }
        } while (todo)
    }

    override fun close() {
        raf.close()
    }

}

internal class AndroidMemory(val memory: ByteBuffer, val size: Int) : Memory {
    override fun size(): Int {
        return size
    }

    override fun readBytes(offset: Int, length: Int): ByteArray {
        memory.rewind()
        memory.position(offset)
        val result = ByteArray(length)
        memory.get(result)
        return result
    }

    override fun writeBytes(bytes: ByteArray, offset: Int) {
        memory.rewind()
        memory.position(offset)
        memory.put(bytes)
    }


    override fun rawSource(): RawSource {
        memory.rewind()

        return object : RawSource {
            override fun readAtMostTo(
                sink: Buffer,
                byteCount: Long
            ): Long {
                val read = min(byteCount, memory.remaining().toLong())
                if (read > 0) {
                    val data = memory.getByteString(read.toInt())
                    sink.write(data.toByteArray())
                    return read
                } else {
                    return -1
                }
            }

            override fun close() {
                // nothing to do
            }

        }
    }
}


@OptIn(ExperimentalUuidApi::class)
actual fun allocateMemory(size: Int): Memory {

    val memory = ByteBuffer.allocateDirect(size)

    return AndroidMemory(memory, size)

}

actual fun randomAccessFile(path: Path): io.github.remmerw.grid.RandomAccessFile {
    val raf = RandomAccessFile(path.toString(), "rw")
    return AndroidRandomAccessFile(raf)
}