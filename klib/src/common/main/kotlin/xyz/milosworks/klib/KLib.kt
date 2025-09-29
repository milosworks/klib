package xyz.milosworks.klib

import com.mojang.logging.LogUtils
import org.slf4j.Logger

object KLib {
    const val ID = "klib"

    val LOGGER: Logger = LogUtils.getLogger()

    fun init() {
        LOGGER.info("Hello Common world from Kotlin!")
    }
}