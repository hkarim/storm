#!/bin/zsh
export JAVA_OPTS="-Xms1g -Xmx1g -XX:+UseParallelGC"
opt/maelstrom/maelstrom \
test \
-w broadcast \
--bin service-broadcast/target/universal/stage/bin/service-broadcast \
--node-count 5 \
--time-limit 10 \
--rate 10 \
--nemesis partition
