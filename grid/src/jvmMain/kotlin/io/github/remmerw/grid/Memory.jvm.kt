package io.github.remmerw.grid

import kotlinx.io.Buffer
import kotlinx.io.RawSource
import kotlinx.io.bytestring.getByteString
import java.nio.ByteBuffer
import kotlin.math.min
import kotlin.uuid.ExperimentalUuidApi

class JvmMemory(val memory: ByteBuffer, val size: Int) : Memory {
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

    return JvmMemory(memory, size)

}
