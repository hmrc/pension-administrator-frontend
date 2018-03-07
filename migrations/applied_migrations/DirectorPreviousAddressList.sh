#!/bin/bash

echo "Applying migration DirectorPreviousAddressList"

echo "Adding routes to conf/company.routes"

echo "" >> ../conf/app.routes
echo "GET        /directorPreviousAddressList               controllers.register.company.DirectorPreviousAddressListController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/company.routes
echo "POST       /directorPreviousAddressList               controllers.register.company.DirectorPreviousAddressListController.onSubmit(mode: Mode = NormalMode)" >> ../conf/company.routes

echo "GET        /changeDirectorPreviousAddressList               controllers.register.company.DirectorPreviousAddressListController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/company.routes
echo "POST       /changeDirectorPreviousAddressList               controllers.register.company.DirectorPreviousAddressListController.onSubmit(mode: Mode = CheckMode)" >> ../conf/company.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "directorPreviousAddressList.title = directorPreviousAddressList" >> ../conf/messages.en
echo "directorPreviousAddressList.heading = directorPreviousAddressList" >> ../conf/messages.en
echo "directorPreviousAddressList.option1 = directorPreviousAddressList" Option 1 >> ../conf/messages.en
echo "directorPreviousAddressList.option2 = directorPreviousAddressList" Option 2 >> ../conf/messages.en
echo "directorPreviousAddressList.checkYourAnswersLabel = directorPreviousAddressList" >> ../conf/messages.en
echo "directorPreviousAddressList.error.required = Please give an answer for directorPreviousAddressList" >> ../conf/messages.en

echo "Adding helper method to CheckYourAnswersHelper"
awk '/class/ {\
     print;\
     print "";\
     print "  def directorPreviousAddressList: Option[AnswerRow] = userAnswers.get(identifiers.company.DirectorPreviousAddressListId) map {";\
     print "    x => AnswerRow(\"directorPreviousAddressList.checkYourAnswersLabel\", s\"directorPreviousAddressList.$x\", true, controllers.company.routes.DirectorPreviousAddressListController.onPageLoad(CheckMode).url)";\
     print "  }";\
     next }1' ../app/utils/CheckYourAnswersHelper.scala > tmp && mv tmp ../app/utils/CheckYourAnswersHelper.scala

echo "Moving test files from generated-test/ to test/"
rsync -avm --include='*.scala' -f 'hide,! */' ../generated-test/ ../test/
rm -rf ../generated-test/

echo "Migration DirectorPreviousAddressList completed"
