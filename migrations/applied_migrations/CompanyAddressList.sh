#!/bin/bash

echo "Applying migration CompanyAddressList"

echo "Adding routes to conf/register.company.routes"

echo "" >> ../conf/app.routes
echo "GET        /companyAddressList               controllers.register.company.CompanyAddressListController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/register.company.routes
echo "POST       /companyAddressList               controllers.register.company.CompanyAddressListController.onSubmit(mode: Mode = NormalMode)" >> ../conf/register.company.routes

echo "GET        /changeCompanyAddressList               controllers.register.company.CompanyAddressListController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/register.company.routes
echo "POST       /changeCompanyAddressList               controllers.register.company.CompanyAddressListController.onSubmit(mode: Mode = CheckMode)" >> ../conf/register.company.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "companyAddressList.title = companyAddressList" >> ../conf/messages.en
echo "companyAddressList.heading = companyAddressList" >> ../conf/messages.en
echo "companyAddressList.option1 = companyAddressList" Option 1 >> ../conf/messages.en
echo "companyAddressList.option2 = companyAddressList" Option 2 >> ../conf/messages.en
echo "companyAddressList.checkYourAnswersLabel = companyAddressList" >> ../conf/messages.en
echo "companyAddressList.error.required = Please give an answer for companyAddressList" >> ../conf/messages.en

echo "Adding helper method to CheckYourAnswersHelper"
awk '/class/ {\
     print;\
     print "";\
     print "  def companyAddressList: Option[AnswerRow] = userAnswers.get(identifiers.register.company.CompanyAddressListId) map {";\
     print "    x => AnswerRow(\"companyAddressList.checkYourAnswersLabel\", s\"companyAddressList.$x\", true, controllers.register.company.routes.CompanyAddressListController.onPageLoad(CheckMode).url)";\
     print "  }";\
     next }1' ../app/utils/CheckYourAnswersHelper.scala > tmp && mv tmp ../app/utils/CheckYourAnswersHelper.scala

echo "Moving test files from generated-test/ to test/"
rsync -avm --include='*.scala' -f 'hide,! */' ../generated-test/ ../test/
rm -rf ../generated-test/

echo "Migration CompanyAddressList completed"
