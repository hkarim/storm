#!/usr/bin/env sh
opt/maelstrom/maelstrom \
test \
-w txn-rw-register \
--bin mod/service-txn/target/universal/stage/bin/service-txn \
--node-count 1 \
--time-limit 20 \
--rate 1000 \
--concurrency 2n \
--consistency-models read-uncommitted \
--availability total