#!/bin/sh

java -cp ./target/classes:./target/test-classes:../core/api/target/classes:../core/runtime/target/classes:/home/tcunning/.m2/repository/org/jgroups/jgroups/2.10.0.GA/jgroups-2.10.0.GA.jar org.switchyard.internal.JGroupsStandingRegistry
