While migrating from Spring Boot 3.5.8 to 4.0.0 (including the migration to Jackson 3), I noticed a difference in how GraphQL responses are serialized.
With the following Jackson configuration, I expected no empty properties to be included in GraphQL responses (similar to regular REST responses).

```kotlin
jacksonMapperBuilder()
    .apply {
        changeDefaultPropertyInclusion { it.withValueInclusion(JsonInclude.Include.NON_EMPTY) }
    }.build()
```

Example of a REST response:
```json
[
  {
    "name": "Vehicle #1",
    "price": 10.0,
    "transmissions": [
      "Automatic",
      "Manual"
    ]
  },
  {
    "name": "Vehicle #2",
    "transmissions": [
      "Automatic",
      "Manual"
    ]
  },
  {
    "name": "Vehicle #3",
    "price": 10.0
  },
  {
    "name": "Vehicle #4"
  }
]

```

Example of a GraphQL response:
```json
{
  "data": {
    "vehicles": [
      {
        "name": "Vehicle #1",
        "price": 10.0,
        "transmissions": [
          "Automatic",
          "Manual"
        ]
      },
      {
        "name": "Vehicle #2",
        "price": null,
        "transmissions": [
          "Automatic",
          "Manual"
        ]
      },
      {
        "name": "Vehicle #3",
        "price": 10.0,
        "transmissions": null
      },
      {
        "name": "Vehicle #4",
        "price": null,
        "transmissions": []
      }
    ]
  }
}
```

Notice the inclusion of `null` and empty collections in the GraphQL response that are not included in the REST response.

Even with the Jackson 2 defaults, the result is the same:
```kotlin
JsonMapper.builderWithJackson2Defaults()
    .apply {
        changeDefaultPropertyInclusion { it.withValueInclusion(JsonInclude.Include.NON_EMPTY) }
    }.build()
```

Before migrating to Spring Boot 4.0.0 both responses where similar and no `null` or empty collections where included.
The only way to achieve the same result is by setting `NON_EMPTY` for the content inclusion configuration of Jackson:
```kotlin
jacksonMapperBuilder()
    .apply {
        changeDefaultPropertyInclusion { it.withValueInclusion(JsonInclude.Include.NON_EMPTY) }
        changeDefaultPropertyInclusion { it.withContentInclusion(JsonInclude.Include.NON_EMPTY) }
    }.build()
```

With this configuration, the example REST response is the same, but the GraphQL response is now:
```json
{
  "data": {
    "vehicles": [
      {
        "name": "Vehicle #1",
        "price": 10.0,
        "transmissions": [
          "Automatic",
          "Manual"
        ]
      },
      {
        "name": "Vehicle #2",
        "transmissions": [
          "Automatic",
          "Manual"
        ]
      },
      {
        "name": "Vehicle #3",
        "price": 10.0
      },
      {
        "name": "Vehicle #4"
      }
    ]
  }
}
```

I understand that this configuration works because GraphQL response data is actually a `LinkedHashMap`, whose serialization is affected by the content inclusion configuration of Jackson. However, if the service is exposing `Map`s in the REST API it would be affected by this configuration, which may not be desired.

My questions are:
1. Is this behaviour change expected?
2. Is there any way to work around it other than the way I described?

Maybe this is not the right project to open this issue, if this is the case kindly let me know which one I should address. Thanks!
