package xyz.milosworks.klib

import kotlinx.serialization.cbor.Cbor
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object Constants {
    const val MOD_ID = "klib"
    const val MOD_NAME = "KLibrary"
    @JvmStatic
    val LOG: Logger = LoggerFactory.getLogger(MOD_NAME)
    val x = Cbor {}
}

