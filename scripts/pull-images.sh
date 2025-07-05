read -a images <<< $(cat backend/src/test/java/com/sadi/backend/AbstractBaseIntegrationTest.java | grep "DockerImageName.parse" | sed -n 's/.*DockerImageName\.parse("\([^"]*\)").*/\1/p' | xargs)
for image in "${images[@]}"
do
    docker pull $image
done