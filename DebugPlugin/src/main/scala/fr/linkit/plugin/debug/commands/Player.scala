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

import fr.linkit.api.connection.cache.obj.behavior.annotation.BasicRemoteInvocationRule.BROADCAST
import fr.linkit.api.connection.cache.obj.behavior.annotation.{MethodControl => MC}

import scala.annotation.meta.setter
import scala.collection.mutable.ListBuffer

case class Player(@(MC@setter)(value = BROADCAST, invokeOnly = true) id: Int,
                  @(MC@setter)(value = BROADCAST, invokeOnly = true) owner: String,
                  @(MC@setter)(value = BROADCAST, invokeOnly = true) var name: String,
                  @(MC@setter)(value = BROADCAST, invokeOnly = true) var x: Long,
                  @(MC@setter)(value = BROADCAST, invokeOnly = true) var y: Long) extends Serializable {
    var list: ListBuffer[_] = ListBuffer.empty
    def this(other: Player) = {
        this(other.id, other.owner, other.name, other.x, other.y)
    }

}
