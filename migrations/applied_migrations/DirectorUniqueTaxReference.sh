#!/bin/bash

echo "Applying migration DirectorUniqueTaxReference"

echo "Adding routes to conf/register.company.routes"

echo "" >> ../conf/app.routes
echo "GET        /directorUniqueTaxReference               controllers.register.company.DirectorUniqueTaxReferenceController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/register.company.routes
echo "POST       /directorUniqueTaxReference               controllers.register.company.DirectorUniqueTaxReferenceController.onSubmit(mode: Mode = NormalMode)" >> ../conf/register.company.routes

echo "GET        /changeDirectorUniqueTaxReference               controllers.register.company.DirectorUniqueTaxReferenceController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/register.company.routes
echo "POST       /changeDirectorUniqueTaxReference               controllers.register.company.DirectorUniqueTaxReferenceController.onSubmit(mode: Mode = CheckMode)" >> ../conf/register.company.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "directorUniqueTaxReference.title = directorUniqueTaxReference" >> ../conf/messages.en
echo "directorUniqueTaxReference.heading = directorUniqueTaxReference" >> ../conf/messages.en
echo "directorUniqueTaxReference.option1 = directorUniqueTaxReference" Option 1 >> ../conf/messages.en
echo "directorUniqueTaxReference.option2 = directorUniqueTaxReference" Option 2 >> ../conf/messages.en
echo "directorUniqueTaxReference.checkYourAnswersLabel = directorUniqueTaxReference" >> ../conf/messages.en
echo "directorUniqueTaxReference.error.required = Please give an answer for directorUniqueTaxReference" >> ../conf/messages.en

echo "Adding helper method to CheckYourAnswersHelper"
awk '/class/ {\
     print;\
     print "";\
     print "  def directorUniqueTaxReference: Option[AnswerRow] = userAnswers.get(identifiers.register.company.DirectorUniqueTaxReferenceId) map {";\
     print "    x => AnswerRow(\"directorUniqueTaxReference.checkYourAnswersLabel\", s\"directorUniqueTaxReference.$x\", true, controllers.register.company.routes.DirectorUniqueTaxReferenceController.onPageLoad(CheckMode).url)";\
     print "  }";\
     next }1' ../app/utils/CheckYourAnswersHelper.scala > tmp && mv tmp ../app/utils/CheckYourAnswersHelper.scala

echo "Moving test files from generated-test/ to test/"
rsync -avm --include='*.scala' -f 'hide,! */' ../generated-test/ ../test/
rm -rf ../generated-test/

echo "Migration DirectorUniqueTaxReference completed"
