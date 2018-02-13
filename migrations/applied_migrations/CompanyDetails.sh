#!/bin/bash

echo "Applying migration CompanyDetails"

echo "Adding routes to register.company.routes"

echo "" >> ../conf/register.company.routes
echo "GET        /companyDetails                       controllers.register.company.CompanyDetailsController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/register.company.routes
echo "POST       /companyDetails                       controllers.register.company.CompanyDetailsController.onSubmit(mode: Mode = NormalMode)" >> ../conf/register.company.routes

echo "GET        /changeCompanyDetails                       controllers.register.company.CompanyDetailsController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/register.company.routes
echo "POST       /changeCompanyDetails                       controllers.register.company.CompanyDetailsController.onSubmit(mode: Mode = CheckMode)" >> ../conf/register.company.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "companyDetails.title = companyDetails" >> ../conf/messages.en
echo "companyDetails.heading = companyDetails" >> ../conf/messages.en
echo "companyDetails.field1 = Field 1" >> ../conf/messages.en
echo "companyDetails.field2 = Field 2" >> ../conf/messages.en
echo "companyDetails.checkYourAnswersLabel = companyDetails" >> ../conf/messages.en
echo "companyDetails.error.field1.required = Please give an answer for field1" >> ../conf/messages.en
echo "companyDetails.error.field2.required = Please give an answer for field2" >> ../conf/messages.en

echo "Adding helper method to CheckYourAnswersHelper"
awk '/class/ {\
     print;\
     print "";\
     print "  def companyDetails: Option[AnswerRow] = userAnswers.get(identifiers.register.company.CompanyDetailsId) map {";\
     print "    x => AnswerRow(\"companyDetails.checkYourAnswersLabel\", s\"${x.field1} ${x.field2}\", false, controllers.register.company.routes.CompanyDetailsController.onPageLoad(CheckMode).url)";\
     print "  }";\
     next }1' ../app/utils/CheckYourAnswersHelper.scala > tmp && mv tmp ../app/utils/CheckYourAnswersHelper.scala

echo "Moving test files from generated-test/ to test/"
rsync -avm --include='*.scala' -f 'hide,! */' ../generated-test/ ../test/
rm -rf ../generated-test/

echo "Migration CompanyDetails completed"
