#!/bin/bash

echo "Applying migration CompanyDirectorAddressPostCodeLookup"

echo "Adding routes to register.company.routes"

echo "" >> ../conf/register.company.routes
echo "GET        /companyDirectorAddressPostCodeLookup               controllers.register.company.CompanyDirectorAddressPostCodeLookupController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/register.company.routes
echo "POST       /companyDirectorAddressPostCodeLookup               controllers.register.company.CompanyDirectorAddressPostCodeLookupController.onSubmit(mode: Mode = NormalMode)" >> ../conf/register.company.routes

echo "GET        /changeCompanyDirectorAddressPostCodeLookup                        controllers.register.company.CompanyDirectorAddressPostCodeLookupController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/register.company.routes
echo "POST       /changeCompanyDirectorAddressPostCodeLookup                        controllers.register.company.CompanyDirectorAddressPostCodeLookupController.onSubmit(mode: Mode = CheckMode)" >> ../conf/register.company.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "companyDirectorAddressPostCodeLookup.title = companyDirectorAddressPostCodeLookup" >> ../conf/messages.en
echo "companyDirectorAddressPostCodeLookup.heading = companyDirectorAddressPostCodeLookup" >> ../conf/messages.en
echo "companyDirectorAddressPostCodeLookup.checkYourAnswersLabel = companyDirectorAddressPostCodeLookup" >> ../conf/messages.en
echo "companyDirectorAddressPostCodeLookup.error.required = Please give an answer for companyDirectorAddressPostCodeLookup" >> ../conf/messages.en

echo "Adding helper method to CheckYourAnswersHelper"
awk '/class/ {\
     print;\
     print "";\
     print "  def companyDirectorAddressPostCodeLookup: Option[AnswerRow] = userAnswers.get(identifiers.register.company.CompanyDirectorAddressPostCodeLookupId) map {";\
     print "    x => AnswerRow(\"companyDirectorAddressPostCodeLookup.checkYourAnswersLabel\", s\"$x\", false, controllers.register.company.routes.CompanyDirectorAddressPostCodeLookupController.onPageLoad(CheckMode).url)";\
     print "  }";\
     next }1' ../app/utils/CheckYourAnswersHelper.scala > tmp && mv tmp ../app/utils/CheckYourAnswersHelper.scala

echo "Moving test files from generated-test/ to test/"
rsync -avm --include='*.scala' -f 'hide,! */' ../generated-test/ ../test/
rm -rf ../generated-test/

echo "Migration CompanyDirectorAddressPostCodeLookup completed"
