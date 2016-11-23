package example

import java.util.concurrent.TimeUnit

import example.hello_world.{GreeterGrpc, HelloRequest}
import example.hello_world.GreeterGrpc.GreeterBlockingStub
import io.grpc.{ManagedChannel, ManagedChannelBuilder}

import scala.util.control.NonFatal

object HelloWorldClient {
  def apply(host: String, port: Int): HelloWorldClient = {
    val channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext(true).build
    val blockingStub = GreeterGrpc.blockingStub(channel)
    new HelloWorldClient(channel, blockingStub)
  }

  def main(args: Array[String]): Unit = {
    val client = HelloWorldClient("localhost", 50051)
    try {
      val user = args.headOption.getOrElse("world")
      client.greet(user)
    } finally {
      client.shutdown()
    }
  }
}

class HelloWorldClient private(
                                private val channel: ManagedChannel,
                                private val blockingStub: GreeterBlockingStub
                              ) {
  def shutdown(): Unit = {
    channel.shutdown.awaitTermination(5, TimeUnit.SECONDS)
  }

  def greet(name: String): Unit = {
    try {
      val request = HelloRequest(name)
      println(request)
      val response = blockingStub.sayHello(request)
      println(response)
    } catch {
      case NonFatal(e) =>
    }
  }
}
