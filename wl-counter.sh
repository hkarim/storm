#!/bin/zsh
opt/maelstrom/maelstrom \
test \
-w g-counter \
--bin service-counter/target/universal/stage/bin/service-counter \
--time-limit 20 \
--rate 100 \
--node-count 3 \
--nemesis partition
