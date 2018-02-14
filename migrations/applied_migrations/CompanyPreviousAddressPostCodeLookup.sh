#!/bin/bash

echo "Applying migration CompanyPreviousAddressPostCodeLookup"

echo "Adding routes to register.company.routes"

echo "" >> ../conf/register.company.routes
echo "GET        /companyPreviousAddressPostCodeLookup               controllers.register.company.CompanyPreviousAddressPostCodeLookupController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/register.company.routes
echo "POST       /companyPreviousAddressPostCodeLookup               controllers.register.company.CompanyPreviousAddressPostCodeLookupController.onSubmit(mode: Mode = NormalMode)" >> ../conf/register.company.routes

echo "GET        /changeCompanyPreviousAddressPostCodeLookup                        controllers.register.company.CompanyPreviousAddressPostCodeLookupController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/register.company.routes
echo "POST       /changeCompanyPreviousAddressPostCodeLookup                        controllers.register.company.CompanyPreviousAddressPostCodeLookupController.onSubmit(mode: Mode = CheckMode)" >> ../conf/register.company.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "companyPreviousAddressPostCodeLookup.title = companyPreviousAddressPostCodeLookup" >> ../conf/messages.en
echo "companyPreviousAddressPostCodeLookup.heading = companyPreviousAddressPostCodeLookup" >> ../conf/messages.en
echo "companyPreviousAddressPostCodeLookup.checkYourAnswersLabel = companyPreviousAddressPostCodeLookup" >> ../conf/messages.en
echo "companyPreviousAddressPostCodeLookup.error.required = Please give an answer for companyPreviousAddressPostCodeLookup" >> ../conf/messages.en

echo "Adding helper method to CheckYourAnswersHelper"
awk '/class/ {\
     print;\
     print "";\
     print "  def companyPreviousAddressPostCodeLookup: Option[AnswerRow] = userAnswers.get(identifiers.register.company.CompanyPreviousAddressPostCodeLookupId) map {";\
     print "    x => AnswerRow(\"companyPreviousAddressPostCodeLookup.checkYourAnswersLabel\", s\"$x\", false, controllers.register.company.routes.CompanyPreviousAddressPostCodeLookupController.onPageLoad(CheckMode).url)";\
     print "  }";\
     next }1' ../app/utils/CheckYourAnswersHelper.scala > tmp && mv tmp ../app/utils/CheckYourAnswersHelper.scala

echo "Moving test files from generated-test/ to test/"
rsync -avm --include='*.scala' -f 'hide,! */' ../generated-test/ ../test/
rm -rf ../generated-test/

echo "Migration CompanyPreviousAddressPostCodeLookup completed"
