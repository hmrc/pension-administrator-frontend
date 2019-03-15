/*
 * Copyright 2019 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package identifiers.register

import identifiers.TypedIdentifier

/*
   Note on the PAInDeclarationJourneyId flag.
   ------------------------------------------
   If the save and continue button on the variations working knowledge page is clicked and
   the page has been navigated to within the declaration journey (hence mode is CheckUpdateMode)
   then the PAInDeclarationJourneyId flag is saved with value true. This is so that when the
   pension adviser cya page at the end of the declaration journey is submitted the cya controller
   knows to direct the user to the fit and proper page rather than back to the PSA details page
   (since the pension adviser cya controller is also now reachable via the change link on the PSA
   details page).
*/

case object PAInDeclarationJourneyId extends TypedIdentifier[Boolean] {
  self =>
  override def toString: String = "pensionAdviserInDeclarationJourney"

}
