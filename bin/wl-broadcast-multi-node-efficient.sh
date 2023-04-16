#!/usr/bin/env sh
opt/maelstrom/maelstrom \
test \
-w broadcast \
--bin mod/service-broadcast/target/universal/stage/bin/service-broadcast \
--node-count 25 \
--time-limit 20 \
--rate 100 \
--latency 100
