package math.flow

import java.util.concurrent.TimeUnit

import _root_.math.math.MathGrpc.MathStub
import _root_.math.math._
import io.grpc.ClientCall.Listener
import io.grpc.netty.NettyChannelBuilder
import io.grpc.{CallOptions, Metadata, Status}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future, Promise}

object MathFlowControlWorldClient {
  def main(args: Array[String]): Unit = {
    // if use NettyChannelBuilder instead of ChannelBuilder, we can setMaxMessageSize or some options
    val channel = NettyChannelBuilder.forAddress("localhost", 50051).usePlaintext(true).maxMessageSize(1024 * 1024 * 1024).build
    val stub = MathGrpc.stub(channel)
    val client = new MathFlowControlWorldClient(stub)

    val divManyPromise = Promise[Unit]()
    try {
      client.divMany(divManyPromise)
      Await.result(divManyPromise.future, Duration(600, TimeUnit.MINUTES))
    } finally {
      channel.shutdown().awaitTermination(5, TimeUnit.SECONDS)
    }
  }
}

class MathFlowControlWorldClient(stub: MathStub) {
  def divMany(promise: Promise[Unit]): Unit = {
    // stub.divMany() Flow-controll version. see http://www.grpc.io/grpc-java/javadoc/
    val call = stub.getChannel.newCall(MathGrpc.METHOD_DIV_MANY, CallOptions.DEFAULT)
    val listener = new Listener[DivReply] {
      private var dividend = 1
      private var divisor = 3

      override def onHeaders(headers: Metadata): Unit = {
        println(s"onHeaders ${headers.toString}")
      }

      override def onReady(): Unit = {
        while (call.isReady) {
          if (dividend < 100) {
            val req = DivArgs(dividend, divisor)
            call.sendMessage(req)
            dividend += 1
            divisor += 2
          } else {
            println(s"onReady closing")
            call.halfClose()
          }
        }
      }

      var i = 0

      override def onMessage(message: DivReply): Unit = {
        i = i + 1
        println(s"onMessage + $i")
        Future {
          Thread.sleep(2000)
          call.request(1)
        }(concurrent.ExecutionContext.global)
      }

      override def onClose(status: Status, trailers: Metadata): Unit = {
        println(s"onClose status ${status.toString} + trailers: ${trailers.toString}")
        promise.success(())
      }
    }

    call.start(listener, new Metadata)

    // it's needed for get 1st message
    call.request(1)
  }
}
