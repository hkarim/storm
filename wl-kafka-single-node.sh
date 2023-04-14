#!/bin/zsh
opt/maelstrom/maelstrom \
test \
-w kafka \
--bin service-kafka/target/universal/stage/bin/service-kafka \
--node-count 1 \
--concurrency 2n \
--time-limit 20 \
--rate 1000