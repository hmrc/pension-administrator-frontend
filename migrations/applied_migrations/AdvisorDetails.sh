#!/bin/bash

echo "Applying migration AdvisorDetails"

echo "Adding routes to register.advisor.routes"

echo "" >> ../conf/register.advisor.routes
echo "GET        /advisorDetails                       controllers.register.advisor.AdvisorDetailsController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/register.advisor.routes
echo "POST       /advisorDetails                       controllers.register.advisor.AdvisorDetailsController.onSubmit(mode: Mode = NormalMode)" >> ../conf/register.advisor.routes

echo "GET        /changeAdvisorDetails                       controllers.register.advisor.AdvisorDetailsController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/register.advisor.routes
echo "POST       /changeAdvisorDetails                       controllers.register.advisor.AdvisorDetailsController.onSubmit(mode: Mode = CheckMode)" >> ../conf/register.advisor.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "advisorDetails.title = advisorDetails" >> ../conf/messages.en
echo "advisorDetails.heading = advisorDetails" >> ../conf/messages.en
echo "advisorDetails.field1 = Field 1" >> ../conf/messages.en
echo "advisorDetails.field2 = Field 2" >> ../conf/messages.en
echo "advisorDetails.checkYourAnswersLabel = advisorDetails" >> ../conf/messages.en
echo "advisorDetails.error.field1.required = Please give an answer for field1" >> ../conf/messages.en
echo "advisorDetails.error.field2.required = Please give an answer for field2" >> ../conf/messages.en

echo "Adding helper method to CheckYourAnswersHelper"
awk '/class/ {\
     print;\
     print "";\
     print "  def advisorDetails: Seq[AnswerRow] = userAnswers.get(identifiers.register.advisor.AdvisorDetailsId) match {";\
     print "    case Some(x) => Seq(AnswerRow(\"advisorDetails.checkYourAnswersLabel\", s\"${x.field1} ${x.field2}\", false, controllers.register.advisor.routes.AdvisorDetailsController.onPageLoad(CheckMode).url))";\
     print "    case _ => Nil";\
     print "  }";\
     next }1' ../app/utils/CheckYourAnswersHelper.scala > tmp && mv tmp ../app/utils/CheckYourAnswersHelper.scala

echo "Moving test files from generated-test/ to test/"
rsync -avm --include='*.scala' -f 'hide,! */' ../generated-test/ ../test/
rm -rf ../generated-test/

echo "Migration AdvisorDetails completed"
