#!/bin/bash

echo "Applying migration InterceptPSA"

echo "Adding routes to app.routes"
echo "" >> ../conf/app.routes
echo "GET        /interceptPSA                       controllers.InterceptPSAController.onPageLoad()" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "interceptPSA.title = interceptPSA" >> ../conf/messages.en
echo "interceptPSA.heading = interceptPSA" >> ../conf/messages.en

echo "Moving test files from generated-test/ to test/"
rsync -avm --include='*.scala' -f 'hide,! */' ../generated-test/ ../test/
rm -rf ../generated-test/

echo "Migration InterceptPSA completed"
