#!/bin/zsh
opt/maelstrom/maelstrom \
test \
-w broadcast \
--bin service-broadcast/target/universal/stage/bin/service-broadcast \
--time-limit 20 \
--rate 10 \
