#!/usr/bin/env sh
opt/maelstrom/maelstrom \
test \
-w txn-rw-register \
--bin mod/service-txn/target/universal/stage/bin/service-txn \
--node-count 2 \
--concurrency 2n \
--time-limit 20 \
--rate 1000 \
--consistency-models read-committed \
--availability total \
--nemesis partition
