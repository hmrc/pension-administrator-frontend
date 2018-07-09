#!/bin/bash

echo "Applying migration AddEntity"

echo "Adding routes to register.routes"

echo "" >> ../conf/register.routes
echo "GET        /addEntity                       controllers.register.AddEntityController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/register.routes
echo "POST       /addEntity                       controllers.register.AddEntityController.onSubmit(mode: Mode = NormalMode)" >> ../conf/register.routes

echo "GET        /changeAddEntity                       controllers.register.AddEntityController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/register.routes
echo "POST       /changeAddEntity                       controllers.register.AddEntityController.onSubmit(mode: Mode = CheckMode)" >> ../conf/register.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "addEntity.title = addEntity" >> ../conf/messages.en
echo "addEntity.heading = addEntity" >> ../conf/messages.en
echo "addEntity.checkYourAnswersLabel = addEntity" >> ../conf/messages.en
echo "addEntity.error.required = Please give an answer for addEntity" >> ../conf/messages.en

echo "Adding helper method to CheckYourAnswersHelper"
awk '/class/ {\
     print;\
     print "";\
     print "  def addEntity: Seq[AnswerRow] = userAnswers.get(identifiers.register.AddEntityId) match {";\
     print "    case Some(x) => Seq(AnswerRow(\"addEntity.checkYourAnswersLabel\", if(x) \"site.yes\" else \"site.no\", true, controllers.register.routes.AddEntityController.onPageLoad(CheckMode).url))";\
     print "    case _ => Nil";\
     print "  }";\
     next }1' ../app/utils/CheckYourAnswersHelper.scala > tmp && mv tmp ../app/utils/CheckYourAnswersHelper.scala

echo "Moving test files from generated-test/ to test/"
rsync -avm --include='*.scala' -f 'hide,! */' ../generated-test/ ../test/
rm -rf ../generated-test/

echo "Migration AddEntity completed"
