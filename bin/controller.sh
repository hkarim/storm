#!/usr/bin/env sh
mod/service-controller/target/universal/stage/bin/service-controller -- \
network \
-p mod/service-echo/target/universal/stage/bin/service-echo \
-n 3
