/*
 * Copyright 2015 IBM Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ibm.spark.kernel.modules

import akka.actor.{ActorRef, ActorSystem, PoisonPill, Props}
import com.ibm.spark.kernel.module.ModuleLike
import com.ibm.spark.kernel.protocol.v5.{SocketType, SystemActorType}
import com.ibm.spark.kernel.protocol.v5.dispatch.StatusDispatch
import com.ibm.spark.kernel.protocol.v5.kernel.ActorLoader
import com.ibm.spark.kernel.protocol.v5.kernel.socket.{SocketFactory, SocketConfig, Heartbeat}
import com.ibm.spark.utils.LogLike

/**
 * Represents the module for creating a heartbeat actor.
 *
 * @param actorSystem The actor system to use
 * @param socketConfig The socket configuration to use
 * @param socketFactory The factory to create sockets
 */
class HeartbeatActorModule(
  private val actorSystem: ActorSystem,
  private val socketConfig: SocketConfig,
  private val socketFactory: SocketFactory
) extends ModuleLike with LogLike {
  private var heartbeatActor: Option[ActorRef] = None

  override def isInitialized: Boolean = heartbeatActor.nonEmpty

  override def startImpl(): Unit = {
    val port = socketConfig.hb_port

    logger.debug(s"Initializing Heartbeat on port $port")
    heartbeatActor = Some(actorSystem.actorOf(
      Props(classOf[Heartbeat], socketFactory),
      name = SocketType.Heartbeat.toString
    ))
  }

  override def stopImpl(): Unit = {
    heartbeatActor.get ! PoisonPill
    heartbeatActor = None
  }
}
