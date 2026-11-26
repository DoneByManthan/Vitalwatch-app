package com.vitalwatch.app.inference

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.vitalwatch.app.domain.model.HRVFeatures
import com.vitalwatch.app.domain.model.InferenceResult
import dagger.hilt.android.qualifiers.ApplicationContext
import java.nio.FloatBuffer
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OnnxInferenceEngine @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    private val tag = "OnnxInferenceEngine"
    private var ortEnv: OrtEnvironment? = null
    private var ortSession: OrtSession? = null
    private var normParams: NormalizationParams? = null
    private var isInitialized = false

    init {
        try {
            initEngine()
        } catch (e: Exception) {
            Log.e(tag, "Failed to initialize ONNX engine", e)
        }
    }

    private fun initEngine() {
        ortEnv = OrtEnvironment.getEnvironment()

        // Load model from assets
        val modelBytes = context.assets.open("glucose_model.onnx").readBytes()
        ortSession = ortEnv?.createSession(modelBytes, OrtSession.SessionOptions())

        // Load normalization params from assets
        val paramsJson = context.assets.open("normalization_params.json")
            .bufferedReader().readText()
        val type = object : TypeToken<Map<String, Map<String, Float>>>() {}.type
        val raw: Map<String, Map<String, Float>> = Gson().fromJson(paramsJson, type)
        val featMap = raw.mapValues { (_, v) ->
            FeatureNorm(
                min = v["min"] ?: 0f,
                max = v["max"] ?: 1f
            )
        }
        normParams = NormalizationParams(featMap)
        isInitialized = true
        Log.d(tag, "ONNX inference engine initialized")
    }

    fun predict(features: HRVFeatures, signalQuality: Int): InferenceResult? {
        if (!isInitialized) {
            Log.w(tag, "Engine not initialized")
            return null
        }
        return try {
            val rawFeatures = features.toFloatArray()
            val normalized = normParams?.normalize(rawFeatures) ?: rawFeatures

            // Build input tensor [1, 18]
            val env = ortEnv ?: return null
            val session = ortSession ?: return null

            val floatBuffer = FloatBuffer.wrap(normalized)
            val shape = longArrayOf(1L, 18L)
            val tensor = OnnxTensor.createTensor(env, floatBuffer, shape)

            // Run inference
            val inputName = session.inputNames.first()
            val result = session.run(mapOf(inputName to tensor))

            // Extract outputs
            val outputTensor = result[0].value as Array<FloatArray>
            val glucoseMgdl = outputTensor[0][0]

            // Derive supplementary estimates from HRV
            val heartRateBpm = if (features.meanNn > 0) 60000f / features.meanNn else 75f
            val systolicMmhg = estimateSystolic(features)
            val diastolicMmhg = estimateDiastolic(features)

            val isReliable = signalQuality > 60
            val confidenceScore = calculateConfidence(normalized, signalQuality)

            tensor.close()
            result.close()

            InferenceResult(
                glucoseMgdl = glucoseMgdl.coerceIn(40f, 400f),
                systolicMmhg = systolicMmhg.coerceIn(70f, 200f),
                diastolicMmhg = diastolicMmhg.coerceIn(40f, 130f),
                heartRateBpm = heartRateBpm.coerceIn(30f, 220f),
                isReliable = isReliable,
                confidenceScore = confidenceScore,
                timestamp = System.currentTimeMillis()
            )
        } catch (e: Exception) {
            Log.e(tag, "Inference failed", e)
            null
        }
    }

    // Heuristic BP estimates from HRV (to be replaced by dedicated model)
    private fun estimateSystolic(f: HRVFeatures): Float {
        val base = 120f
        val stressComponent = (f.si - 100f) * 0.05f
        val hrvComponent = -(f.sdsd - 30f) * 0.1f
        return base + stressComponent + hrvComponent
    }

    private fun estimateDiastolic(f: HRVFeatures): Float {
        val base = 80f
        val stressComponent = (f.si - 100f) * 0.03f
        return base + stressComponent
    }

    private fun calculateConfidence(normalized: FloatArray, signalQuality: Int): Float {
        val featureVariance = normalized.asSequence().map { (it - 0.5f) * (it - 0.5f) }.average().toFloat()
        val qualityFactor = signalQuality / 100f
        val variance = (1f - (featureVariance * 2f)).coerceIn(0f, 1f)
        return ((qualityFactor * 0.6f) + (variance * 0.4f)).coerceIn(0f, 1f)
    }

    fun close() {
        ortSession?.close()
        ortEnv?.close()
        isInitialized = false
    }
}
