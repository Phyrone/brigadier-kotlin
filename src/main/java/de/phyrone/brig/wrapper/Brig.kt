package de.phyrone.brig.wrapper

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import kotlinx.coroutines.*


interface CommandNode<T> {
    fun alias(alias: String)
    fun require(onCheck: T.() -> Boolean)
    fun executes(executed: suspend T.(context: CommandContext<T>) -> Int)
    fun command(name: String, setup: CommandNode<T>.() -> Unit)
    fun <S> argument(name: String, type: ArgumentType<S>, setup: CommandNode<T>.() -> Unit)
}

abstract class AbstractCommandNodeImpl<T>(private val arg: ArgumentBuilder<T, *>) : CommandNode<T> {


    private val childSet = hashSetOf<AbstractCommandNodeImpl<*>>()
    private var aliasSet: HashSet<LiteralArgumentBuilder<T>>? = hashSetOf()
    override fun require(onCheck: T.() -> Boolean) {
        arg.requires { onCheck.invoke(it) }
    }

    override fun alias(alias: String) {
        val lit = LiteralArgumentBuilder.literal<T>(alias)
        aliasSet?.add(lit) ?: addLitAsAlias(lit)
    }

    private fun addLitAsAlias(literalArgumentBuilder: LiteralArgumentBuilder<T>) {
        literalArgumentBuilder.redirect(arg.build())
    }

    override fun <S> argument(name: String, type: ArgumentType<S>, setup: CommandNode<T>.() -> Unit) {
        val arg = RequiredArgumentBuilder.argument<T, S>(name, type)
        addChildNode(arg, setup)
    }

    override fun executes(executed: suspend T.(context: CommandContext<T>) -> Int) {
        arg.executes { context ->
            runBlocking {
                executed.invoke(context.source, context)
            }
        }
    }

    private fun addChildNode(arg: ArgumentBuilder<T, *>, setup: CommandNode<T>.() -> Unit) {
        val node = ChildCommandNodeImpl(arg, this)
        childSet.add(node)
        setup.invoke(node)
    }

    override fun command(name: String, setup: CommandNode<T>.() -> Unit) {
        val lit = LiteralArgumentBuilder.literal<T>(name)
        addChildNode(lit, setup)
    }

    internal fun addChildArg(toParentArg: ArgumentBuilder<T, *>) {
        arg.then(toParentArg)
    }

    internal fun postReqister() {
        /* for Later */
        aliasSet?.forEach { lit ->
            addLitAsAlias(lit)
        }
        aliasSet = null
        childSet.forEach { node ->
            node.postReqister()
        }
    }

    abstract fun addToParent(toParentArg: LiteralArgumentBuilder<T>)
}

class ChildCommandNodeImpl<T>(arg: ArgumentBuilder<T, *>, private val parentNode: AbstractCommandNodeImpl<T>) : AbstractCommandNodeImpl<T>(arg) {
    override fun addToParent(toParentArg: LiteralArgumentBuilder<T>) {
        parentNode.addChildArg(toParentArg)
    }
}

class RootCommandNodeImpl<T>(arg: ArgumentBuilder<T, *>, val dispatcher: CommandDispatcher<T>) : AbstractCommandNodeImpl<T>(arg) {
    override fun addToParent(toParentArg: LiteralArgumentBuilder<T>) {
        dispatcher.register(toParentArg)
    }
}

fun <T> CommandDispatcher<T>.command(name: String, setup: CommandNode<T>.() -> Unit) {
    val literal = LiteralArgumentBuilder.literal<T>(name)
    val node = RootCommandNodeImpl<T>(literal, this)
    setup.invoke(node)
    node.postReqister()
}

fun main(args: Array<String>) {
    val dispatcher = CommandDispatcher<String>()
    dispatcher.command("rl") {
        executes {
            0
        }
    }
}