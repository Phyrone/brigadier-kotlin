[![](https://jitpack.io/v/Phyrone/brigadier-kotlin.svg)](https://jitpack.io/#Phyrone/brigardier-kotlin) [![Build Status](https://travis-ci.com/Phyrone/brigardier-kotlin.svg?branch=master)](https://travis-ci.com/Phyrone/brigardier-kotlin)

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
## Example
```kotlin
    //Command is a Test Interface (just ignore it)
    val dispatcher = CommandDispatcher<Command>()
    dispatcher.literal("stop") {
        executes {
            //exit
            exitProcess(0)
        }
    }
    dispatcher.literal("help") {
        runs {
            //"runs" Always return 0
            sender.sendMessage("All Commands:")
            dispatcher.getAllUsage(dispatcher.root, this, false).forEach {
                sender.sendMessage("> $it")
            }
        }
        argument("startswith", StringArgument) {
            runs {
                sender.sendMessage("All Commands:")
                //get the argument
                val startsWthInput = it.getArgument<String>("startswith")
                dispatcher.getAllUsage(dispatcher.root, this, false).forEach { line ->
                    if (line.startsWith(startsWthInput, true))
                        sender.sendMessage("> $line")
                }
            }
        }
    }
    dispatcher.literal("foo") {
        literal("bar") {
            literal("moreDeepFoo") {
                literal("howMuchMore") {
                    executes {
                        sender.sendMessage("to infinity")
                        return@executes 0
                    }
                }
            }
        }
    }
```
