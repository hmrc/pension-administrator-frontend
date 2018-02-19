#!/bin/bash

echo "Applying migration MoreThanTenDirectors"

echo "Adding routes to register.company.routes"

echo "" >> ../conf/register.company.routes
echo "GET        /moreThanTenDirectors                       controllers.register.company.MoreThanTenDirectorsController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/register.company.routes
echo "POST       /moreThanTenDirectors                       controllers.register.company.MoreThanTenDirectorsController.onSubmit(mode: Mode = NormalMode)" >> ../conf/register.company.routes

echo "GET        /changeMoreThanTenDirectors                       controllers.register.company.MoreThanTenDirectorsController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/register.company.routes
echo "POST       /changeMoreThanTenDirectors                       controllers.register.company.MoreThanTenDirectorsController.onSubmit(mode: Mode = CheckMode)" >> ../conf/register.company.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "moreThanTenDirectors.title = moreThanTenDirectors" >> ../conf/messages.en
echo "moreThanTenDirectors.heading = moreThanTenDirectors" >> ../conf/messages.en
echo "moreThanTenDirectors.checkYourAnswersLabel = moreThanTenDirectors" >> ../conf/messages.en
echo "moreThanTenDirectors.error.required = Please give an answer for moreThanTenDirectors" >> ../conf/messages.en

echo "Adding helper method to CheckYourAnswersHelper"
awk '/class/ {\
     print;\
     print "";\
     print "  def moreThanTenDirectors: Option[AnswerRow] = userAnswers.get(identifiers.register.company.MoreThanTenDirectorsId) map {";\
     print "    x => AnswerRow(\"moreThanTenDirectors.checkYourAnswersLabel\", if(x) \"site.yes\" else \"site.no\", true, controllers.register.company.routes.MoreThanTenDirectorsController.onPageLoad(CheckMode).url)"; print "  }";\
     next }1' ../app/utils/CheckYourAnswersHelper.scala > tmp && mv tmp ../app/utils/CheckYourAnswersHelper.scala

echo "Moving test files from generated-test/ to test/"
rsync -avm --include='*.scala' -f 'hide,! */' ../generated-test/ ../test/
rm -rf ../generated-test/

echo "Migration MoreThanTenDirectors completed"
