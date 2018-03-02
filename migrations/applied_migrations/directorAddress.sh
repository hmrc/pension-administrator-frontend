#!/bin/bash

echo "Applying migration directorAddress"

echo "Adding routes to company.routes"

echo "" >> ../conf/company.routes
echo "GET        /directorAddress                       controllers.company.directorAddressController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/company.routes
echo "POST       /directorAddress                       controllers.company.directorAddressController.onSubmit(mode: Mode = NormalMode)" >> ../conf/company.routes

echo "GET        /changedirectorAddress                       controllers.company.directorAddressController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/company.routes
echo "POST       /changedirectorAddress                       controllers.company.directorAddressController.onSubmit(mode: Mode = CheckMode)" >> ../conf/company.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "directorAddress.title = directorAddress" >> ../conf/messages.en
echo "directorAddress.heading = directorAddress" >> ../conf/messages.en
echo "directorAddress.field1 = Field 1" >> ../conf/messages.en
echo "directorAddress.field2 = Field 2" >> ../conf/messages.en
echo "directorAddress.checkYourAnswersLabel = directorAddress" >> ../conf/messages.en
echo "directorAddress.error.field1.required = Please give an answer for field1" >> ../conf/messages.en
echo "directorAddress.error.field2.required = Please give an answer for field2" >> ../conf/messages.en

echo "Adding helper method to CheckYourAnswersHelper"
awk '/class/ {\
     print;\
     print "";\
     print "  def directorAddress: Option[AnswerRow] = userAnswers.get(identifiers.company.directorAddressId) map {";\
     print "    x => AnswerRow(\"directorAddress.checkYourAnswersLabel\", s\"${x.field1} ${x.field2}\", false, controllers.company.routes.directorAddressController.onPageLoad(CheckMode).url)";\
     print "  }";\
     next }1' ../app/utils/CheckYourAnswersHelper.scala > tmp && mv tmp ../app/utils/CheckYourAnswersHelper.scala

echo "Moving test files from generated-test/ to test/"
rsync -avm --include='*.scala' -f 'hide,! */' ../generated-test/ ../test/
rm -rf ../generated-test/

echo "Migration directorAddress completed"
