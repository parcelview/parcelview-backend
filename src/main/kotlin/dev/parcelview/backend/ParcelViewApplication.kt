package dev.parcelview.backend

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ParcelViewApplication

fun main(args: Array<String>) {
	runApplication<ParcelViewApplication>(*args)
}
