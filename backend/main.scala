package com.github.timbess

import scala.concurrent.ExecutionContext
import scala.concurrent.Future

import org.apache.pekko
import pekko.actor.typed.ActorSystem
import pekko.actor.typed.Behavior
import pekko.actor.typed.scaladsl.AbstractBehavior
import pekko.actor.typed.scaladsl.ActorContext
import pekko.actor.typed.scaladsl.Behaviors
import pekko.http.scaladsl.server.Route
import pekko.http.scaladsl.Http
import pekko.http.scaladsl.model._
import pekko.http.scaladsl.server.Directives._
import scala.io.StdIn

object PrintMyActorRefActor {
  def apply(): Behavior[String] =
    Behaviors.setup(context => new PrintMyActorRefActor(context))
}

class PrintMyActorRefActor(context: ActorContext[String])
    extends AbstractBehavior[String](context) {

  override def onMessage(msg: String): Behavior[String] =
    msg match {
      case "printit" =>
        val secondRef = context.spawn(Behaviors.empty[String], "second-actor")
        println(s"Second: $secondRef")
        this
    }
}

object Main {
  def apply(): Behavior[String] =
    Behaviors.setup(context => new Main(context))

}

class Main(context: ActorContext[String])
    extends AbstractBehavior[String](context) {
  override def onMessage(msg: String): Behavior[String] =
    msg match {
      case "start" =>
        val firstRef = context.spawn(PrintMyActorRefActor(), "first-actor")
        println(s"First: $firstRef")
        firstRef ! "printit"
        this
    }
}

object ActorHierarchyExperiments extends App {
  given system: ActorSystem[_] = ActorSystem(Behaviors.empty, "main")
  given executionContext: ExecutionContext = system.executionContext
  // implicit val system: ActorSystem[String] =
  //   ActorSystem(Behaviors.empty, "main")
  // implicit val executionContext: ExecutionContext = system.executionContext

  val route: Route =
    path("hello") {
      get {
        complete(
          HttpEntity(
            ContentTypes.`text/html(UTF-8)`,
            "<h1>Say hello to Pekko HTTP</h1>"
          )
        )
      }
    }

  val host = "localhost"
  val port = 8080
  val url = s"http://$host:$port/hello"

  val bindingFuture = Http().newServerAt(host, port).bind(route)

  // import org.slf4j.LoggerFactory
  // val foo = LoggerFactory.getLogger("com.github.timbess")
  println(
    s"Server now online. Please navigate to ${url}\nPress RETURN to stop..."
  )
  StdIn.readLine() // let it run until user presses return
  bindingFuture
    .flatMap(_.unbind()) // trigger unbinding from the port
    .onComplete(_ => system.terminate()) // and shutdown when done

}
