#!/bin/bash

echo "Applying migration CompanyUpdateDetails"

echo "Adding routes to register.company.routes"
echo "" >> ../conf/register.company.routes
echo "GET        /companyUpdateDetails                       controllers.register.company.CompanyUpdateDetailsController.onPageLoad()" >> ../conf/register.company.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "companyUpdateDetails.title = companyUpdateDetails" >> ../conf/messages.en
echo "companyUpdateDetails.heading = companyUpdateDetails" >> ../conf/messages.en

echo "Moving test files from generated-test/ to test/"
rsync -avm --include='*.scala' -f 'hide,! */' ../generated-test/ ../test/
rm -rf ../generated-test/

echo "Migration CompanyUpdateDetails completed"
