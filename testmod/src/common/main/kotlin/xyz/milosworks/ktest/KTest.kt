package xyz.milosworks.ktest

import com.mojang.logging.LogUtils
import org.slf4j.Logger
import xyz.milosworks.klib.KLib

object KTest {
    const val ID = "klib_test"

    val LOGGER: Logger = LogUtils.getLogger()

    fun init() {
        LOGGER.info("Hello Common world from Kotlin TestMod!")
        KLib.LOGGER.info("Calling KLib logger from Kotlin TestMod!")
    }
}