<div>
    <div>
        <img src="https://img.shields.io/maven-central/v/io.github.remmerw/grid" alt="Kotlin Maven Version" />
        <img src="https://img.shields.io/badge/Platform-Android-brightgreen.svg?logo=android" alt="Badge Android" />
        <img src="https://img.shields.io/badge/Platform-JVM-8A2BE2.svg?logo=openjdk" alt="Badge JVM" />
    </div>
</div>

## Grid

The **Grid** is a tiny library which contains memory and file functionality.


## Integration

```
    
kotlin {
    sourceSets {
        commonMain.dependencies {
            ...
            implementation("io.github.remmerw:grid:0.1.0")
        }
        ...
    }
}
    
```

## API

```



interface Memory {
    fun writeBytes(bytes: ByteArray, offset: Int)
    fun size(): Int
    fun readBytes(offset: Int, length: Int): ByteArray
    fun transferTo(sink: RawSink) {
        rawSource().buffered().transferTo(sink)
    }
    fun rawSource(): RawSource
}


interface RandomAccessFile : AutoCloseable {
    fun read(position: Long, bytes: ByteArray) : Int
    fun writeBoolean(position: Long, boolean: Boolean)
    fun readBytes(position: Long, bytes: ByteArray, offset: Int = 0, length: Int = bytes.size)
    fun writeBytes(position: Long, bytes: ByteArray, offset: Int = 0, length: Int = bytes.size)
    fun writeMemory(position: Long, memory: Memory)
    fun transferTo(position: Long, sink: Sink, length: Long)
    override fun close()
}


fun allocateMemory(size: Int): Memory {...}
fun allocateMemory(bytes: ByteArray): Memory {...}
fun allocateMemory(path: Path): Memory {...}


fun randomAccessFile(path:Path): RandomAccessFile {...}

```

