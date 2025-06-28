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
import kotlinx.io.RawSource
import platform.Foundation.NSMakeRange
import platform.Foundation.NSMutableData
import platform.Foundation.create
import platform.Foundation.replaceBytesInRange
import platform.Foundation.subdataWithRange
import kotlin.math.min
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

    override fun rawSource(): RawSource {
        var offset = 0
        return object : RawSource {
            override fun readAtMostTo(
                sink: Buffer,
                byteCount: Long
            ): Long {
                val read = min(byteCount, (size() - offset).toLong())
                val bytes = readBytes(offset, read.toInt())
                val buffer = Buffer()
                buffer.write(bytes)
                sink.write(buffer, read)
                offset += read.toInt()
                return read
            }

            override fun close() {
                // nothing to do
            }

        }
    }

}

@OptIn(ExperimentalUuidApi::class, BetaInteropApi::class, ExperimentalForeignApi::class)
actual fun allocateMemory(size: Int): Memory {

    val memory = NSMutableData.create(length = size.convert())!!

    return IosMemory(memory, size)

}
