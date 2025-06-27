package io.github.remmerw.grid

import kotlinx.io.Buffer
import kotlinx.io.RawSink
import kotlinx.io.buffered
import kotlinx.io.write
import java.nio.ByteBuffer
import kotlin.uuid.ExperimentalUuidApi

class AndroidMemory(val memory: ByteBuffer,) : Memory {
    override fun length(): Int {
        return memory.capacity()
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

    override fun transferTo(sink: RawSink) {
        memory.rewind()
        val bytes = ByteArray(UShort.MAX_VALUE.toInt())
        do {
            val remains = memory.remaining()
            if(remains >= bytes.size){
                memory.get(bytes)
                val buffer = Buffer()
                buffer.write(bytes)
                sink.write(buffer, bytes.size.toLong())
            } else {
                val data = ByteArray(remains)
                memory.get(data)
                val buffer = Buffer()
                buffer.write(data)
                sink.write(buffer, data.size.toLong())
            }

        } while(remains > 0)
    }

}

@OptIn(ExperimentalUuidApi::class)
actual fun allocateMemory(size: Int): Memory {

    val memory = ByteBuffer.allocateDirect(size)

    return AndroidMemory(memory)

}