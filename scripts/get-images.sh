#!/bin/bash
cat backend/src/test/java/com/sadi/backend/AbstractBaseIntegrationTest.java" | sed -n 's/.*DockerImageName\.parse("\([^"]*\)").*/\1/p'