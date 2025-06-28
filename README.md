<div>
    <div>
        <img src="https://img.shields.io/maven-central/v/io.github.remmerw/grid" alt="Kotlin Maven Version" />
        <img src="https://img.shields.io/badge/Platform-Android-brightgreen.svg?logo=android" alt="Badge Android" />
        <img src="https://img.shields.io/badge/Platform-iOS%20%2F%20macOS-lightgrey.svg?logo=apple" alt="Badge iOS" />
        <img src="https://img.shields.io/badge/Platform-JVM-8A2BE2.svg?logo=openjdk" alt="Badge JVM" />
    </div>
</div>

## Grid

The **Grid** is a small library which allocates memory outside the "heap".


## Integration

```
    
kotlin {
    sourceSets {
        commonMain.dependencies {
            ...
            implementation("io.github.remmerw:grid:0.0.2")
        }
        ...
    }
}
    
```

## API

```
    
interface Memory : ReadOnlyMemory {
    fun writeBytes(bytes: ByteArray, offset: Int)

}
interface ReadOnlyMemory {
    fun size(): Int
    fun readBytes(offset: Int, length: Int): ByteArray
    fun transferTo(sink: RawSink) {
        rawSource().buffered().transferTo(sink)
    }
    fun rawSource(): RawSource
}

expect fun allocateMemory(size: Int): Memory

fun allocateReadOnlyMemory(bytes: ByteArray): ReadOnlyMemory {
    val memory = allocateMemory(bytes.size)
    memory.writeBytes(bytes, 0)
    return memory
}

```

