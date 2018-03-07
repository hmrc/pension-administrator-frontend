#!/bin/bash

echo "Applying migration DirectorPreviousAddress"

echo "Adding routes to register.company.routes"

echo "" >> ../conf/register.company.routes
echo "GET        /directorPreviousAddress                       controllers.register.company.DirectorPreviousAddressController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/register.company.routes
echo "POST       /directorPreviousAddress                       controllers.register.company.DirectorPreviousAddressController.onSubmit(mode: Mode = NormalMode)" >> ../conf/register.company.routes

echo "GET        /changeDirectorPreviousAddress                       controllers.register.company.DirectorPreviousAddressController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/register.company.routes
echo "POST       /changeDirectorPreviousAddress                       controllers.register.company.DirectorPreviousAddressController.onSubmit(mode: Mode = CheckMode)" >> ../conf/register.company.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "directorPreviousAddress.title = directorPreviousAddress" >> ../conf/messages.en
echo "directorPreviousAddress.heading = directorPreviousAddress" >> ../conf/messages.en
echo "directorPreviousAddress.field1 = Field 1" >> ../conf/messages.en
echo "directorPreviousAddress.field2 = Field 2" >> ../conf/messages.en
echo "directorPreviousAddress.checkYourAnswersLabel = directorPreviousAddress" >> ../conf/messages.en
echo "directorPreviousAddress.error.field1.required = Please give an answer for field1" >> ../conf/messages.en
echo "directorPreviousAddress.error.field2.required = Please give an answer for field2" >> ../conf/messages.en

echo "Adding helper method to CheckYourAnswersHelper"
awk '/class/ {\
     print;\
     print "";\
     print "  def directorPreviousAddress: Option[AnswerRow] = userAnswers.get(identifiers.register.company.DirectorPreviousAddressId) map {";\
     print "    x => AnswerRow(\"directorPreviousAddress.checkYourAnswersLabel\", s\"${x.field1} ${x.field2}\", false, controllers.register.company.routes.DirectorPreviousAddressController.onPageLoad(CheckMode).url)";\
     print "  }";\
     next }1' ../app/utils/CheckYourAnswersHelper.scala > tmp && mv tmp ../app/utils/CheckYourAnswersHelper.scala

echo "Moving test files from generated-test/ to test/"
rsync -avm --include='*.scala' -f 'hide,! */' ../generated-test/ ../test/
rm -rf ../generated-test/

echo "Migration DirectorPreviousAddress completed"
