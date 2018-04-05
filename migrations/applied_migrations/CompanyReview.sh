#!/bin/bash

echo "Applying migration CompanyReview"

echo "Adding routes to register.company.routes"
echo "" >> ../conf/register.company.routes
echo "GET        /companyReview                       controllers.register.company.CompanyReviewController.onPageLoad()" >> ../conf/register.company.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "companyReview.title = companyReview" >> ../conf/messages.en
echo "companyReview.heading = companyReview" >> ../conf/messages.en

echo "Moving test files from generated-test/ to test/"
rsync -avm --include='*.scala' -f 'hide,! */' ../generated-test/ ../test/
rm -rf ../generated-test/

echo "Migration CompanyReview completed"
