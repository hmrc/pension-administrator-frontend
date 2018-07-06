#!/bin/bash

echo "Applying migration ConfirmDeletePartner"

echo "Adding routes to partners.routes"
echo "" >> ../conf/partners.routes
echo "GET        /confirmDeletePartner                       controllers.register.partnership.partners.ConfirmDeletePartnerController.onPageLoad()" >> ../conf/partners.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "confirmDeletePartner.title = confirmDeletePartner" >> ../conf/messages.en
echo "confirmDeletePartner.heading = confirmDeletePartner" >> ../conf/messages.en

echo "Moving test files from generated-test/ to test/"
rsync -avm --include='*.scala' -f 'hide,! */' ../generated-test/ ../test/
rm -rf ../generated-test/

echo "Migration ConfirmDeletePartner completed"
