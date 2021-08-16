/*
 *  Copyright (c) 2021. Linkit and or its affiliates. All rights reserved.
 *  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 *  This code is free software; you can only use it for personal uses, studies or documentation.
 *  You can download this source code, and modify it ONLY FOR PERSONAL USE and you
 *  ARE NOT ALLOWED to distribute your MODIFIED VERSION.
 *
 *  Please contact maximebatista18@gmail.com if you need additional information or have any
 *  questions.
 */

package fr.linkit.plugin.debug.commands

import fr.linkit.api.connection.cache.SharedCacheManager
import fr.linkit.api.connection.cache.obj.behavior.annotation.BasicInvocationRule
import fr.linkit.engine.connection.cache.obj.DefaultSynchronizedObjectCenter
import fr.linkit.engine.connection.cache.obj.behavior.WrapperBehaviorBuilder.MethodControl
import fr.linkit.engine.connection.cache.obj.behavior.{AnnotationBasedMemberBehaviorFactory, WrapperBehaviorBuilder, WrapperBehaviorTreeBuilder}
import fr.linkit.plugin.controller.cli.{CommandException, CommandExecutor, CommandUtils}

import scala.collection.mutable.ListBuffer

class PlayerCommand(cacheHandler: SharedCacheManager, currentIdentifier: String) extends CommandExecutor {

    /*println("Press enter to continue...")
    new Scanner(System.in).nextLine()*/

    private val tree = new WrapperBehaviorTreeBuilder(AnnotationBasedMemberBehaviorFactory) {
        behaviors += new WrapperBehaviorBuilder[ListBuffer[Player]]() {
            annotateAllMethods("+=") and "addOne" by MethodControl(BasicInvocationRule.BROADCAST, invokeOnly = true, synchronizedParams = Seq(true))
        }
    }.build

    private val repo    = cacheHandler.attachToCache(50, DefaultSynchronizedObjectCenter[ListBuffer[Player]](tree))
    private val players = repo.getOrPost(0)(ListBuffer.empty[Player])
    println(s"players = ${players}")
    /*println(s"players.getClass.getDeclaredFields = ${players.getClass.getDeclaredFields.mkString("Array(", ", ", ")")}")
    println(s"LOL")*/

    override def execute(implicit args: Array[String]): Unit = {
        val order = if (args.length == 0) "" else args(0)
        //println(s"players.toSeq = ${players}")
        //println(s"players.getChoreographer.isMethodExecutionForcedToLocal = ${players.getChoreographer.isMethodExecutionForcedToLocal}")
        order match {
            case "create"   => createPlayer(args.drop(1)) //remove first arg which is obviously 'create'
            case "update"   => updatePlayer(args.drop(1)) //remove first arg which is obviously 'update'
            case "reinject" => reInjectPlayer(args.drop(1)) //remove first arg which is obviously 'update'
            case "list"     =>
                val content = players.toArray
                println(s"players: $players")
            case "desc"     => describePlayerClass()
            case _          => throw CommandException("usage: player [create|update|reinject|list|desc] [...]")
        }
    }

    private def reInjectPlayer(args: Array[String]): Unit = {
        val id = args.head.toInt
        players += players.find(_.id == id).get
        println("Player re-injected !")
    }

    private def createPlayer(args: Array[String]): Unit = {
        implicit val usage: String = "usage: player create [id=?|name=?|x=?|y=?]"

        val id     = CommandUtils.getValue("id", args).toInt
        val name   = CommandUtils.getValue("name", args)
        val x      = CommandUtils.getValue("x", args).toInt
        val y      = CommandUtils.getValue("y", args).toInt
        val player = Player(id, currentIdentifier, name, x, y)
        player.list = players

        //println(s"Created $player ! (identifier = $id)")
        players += player
        println(s"Added player $player in $players")
    }

    private def describePlayerClass(): Unit = {
        classOf[Player].getDeclaredFields.foreach(println)
    }

    private def updatePlayer(args: Array[String]): Unit = {
        implicit val usage: String = "usage: player update [id=?] <name=?|x=?|y=?>"
        val id     = CommandUtils.getValue("id", args).toInt
        val player = players.find(_.id == id).getOrElse(throw CommandException("Player not found."))

        val name = CommandUtils.getValue("name", player.name, args)
        val x    = CommandUtils.getValue("x", player.x.toString, args).toInt
        val y    = CommandUtils.getValue("y", player.y.toString, args).toInt

        println(s"Updating player $player...")
        player.x = x
        player.y = y
        player.name = name
        println(s"Player is now $player")
    }

}