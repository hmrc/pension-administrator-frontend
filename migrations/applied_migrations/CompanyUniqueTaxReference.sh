#!/bin/bash

echo "Applying migration CompanyUniqueTaxReference"

echo "Adding routes to company.routes"

echo "" >> ../conf/company.routes
echo "GET        /companyUniqueTaxReference               controllers.company.CompanyUniqueTaxReferenceController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/company.routes
echo "POST       /companyUniqueTaxReference               controllers.company.CompanyUniqueTaxReferenceController.onSubmit(mode: Mode = NormalMode)" >> ../conf/company.routes

echo "GET        /changeCompanyUniqueTaxReference                        controllers.company.CompanyUniqueTaxReferenceController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/company.routes
echo "POST       /changeCompanyUniqueTaxReference                        controllers.company.CompanyUniqueTaxReferenceController.onSubmit(mode: Mode = CheckMode)" >> ../conf/company.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "companyUniqueTaxReference.title = companyUniqueTaxReference" >> ../conf/messages.en
echo "companyUniqueTaxReference.heading = companyUniqueTaxReference" >> ../conf/messages.en
echo "companyUniqueTaxReference.checkYourAnswersLabel = companyUniqueTaxReference" >> ../conf/messages.en
echo "companyUniqueTaxReference.error__required = Please give an answer for companyUniqueTaxReference" >> ../conf/messages.en

echo "Adding helper method to CheckYourAnswersHelper"
awk '/class/ {\
     print;\
     print "";\
     print "  def companyUniqueTaxReference: Option[AnswerRow] = userAnswers.get(identifiers.company.CompanyUniqueTaxReferenceId) map {";\
     print "    x => AnswerRow(\"companyUniqueTaxReference.checkYourAnswersLabel\", s\"$x\", false, controllers.company.routes.CompanyUniqueTaxReferenceController.onPageLoad(CheckMode).url)";\
     print "  }";\
     next }1' ../app/utils/CheckYourAnswersHelper.scala > tmp && mv tmp ../app/utils/CheckYourAnswersHelper.scala

echo "Moving test files from generated-test/ to test/"
rsync -avm --include='*.scala' -f 'hide,! */' ../generated-test/ ../test/
rm -rf ../generated-test/

echo "Migration CompanyUniqueTaxReference completed"
