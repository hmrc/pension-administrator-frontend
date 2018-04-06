#!/bin/bash

echo "Applying migration BusinessType"

echo "Adding routes to conf/register.routes"

echo "" >> ../conf/app.routes
echo "GET        /businessType               controllers.register.BusinessTypeController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/register.routes
echo "POST       /businessType               controllers.register.BusinessTypeController.onSubmit(mode: Mode = NormalMode)" >> ../conf/register.routes

echo "GET        /changeBusinessType               controllers.register.BusinessTypeController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/register.routes
echo "POST       /changeBusinessType               controllers.register.BusinessTypeController.onSubmit(mode: Mode = CheckMode)" >> ../conf/register.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "businessType.title = businessType" >> ../conf/messages.en
echo "businessType.heading = businessType" >> ../conf/messages.en
echo "businessType.option1 = businessType" Option 1 >> ../conf/messages.en
echo "businessType.option2 = businessType" Option 2 >> ../conf/messages.en
echo "businessType.checkYourAnswersLabel = businessType" >> ../conf/messages.en
echo "businessType.error.required = Please give an answer for businessType" >> ../conf/messages.en

echo "Adding helper method to CheckYourAnswersHelper"
awk '/class/ {\
     print;\
     print "";\
     print "  def businessType: Seq[AnswerRow] = userAnswers.get(identifiers.register.BusinessTypeId) match {";\
     print "    case Some(x) => Seq(AnswerRow(\"businessType.checkYourAnswersLabel\", s\"businessType.$x\", true, controllers.register.routes.BusinessTypeController.onPageLoad(CheckMode).url))";\
     print "    case _ => Nil";\
     print "  }";\
     next }1' ../app/utils/CheckYourAnswersHelper.scala > tmp && mv tmp ../app/utils/CheckYourAnswersHelper.scala

echo "Moving test files from generated-test/ to test/"
rsync -avm --include='*.scala' -f 'hide,! */' ../generated-test/ ../test/
rm -rf ../generated-test/

echo "Migration BusinessType completed"
