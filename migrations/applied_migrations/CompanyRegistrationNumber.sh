#!/bin/bash

echo "Applying migration CompanyRegistrationNumber"

echo "Adding routes to register.company.routes"

echo "" >> ../conf/register.company.routes
echo "GET        /companyRegistrationNumber               controllers.register.company.CompanyRegistrationNumberController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/register.company.routes
echo "POST       /companyRegistrationNumber               controllers.register.company.CompanyRegistrationNumberController.onSubmit(mode: Mode = NormalMode)" >> ../conf/register.company.routes

echo "GET        /changeCompanyRegistrationNumber                        controllers.register.company.CompanyRegistrationNumberController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/register.company.routes
echo "POST       /changeCompanyRegistrationNumber                        controllers.register.company.CompanyRegistrationNumberController.onSubmit(mode: Mode = CheckMode)" >> ../conf/register.company.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "companyRegistrationNumber.title = companyRegistrationNumber" >> ../conf/messages.en
echo "companyRegistrationNumber.heading = companyRegistrationNumber" >> ../conf/messages.en
echo "companyRegistrationNumber.checkYourAnswersLabel = companyRegistrationNumber" >> ../conf/messages.en
echo "companyRegistrationNumber.error__required = Please give an answer for companyRegistrationNumber" >> ../conf/messages.en

echo "Adding helper method to CheckYourAnswersHelper"
awk '/class/ {\
     print;\
     print "";\
     print "  def companyRegistrationNumber: Option[AnswerRow] = userAnswers.get(identifiers.register.company.CompanyRegistrationNumberId) map {";\
     print "    x => AnswerRow(\"companyRegistrationNumber.checkYourAnswersLabel\", s\"$x\", false, controllers.register.company.routes.CompanyRegistrationNumberController.onPageLoad(CheckMode).url)";\
     print "  }";\
     next }1' ../app/utils/CheckYourAnswersHelper.scala > tmp && mv tmp ../app/utils/CheckYourAnswersHelper.scala

echo "Moving test files from generated-test/ to test/"
rsync -avm --include='*.scala' -f 'hide,! */' ../generated-test/ ../test/
rm -rf ../generated-test/

echo "Migration CompanyRegistrationNumber completed"
