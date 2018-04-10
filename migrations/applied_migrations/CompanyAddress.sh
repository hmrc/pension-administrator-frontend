#!/bin/bash

echo "Applying migration CompanyAddress"

echo "Adding routes to company.routes"

echo "" >> ../conf/company.routes
echo "GET        /companyAddress                       controllers.company.CompanyAddressController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/company.routes
echo "POST       /companyAddress                       controllers.company.CompanyAddressController.onSubmit(mode: Mode = NormalMode)" >> ../conf/company.routes

echo "GET        /changeCompanyAddress                       controllers.company.CompanyAddressController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/company.routes
echo "POST       /changeCompanyAddress                       controllers.company.CompanyAddressController.onSubmit(mode: Mode = CheckMode)" >> ../conf/company.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "companyAddress.title = companyAddress" >> ../conf/messages.en
echo "companyAddress.heading = companyAddress" >> ../conf/messages.en
echo "companyAddress.checkYourAnswersLabel = companyAddress" >> ../conf/messages.en
echo "companyAddress.error.required = Please give an answer for companyAddress" >> ../conf/messages.en

echo "Adding helper method to CheckYourAnswersHelper"
awk '/class/ {\
     print;\
     print "";\
     print "  def companyAddress: Seq[AnswerRow] = userAnswers.get(identifiers.company.CompanyAddressId) match {";\
     print "    case Some(x) => Seq(AnswerRow(\"companyAddress.checkYourAnswersLabel\", if(x) \"site.yes\" else \"site.no\", true, controllers.company.routes.CompanyAddressController.onPageLoad(CheckMode).url))";\
     print "    case _ => Nil";\
     print "  }";\
     next }1' ../app/utils/CheckYourAnswersHelper.scala > tmp && mv tmp ../app/utils/CheckYourAnswersHelper.scala

echo "Moving test files from generated-test/ to test/"
rsync -avm --include='*.scala' -f 'hide,! */' ../generated-test/ ../test/
rm -rf ../generated-test/

echo "Migration CompanyAddress completed"
