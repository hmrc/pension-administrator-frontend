#!/bin/bash

echo "Applying migration WhatYouWillNeed"

echo "Adding routes to register.partnership.routes"
echo "" >> ../conf/register.partnership.routes
echo "GET        /whatYouWillNeed                       controllers.register.partnership.WhatYouWillNeedController.onPageLoad()" >> ../conf/register.partnership.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "whatYouWillNeed.title = whatYouWillNeed" >> ../conf/messages.en
echo "whatYouWillNeed.heading = whatYouWillNeed" >> ../conf/messages.en

echo "Moving test files from generated-test/ to test/"
rsync -avm --include='*.scala' -f 'hide,! */' ../generated-test/ ../test/
rm -rf ../generated-test/

echo "Migration WhatYouWillNeed completed"
