#!/bin/bash

echo "Applying migration PartnerUniqueTaxReference"

echo "Adding routes to conf/register.partnership.partners.routes"

echo "" >> ../conf/app.routes
echo "GET        /partnerUniqueTaxReference               controllers.register.partnership.partners.PartnerUniqueTaxReferenceController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/register.partnership.partners.routes
echo "POST       /partnerUniqueTaxReference               controllers.register.partnership.partners.PartnerUniqueTaxReferenceController.onSubmit(mode: Mode = NormalMode)" >> ../conf/register.partnership.partners.routes

echo "GET        /changePartnerUniqueTaxReference               controllers.register.partnership.partners.PartnerUniqueTaxReferenceController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/register.partnership.partners.routes
echo "POST       /changePartnerUniqueTaxReference               controllers.register.partnership.partners.PartnerUniqueTaxReferenceController.onSubmit(mode: Mode = CheckMode)" >> ../conf/register.partnership.partners.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "partnerUniqueTaxReference.title = partnerUniqueTaxReference" >> ../conf/messages.en
echo "partnerUniqueTaxReference.heading = partnerUniqueTaxReference" >> ../conf/messages.en
echo "partnerUniqueTaxReference.option1 = partnerUniqueTaxReference" Option 1 >> ../conf/messages.en
echo "partnerUniqueTaxReference.option2 = partnerUniqueTaxReference" Option 2 >> ../conf/messages.en
echo "partnerUniqueTaxReference.checkYourAnswersLabel = partnerUniqueTaxReference" >> ../conf/messages.en
echo "partnerUniqueTaxReference.error.required = Please give an answer for partnerUniqueTaxReference" >> ../conf/messages.en

echo "Adding helper method to CheckYourAnswersHelper"
awk '/class/ {\
     print;\
     print "";\
     print "  def partnerUniqueTaxReference: Seq[AnswerRow] = userAnswers.get(identifiers.register.partnership.partners.PartnerUniqueTaxReferenceId) match {";\
     print "    case Some(x) => Seq(AnswerRow(\"partnerUniqueTaxReference.checkYourAnswersLabel\", s\"partnerUniqueTaxReference.$x\", true, controllers.register.partnership.partners.routes.PartnerUniqueTaxReferenceController.onPageLoad(CheckMode).url))";\
     print "    case _ => Nil";\
     print "  }";\
     next }1' ../app/utils/CheckYourAnswersHelper.scala > tmp && mv tmp ../app/utils/CheckYourAnswersHelper.scala

echo "Moving test files from generated-test/ to test/"
rsync -avm --include='*.scala' -f 'hide,! */' ../generated-test/ ../test/
rm -rf ../generated-test/

echo "Migration PartnerUniqueTaxReference completed"
