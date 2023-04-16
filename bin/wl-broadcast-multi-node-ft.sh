#!/usr/bin/env sh
opt/maelstrom/maelstrom \
test \
-w broadcast \
--bin mod/service-broadcast/target/universal/stage/bin/service-broadcast \
--node-count 5 \
--time-limit 10 \
--rate 10 \
--nemesis partition
