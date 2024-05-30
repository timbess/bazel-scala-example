package com.github.timbess.cats

import cats.effect.IO
import cats.effect.IOApp
import cats.kernel.instances._
import cats.implicits._
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import org.http4s.implicits._
import io.circe.literal._
import org.http4s.EntityEncoder
import cats.Monad
import org.http4s.ember.server.EmberServerBuilder
import org.typelevel.log4cats.LoggerFactory
import org.typelevel.log4cats.slf4j.Slf4jFactory
import com.comcast.ip4s._

object Main extends IOApp.Simple {
  implicit val logging: LoggerFactory[IO] = Slf4jFactory.create[IO]

  def routes[F[_]: Monad]: HttpRoutes[F] = {
    val dsl = Http4sDsl[F]
    import dsl._
    HttpRoutes.of[F] { case GET -> Root / "hello" =>
      Ok("Example")
    }
  }
  override def run: IO[Unit] =
    EmberServerBuilder
      .default[IO]
      .withHost(ipv4"0.0.0.0")
      .withPort(port"8080")
      .withHttpApp(routes[IO].orNotFound)
      .build
      .useForever
}
