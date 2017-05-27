#!/bin/bash
docker build -t genderizer-python -f dockerfiles/DockerfilePython .
docker build -t genderizer-r -f dockerfiles/DockerfileR .