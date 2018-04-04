#!/bin/bash

echo "Applying migration IndividualPreviousAddress"

echo "Adding routes to register.individual.routes"
echo "" >> ../conf/register.individual.routes
echo "GET        /individualPreviousAddress                       controllers.register.individual.IndividualPreviousAddressController.onPageLoad()" >> ../conf/register.individual.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "individualPreviousAddress.title = individualPreviousAddress" >> ../conf/messages.en
echo "individualPreviousAddress.heading = individualPreviousAddress" >> ../conf/messages.en

echo "Moving test files from generated-test/ to test/"
rsync -avm --include='*.scala' -f 'hide,! */' ../generated-test/ ../test/
rm -rf ../generated-test/

echo "Migration IndividualPreviousAddress completed"
