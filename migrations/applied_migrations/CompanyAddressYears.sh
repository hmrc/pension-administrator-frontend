#!/bin/bash

echo "Applying migration CompanyAddressYears"

echo "Adding routes to conf/register.company.routes"

echo "" >> ../conf/app.routes
echo "GET        /companyAddressYears               controllers.register.company.CompanyAddressYearsController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/register.company.routes
echo "POST       /companyAddressYears               controllers.register.company.CompanyAddressYearsController.onSubmit(mode: Mode = NormalMode)" >> ../conf/register.company.routes

echo "GET        /changeCompanyAddressYears               controllers.register.company.CompanyAddressYearsController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/register.company.routes
echo "POST       /changeCompanyAddressYears               controllers.register.company.CompanyAddressYearsController.onSubmit(mode: Mode = CheckMode)" >> ../conf/register.company.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "companyAddressYears.title = companyAddressYears" >> ../conf/messages.en
echo "companyAddressYears.heading = companyAddressYears" >> ../conf/messages.en
echo "companyAddressYears.option1 = companyAddressYears" Option 1 >> ../conf/messages.en
echo "companyAddressYears.option2 = companyAddressYears" Option 2 >> ../conf/messages.en
echo "companyAddressYears.checkYourAnswersLabel = companyAddressYears" >> ../conf/messages.en
echo "companyAddressYears.error.required = Please give an answer for companyAddressYears" >> ../conf/messages.en

echo "Adding helper method to CheckYourAnswersHelper"
awk '/class/ {\
     print;\
     print "";\
     print "  def companyAddressYears: Option[AnswerRow] = userAnswers.get(identifiers.register.company.CompanyAddressYearsId) map {";\
     print "    x => AnswerRow(\"companyAddressYears.checkYourAnswersLabel\", s\"companyAddressYears.$x\", true, controllers.register.company.routes.CompanyAddressYearsController.onPageLoad(CheckMode).url)";\
     print "  }";\
     next }1' ../app/utils/CheckYourAnswersHelper.scala > tmp && mv tmp ../app/utils/CheckYourAnswersHelper.scala

echo "Moving test files from generated-test/ to test/"
rsync -avm --include='*.scala' -f 'hide,! */' ../generated-test/ ../test/
rm -rf ../generated-test/

echo "Migration CompanyAddressYears completed"
