# Pension Administrator Frontend

## Adding a page using Leg Up

The steps to add a new page are as follows:

1. In sbt run `g8Scaffold yesNoPage` or whichever scaffold you want to apply - the options are:
    1. `intPage` - a page with a single text field for a whole-number value
    2. `optionsPage` - a page with a set of radio buttons
    3. `questionPage` - a page with multiple questions on it backed by a case class
    4. `repeater` - a set of screens to allow multiple items to be added
    5. `yesNoPage` - a page with a pair of yes/no radio options
    6. `stringPage` - a page with a single text field for a string value
2. It will ask for a name which should be CamelCased, e.g. `ChildAgedTwo`.  This will be used throughout, so for example you'll get a `ChildAgedTwoController`, `ChildAgedTwoForm` etc. as needed
3. Exit sbt and run the script `./migrate.sh`
4. Run `sbt test`
5. `git add .` and `git commit` your work at this point

You'll then need to touch these areas yourself as needed (and the order is less important):

* Change `NavigatorSpec` and `Navigator` to include your page in the routing
* If necessary, change `CascadeUpsertSpec` and `CascadeUpsert` to add any data-cleanup logic you may need
* Add in the messages, and add any guidance etc. you need to the screen.  You can easily test this in the relevant `ViewSpec`
* Change the URLs in `app.routes` as necessary
* If you're using a Check Your Answers page, add the new screen to that and add appropriate tests
* If you added an `optionsPage` you can change the available options in `forms\<YourClass>Form`
* If you added a `questionPage` you'll need to change `models\<YourClass>`, `forms\<YourClass>Form` and `views\<yourClass>` as needed, along with the relevant tests and messages
* If you added an `intPage` you may want to change the validation in `forms\<YourClass>Form` - by default it expects non-negative numbers (0 or greater) which you may want to change, or you may need to add a maximum value etc.
