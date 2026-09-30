package com.example.bypasscharging

import android.content.Context
import android.content.pm.PackageManager
import rikka.shizuku.Shizuku

/** Shared logic used by the app and the Quick Settings tile. Call off the main thread. */
object BypassController {

    enum class Mode { ROOT, SHIZUKU }

    /** enabled == null means unknown/unset; error != null means the command failed. */
    data class Status(val enabled: Boolean?, val error: String? = null, val raw: String = "")

    private data class Result(val ok: Boolean, val out: String)

    private const val PREFS = "bypassify"
    private const val KEY_MODE = "mode"

    fun getMode(ctx: Context): Mode =
        if (ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_MODE, "ROOT") == "SHIZUKU")
            Mode.SHIZUKU else Mode.ROOT

    fun setMode(ctx: Context, mode: Mode) {
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(KEY_MODE, mode.name).apply()
    }

    fun shizukuRunning(): Boolean = try { Shizuku.pingBinder() } catch (e: Throwable) { false }

    fun shizukuGranted(): Boolean = try {
        Shizuku.pingBinder() && Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
    } catch (e: Throwable) { false }

    private fun readProcess(p: Process): Result {
        val out = p.inputStream.bufferedReader().readText().trim()
        val err = p.errorStream.bufferedReader().readText().trim()
        val code = p.waitFor()
        return Result(code == 0, if (out.isNotEmpty()) out else err)
    }

    private fun runRoot(cmd: String): Result = try {
        readProcess(Runtime.getRuntime().exec(arrayOf("su", "-c", cmd)))
    } catch (e: Exception) {
        Result(false, e.message ?: "Root not available")
    }

    private fun runShizuku(cmd: String): Result {
        return try {
            if (!Shizuku.pingBinder())
                return Result(false, "Shizuku isn't running. Open Shizuku and start it.")
            if (Shizuku.checkSelfPermission() != PackageManager.PERMISSION_GRANTED)
                return Result(false, "Shizuku permission not granted. Tap the Shizuku button to allow it.")
            // Shizuku.newProcess is not public in API 13, so call it via reflection.
            val m = Shizuku::class.java.getDeclaredMethod(
                "newProcess",
                Array<String>::class.java, Array<String>::class.java, String::class.java
            )
            m.isAccessible = true
            val p = m.invoke(null, arrayOf("sh", "-c", cmd), null, null) as Process
            readProcess(p)
        } catch (e: Throwable) {
            Result(false, e.cause?.message ?: e.message ?: "Shizuku command failed")
        }
    }

    private fun run(ctx: Context, cmd: String): Result =
        if (getMode(ctx) == Mode.SHIZUKU) runShizuku(cmd) else runRoot(cmd)

    fun read(ctx: Context): Status {
        val r = run(ctx, "settings get system pass_through")
        return when {
            !r.ok -> Status(null, r.out.ifEmpty { "Command failed" })
            r.out == "1" -> Status(true, raw = r.out)
            r.out == "0" -> Status(false, raw = r.out)
            else -> Status(null, raw = r.out)
        }
    }

    fun write(ctx: Context, enabled: Boolean): Status {
        val r = run(ctx, "settings put system pass_through ${if (enabled) 1 else 0}")
        return if (r.ok) read(ctx) else Status(null, r.out.ifEmpty { "Command failed" })
    }
}
