grpcの練習

# scala編

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

# PHP編

```bash
protoc --plugin=protoc-gen-php.php --php_out=./src ./src/protobuf/hello_world.proto
```

datto/Protobuf-PHPはpecl無しでインストールできそうだけど、
`Pear's Console_CommandLine (for the protoc wrapper tool)` がrequiermentsに入っていて結局pear使う必要がありそうだったのでpear installで入れている。
解決方法が見つかったらpearなしでやりたい。

google/authはgrpcに必要だがdependencyが解決されなかったので明示的に指定している。

# memo

sbtでprotoc-jarが入っていて、phpのためにprotocも入れているので二度手間だが、
各々の言語で独立して後でそのままコピペすれば動くような環境を目指しているので
そのままにする。

今のところprotobufファイルをそれぞれコピーして使っているが、
これも各々の言語で独立して動かしたい気持ちによるものなので
そのままにする。
