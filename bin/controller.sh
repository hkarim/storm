#!/usr/bin/env sh
mod/service-controller/target/universal/stage/bin/service-controller -- \
network \
-p mod/service-broadcast/target/universal/stage/bin/service-broadcast \
-n 3
