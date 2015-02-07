/*
 * Copyright 2015 IBM Corp.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.ibm.spark.kernel.protocol.v5.handler

import akka.actor.ActorRef
import com.ibm.spark.comm.{CommRegistrar, CommStorage}
import com.ibm.spark.kernel.protocol.v5.{SystemActorType, OrderedSupport, KernelMessage}
import com.ibm.spark.kernel.protocol.v5.content.{InputReply, CommOpen}
import com.ibm.spark.kernel.protocol.v5.kernel.{Utilities, ActorLoader}
import com.ibm.spark.kernel.protocol.v5
import com.ibm.spark.utils.MessageLogSupport
import play.api.libs.json.Json

import scala.concurrent.{Promise, Future}

/**
 * Represents the handler for both input request and input reply messages. Does
 * not extend BaseHandler because it does not send busy/idle status messages.
 *
 * @param actorLoader The actor loader to use for actor communication
 * @param responseMap The map used to maintain the links to responses
 */
class InputRequestReplyHandler(
  actorLoader: ActorLoader,
  responseMap: collection.mutable.Map[String, ActorRef]
) extends OrderedSupport with MessageLogSupport
{
  // TODO: Is there a better way than storing actor refs?
  def receive = {
    case kernelMessage: KernelMessage =>
      startProcessing()

      val kernelMessageType = kernelMessage.header.msg_type
      val inputRequestType = v5.MessageType.Outgoing.InputRequest.toString
      val inputReplyType = v5.MessageType.Incoming.InputReply.toString

      // Is this an outgoing message to request data?
      if (kernelMessageType == inputRequestType) {
        val session = kernelMessage.parentHeader.session
        responseMap(session) = sender

        logger.debug("Associating input request with session " + session)

        actorLoader.load(SystemActorType.KernelMessageRelay) ! kernelMessage

      // Is this an incoming response to a previous request for data?
      } else if (kernelMessageType == inputReplyType) {
        val session = kernelMessage.header.session
        val inputReply = Json.parse(kernelMessage.contentString).as[InputReply]

        logger.debug(s"Received input reply for session $session with value " +
          s"'${inputReply.value}'")

        responseMap(session) ! inputReply.value
        responseMap.remove(session)
      }

      finishedProcessing()
  }

  override def orderedTypes() : Seq[Class[_]] = {Seq(classOf[KernelMessage])}
}

