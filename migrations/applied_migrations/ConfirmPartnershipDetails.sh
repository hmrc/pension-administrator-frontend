#!/bin/bash

echo "Applying migration ConfirmPartnershipDetails"

echo "Adding routes to register.partnership.routes"

echo "" >> ../conf/register.partnership.routes
echo "GET        /confirmPartnershipDetails                       controllers.register.partnership.ConfirmPartnershipDetailsController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/register.partnership.routes
echo "POST       /confirmPartnershipDetails                       controllers.register.partnership.ConfirmPartnershipDetailsController.onSubmit(mode: Mode = NormalMode)" >> ../conf/register.partnership.routes

echo "GET        /changeConfirmPartnershipDetails                       controllers.register.partnership.ConfirmPartnershipDetailsController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/register.partnership.routes
echo "POST       /changeConfirmPartnershipDetails                       controllers.register.partnership.ConfirmPartnershipDetailsController.onSubmit(mode: Mode = CheckMode)" >> ../conf/register.partnership.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "confirmPartnershipDetails.title = confirmPartnershipDetails" >> ../conf/messages.en
echo "confirmPartnershipDetails.heading = confirmPartnershipDetails" >> ../conf/messages.en
echo "confirmPartnershipDetails.checkYourAnswersLabel = confirmPartnershipDetails" >> ../conf/messages.en
echo "confirmPartnershipDetails.error.required = Please give an answer for confirmPartnershipDetails" >> ../conf/messages.en

echo "Adding helper method to CheckYourAnswersHelper"
awk '/class/ {\
     print;\
     print "";\
     print "  def confirmPartnershipDetails: Seq[AnswerRow] = userAnswers.get(identifiers.register.partnership.ConfirmPartnershipDetailsId) match {";\
     print "    case Some(x) => Seq(AnswerRow(\"confirmPartnershipDetails.checkYourAnswersLabel\", if(x) \"site.yes\" else \"site.no\", true, controllers.register.partnership.routes.ConfirmPartnershipDetailsController.onPageLoad(CheckMode).url))";\
     print "    case _ => Nil";\
     print "  }";\
     next }1' ../app/utils/CheckYourAnswersHelper.scala > tmp && mv tmp ../app/utils/CheckYourAnswersHelper.scala

echo "Moving test files from generated-test/ to test/"
rsync -avm --include='*.scala' -f 'hide,! */' ../generated-test/ ../test/
rm -rf ../generated-test/

echo "Migration ConfirmPartnershipDetails completed"
