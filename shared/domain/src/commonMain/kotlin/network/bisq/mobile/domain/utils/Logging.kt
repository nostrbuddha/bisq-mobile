package network.bisq.mobile.domain.utils

import co.touchlab.kermit.Logger

private val loggerCache = mutableMapOf<String, Logger>()

interface Logging {
    val log: Logger
        get() {
            return getLogger(this)
        }
}

fun getLogger(anyObj: Any): Logger {
    val tag = anyObj::class.simpleName
    return doGetLogger(tag)
}

fun getLogger(tag: String): Logger {
    return doGetLogger(tag)
}

private fun doGetLogger(tag: String?): Logger {
    return if (tag != null) {
        loggerCache.getOrPut(tag) { Logger.withTag(tag) }
    } else {
        // Anonymous classes or lambda expressions do not provide a simpleName
        loggerCache.getOrPut("Default") { Logger }
    }
}