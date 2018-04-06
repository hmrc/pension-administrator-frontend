#!/bin/bash

echo "Applying migration DeclarationWorkingKnowledge"

echo "Adding routes to register.routes"

echo "" >> ../conf/register.routes
echo "GET        /declarationWorkingKnowledge                       controllers.register.DeclarationWorkingKnowledgeController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/register.routes
echo "POST       /declarationWorkingKnowledge                       controllers.register.DeclarationWorkingKnowledgeController.onSubmit(mode: Mode = NormalMode)" >> ../conf/register.routes

echo "GET        /changeDeclarationWorkingKnowledge                       controllers.register.DeclarationWorkingKnowledgeController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/register.routes
echo "POST       /changeDeclarationWorkingKnowledge                       controllers.register.DeclarationWorkingKnowledgeController.onSubmit(mode: Mode = CheckMode)" >> ../conf/register.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "declarationWorkingKnowledge.title = declarationWorkingKnowledge" >> ../conf/messages.en
echo "declarationWorkingKnowledge.heading = declarationWorkingKnowledge" >> ../conf/messages.en
echo "declarationWorkingKnowledge.checkYourAnswersLabel = declarationWorkingKnowledge" >> ../conf/messages.en
echo "declarationWorkingKnowledge.error.required = Please give an answer for declarationWorkingKnowledge" >> ../conf/messages.en

echo "Adding helper method to CheckYourAnswersHelper"
awk '/class/ {\
     print;\
     print "";\
     print "  def declarationWorkingKnowledge: Seq[AnswerRow] = userAnswers.get(identifiers.register.DeclarationWorkingKnowledgeId) match {";\
     print "    case Some(x) => Seq(AnswerRow(\"declarationWorkingKnowledge.checkYourAnswersLabel\", if(x) \"site.yes\" else \"site.no\", true, controllers.register.routes.DeclarationWorkingKnowledgeController.onPageLoad(CheckMode).url))";\
     print "    case _ => Nil";\
     print "  }";\
     next }1' ../app/utils/CheckYourAnswersHelper.scala > tmp && mv tmp ../app/utils/CheckYourAnswersHelper.scala

echo "Moving test files from generated-test/ to test/"
rsync -avm --include='*.scala' -f 'hide,! */' ../generated-test/ ../test/
rm -rf ../generated-test/

echo "Migration DeclarationWorkingKnowledge completed"
