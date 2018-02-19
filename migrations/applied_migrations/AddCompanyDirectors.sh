#!/bin/bash

echo "Applying migration AddCompanyDirectors"

echo "Adding routes to register.company.routes"

echo "" >> ../conf/register.company.routes
echo "GET        /addCompanyDirectors                       controllers.register.company.AddCompanyDirectorsController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/register.company.routes
echo "POST       /addCompanyDirectors                       controllers.register.company.AddCompanyDirectorsController.onSubmit(mode: Mode = NormalMode)" >> ../conf/register.company.routes

echo "GET        /changeAddCompanyDirectors                       controllers.register.company.AddCompanyDirectorsController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/register.company.routes
echo "POST       /changeAddCompanyDirectors                       controllers.register.company.AddCompanyDirectorsController.onSubmit(mode: Mode = CheckMode)" >> ../conf/register.company.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "addCompanyDirectors.title = addCompanyDirectors" >> ../conf/messages.en
echo "addCompanyDirectors.heading = addCompanyDirectors" >> ../conf/messages.en
echo "addCompanyDirectors.checkYourAnswersLabel = addCompanyDirectors" >> ../conf/messages.en
echo "addCompanyDirectors.error.required = Please give an answer for addCompanyDirectors" >> ../conf/messages.en

echo "Adding helper method to CheckYourAnswersHelper"
awk '/class/ {\
     print;\
     print "";\
     print "  def addCompanyDirectors: Option[AnswerRow] = userAnswers.get(identifiers.register.company.AddCompanyDirectorsId) map {";\
     print "    x => AnswerRow(\"addCompanyDirectors.checkYourAnswersLabel\", if(x) \"site.yes\" else \"site.no\", true, controllers.register.company.routes.AddCompanyDirectorsController.onPageLoad(CheckMode).url)"; print "  }";\
     next }1' ../app/utils/CheckYourAnswersHelper.scala > tmp && mv tmp ../app/utils/CheckYourAnswersHelper.scala

echo "Moving test files from generated-test/ to test/"
rsync -avm --include='*.scala' -f 'hide,! */' ../generated-test/ ../test/
rm -rf ../generated-test/

echo "Migration AddCompanyDirectors completed"
