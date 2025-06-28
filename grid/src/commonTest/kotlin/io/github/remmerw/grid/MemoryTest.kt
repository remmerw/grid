package io.github.remmerw.grid

import kotlinx.io.Buffer
import kotlinx.io.buffered
import kotlinx.io.readByteArray
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertTrue

class MemoryTest {

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
    fun allocateReadOnlyBig() {

        val data = Random.nextBytes(3.times(UShort.MAX_VALUE.toInt()) + 10000)
        val memory = allocateReadOnlyMemory(data)

        val a = memory.readBytes(0, data.size)
        assertTrue(data.contentEquals(a))

        val buffer = Buffer()
        memory.transferTo(buffer)
        val b = buffer.readByteArray()
        assertTrue(data.contentEquals(b))
        val c = memory.rawSource().buffered().readByteArray()
        assertTrue(data.contentEquals(c))
    }
}