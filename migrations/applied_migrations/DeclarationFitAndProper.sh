#!/bin/bash

echo "Applying migration DeclarationFitAndProper"

echo "Adding routes to register.routes"
echo "" >> ../conf/register.routes
echo "GET        /declarationFitAndProper                       controllers.register.DeclarationFitAndProperController.onPageLoad()" >> ../conf/register.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "declarationFitAndProper.title = declarationFitAndProper" >> ../conf/messages.en
echo "declarationFitAndProper.heading = declarationFitAndProper" >> ../conf/messages.en

echo "Moving test files from generated-test/ to test/"
rsync -avm --include='*.scala' -f 'hide,! */' ../generated-test/ ../test/
rm -rf ../generated-test/

echo "Migration DeclarationFitAndProper completed"
