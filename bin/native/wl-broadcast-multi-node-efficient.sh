#!/usr/bin/env sh
opt/maelstrom/maelstrom \
test \
-w broadcast \
--bin mod/service-broadcast/target/graalvm-native-image/service-broadcast \
--node-count 25 \
--time-limit 20 \
--rate 100 \
--latency 100
