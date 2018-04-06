#!/bin/bash

echo "Applying migration CompanyNotFound"

echo "Adding routes to register.company.routes"
echo "" >> ../conf/register.company.routes
echo "GET        /companyNotFound                       controllers.register.company.CompanyNotFoundController.onPageLoad()" >> ../conf/register.company.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "companyNotFound.title = companyNotFound" >> ../conf/messages.en
echo "companyNotFound.heading = companyNotFound" >> ../conf/messages.en

echo "Moving test files from generated-test/ to test/"
rsync -avm --include='*.scala' -f 'hide,! */' ../generated-test/ ../test/
rm -rf ../generated-test/

echo "Migration CompanyNotFound completed"
