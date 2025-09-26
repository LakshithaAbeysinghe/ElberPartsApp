package com.elber.parts.util

import java.security.MessageDigest

object Security {
    fun hash(input: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val bytes = md.digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
