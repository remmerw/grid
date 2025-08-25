package io.github.remmerw.grid

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid


interface Work {
    suspend fun run()
}

class WorkManager : AutoCloseable {

    private val works: MutableMap<String, Job> = ConcurrentHashMap()
    private val scope = CoroutineScope(Dispatchers.IO)

    fun cancel(uuid: String) {
        val job = works.remove(uuid)
        try {
            job?.cancel()
        } catch (throwable: Throwable) {
            debug(throwable)
        }
    }

    fun pruneWork() {
        works.keys.forEach { uuid -> cancel(uuid) }
    }


    @OptIn(ExperimentalUuidApi::class)
    fun start(work: Work): String {
        val uuid = Uuid.toString()

        val job = scope.launch {
            try {
                work.run()
            } catch (throwable: Throwable) {
                debug( throwable)
            } finally {
                works.remove(uuid)
            }
        }

        works.put(uuid, job)
        return uuid
    }

    override fun close() {
        pruneWork()
        try {
            scope.cancel()
        } catch (throwable: Throwable) {
            debug( throwable)
        }
    }
}