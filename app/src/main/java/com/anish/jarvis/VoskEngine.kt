package com.anish.jarvis

import android.content.Context
import org.vosk.LibVosk
import org.vosk.LogLevel
import org.vosk.Model
import org.vosk.SpeakerModel
import java.io.IOException

/** Keeps the (large) offline models loaded once per process. */
object VoskEngine {
    const val SAMPLE_RATE = 16000f

    private var model: Model? = null
    private var modelPath: String? = null
    private var speaker: SpeakerModel? = null

    @Synchronized
    fun model(ctx: Context): Model {
        val info = ModelManager.speechModel(ctx)
        if (!ModelManager.isReady(ctx, info)) throw IOException("${info.label} not downloaded yet")
        val path = ModelManager.dir(ctx, info).absolutePath
        if (model == null || modelPath != path) {
            LibVosk.setLogLevel(LogLevel.WARNINGS)
            model = Model(path)
            modelPath = path
        }
        return model!!
    }

    @Synchronized
    fun speaker(ctx: Context): SpeakerModel {
        if (!ModelManager.isReady(ctx, ModelManager.SPEAKER)) throw IOException("Speaker model not downloaded yet")
        if (speaker == null) speaker = SpeakerModel(ModelManager.dir(ctx, ModelManager.SPEAKER).absolutePath)
        return speaker!!
    }
}
