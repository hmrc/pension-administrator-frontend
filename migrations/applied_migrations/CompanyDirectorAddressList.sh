#!/bin/bash

echo "Applying migration CompanyDirectorAddressList"

echo "Adding routes to conf/register.company.routes"

echo "" >> ../conf/app.routes
echo "GET        /companyDirectorAddressList               controllers.register.company.CompanyDirectorAddressListController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/register.company.routes
echo "POST       /companyDirectorAddressList               controllers.register.company.CompanyDirectorAddressListController.onSubmit(mode: Mode = NormalMode)" >> ../conf/register.company.routes

echo "GET        /changeCompanyDirectorAddressList               controllers.register.company.CompanyDirectorAddressListController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/register.company.routes
echo "POST       /changeCompanyDirectorAddressList               controllers.register.company.CompanyDirectorAddressListController.onSubmit(mode: Mode = CheckMode)" >> ../conf/register.company.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "companyDirectorAddressList.title = companyDirectorAddressList" >> ../conf/messages.en
echo "companyDirectorAddressList.heading = companyDirectorAddressList" >> ../conf/messages.en
echo "companyDirectorAddressList.option1 = companyDirectorAddressList" Option 1 >> ../conf/messages.en
echo "companyDirectorAddressList.option2 = companyDirectorAddressList" Option 2 >> ../conf/messages.en
echo "companyDirectorAddressList.checkYourAnswersLabel = companyDirectorAddressList" >> ../conf/messages.en
echo "companyDirectorAddressList.error.required = Please give an answer for companyDirectorAddressList" >> ../conf/messages.en

echo "Adding helper method to CheckYourAnswersHelper"
awk '/class/ {\
     print;\
     print "";\
     print "  def companyDirectorAddressList: Option[AnswerRow] = userAnswers.get(identifiers.register.company.CompanyDirectorAddressListId) map {";\
     print "    x => AnswerRow(\"companyDirectorAddressList.checkYourAnswersLabel\", s\"companyDirectorAddressList.$x\", true, controllers.register.company.routes.CompanyDirectorAddressListController.onPageLoad(CheckMode).url)";\
     print "  }";\
     next }1' ../app/utils/CheckYourAnswersHelper.scala > tmp && mv tmp ../app/utils/CheckYourAnswersHelper.scala

echo "Moving test files from generated-test/ to test/"
rsync -avm --include='*.scala' -f 'hide,! */' ../generated-test/ ../test/
rm -rf ../generated-test/

echo "Migration CompanyDirectorAddressList completed"
