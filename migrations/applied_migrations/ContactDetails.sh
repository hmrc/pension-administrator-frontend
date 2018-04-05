#!/bin/bash

echo "Applying migration ContactDetails"

echo "Adding routes to register.individual.routes"

echo "" >> ../conf/register.individual.routes
echo "GET        /contactDetails                       controllers.register.individual.ContactDetailsController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/register.individual.routes
echo "POST       /contactDetails                       controllers.register.individual.ContactDetailsController.onSubmit(mode: Mode = NormalMode)" >> ../conf/register.individual.routes

echo "GET        /changeContactDetails                       controllers.register.individual.ContactDetailsController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/register.individual.routes
echo "POST       /changeContactDetails                       controllers.register.individual.ContactDetailsController.onSubmit(mode: Mode = CheckMode)" >> ../conf/register.individual.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "contactDetails.title = contactDetails" >> ../conf/messages.en
echo "contactDetails.heading = contactDetails" >> ../conf/messages.en
echo "contactDetails.field1 = Field 1" >> ../conf/messages.en
echo "contactDetails.field2 = Field 2" >> ../conf/messages.en
echo "contactDetails.checkYourAnswersLabel = contactDetails" >> ../conf/messages.en
echo "contactDetails.error.field1.required = Please give an answer for field1" >> ../conf/messages.en
echo "contactDetails.error.field2.required = Please give an answer for field2" >> ../conf/messages.en

echo "Adding helper method to CheckYourAnswersHelper"
awk '/class/ {\
     print;\
     print "";\
     print "  def contactDetails: Seq[AnswerRow] = userAnswers.get(identifiers.register.individual.ContactDetailsId) match {";\
     print "    case Some(x) => Seq(AnswerRow(\"contactDetails.checkYourAnswersLabel\", s\"${x.field1} ${x.field2}\", false, controllers.register.individual.routes.ContactDetailsController.onPageLoad(CheckMode).url))";\
     print "    case _ => Nil";\
     print "  }";\
     next }1' ../app/utils/CheckYourAnswersHelper.scala > tmp && mv tmp ../app/utils/CheckYourAnswersHelper.scala

echo "Moving test files from generated-test/ to test/"
rsync -avm --include='*.scala' -f 'hide,! */' ../generated-test/ ../test/
rm -rf ../generated-test/

echo "Migration ContactDetails completed"
