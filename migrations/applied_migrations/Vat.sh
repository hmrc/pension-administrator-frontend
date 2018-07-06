#!/bin/bash

echo "Applying migration Vat"

echo "Adding routes to register.routes"

echo "" >> ../conf/register.routes
echo "GET        /vat                       controllers.register.VatController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/register.routes
echo "POST       /vat                       controllers.register.VatController.onSubmit(mode: Mode = NormalMode)" >> ../conf/register.routes

echo "GET        /changeVat                       controllers.register.VatController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/register.routes
echo "POST       /changeVat                       controllers.register.VatController.onSubmit(mode: Mode = CheckMode)" >> ../conf/register.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "vat.title = vat" >> ../conf/messages.en
echo "vat.heading = vat" >> ../conf/messages.en
echo "vat.checkYourAnswersLabel = vat" >> ../conf/messages.en
echo "vat.error.required = Please give an answer for vat" >> ../conf/messages.en

echo "Adding helper method to CheckYourAnswersHelper"
awk '/class/ {\
     print;\
     print "";\
     print "  def vat: Seq[AnswerRow] = userAnswers.get(identifiers.register.VatId) match {";\
     print "    case Some(x) => Seq(AnswerRow(\"vat.checkYourAnswersLabel\", if(x) \"site.yes\" else \"site.no\", true, controllers.register.routes.VatController.onPageLoad(CheckMode).url))";\
     print "    case _ => Nil";\
     print "  }";\
     next }1' ../app/utils/CheckYourAnswersHelper.scala > tmp && mv tmp ../app/utils/CheckYourAnswersHelper.scala

echo "Moving test files from generated-test/ to test/"
rsync -avm --include='*.scala' -f 'hide,! */' ../generated-test/ ../test/
rm -rf ../generated-test/

echo "Migration Vat completed"
