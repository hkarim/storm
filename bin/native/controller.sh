#!/usr/bin/env sh
mod/service-controller/target/graalvm-native-image/service-controller \
network \
-p mod/service-broadcast/target/graalvm-native-image/service-broadcast \
-n 3
