package com.vitalwatch.app.inference

data class FeatureNorm(val min: Float, val max: Float)

data class NormalizationParams(
    val features: Map<String, FeatureNorm>
) {
    fun normalize(featureValues: FloatArray): FloatArray {
        val keys = listOf(
            "ppg_rate_sd", "sd1d", "sdrmssd", "mean_nn", "fuzzy_en",
            "sdsd", "cvnn", "tp", "cvi", "min_nn", "mad_nn", "mcvnn",
            "pi", "si", "max_nn", "shan_en", "prc20nn", "mfdfa_alpha1_mean"
        )
        return FloatArray(featureValues.size) { i ->
            val norm = features[keys[i]] ?: FeatureNorm(0f, 1f)
            val range = norm.max - norm.min
            if (range == 0f) 0f else (featureValues[i] - norm.min) / range
        }
    }
}
