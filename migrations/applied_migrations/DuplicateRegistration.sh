#!/bin/bash

echo "Applying migration DuplicateRegistration"

echo "Adding routes to register.routes"
echo "" >> ../conf/register.routes
echo "GET        /duplicateRegistration                       controllers.DuplicateRegistrationController.onPageLoad()" >> ../conf/register.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "duplicateRegistration.title = duplicateRegistration" >> ../conf/messages.en
echo "duplicateRegistration.heading = duplicateRegistration" >> ../conf/messages.en

echo "Moving test files from generated-test/ to test/"
rsync -avm --include='*.scala' -f 'hide,! */' ../generated-test/ ../test/
rm -rf ../generated-test/

echo "Migration DuplicateRegistration completed"
