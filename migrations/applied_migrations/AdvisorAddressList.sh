#!/bin/bash

echo "Applying migration AdvisorAddressList"

echo "Adding routes to register.advisor.routes"
echo "" >> ../conf/register.advisor.routes
echo "GET        /advisorAddressList                       controllers.register.advisor.AdvisorAddressListController.onPageLoad()" >> ../conf/register.advisor.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "advisorAddressList.title = advisorAddressList" >> ../conf/messages.en
echo "advisorAddressList.heading = advisorAddressList" >> ../conf/messages.en

echo "Moving test files from generated-test/ to test/"
rsync -avm --include='*.scala' -f 'hide,! */' ../generated-test/ ../test/
rm -rf ../generated-test/

echo "Migration AdvisorAddressList completed"
