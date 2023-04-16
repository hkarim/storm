#!/usr/bin/env sh
opt/maelstrom/maelstrom \
test \
-w unique-ids \
--bin mod/service-unique-id/target/universal/stage/bin/service-unique-id \
--time-limit 30 \
--rate 1000 \
--node-count 3 \
--availability total \
--nemesis partition
