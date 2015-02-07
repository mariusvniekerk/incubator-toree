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

package com.ibm.spark.kernel.protocol.v5.input

import java.io.InputStream
import java.nio.charset.Charset

import com.ibm.spark.kernel.protocol.v5.{MessageType, SystemActorType, KMBuilder}
import com.ibm.spark.kernel.protocol.v5.content.InputRequest
import com.ibm.spark.kernel.protocol.v5.kernel.ActorLoader

import com.ibm.spark.kernel.protocol.v5.kernel.Utilities.timeout

import akka.pattern.ask

import scala.collection.mutable.ListBuffer
import scala.concurrent.{Await, Future}

/**
 * Represents an OutputStream that sends data back to the clients connect to the
 * kernel instance.
 *
 * @param actorLoader The actor loader used to access the message relay
 * @param kmBuilder The KMBuilder used to construct outgoing kernel messages
 * @param prompt The prompt to use for input requests
 * @param password Whether or not the input request is for a password
 */
class KernelInputStream(
  actorLoader: ActorLoader,
  kmBuilder: KMBuilder,
  prompt: String = "",
  password: Boolean = false
) extends InputStream {
  private val EncodingType = Charset.forName("UTF-8")
  @volatile private var internalBytes: ListBuffer[Byte] = ListBuffer()

  /**
   * Requests the next byte of data from the client.
   * @return The byte of data as an integer
   */
  override def read(): Int = {
    if (!this.hasByte) this.requestBytes()

    this.nextByte()
  }

  private def hasByte: Boolean = internalBytes.nonEmpty

  private def nextByte(): Int = {
    val byte = internalBytes.head

    internalBytes = internalBytes.tail

    byte
  }

  private def requestBytes(): Unit = {
    val inputRequest = InputRequest(prompt, password)
    // NOTE: Assuming already provided parent header and correct ids
    val kernelMessage = kmBuilder
      .withHeader(MessageType.Outgoing.InputRequest)
      .withContentString(inputRequest)
      .build

    // NOTE: The same handler is being used in both request and reply
    val responseFuture: Future[String] =
      (actorLoader.load(MessageType.Incoming.InputReply) ? kernelMessage)
      .mapTo[String]

    // Block until we get a response
    import scala.concurrent.duration._
    internalBytes ++=
      Await.result(responseFuture, Duration.Inf).getBytes(EncodingType)
  }
}
