#!/bin/bash

echo "Applying migration DirectorPreviousAddressPostCodeLookup"

echo "Adding routes to register.company.routes"

echo "" >> ../conf/register.company.routes
echo "GET        /directorPreviousAddressPostCodeLookup               controllers.register.company.DirectorPreviousAddressPostCodeLookupController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/register.company.routes
echo "POST       /directorPreviousAddressPostCodeLookup               controllers.register.company.DirectorPreviousAddressPostCodeLookupController.onSubmit(mode: Mode = NormalMode)" >> ../conf/register.company.routes

echo "GET        /changeDirectorPreviousAddressPostCodeLookup                        controllers.register.company.DirectorPreviousAddressPostCodeLookupController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/register.company.routes
echo "POST       /changeDirectorPreviousAddressPostCodeLookup                        controllers.register.company.DirectorPreviousAddressPostCodeLookupController.onSubmit(mode: Mode = CheckMode)" >> ../conf/register.company.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "directorPreviousAddressPostCodeLookup.title = directorPreviousAddressPostCodeLookup" >> ../conf/messages.en
echo "directorPreviousAddressPostCodeLookup.heading = directorPreviousAddressPostCodeLookup" >> ../conf/messages.en
echo "directorPreviousAddressPostCodeLookup.checkYourAnswersLabel = directorPreviousAddressPostCodeLookup" >> ../conf/messages.en
echo "directorPreviousAddressPostCodeLookup.error.required = Please give an answer for directorPreviousAddressPostCodeLookup" >> ../conf/messages.en

echo "Adding helper method to CheckYourAnswersHelper"
awk '/class/ {\
     print;\
     print "";\
     print "  def directorPreviousAddressPostCodeLookup: Option[AnswerRow] = userAnswers.get(identifiers.register.company.DirectorPreviousAddressPostCodeLookupId) map {";\
     print "    x => AnswerRow(\"directorPreviousAddressPostCodeLookup.checkYourAnswersLabel\", s\"$x\", false, controllers.register.company.routes.DirectorPreviousAddressPostCodeLookupController.onPageLoad(CheckMode).url)";\
     print "  }";\
     next }1' ../app/utils/CheckYourAnswersHelper.scala > tmp && mv tmp ../app/utils/CheckYourAnswersHelper.scala

echo "Moving test files from generated-test/ to test/"
rsync -avm --include='*.scala' -f 'hide,! */' ../generated-test/ ../test/
rm -rf ../generated-test/

echo "Migration DirectorPreviousAddressPostCodeLookup completed"
