//#full-example
package com.example

import java.nio.file.Paths

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl._
import akka.util.ByteString

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.language.postfixOps

object AkkaQuickstart extends App {
    implicit val actorSystem = ActorSystem()

    import actorSystem.dispatcher

    implicit val flowMaterializer = ActorMaterializer()

    val logFile = Paths.get("src/main/resources/data1.csv")

    val source = FileIO.fromPath(logFile)

    val flow = Framing
               .delimiter(ByteString(System.lineSeparator()), maximumFrameLength = 512, allowTruncation = true)
               .map(_.utf8String)

    val sink = Sink.foreach(println)

    source
    .via(flow)
    .runWith(sink)
    .andThen {
        case _ =>
            actorSystem.terminate()
            Await.ready(actorSystem.whenTerminated, 1 minute)
    }
}
