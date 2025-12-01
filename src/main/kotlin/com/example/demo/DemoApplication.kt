package com.example.demo

import com.fasterxml.jackson.annotation.JsonInclude
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.http.codec.CodecCustomizer
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.graphql.MediaTypes
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.codec.json.JacksonJsonEncoder
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.jacksonMapperBuilder

@SpringBootApplication
class DemoApplication

fun main(args: Array<String>) {
    runApplication<DemoApplication>(*args)
}

val VEHICLES =
    listOf(
        Vehicle(name = "Vehicle #1", price = 10.0, transmissions = listOf("Automatic", "Manual")),
        Vehicle(name = "Vehicle #2", price = null, transmissions = listOf("Automatic", "Manual")),
        Vehicle(name = "Vehicle #3", price = 10.0, transmissions = null),
        Vehicle(name = "Vehicle #4", price = null, transmissions = emptyList()),
    )

@Configuration
class JacksonConfiguration {
//    @Bean
//    @Primary
//    fun jsonMapper(): JsonMapper =
//        jacksonMapperBuilder()
//            .apply {
//                changeDefaultPropertyInclusion { it.withValueInclusion(JsonInclude.Include.NON_EMPTY) }
//                // Uncommenting the line bellow fixes the GraphQL issue (GraphQL response data is a LinkedHashMap)
// //                changeDefaultPropertyInclusion { it.withContentInclusion(JsonInclude.Include.NON_NULL) }
//            }.build()

    @Bean
    fun jacksonInclusionCustomizer(): CodecCustomizer {
        val jsonMapper =
            jacksonMapperBuilder()
                .apply {
                    changeDefaultPropertyInclusion { it.withValueInclusion(JsonInclude.Include.NON_EMPTY) }
                    changeDefaultPropertyInclusion { it.withContentInclusion(JsonInclude.Include.NON_EMPTY) }
                }.build()
        return CodecCustomizer {
            it.customCodecs().register(JacksonJsonEncoder(jsonMapper, MediaTypes.APPLICATION_GRAPHQL_RESPONSE))
        }
    }
}

@RestController
@RequestMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
class VehiclesController {
    @GetMapping("/vehicles")
    @ResponseStatus(HttpStatus.OK)
    suspend fun getVehicles() = VEHICLES
}

@Controller
class VehiclesGraphQLController {
    @QueryMapping
    suspend fun vehicles() = VEHICLES
}

data class Vehicle(
    val name: String,
    val price: Double?,
    val transmissions: List<String>?,
)
