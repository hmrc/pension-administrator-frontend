#!/bin/bash

echo "Applying migration IndividualContactAddressList"

echo "Adding routes to register.individual.routes"
echo "" >> ../conf/register.individual.routes
echo "GET        /individualContactAddressList                       controllers.register.individual.IndividualContactAddressListController.onPageLoad()" >> ../conf/register.individual.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "individualContactAddressList.title = individualContactAddressList" >> ../conf/messages.en
echo "individualContactAddressList.heading = individualContactAddressList" >> ../conf/messages.en

echo "Moving test files from generated-test/ to test/"
rsync -avm --include='*.scala' -f 'hide,! */' ../generated-test/ ../test/
rm -rf ../generated-test/

echo "Migration IndividualContactAddressList completed"
