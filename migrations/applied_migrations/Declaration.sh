#!/bin/bash

echo "Applying migration Declaration"

echo "Adding routes to register.routes"
echo "" >> ../conf/register.routes
echo "GET        /declaration                       controllers.register.DeclarationController.onPageLoad()" >> ../conf/register.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "declaration.title = declaration" >> ../conf/messages.en
echo "declaration.heading = declaration" >> ../conf/messages.en

echo "Moving test files from generated-test/ to test/"
rsync -avm --include='*.scala' -f 'hide,! */' ../generated-test/ ../test/
rm -rf ../generated-test/

echo "Migration Declaration completed"
