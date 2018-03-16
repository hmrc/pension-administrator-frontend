#!/bin/bash

echo "Applying migration WhatYouWillNeed"

echo "Adding routes to register.individual.routes"
echo "" >> ../conf/register.individual.routes
echo "GET        /whatYouWillNeed                       controllers.register.individual.WhatYouWillNeedController.onPageLoad()" >> ../conf/register.individual.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "whatYouWillNeed.title = whatYouWillNeed" >> ../conf/messages.en
echo "whatYouWillNeed.heading = whatYouWillNeed" >> ../conf/messages.en

echo "Moving test files from generated-test/ to test/"
rsync -avm --include='*.scala' -f 'hide,! */' ../generated-test/ ../test/
rm -rf ../generated-test/

echo "Migration WhatYouWillNeed completed"
