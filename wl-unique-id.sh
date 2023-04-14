#!/bin/zsh
opt/maelstrom/maelstrom \
test \
-w unique-ids \
--bin service-unique-id/target/universal/stage/bin/service-unique-id \
--time-limit 30 \
--rate 1000 \
--node-count 3 \
--availability total \
--nemesis partition
