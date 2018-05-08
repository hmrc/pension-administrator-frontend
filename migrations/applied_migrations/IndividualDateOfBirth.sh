#!/bin/bash

echo "Applying migration IndividualDateOfBirth"

echo "Adding routes to register.individual.routes"

echo "" >> ../conf/register.individual.routes
echo "GET        /individualDateOfBirth               controllers.register.individual.IndividualDateOfBirthController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/register.individual.routes
echo "POST       /individualDateOfBirth               controllers.register.individual.IndividualDateOfBirthController.onSubmit(mode: Mode = NormalMode)" >> ../conf/register.individual.routes

echo "GET        /changeIndividualDateOfBirth                        controllers.register.individual.IndividualDateOfBirthController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/register.individual.routes
echo "POST       /changeIndividualDateOfBirth                        controllers.register.individual.IndividualDateOfBirthController.onSubmit(mode: Mode = CheckMode)" >> ../conf/register.individual.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "individualDateOfBirth.title = individualDateOfBirth" >> ../conf/messages.en
echo "individualDateOfBirth.heading = individualDateOfBirth" >> ../conf/messages.en
echo "individualDateOfBirth.checkYourAnswersLabel = individualDateOfBirth" >> ../conf/messages.en
echo "individualDateOfBirth.error.required = Please give an answer for individualDateOfBirth" >> ../conf/messages.en

echo "Adding helper method to CheckYourAnswersHelper"
awk '/class/ {\
     print;\
     print "";\
     print "  def individualDateOfBirth: Seq[AnswerRow] = userAnswers.get(identifiers.register.individual.IndividualDateOfBirthId) match {";\
     print "    case Some(x) => Seq(AnswerRow(\"individualDateOfBirth.checkYourAnswersLabel\", s\"$x\", false, controllers.register.individual.routes.IndividualDateOfBirthController.onPageLoad(CheckMode).url))";\
     print "    case _ => Nil";\
     print "  }";\
     next }1' ../app/utils/CheckYourAnswersHelper.scala > tmp && mv tmp ../app/utils/CheckYourAnswersHelper.scala

echo "Moving test files from generated-test/ to test/"
rsync -avm --include='*.scala' -f 'hide,! */' ../generated-test/ ../test/
rm -rf ../generated-test/

echo "Migration IndividualDateOfBirth completed"
