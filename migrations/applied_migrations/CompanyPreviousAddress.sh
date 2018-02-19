#!/bin/bash

echo "Applying migration CompanyPreviousAddress"

echo "Adding routes to register.company.routes"

echo "" >> ../conf/register.company.routes
echo "GET        /companyPreviousAddress                       controllers.register.company.CompanyPreviousAddressController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/register.company.routes
echo "POST       /companyPreviousAddress                       controllers.register.company.CompanyPreviousAddressController.onSubmit(mode: Mode = NormalMode)" >> ../conf/register.company.routes

echo "GET        /changeCompanyPreviousAddress                       controllers.register.company.CompanyPreviousAddressController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/register.company.routes
echo "POST       /changeCompanyPreviousAddress                       controllers.register.company.CompanyPreviousAddressController.onSubmit(mode: Mode = CheckMode)" >> ../conf/register.company.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "companyPreviousAddress.title = companyPreviousAddress" >> ../conf/messages.en
echo "companyPreviousAddress.heading = companyPreviousAddress" >> ../conf/messages.en
echo "companyPreviousAddress.field1 = Field 1" >> ../conf/messages.en
echo "companyPreviousAddress.field2 = Field 2" >> ../conf/messages.en
echo "companyPreviousAddress.checkYourAnswersLabel = companyPreviousAddress" >> ../conf/messages.en
echo "companyPreviousAddress.error.field1.required = Please give an answer for field1" >> ../conf/messages.en
echo "companyPreviousAddress.error.field2.required = Please give an answer for field2" >> ../conf/messages.en

echo "Adding helper method to CheckYourAnswersHelper"
awk '/class/ {\
     print;\
     print "";\
     print "  def companyPreviousAddress: Option[AnswerRow] = userAnswers.get(identifiers.register.company.CompanyPreviousAddressId) map {";\
     print "    x => AnswerRow(\"companyPreviousAddress.checkYourAnswersLabel\", s\"${x.field1} ${x.field2}\", false, controllers.register.company.routes.CompanyPreviousAddressController.onPageLoad(CheckMode).url)";\
     print "  }";\
     next }1' ../app/utils/CheckYourAnswersHelper.scala > tmp && mv tmp ../app/utils/CheckYourAnswersHelper.scala

echo "Moving test files from generated-test/ to test/"
rsync -avm --include='*.scala' -f 'hide,! */' ../generated-test/ ../test/
rm -rf ../generated-test/

echo "Migration CompanyPreviousAddress completed"
