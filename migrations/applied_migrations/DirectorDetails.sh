#!/bin/bash

echo "Applying migration DirectorDetails"

echo "Adding routes to register.company.routes"

echo "" >> ../conf/register.company.routes
echo "GET        /directorDetails                       controllers.register.company.DirectorDetailsController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/register.company.routes
echo "POST       /directorDetails                       controllers.register.company.DirectorDetailsController.onSubmit(mode: Mode = NormalMode)" >> ../conf/register.company.routes

echo "GET        /changeDirectorDetails                       controllers.register.company.DirectorDetailsController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/register.company.routes
echo "POST       /changeDirectorDetails                       controllers.register.company.DirectorDetailsController.onSubmit(mode: Mode = CheckMode)" >> ../conf/register.company.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "directorDetails.title = directorDetails" >> ../conf/messages.en
echo "directorDetails.heading = directorDetails" >> ../conf/messages.en
echo "directorDetails.field1 = Field 1" >> ../conf/messages.en
echo "directorDetails.field2 = Field 2" >> ../conf/messages.en
echo "directorDetails.checkYourAnswersLabel = directorDetails" >> ../conf/messages.en
echo "directorDetails.error.field1.required = Please give an answer for field1" >> ../conf/messages.en
echo "directorDetails.error.field2.required = Please give an answer for field2" >> ../conf/messages.en

echo "Adding helper method to CheckYourAnswersHelper"
awk '/class/ {\
     print;\
     print "";\
     print "  def directorDetails: Option[AnswerRow] = userAnswers.get(identifiers.register.company.DirectorDetailsId) map {";\
     print "    x => AnswerRow(\"directorDetails.checkYourAnswersLabel\", s\"${x.field1} ${x.field2}\", false, controllers.register.company.routes.DirectorDetailsController.onPageLoad(CheckMode).url)";\
     print "  }";\
     next }1' ../app/utils/CheckYourAnswersHelper.scala > tmp && mv tmp ../app/utils/CheckYourAnswersHelper.scala

echo "Moving test files from generated-test/ to test/"
rsync -avm --include='*.scala' -f 'hide,! */' ../generated-test/ ../test/
rm -rf ../generated-test/

echo "Migration DirectorDetails completed"
