#!/bin/bash

echo "Applying migration AdviserAddressList"

echo "Adding routes to register.adviser.routes"
echo "" >> ../conf/register.adviser.routes
echo "GET        /adviserAddressList                       controllers.register.adviser.AdviserAddressListController.onPageLoad()" >> ../conf/register.adviser.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "adviserAddressList.title = adviserAddressList" >> ../conf/messages.en
echo "adviserAddressList.heading = adviserAddressList" >> ../conf/messages.en

echo "Moving test files from generated-test/ to test/"
rsync -avm --include='*.scala' -f 'hide,! */' ../generated-test/ ../test/
rm -rf ../generated-test/

echo "Migration AdviserAddressList completed"
