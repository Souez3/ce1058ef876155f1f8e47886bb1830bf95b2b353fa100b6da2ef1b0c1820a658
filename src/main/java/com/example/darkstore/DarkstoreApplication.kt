package com.example.darkstore

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class DarkstoreApplication

fun main(args: Array<String>) {
    runApplication<DarkstoreApplication>(*args)
}
