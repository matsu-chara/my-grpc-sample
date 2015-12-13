package example

import example.hello_world.{GreeterGrpc, HelloRequest, HelloResponse}
import io.grpc.{Server, ServerBuilder}

import scala.concurrent.{ExecutionContext, Future}

object HelloWorldServer {
  private val port = 50051

  def main(args: Array[String]) {
    val server = new HelloWorldServer(ExecutionContext.global)
    server.start()
    server.blockUntilShutdown()
  }
}

class HelloWorldServer(executionContext: ExecutionContext) { self =>
  private[this] var server: Server = null

  private def start(): Unit = {
    server = ServerBuilder.forPort(HelloWorldServer.port).addService(GreeterGrpc.bindService(new GreeterImpl, executionContext)).build.start

    Runtime.getRuntime.addShutdownHook(new Thread() {
      override def run(): Unit = {
        self.stop()
      }
    })
  }

  private def stop(): Unit = {
    if (server != null) {
      server.shutdown()
    }
  }

  private def blockUntilShutdown(): Unit = {
    if (server != null) {
      server.awaitTermination()
    }
  }

  private class GreeterImpl extends GreeterGrpc.Greeter {
    override def sayHello(request: HelloRequest): Future[HelloResponse] = {
      println(request)
      val reply = HelloResponse(message = "Hello " + request.name)
      println(reply)
      Future.successful(reply)
    }
  }
}

