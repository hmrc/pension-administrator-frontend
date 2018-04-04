#!/bin/bash

echo "Applying migration YouWillNeedToUpdate"

echo "Adding routes to register.individual.routes"
echo "" >> ../conf/register.individual.routes
echo "GET        /youWillNeedToUpdate                       controllers.register.individual.YouWillNeedToUpdateController.onPageLoad()" >> ../conf/register.individual.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "youWillNeedToUpdate.title = youWillNeedToUpdate" >> ../conf/messages.en
echo "youWillNeedToUpdate.heading = youWillNeedToUpdate" >> ../conf/messages.en

echo "Moving test files from generated-test/ to test/"
rsync -avm --include='*.scala' -f 'hide,! */' ../generated-test/ ../test/
rm -rf ../generated-test/

echo "Migration YouWillNeedToUpdate completed"
