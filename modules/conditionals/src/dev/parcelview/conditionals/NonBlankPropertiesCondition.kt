package dev.parcelview.conditionals

import org.springframework.context.annotation.ConditionContext
import org.springframework.context.annotation.ConfigurationCondition
import org.springframework.core.type.AnnotatedTypeMetadata

class NonBlankPropertiesCondition : ConfigurationCondition {

    override fun getConfigurationPhase() = ConfigurationCondition.ConfigurationPhase.REGISTER_BEAN

    override fun matches(context: ConditionContext, metadata: AnnotatedTypeMetadata): Boolean {
        val attributes = metadata.getAnnotationAttributes(
            ConditionalOnNonBlankProperties::class.qualifiedName!!
        ) ?: return false

        val prefix = (attributes[ConditionalOnNonBlankProperties.PREFIX] as? String)
            ?.trimEnd('.')
            ?.let { if (it.isNotBlank()) "$it." else "" }
            .orEmpty()

        val keys = (attributes[ConditionalOnNonBlankProperties.NAME] as? Array<*>)
            .orEmpty()
            .filterIsInstance<String>()
            .map { "$prefix$it" }

        if (keys.isEmpty()) return false

        return keys.all { key ->
            context.environment.getProperty(key)?.isNotBlank() == true
        }
    }
}