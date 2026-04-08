package dev.parcelview.exceptions.config

import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration

@Configuration
@ComponentScan(basePackages = ["dev.parcelview.exceptions"])
class ExceptionModuleConfig {
}