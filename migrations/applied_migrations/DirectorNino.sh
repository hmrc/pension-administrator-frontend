#!/bin/bash

echo "Applying migration DirectorNino"

echo "Adding routes to conf/register.company.routes"

echo "" >> ../conf/app.routes
echo "GET        /directorNino               controllers.register.company.DirectorNinoController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/register.company.routes
echo "POST       /directorNino               controllers.register.company.DirectorNinoController.onSubmit(mode: Mode = NormalMode)" >> ../conf/register.company.routes

echo "GET        /changeDirectorNino               controllers.register.company.DirectorNinoController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/register.company.routes
echo "POST       /changeDirectorNino               controllers.register.company.DirectorNinoController.onSubmit(mode: Mode = CheckMode)" >> ../conf/register.company.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "directorNino.title = directorNino" >> ../conf/messages.en
echo "directorNino.heading = directorNino" >> ../conf/messages.en
echo "directorNino.option1 = directorNino" Option 1 >> ../conf/messages.en
echo "directorNino.option2 = directorNino" Option 2 >> ../conf/messages.en
echo "directorNino.checkYourAnswersLabel = directorNino" >> ../conf/messages.en
echo "directorNino.error.required = Please give an answer for directorNino" >> ../conf/messages.en

echo "Adding helper method to CheckYourAnswersHelper"
awk '/class/ {\
     print;\
     print "";\
     print "  def directorNino: Option[AnswerRow] = userAnswers.get(identifiers.register.company.DirectorNinoId) map {";\
     print "    x => AnswerRow(\"directorNino.checkYourAnswersLabel\", s\"directorNino.$x\", true, controllers.register.company.routes.DirectorNinoController.onPageLoad(CheckMode).url)";\
     print "  }";\
     next }1' ../app/utils/CheckYourAnswersHelper.scala > tmp && mv tmp ../app/utils/CheckYourAnswersHelper.scala

echo "Moving test files from generated-test/ to test/"
rsync -avm --include='*.scala' -f 'hide,! */' ../generated-test/ ../test/
rm -rf ../generated-test/

echo "Migration DirectorNino completed"
