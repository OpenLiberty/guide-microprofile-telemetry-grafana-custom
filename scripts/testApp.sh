#!/bin/bash
set -euxo pipefail

./mvnw -version

testApp() {
    ./mvnw -ntp -Dhttp.keepAlive=false \
        -Dmaven.wagon.http.pool=false \
        -Dmaven.wagon.httpconnectionManager.ttlSeconds=120 \
        -pl system,inventory -q clean package liberty:create liberty:install-feature liberty:deploy

    ./mvnw -ntp -pl system,inventory liberty:start

    ./mvnw -ntp -Dhttp.keepAlive=false \
        -Dmaven.wagon.http.pool=false \
        -Dmaven.wagon.httpconnectionManager.ttlSeconds=120 \
        -pl system,inventory failsafe:integration-test

    ./mvnw -ntp -pl system,inventory liberty:stop

    ./mvnw -ntp -pl system,inventory failsafe:verify
}

testApp
cd ../start
testApp
