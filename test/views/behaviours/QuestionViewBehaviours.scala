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

package views.behaviours

import play.api.data.{Field, Form, FormError}
import play.twirl.api.HtmlFormat

trait QuestionViewBehaviours[A] extends ViewBehaviours {

  val errorKey = "value"
  val errorMessage = "error.number"
  lazy val error = FormError(errorKey, errorMessage)

  val form: Form[A]

  private def assertFieldExists(
                                 createView: Form[A] => HtmlFormat.Appendable,
                                 fieldName: String,
                                 text: Option[String] = None,
                                 error: Option[FormError] = None): Unit = {

    val doc = error match {
      case Some(e) => asDocument(createView(form.withError(e)))
      case _ => asDocument(createView(form))
    }
    val field: Field = form.apply(fieldName)

    text match {
      case Some(s) =>
        assertRenderedByIdWithText(doc, field.id, s)
      case _ => assertRenderedById(doc, field.id)
    }
  }

  private def assertLabelExists(createView: Form[A] => HtmlFormat.Appendable, forElement: String, text: String): Unit = {
    val doc = asDocument(createView(form))
    val field: Field = form.apply(forElement)
    assertRenderedByForWithText(doc, field.id, text)
  }

  private def assertDatePartErrorExists(createView: Form[A] => HtmlFormat.Appendable, fieldName: String, datePart: String): Unit = {
    assertFieldExists(
      createView,
      "error-message-date",
      Some(messages("site.error") + " " + messages("error.invalid_date")),
      Some(FormError(s"$fieldName.$datePart", "error-text"))
    )
  }

  def pageWithTextFields(createView: Form[A] => HtmlFormat.Appendable,
                         messageKeyPrefix: String,
                         expectedFormAction: String,
                         fields: String*): Unit = {

    "behave like a question page" when {
      "rendered" must {
        for (field <- fields) {
          s"contain an input for $field" in {
            val doc = asDocument(createView(form))
            assertRenderedById(doc, field)
          }
        }

        "not render an error summary" in {
          val doc = asDocument(createView(form))
          assertNotRenderedById(doc, "error-summary-heading")
        }
      }

      for (field <- fields) {
        s"rendered with an error with field '$field'" must {
          "show an error summary" in {
            val doc = asDocument(createView(form.withError(FormError(field, "error"))))
            assertRenderedById(doc, "error-summary-heading")
          }

          s"show an error in the label for field '$field'" in {
            val doc = asDocument(createView(form.withError(FormError(field, "error"))))
            val errorSpan = doc.getElementsByClass("error-notification").first
            errorSpan.hasText mustBe true
          }
        }
      }
    }
  }

  def pageWithLabel(createView: Form[A] => HtmlFormat.Appendable,
                    forElement: String,
                    expectedText: String,
                    expectedHintText: Option[String] = None): Unit = {
    s"behave like a question page with labels and optional hint for $forElement" in {
      val doc = asDocument(createView(form))
      assertContainsLabel(doc, forElement, expectedText, expectedHintText)
    }
  }

  // scalastyle:off method.length
  def pageWithDateField(
                         createView: Form[A] => HtmlFormat.Appendable,
                         fieldName: String,
                         label: String,
                         hint: Option[String]): Unit = {

    "hava a date label?" in {
      assertFieldExists(createView, s"$fieldName-label", Some(label))
    }

    "have a hint?" in {
      hint match {
        case Some(_) => assertFieldExists(createView, s"$fieldName-date-hint", hint)
        case _ => Unit
      }
    }

    "have a day input" in {
      assertFieldExists(createView, s"$fieldName.day")
    }

    "have a month input" in {
      assertFieldExists(createView, s"$fieldName.month")
    }

    "have a year input" in {
      assertFieldExists(createView, s"$fieldName.year")
    }

    "have a label for day" in {
      assertLabelExists(createView, s"$fieldName.day", messages("date.day"))
    }

    "have a label for month" in {
      assertLabelExists(createView, s"$fieldName.month", messages("date.month"))
    }

    "have a label for year" in {
      assertLabelExists(createView, s"$fieldName.year", messages("date.year"))
    }

    "render errors when day is invalid" in {
      assertDatePartErrorExists(createView, fieldName, "day")
    }

    "render errors when month is invalid" in {
      assertDatePartErrorExists(createView, fieldName, "month")
    }

    "render errors when year is invalid" in {
      assertDatePartErrorExists(createView, fieldName, "year")
    }

    "render errors when date field is invalid" in {
      assertFieldExists(
        createView,
        s"error-message-$fieldName-input",
        Some(messages("site.error") + " " + messages("dummy-error-message-key")),
        Some(FormError(fieldName, "dummy-error-message-key"))
      )
    }

  }

  // scalastyle:on method.length

}
