#!/usr/bin/env sh
opt/maelstrom/maelstrom \
test \
-w echo \
--bin mod/service-echo/target/universal/stage/bin/service-echo \
--time-limit 5