package com.vitalwatch.app.domain.model

data class HRVFeatures(
    val timestamp: Long,
    val sequenceNumber: Long,
    val ppgRateSd: Float,
    val sd1d: Float,
    val sdrmssd: Float,
    val meanNn: Float,
    val fuzzyEn: Float,
    val sdsd: Float,
    val cvnn: Float,
    val tp: Float,
    val cvi: Float,
    val minNn: Float,
    val madNn: Float,
    val mcvnn: Float,
    val pi: Float,
    val si: Float,
    val maxNn: Float,
    val shanEn: Float,
    val prc20nn: Float,
    val mfdfaAlpha1Mean: Float
) {
    fun toFloatArray(): FloatArray = floatArrayOf(
        ppgRateSd, sd1d, sdrmssd, meanNn, fuzzyEn,
        sdsd, cvnn, tp, cvi, minNn, madNn, mcvnn,
        pi, si, maxNn, shanEn, prc20nn, mfdfaAlpha1Mean
    )
}
