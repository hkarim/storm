#!/usr/bin/env sh
opt/maelstrom/maelstrom \
test \
-w kafka \
--bin mod/service-kafka/target/universal/stage/bin/service-kafka \
--node-count 2 \
--concurrency 2n \
--time-limit 20 \
--rate 1000