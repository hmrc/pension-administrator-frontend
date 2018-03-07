#!/bin/bash

echo "Applying migration DirectorContactDetails"

echo "Adding routes to register.company.routes"

echo "" >> ../conf/register.company.routes
echo "GET        /directorContactDetails                       controllers.register.company.DirectorContactDetailsController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/register.company.routes
echo "POST       /directorContactDetails                       controllers.register.company.DirectorContactDetailsController.onSubmit(mode: Mode = NormalMode)" >> ../conf/register.company.routes

echo "GET        /changeDirectorContactDetails                       controllers.register.company.DirectorContactDetailsController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/register.company.routes
echo "POST       /changeDirectorContactDetails                       controllers.register.company.DirectorContactDetailsController.onSubmit(mode: Mode = CheckMode)" >> ../conf/register.company.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "directorContactDetails.title = directorContactDetails" >> ../conf/messages.en
echo "directorContactDetails.heading = directorContactDetails" >> ../conf/messages.en
echo "directorContactDetails.field1 = Field 1" >> ../conf/messages.en
echo "directorContactDetails.field2 = Field 2" >> ../conf/messages.en
echo "directorContactDetails.checkYourAnswersLabel = directorContactDetails" >> ../conf/messages.en
echo "directorContactDetails.error.field1.required = Please give an answer for field1" >> ../conf/messages.en
echo "directorContactDetails.error.field2.required = Please give an answer for field2" >> ../conf/messages.en

echo "Adding helper method to CheckYourAnswersHelper"
awk '/class/ {\
     print;\
     print "";\
     print "  def directorContactDetails: Option[AnswerRow] = userAnswers.get(identifiers.register.company.DirectorContactDetailsId) map {";\
     print "    x => AnswerRow(\"directorContactDetails.checkYourAnswersLabel\", s\"${x.field1} ${x.field2}\", false, controllers.register.company.routes.DirectorContactDetailsController.onPageLoad(CheckMode).url)";\
     print "  }";\
     next }1' ../app/utils/CheckYourAnswersHelper.scala > tmp && mv tmp ../app/utils/CheckYourAnswersHelper.scala

echo "Moving test files from generated-test/ to test/"
rsync -avm --include='*.scala' -f 'hide,! */' ../generated-test/ ../test/
rm -rf ../generated-test/

echo "Migration DirectorContactDetails completed"
