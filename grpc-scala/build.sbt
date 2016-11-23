name := "grpc-scala"

version := "1.0"

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
  "io.grpc" % "grpc-netty" % "1.0.1",
  "com.trueaccord.scalapb" %% "scalapb-runtime-grpc" % com.trueaccord.scalapb.compiler.Version.scalapbVersion
)
watchSources ++= (((sourceDirectory in Compile).value / "protobuf") ** "*.proto").get

PB.targets in Compile := Seq(
  scalapb.gen(singleLineToString = true) -> (sourceManaged in Compile).value
)
