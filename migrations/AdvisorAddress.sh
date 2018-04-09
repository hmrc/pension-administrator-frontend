#!/bin/bash

echo "Applying migration AdvisorAddress"

echo "Adding routes to register.advisor.routes"

echo "" >> ../conf/register.advisor.routes
echo "GET        /advisorAddress                       controllers.register.advisor.AdvisorAddressController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/register.advisor.routes
echo "POST       /advisorAddress                       controllers.register.advisor.AdvisorAddressController.onSubmit(mode: Mode = NormalMode)" >> ../conf/register.advisor.routes

echo "GET        /changeAdvisorAddress                       controllers.register.advisor.AdvisorAddressController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/register.advisor.routes
echo "POST       /changeAdvisorAddress                       controllers.register.advisor.AdvisorAddressController.onSubmit(mode: Mode = CheckMode)" >> ../conf/register.advisor.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "advisorAddress.title = advisorAddress" >> ../conf/messages.en
echo "advisorAddress.heading = advisorAddress" >> ../conf/messages.en
echo "advisorAddress.field1 = Field 1" >> ../conf/messages.en
echo "advisorAddress.field2 = Field 2" >> ../conf/messages.en
echo "advisorAddress.checkYourAnswersLabel = advisorAddress" >> ../conf/messages.en
echo "advisorAddress.error.field1.required = Please give an answer for field1" >> ../conf/messages.en
echo "advisorAddress.error.field2.required = Please give an answer for field2" >> ../conf/messages.en

echo "Adding helper method to CheckYourAnswersHelper"
awk '/class/ {\
     print;\
     print "";\
     print "  def advisorAddress: Seq[AnswerRow] = userAnswers.get(identifiers.register.advisor.AdvisorAddressId) match {";\
     print "    case Some(x) => Seq(AnswerRow(\"advisorAddress.checkYourAnswersLabel\", s\"${x.field1} ${x.field2}\", false, controllers.register.advisor.routes.AdvisorAddressController.onPageLoad(CheckMode).url))";\
     print "    case _ => Nil";\
     print "  }";\
     next }1' ../app/utils/CheckYourAnswersHelper.scala > tmp && mv tmp ../app/utils/CheckYourAnswersHelper.scala

echo "Moving test files from generated-test/ to test/"
rsync -avm --include='*.scala' -f 'hide,! */' ../generated-test/ ../test/
rm -rf ../generated-test/

echo "Migration AdvisorAddress completed"
