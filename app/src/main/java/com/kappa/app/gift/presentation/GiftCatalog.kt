package com.kappa.app.gift.presentation

data class GiftCatalog(
    val type: String,
    val conversion_rate: Double,
    val rules: GiftRules,
    val gifts: List<GiftDefinition>
)

data class GiftRules(
    val visual_variations: Int,
    val kappa_allowed: Boolean? = null,
    val kappa_used_only_for_last_3: Boolean? = null
)

data class GiftDefinition(
    val value: Long,
    val name: String,
    val variations: List<String>
)
