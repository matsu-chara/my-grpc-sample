grpcの練習

streamを中心にやる。

```bash
sbt runMain example.HelloWorldServer

# 起動を待って別Terminalで行う。
sbt runMain example.HelloWorldClient
```

```bash
sbt runMain math.MathWorldServer

# 起動を待って別Terminalで行う。
sbt runMain math.MathWorldClient
```

`math.proto`で

- StreamRequest & StreamResponse なDivMany
- StreamResponse なFib
- StreamRequest なSum

を扱っている。


# todo

grpc-phpとの通信
