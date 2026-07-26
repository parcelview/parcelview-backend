package dev.parcelview.conditionals

import org.springframework.context.annotation.Conditional

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Conditional(NonBlankPropertiesCondition::class)
annotation class ConditionalOnNonBlankProperties(
    val prefix: String = "",
    val name: Array<String> = [],
) {
    companion object Attributes {
        const val PREFIX = "prefix"
        const val NAME = "name"
    }
}