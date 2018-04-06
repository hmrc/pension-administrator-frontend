#!/bin/bash

echo "Applying migration CompanyNameUniqueTaxReference"

echo "Adding routes to register.company.routes"

echo "" >> ../conf/register.company.routes
echo "GET        /companyNameUniqueTaxReference                       controllers.register.company.CompanyNameUniqueTaxReferenceController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/register.company.routes
echo "POST       /companyNameUniqueTaxReference                       controllers.register.company.CompanyNameUniqueTaxReferenceController.onSubmit(mode: Mode = NormalMode)" >> ../conf/register.company.routes

echo "GET        /changeCompanyNameUniqueTaxReference                       controllers.register.company.CompanyNameUniqueTaxReferenceController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/register.company.routes
echo "POST       /changeCompanyNameUniqueTaxReference                       controllers.register.company.CompanyNameUniqueTaxReferenceController.onSubmit(mode: Mode = CheckMode)" >> ../conf/register.company.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "companyNameUniqueTaxReference.title = companyNameUniqueTaxReference" >> ../conf/messages.en
echo "companyNameUniqueTaxReference.heading = companyNameUniqueTaxReference" >> ../conf/messages.en
echo "companyNameUniqueTaxReference.field1 = Field 1" >> ../conf/messages.en
echo "companyNameUniqueTaxReference.field2 = Field 2" >> ../conf/messages.en
echo "companyNameUniqueTaxReference.checkYourAnswersLabel = companyNameUniqueTaxReference" >> ../conf/messages.en
echo "companyNameUniqueTaxReference.error.field1.required = Please give an answer for field1" >> ../conf/messages.en
echo "companyNameUniqueTaxReference.error.field2.required = Please give an answer for field2" >> ../conf/messages.en

echo "Adding helper method to CheckYourAnswersHelper"
awk '/class/ {\
     print;\
     print "";\
     print "  def companyNameUniqueTaxReference: Seq[AnswerRow] = userAnswers.get(identifiers.register.company.CompanyNameUniqueTaxReferenceId) match {";\
     print "    case Some(x) => Seq(AnswerRow(\"companyNameUniqueTaxReference.checkYourAnswersLabel\", s\"${x.field1} ${x.field2}\", false, controllers.register.company.routes.CompanyNameUniqueTaxReferenceController.onPageLoad(CheckMode).url))";\
     print "    case _ => Nil";\
     print "  }";\
     next }1' ../app/utils/CheckYourAnswersHelper.scala > tmp && mv tmp ../app/utils/CheckYourAnswersHelper.scala

echo "Moving test files from generated-test/ to test/"
rsync -avm --include='*.scala' -f 'hide,! */' ../generated-test/ ../test/
rm -rf ../generated-test/

echo "Migration CompanyNameUniqueTaxReference completed"
