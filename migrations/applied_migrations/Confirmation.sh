#!/bin/bash

echo "Applying migration Confirmation"

echo "Adding routes to register.routes"
echo "" >> ../conf/register.routes
echo "GET        /confirmation                       controllers.register.ConfirmationController.onPageLoad()" >> ../conf/register.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "confirmation.title = confirmation" >> ../conf/messages.en
echo "confirmation.heading = confirmation" >> ../conf/messages.en

echo "Moving test files from generated-test/ to test/"
rsync -avm --include='*.scala' -f 'hide,! */' ../generated-test/ ../test/
rm -rf ../generated-test/

echo "Migration Confirmation completed"
