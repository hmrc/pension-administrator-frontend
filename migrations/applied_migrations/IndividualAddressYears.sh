#!/bin/bash

echo "Applying migration IndividualAddressYears"

echo "Adding routes to conf/register.individual.routes"

echo "" >> ../conf/app.routes
echo "GET        /individualAddressYears               controllers.register.individual.IndividualAddressYearsController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/register.individual.routes
echo "POST       /individualAddressYears               controllers.register.individual.IndividualAddressYearsController.onSubmit(mode: Mode = NormalMode)" >> ../conf/register.individual.routes

echo "GET        /changeIndividualAddressYears               controllers.register.individual.IndividualAddressYearsController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/register.individual.routes
echo "POST       /changeIndividualAddressYears               controllers.register.individual.IndividualAddressYearsController.onSubmit(mode: Mode = CheckMode)" >> ../conf/register.individual.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "individualAddressYears.title = individualAddressYears" >> ../conf/messages.en
echo "individualAddressYears.heading = individualAddressYears" >> ../conf/messages.en
echo "individualAddressYears.option1 = individualAddressYears" Option 1 >> ../conf/messages.en
echo "individualAddressYears.option2 = individualAddressYears" Option 2 >> ../conf/messages.en
echo "individualAddressYears.checkYourAnswersLabel = individualAddressYears" >> ../conf/messages.en
echo "individualAddressYears.error.required = Please give an answer for individualAddressYears" >> ../conf/messages.en

echo "Adding helper method to CheckYourAnswersHelper"
awk '/class/ {\
     print;\
     print "";\
     print "  def individualAddressYears: Seq[AnswerRow] = userAnswers.get(identifiers.register.individual.IndividualAddressYearsId) match {";\
     print "    case Some(x) => Seq(AnswerRow(\"individualAddressYears.checkYourAnswersLabel\", s\"individualAddressYears.$x\", true, controllers.register.individual.routes.IndividualAddressYearsController.onPageLoad(CheckMode).url))";\
     print "    case _ => Nil";\
     print "  }";\
     next }1' ../app/utils/CheckYourAnswersHelper.scala > tmp && mv tmp ../app/utils/CheckYourAnswersHelper.scala

echo "Moving test files from generated-test/ to test/"
rsync -avm --include='*.scala' -f 'hide,! */' ../generated-test/ ../test/
rm -rf ../generated-test/

echo "Migration IndividualAddressYears completed"
