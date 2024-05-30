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
import org.http4s.HttpApp
import cats.effect.kernel.Ref
import cats.effect.kernel.Sync

object Main extends IOApp.Simple {
  implicit val logging: LoggerFactory[IO] = Slf4jFactory.create[IO]

  def routes[F[_]: Monad](ref: Ref[F, Int]): HttpRoutes[F] = {
    val dsl = Http4sDsl[F]
    import dsl._
    HttpRoutes.of[F] { case GET -> Root / "hello" =>
      ref.get.flatMap { count =>
        ref
          .update(_ + 1) >>
          Ok(s"Hello, you are visitor number $count")
      }
    }
  }

  def program[F[_]: Sync]: F[HttpApp[F]] = for {
    ref <- Ref.of[F, Int](0)
  } yield routes[F](ref).orNotFound

  override def run: IO[Unit] =
    program[IO].flatMap { app =>
      EmberServerBuilder
        .default[IO]
        .withHost(ipv4"0.0.0.0")
        .withPort(port"8080")
        .withHttpApp(app)
        .build
        .useForever
    }
}
