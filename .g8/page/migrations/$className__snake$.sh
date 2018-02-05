#!/bin/bash

echo "Applying migration $className;format="snake"$"

echo "Adding routes to $routeFile$.routes"
echo "" >> ../conf/$routeFile$.routes
echo "GET        /$className;format="decap"$                       controllers.$routeFile$.$className$Controller.onPageLoad()" >> ../conf/$routeFile$.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "$className;format="decap"$.title = $className;format="decap"$" >> ../conf/messages.en
echo "$className;format="decap"$.heading = $className;format="decap"$" >> ../conf/messages.en

echo "Moving test files from generated-test/ to test/"
rsync -avm --include='*.scala' -f 'hide,! */' ../generated-test/ ../test/
rm -rf ../generated-test/

echo "Migration $className;format="snake"$ completed"
