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

package fr.linkit.plugin.controller.cli

import fr.linkit.api.local.concurrency.ProcrastinatorControl
import fr.linkit.api.local.plugin.fragment.PluginFragment

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.util.control.NonFatal

class CommandManager(procrastinator: ProcrastinatorControl) extends PluginFragment {

    private val commands          : mutable.Map[String, CommandExecutor] = mutable.Map.empty
    @volatile private var alive                                          = true

    def register(command: String, executor: CommandExecutor): Unit =
        commands.put(command.toLowerCase, executor)

    override def start(): Unit = {
        val thread = new Thread(() => {
            while (alive)
                perform(InputConsole.requestNextInput())
        })

        thread.setName("Command listener Thread")
        thread.setDaemon(true)
        thread.start()
    }

    def perform(command: String): Unit = {
        if (command == null)
            return
        if (command.startsWith("help")) {
            commands.foreach(cmd => println(cmd._1 + " -> " + cmd._2.getClass.getSimpleName))
            return
        }
        val args = parseLine(command.trim())
        val cmd  = command.takeWhile(c => !Character.isWhitespace(c)).toLowerCase
        if (!commands.contains(cmd)) {
            Console.err.println(s"cmd '$cmd' not found.")
            return
        }

        procrastinator.runLater {
            try {
                commands(cmd).execute(args)
            } catch {
                case e@(_: CommandException) => Console.err.println(e.getMessage)
                case NonFatal(e)             => e.printStackTrace()
            }
        }
    }

    private def parseLine(line: String): Array[String] = {
        val argBuilder = new StringBuilder
        val args       = ListBuffer.empty[String]

        //exclude first arg, which is the command label
        val indexOfFirstBlankLine = line.indexWhere(Character.isWhitespace)
        if (indexOfFirstBlankLine == -1)
            return Array()
        val rawArgs = line.substring(indexOfFirstBlankLine).trim()

        var insideString = false
        var last         = '\u0000'
        for (c <- rawArgs) {
            if (c == '"' && last != '\\')
                insideString = !insideString
            else if (!c.isWhitespace || (insideString && last != '\\'))
                argBuilder.append(c)
            else if (!last.isWhitespace) {
                args += argBuilder.toString()
                argBuilder.clear()
            }
            last = c
        }
        args += argBuilder.toString()
        args.toArray
    }

    override def destroy(): Unit = {
        commands.clear()
        alive = false
    }
}
