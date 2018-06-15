#!/bin/bash

echo "Applying migration AdviserDetails"

echo "Adding routes to register.adviser.routes"

echo "" >> ../conf/register.adviser.routes
echo "GET        /adviserDetails                       controllers.register.adviser.AdviserDetailsController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/register.adviser.routes
echo "POST       /adviserDetails                       controllers.register.adviser.AdviserDetailsController.onSubmit(mode: Mode = NormalMode)" >> ../conf/register.adviser.routes

echo "GET        /changeAdviserDetails                       controllers.register.adviser.AdviserDetailsController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/register.adviser.routes
echo "POST       /changeAdviserDetails                       controllers.register.adviser.AdviserDetailsController.onSubmit(mode: Mode = CheckMode)" >> ../conf/register.adviser.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "adviserDetails.title = adviserDetails" >> ../conf/messages.en
echo "adviserDetails.heading = adviserDetails" >> ../conf/messages.en
echo "adviserDetails.field1 = Field 1" >> ../conf/messages.en
echo "adviserDetails.field2 = Field 2" >> ../conf/messages.en
echo "adviserDetails.checkYourAnswersLabel = adviserDetails" >> ../conf/messages.en
echo "adviserDetails.error.field1.required = Please give an answer for field1" >> ../conf/messages.en
echo "adviserDetails.error.field2.required = Please give an answer for field2" >> ../conf/messages.en

echo "Adding helper method to CheckYourAnswersHelper"
awk '/class/ {\
     print;\
     print "";\
     print "  def adviserDetails: Seq[AnswerRow] = userAnswers.get(identifiers.register.adviser.AdviserDetailsId) match {";\
     print "    case Some(x) => Seq(AnswerRow(\"adviserDetails.checkYourAnswersLabel\", s\"${x.field1} ${x.field2}\", false, controllers.register.adviser.routes.AdviserDetailsController.onPageLoad(CheckMode).url))";\
     print "    case _ => Nil";\
     print "  }";\
     next }1' ../app/utils/CheckYourAnswersHelper.scala > tmp && mv tmp ../app/utils/CheckYourAnswersHelper.scala

echo "Moving test files from generated-test/ to test/"
rsync -avm --include='*.scala' -f 'hide,! */' ../generated-test/ ../test/
rm -rf ../generated-test/

echo "Migration AdviserDetails completed"
