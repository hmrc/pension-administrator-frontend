#!/bin/bash

echo "Applying migration AdviserAddressPostCodeLookup"

echo "Adding routes to register.adviser.routes"

echo "" >> ../conf/register.adviser.routes
echo "GET        /adviserAddressPostCodeLookup               controllers.register.adviser.AdviserAddressPostCodeLookupController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/register.adviser.routes
echo "POST       /adviserAddressPostCodeLookup               controllers.register.adviser.AdviserAddressPostCodeLookupController.onSubmit(mode: Mode = NormalMode)" >> ../conf/register.adviser.routes

echo "GET        /changeAdviserAddressPostCodeLookup                        controllers.register.adviser.AdviserAddressPostCodeLookupController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/register.adviser.routes
echo "POST       /changeAdviserAddressPostCodeLookup                        controllers.register.adviser.AdviserAddressPostCodeLookupController.onSubmit(mode: Mode = CheckMode)" >> ../conf/register.adviser.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "adviserAddressPostCodeLookup.title = adviserAddressPostCodeLookup" >> ../conf/messages.en
echo "adviserAddressPostCodeLookup.heading = adviserAddressPostCodeLookup" >> ../conf/messages.en
echo "adviserAddressPostCodeLookup.checkYourAnswersLabel = adviserAddressPostCodeLookup" >> ../conf/messages.en
echo "adviserAddressPostCodeLookup.error.required = Please give an answer for adviserAddressPostCodeLookup" >> ../conf/messages.en

echo "Adding helper method to CheckYourAnswersHelper"
awk '/class/ {\
     print;\
     print "";\
     print "  def adviserAddressPostCodeLookup: Seq[AnswerRow] = userAnswers.get(identifiers.register.adviser.AdviserAddressPostCodeLookupId) match {";\
     print "    case Some(x) => Seq(AnswerRow(\"adviserAddressPostCodeLookup.checkYourAnswersLabel\", s\"$x\", false, controllers.register.adviser.routes.AdviserAddressPostCodeLookupController.onPageLoad(CheckMode).url))";\
     print "    case _ => Nil";\
     print "  }";\
     next }1' ../app/utils/CheckYourAnswersHelper.scala > tmp && mv tmp ../app/utils/CheckYourAnswersHelper.scala

echo "Moving test files from generated-test/ to test/"
rsync -avm --include='*.scala' -f 'hide,! */' ../generated-test/ ../test/
rm -rf ../generated-test/

echo "Migration AdviserAddressPostCodeLookup completed"
