# storm

Scala solutions of [Gossip Glomers](https://fly.io/dist-sys/) distributed systems challenges.

## Setup

- Clone this repository
- Download [maelstrom](https://github.com/jepsen-io/maelstrom/releases) and unpack under `./opt`
- Under the root of this project run `sbt stage` 
- Test the workloads using one of the shell scripts under `./bin`. For example to run the `echo` 
workload, run `bin/wl-echo.sh`
- Some solutions will pass the tests although probably wrong!