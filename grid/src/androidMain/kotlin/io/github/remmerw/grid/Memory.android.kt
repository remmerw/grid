package io.github.remmerw.grid

import kotlinx.io.Buffer
import kotlinx.io.RawSource
import kotlinx.io.bytestring.getByteString
import kotlinx.io.files.Path
import kotlinx.io.readByteArray
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import kotlin.math.min
import kotlin.uuid.ExperimentalUuidApi

class AndroidRandomAccessFile(val raf: RandomAccessFile) : io.github.remmerw.grid.RandomAccessFile {
    override fun readBytes(offset: Long, length: Int): ByteArray {
        raf.seek(offset)
        val data = ByteArray(length)
        raf.readFully(data)
        return data
    }

    override fun writeBytes(bytes: ByteArray, offset: Long) {
        raf.seek(offset)
        raf.write(bytes)
    }

    override fun writeMemory(memory: Memory, offset: Long) {
        raf.seek(offset)
        val buffer = Buffer()
        memory.rawSource().use { source ->
            do {
                val written = source.readAtMostTo(buffer, SPLITTER)
                if(written > 0){
                    raf.write(buffer.readByteArray())
                }
            } while(written > 0)
        }
    }

    override fun close() {
        raf.close()
    }

}

class AndroidMemory(val memory: ByteBuffer, val size: Int) : Memory {
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