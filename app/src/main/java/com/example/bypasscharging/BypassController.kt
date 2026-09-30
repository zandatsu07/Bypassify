package com.example.bypasscharging

/** Shared root logic used by both the app and the Quick Settings tile. Call off the main thread. */
object BypassController {

    /** enabled == null means unknown/unset; error != null means the command failed. */
    data class Status(val enabled: Boolean?, val error: String? = null, val raw: String = "")

    private data class Result(val ok: Boolean, val out: String)

    private fun runRoot(cmd: String): Result = try {
        val p = Runtime.getRuntime().exec(arrayOf("su", "-c", cmd))
        val out = p.inputStream.bufferedReader().readText().trim()
        val err = p.errorStream.bufferedReader().readText().trim()
        val code = p.waitFor()
        Result(code == 0, if (out.isNotEmpty()) out else err)
    } catch (e: Exception) {
        Result(false, e.message ?: "Root not available")
    }

    fun read(): Status {
        val r = runRoot("settings get system pass_through")
        return when {
            !r.ok -> Status(null, r.out.ifEmpty { "Root command failed" })
            r.out == "1" -> Status(true, raw = r.out)
            r.out == "0" -> Status(false, raw = r.out)
            else -> Status(null, raw = r.out)
        }
    }

    fun write(enabled: Boolean): Status {
        val r = runRoot("settings put system pass_through ${if (enabled) 1 else 0}")
        return if (r.ok) read() else Status(null, r.out.ifEmpty { "Root command failed" })
    }
}
