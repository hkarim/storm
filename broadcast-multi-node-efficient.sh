#!/bin/zsh

export JAVA_OPTS="-Xms350m -Xmx350m -XX:+UseParallelGC"

opt/maelstrom/maelstrom \
test \
-w broadcast \
--bin service-broadcast/target/universal/stage/bin/service-broadcast \
--node-count 25 \
--time-limit 20 \
--rate 100 \
--latency 100
