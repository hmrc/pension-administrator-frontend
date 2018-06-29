#!/bin/bash

echo "Applying migration PartnershipContactDetails"

echo "Adding routes to register.partnership.routes"
echo "" >> ../conf/register.partnership.routes
echo "GET        /partnershipContactDetails                       controllers.register.partnership.PartnershipContactDetailsController.onPageLoad()" >> ../conf/register.partnership.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "partnershipContactDetails.title = partnershipContactDetails" >> ../conf/messages.en
echo "partnershipContactDetails.heading = partnershipContactDetails" >> ../conf/messages.en

echo "Moving test files from generated-test/ to test/"
rsync -avm --include='*.scala' -f 'hide,! */' ../generated-test/ ../test/
rm -rf ../generated-test/

echo "Migration PartnershipContactDetails completed"
