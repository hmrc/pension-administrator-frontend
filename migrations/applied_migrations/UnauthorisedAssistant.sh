#!/bin/bash

echo "Applying migration UnauthorisedAssistant"

echo "Adding routes to register.routes"
echo "" >> ../conf/register.routes
echo "GET        /unauthorisedAssistant                       controllers.register.UnauthorisedAssistantController.onPageLoad()" >> ../conf/register.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "unauthorisedAssistant.title = unauthorisedAssistant" >> ../conf/messages.en
echo "unauthorisedAssistant.heading = unauthorisedAssistant" >> ../conf/messages.en

echo "Moving test files from generated-test/ to test/"
rsync -avm --include='*.scala' -f 'hide,! */' ../generated-test/ ../test/
rm -rf ../generated-test/

echo "Migration UnauthorisedAssistant completed"
