#!/bin/zsh
export JAVA_OPTS="-Xms1g -Xmx1g -XX:+UseParallelGC"
opt/maelstrom/maelstrom \
test \
-w broadcast \
--bin service-broadcast/target/universal/stage/bin/service-broadcast \
--node-count 1 \
--time-limit 20 \
--rate 10
