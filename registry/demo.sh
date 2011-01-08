#!/bin/sh

#DEBUG_OPTS="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=8000"
java $DEBUG_OPTS -cp ./target/classes:./target/test-classes:../core/api/target/classes:../core/runtime/target/classes:/home/tcunning/.m2/repository/org/jboss/marshalling/marshalling-api/1.2.0.GA/marshalling-api-1.2.0.GA.jar:/home/tcunning/.m2/repository/org/jboss/marshalling/river/1.2.0.GA/river-1.2.0.GA.jar:/home/tcunning/.m2/repository/org/jgroups/jgroups/2.10.0.GA/jgroups-2.10.0.GA.jar org.switchyard.internal.JGroupsRegistryDemo
