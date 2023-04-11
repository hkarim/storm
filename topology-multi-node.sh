#!/bin/zsh
opt/maelstrom/maelstrom \
test \
-w broadcast \
--bin service-broadcast/target/universal/stage/bin/service-broadcast \
--node-count 5 \
--time-limit 10 \
--rate 10
