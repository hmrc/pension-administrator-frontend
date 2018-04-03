#!/bin/bash

echo "Applying migration IndividualDetailsCorrect"

echo "Adding routes to register.individual.routes"

echo "" >> ../conf/register.individual.routes
echo "GET        /individualDetailsCorrect                       controllers.register.individual.IndividualDetailsCorrectController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/register.individual.routes
echo "POST       /individualDetailsCorrect                       controllers.register.individual.IndividualDetailsCorrectController.onSubmit(mode: Mode = NormalMode)" >> ../conf/register.individual.routes

echo "GET        /changeIndividualDetailsCorrect                       controllers.register.individual.IndividualDetailsCorrectController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/register.individual.routes
echo "POST       /changeIndividualDetailsCorrect                       controllers.register.individual.IndividualDetailsCorrectController.onSubmit(mode: Mode = CheckMode)" >> ../conf/register.individual.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "individualDetailsCorrect.title = individualDetailsCorrect" >> ../conf/messages.en
echo "individualDetailsCorrect.heading = individualDetailsCorrect" >> ../conf/messages.en
echo "individualDetailsCorrect.checkYourAnswersLabel = individualDetailsCorrect" >> ../conf/messages.en
echo "individualDetailsCorrect.error.required = Please give an answer for individualDetailsCorrect" >> ../conf/messages.en

echo "Adding helper method to CheckYourAnswersHelper"
awk '/class/ {\
     print;\
     print "";\
     print "  def individualDetailsCorrect: Seq[AnswerRow] = userAnswers.get(identifiers.register.individual.IndividualDetailsCorrectId) match {";\
     print "    case Some(x) => Seq(AnswerRow(\"individualDetailsCorrect.checkYourAnswersLabel\", if(x) \"site.yes\" else \"site.no\", true, controllers.register.individual.routes.IndividualDetailsCorrectController.onPageLoad(CheckMode).url))";\
     print "    case _ => Nil";\
     print "  }";\
     next }1' ../app/utils/CheckYourAnswersHelper.scala > tmp && mv tmp ../app/utils/CheckYourAnswersHelper.scala

echo "Moving test files from generated-test/ to test/"
rsync -avm --include='*.scala' -f 'hide,! */' ../generated-test/ ../test/
rm -rf ../generated-test/

echo "Migration IndividualDetailsCorrect completed"
