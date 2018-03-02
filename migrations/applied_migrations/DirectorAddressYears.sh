#!/bin/bash

echo "Applying migration DirectorAddressYears"

echo "Adding routes to conf/register.company.routes"

echo "" >> ../conf/app.routes
echo "GET        /directorAddressYears               controllers.register.company.DirectorAddressYearsController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/register.company.routes
echo "POST       /directorAddressYears               controllers.register.company.DirectorAddressYearsController.onSubmit(mode: Mode = NormalMode)" >> ../conf/register.company.routes

echo "GET        /changeDirectorAddressYears               controllers.register.company.DirectorAddressYearsController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/register.company.routes
echo "POST       /changeDirectorAddressYears               controllers.register.company.DirectorAddressYearsController.onSubmit(mode: Mode = CheckMode)" >> ../conf/register.company.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "directorAddressYears.title = directorAddressYears" >> ../conf/messages.en
echo "directorAddressYears.heading = directorAddressYears" >> ../conf/messages.en
echo "directorAddressYears.option1 = directorAddressYears" Option 1 >> ../conf/messages.en
echo "directorAddressYears.option2 = directorAddressYears" Option 2 >> ../conf/messages.en
echo "directorAddressYears.checkYourAnswersLabel = directorAddressYears" >> ../conf/messages.en
echo "directorAddressYears.error.required = Please give an answer for directorAddressYears" >> ../conf/messages.en

echo "Adding helper method to CheckYourAnswersHelper"
awk '/class/ {\
     print;\
     print "";\
     print "  def directorAddressYears: Option[AnswerRow] = userAnswers.get(identifiers.register.company.DirectorAddressYearsId) map {";\
     print "    x => AnswerRow(\"directorAddressYears.checkYourAnswersLabel\", s\"directorAddressYears.$x\", true, controllers.register.company.routes.DirectorAddressYearsController.onPageLoad(CheckMode).url)";\
     print "  }";\
     next }1' ../app/utils/CheckYourAnswersHelper.scala > tmp && mv tmp ../app/utils/CheckYourAnswersHelper.scala

echo "Moving test files from generated-test/ to test/"
rsync -avm --include='*.scala' -f 'hide,! */' ../generated-test/ ../test/
rm -rf ../generated-test/

echo "Migration DirectorAddressYears completed"
