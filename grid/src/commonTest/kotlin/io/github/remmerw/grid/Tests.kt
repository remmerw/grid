package io.github.remmerw.grid

import kotlinx.io.Buffer
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.files.SystemTemporaryDirectory
import kotlinx.io.readByteArray
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertTrue
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class Tests {

    @Test
    fun allocateByteArray(){
        val instance = ByteArrayPool.getInstance(500)
        val array = instance.get()
        assertTrue(array.byteArray.size == 500)
        array.close()
    }

    @Test
    fun allocateSmall() {

        val data = "Moin".encodeToByteArray()
        val memory = allocateMemory(data.size)
        memory.writeBytes(data, 0)
        val a = memory.readBytes(0, data.size)
        assertTrue(data.contentEquals(a))

        val buffer = Buffer()
        memory.transferTo(buffer)
        val b = buffer.readByteArray()
        assertTrue(data.contentEquals(b))
        val c = memory.rawSource().buffered().readByteArray()
        assertTrue(data.contentEquals(c))
    }


    @Test
    fun allocateBig() {

        val data = Random.nextBytes(3.times(UShort.MAX_VALUE.toInt()) + 10000)
        val memory = allocateMemory(data.size)
        memory.writeBytes(data, 0)
        val a = memory.readBytes(0, data.size)
        assertTrue(data.contentEquals(a))

        val buffer = Buffer()
        memory.transferTo(buffer)
        val b = buffer.readByteArray()
        assertTrue(data.contentEquals(b))
        val c = memory.rawSource().buffered().readByteArray()
        assertTrue(data.contentEquals(c))
    }

    @Test
    fun allocateDataBig() {

        val data = Random.nextBytes(3.times(UShort.MAX_VALUE.toInt()) + 10000)
        val memory = allocateMemory(data)

        val a = memory.readBytes(0, data.size)
        assertTrue(data.contentEquals(a))

        val buffer = Buffer()
        memory.transferTo(buffer)
        val b = buffer.readByteArray()
        assertTrue(data.contentEquals(b))
        val c = memory.rawSource().buffered().readByteArray()
        assertTrue(data.contentEquals(c))
    }


    @OptIn(ExperimentalUuidApi::class)
    @Test
    fun allocateFileBig() {

        val path = Path(SystemTemporaryDirectory, Uuid.random().toHexString())
        SystemFileSystem.createDirectories(path)
        val file = Path(path, "data.txt")

        val data = Random.nextBytes(3.times(UShort.MAX_VALUE.toInt()) + 10000)

        SystemFileSystem.sink(file, false).buffered().use { sink ->
            sink.write(data)
        }

        val memory = allocateMemory(file)

        val a = memory.readBytes(0, data.size)
        assertTrue(data.contentEquals(a))

        val buffer = Buffer()
        memory.transferTo(buffer)
        val b = buffer.readByteArray()
        assertTrue(data.contentEquals(b))
        val c = memory.rawSource().buffered().readByteArray()
        assertTrue(data.contentEquals(c))
    }


    @OptIn(ExperimentalUuidApi::class)
    @Test
    fun randomAccessFileBasic() {
        val path = Path(SystemTemporaryDirectory, Uuid.random().toHexString())
        SystemFileSystem.createDirectories(path)
        val file = Path(path, "data.db")

        val data = Random.nextBytes(1000)
        val position = 10000L

        randomAccessFile(file).use { raf ->
            raf.writeBytes(position, data)

            val cmp = ByteArray(data.size)
            raf.readBytes(position, cmp)
            assertContentEquals(data, cmp)

        }

        // second run
        randomAccessFile(file).use { raf ->
            val cmp = ByteArray(data.size)
            raf.readBytes(position, cmp, 0, cmp.size)
            assertContentEquals(data, cmp)
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    @Test
    fun randomAccessFileMemory() {
        val path = Path(SystemTemporaryDirectory, Uuid.random().toHexString())
        SystemFileSystem.createDirectories(path)
        val file = Path(path, "data.db")

        val data = Random.nextBytes(10000)
        val memory = allocateMemory(data)
        val offset = 10000L

        randomAccessFile(file).use { raf ->
            raf.writeMemory(offset, memory)

            val cmp = ByteArray(data.size)
            raf.readBytes(offset, cmp)
            assertContentEquals(data, cmp)

        }

        // second run
        val sink = Buffer()
        randomAccessFile(file).use { raf ->
            raf.transferTo(offset, sink, data.size.toLong())
            assertContentEquals(data, sink.readByteArray())
        }
    }


    @OptIn(ExperimentalUuidApi::class)
    @Test
    fun randomAccessFilePieces() {
        val path = Path(SystemTemporaryDirectory, Uuid.random().toHexString())
        SystemFileSystem.createDirectories(path)
        val file = Path(path, "data.db")


        val data = Random.nextBytes(500)
        randomAccessFile(file).use { raf ->
            var position = 0L
            repeat(10) {
                raf.writeBytes(position, data)
                position += data.size
            }
        }

        randomAccessFile(file).use { raf ->
            val cmp = ByteArray(data.size)
            var position = 0L
            repeat(10) {
                raf.readBytes(position, cmp)
                position += data.size
            }
            assertContentEquals(data, cmp)
        }
    }

}