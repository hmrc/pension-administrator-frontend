#!/bin/bash

echo "Applying migration AdvisorAddressPostCodeLookup"

echo "Adding routes to register.advisor.routes"

echo "" >> ../conf/register.advisor.routes
echo "GET        /advisorAddressPostCodeLookup               controllers.register.advisor.AdvisorAddressPostCodeLookupController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/register.advisor.routes
echo "POST       /advisorAddressPostCodeLookup               controllers.register.advisor.AdvisorAddressPostCodeLookupController.onSubmit(mode: Mode = NormalMode)" >> ../conf/register.advisor.routes

echo "GET        /changeAdvisorAddressPostCodeLookup                        controllers.register.advisor.AdvisorAddressPostCodeLookupController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/register.advisor.routes
echo "POST       /changeAdvisorAddressPostCodeLookup                        controllers.register.advisor.AdvisorAddressPostCodeLookupController.onSubmit(mode: Mode = CheckMode)" >> ../conf/register.advisor.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "advisorAddressPostCodeLookup.title = advisorAddressPostCodeLookup" >> ../conf/messages.en
echo "advisorAddressPostCodeLookup.heading = advisorAddressPostCodeLookup" >> ../conf/messages.en
echo "advisorAddressPostCodeLookup.checkYourAnswersLabel = advisorAddressPostCodeLookup" >> ../conf/messages.en
echo "advisorAddressPostCodeLookup.error.required = Please give an answer for advisorAddressPostCodeLookup" >> ../conf/messages.en

echo "Adding helper method to CheckYourAnswersHelper"
awk '/class/ {\
     print;\
     print "";\
     print "  def advisorAddressPostCodeLookup: Seq[AnswerRow] = userAnswers.get(identifiers.register.advisor.AdvisorAddressPostCodeLookupId) match {";\
     print "    case Some(x) => Seq(AnswerRow(\"advisorAddressPostCodeLookup.checkYourAnswersLabel\", s\"$x\", false, controllers.register.advisor.routes.AdvisorAddressPostCodeLookupController.onPageLoad(CheckMode).url))";\
     print "    case _ => Nil";\
     print "  }";\
     next }1' ../app/utils/CheckYourAnswersHelper.scala > tmp && mv tmp ../app/utils/CheckYourAnswersHelper.scala

echo "Moving test files from generated-test/ to test/"
rsync -avm --include='*.scala' -f 'hide,! */' ../generated-test/ ../test/
rm -rf ../generated-test/

echo "Migration AdvisorAddressPostCodeLookup completed"
