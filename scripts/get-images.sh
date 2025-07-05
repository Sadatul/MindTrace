#!/bin/bash
cat backend/src/test/java/com/sadi/backend/AbstractBaseIntegrationTest.java | grep "DockerImageName.parse" | sed -n 's/.*DockerImageName\.parse("\([^"]*\)").*/\1/p'