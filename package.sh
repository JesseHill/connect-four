#!/bin/bash
rm connect-four.zip
lein uberjar
mv ./target/uberjar/connect-four-0.1.0-SNAPSHOT-standalone.jar connect-four.jar
zip connect-four.zip README.md connect-four.jar
rm connect-four.jar
