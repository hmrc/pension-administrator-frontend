#!/bin/bash

echo "Applying migration ConfirmDeleteDirector"

echo "Adding routes to register.company.routes"
echo "" >> ../conf/register.company.routes
echo "GET        /confirmDeleteDirector                       controllers.register.company.ConfirmDeleteDirectorController.onPageLoad()" >> ../conf/register.company.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "confirmDeleteDirector.title = confirmDeleteDirector" >> ../conf/messages.en
echo "confirmDeleteDirector.heading = confirmDeleteDirector" >> ../conf/messages.en

echo "Moving test files from generated-test/ to test/"
rsync -avm --include='*.scala' -f 'hide,! */' ../generated-test/ ../test/
rm -rf ../generated-test/

echo "Migration ConfirmDeleteDirector completed"
