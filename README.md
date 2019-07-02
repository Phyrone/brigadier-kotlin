[![](https://jitpack.io/v/Phyrone/brigardier-kotlin.svg)](https://jitpack.io/#Phyrone/brigardier-kotlin) [![Build Status](https://travis-ci.com/Phyrone/brigardier-kotlin.svg?branch=master)](https://travis-ci.com/Phyrone/brigardier-kotlin)

# Brigardier-Kotlin
## This is an Extension for Mojangs [Brigadier](https://github.com/Mojang/brigadier) with Kotlin
Example with "String" as source
```kotlin
val dispatcher = CommandDispatcher<String>()
dispatcher.liternal("printString") {
   executes {
      println(this)
      return@executes 0
   }
}
```
