package io.github.remmerw.grid

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.readBytes
import kotlinx.cinterop.usePinned
import kotlinx.io.Buffer
import kotlinx.io.RawSink
import platform.Foundation.NSMakeRange
import platform.Foundation.NSMutableData
import platform.Foundation.create
import platform.Foundation.replaceBytesInRange
import platform.Foundation.subdataWithRange
import kotlin.uuid.ExperimentalUuidApi

class IosMemory(val memory: NSMutableData, val size: Int) : Memory {
    override fun size(): Int {
        return size
    }

    @OptIn(ExperimentalForeignApi::class)
    override fun readBytes(offset: Int, length: Int): ByteArray {
        val subDataRange = NSMakeRange(offset.convert(), length.convert())

        @Suppress("UNCHECKED_CAST")
        val subDataBytePointer = memory.subdataWithRange(subDataRange).bytes as CPointer<ByteVar>
        return subDataBytePointer.readBytes(length)
    }

    @OptIn(ExperimentalForeignApi::class)
    override fun writeBytes(bytes: ByteArray, offset: Int) {
        val range = NSMakeRange(offset.convert(), bytes.size.convert())
        bytes.usePinned { pin ->
            memory.replaceBytesInRange(range, pin.addressOf(offset))
        }
    }

    override fun transferTo(sink: RawSink) {
        val split = UShort.MAX_VALUE.toInt()
        var offset = 0
        var remains = size
        do {
            if (remains >= split) {
                val bytes = readBytes(offset, split)
                val buffer = Buffer()
                buffer.write(bytes)
                sink.write(buffer, split.toLong())
                remains -= split
                offset += split
            } else {
                val bytes = readBytes(offset, remains)
                val buffer = Buffer()
                buffer.write(bytes)
                sink.write(buffer, remains.toLong())
            }

        } while (remains > 0)
    }

}

@OptIn(ExperimentalUuidApi::class, BetaInteropApi::class, ExperimentalForeignApi::class)
actual fun allocateMemory(size: Int): Memory {

    val memory = NSMutableData.create(length = size.convert())!!

    return IosMemory(memory, size)

}
