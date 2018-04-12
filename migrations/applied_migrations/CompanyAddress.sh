#!/bin/bash

echo "Applying migration CompanyAddress"

echo "Adding routes to company.routes"
echo "" >> ../conf/company.routes
echo "GET        /companyAddress                       controllers.register.company.CompanyAddressController.onPageLoad()" >> ../conf/company.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "companyAddress.title = companyAddress" >> ../conf/messages.en
echo "companyAddress.heading = companyAddress" >> ../conf/messages.en

echo "Moving test files from generated-test/ to test/"
rsync -avm --include='*.scala' -f 'hide,! */' ../generated-test/ ../test/
rm -rf ../generated-test/

echo "Migration CompanyAddress completed"
