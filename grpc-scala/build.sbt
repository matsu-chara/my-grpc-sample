import com.trueaccord.scalapb.{ScalaPbPlugin => PB}

name := "grpc-scala"

version := "1.0"

scalaVersion := "2.11.7"

PB.protobufSettings
PB.runProtoc in PB.protobufConfig := { args =>
  com.github.os72.protocjar.Protoc.runProtoc("-v300" +: args.toArray)
}
version in PB.protobufConfig := "3.0.0-beta-1"

watchSources ++= (((sourceDirectory in Compile).value / "protobuf") ** "*.proto").get

libraryDependencies += "com.trueaccord.scalapb" %% "scalapb-runtime-grpc" % (PB.scalapbVersion in PB.protobufConfig).value
