# CODE REVIEW CHECKLIST

The following is a list of common things that should be checked when
submitting a pull request.


## Common

- [ ] Include screenshots and animated GIFs in your pull request
    whenever possible.

- [ ] Format your code using the default NetBeans formatter settings.
    For specifics see the [style guide](STYLE_GUIDELINES.md).

- [ ] Document code based on the [style guide](STYLE_GUIDELINES.md).

- [ ] Javadoc classes and methods for any you introduce or modify.

- [ ] Unit testing changes in your PR.

    - Unit testing changes you have made is required and should be
    over 80% code coverage at a minimum.

    - If any classes you have changed do not already have a unit test
    then it is **NOT** on you to test the entire class, rather the
    changes you have made.

    - Refer to the `Core Table View` as a baseline of how tests should
    be written.

    - Use `Mockito`, `FxToolkit` and `FxRobot` as required.

- [ ] Update the [change log](CHANGELOG.md) to inform developers of
    important improvements or changes.

    -   You must start with Added, Fixed, Improved, Moved, Removed,
        Renamed or Updated and sort them alphabetically.

- [ ] Update [what's new](CoreWhatsNewView/src/au/gov/asd/tac/constellation/views/whatsnew/whatsnew.txt)
    if you want to inform users of your change.

- [ ] Update help pages (including relevant images) as required.

- [ ] Ensure that new ***leaks*** or ***code smells*** are not
    introduced by parsing your code using `sonar-scanner` or checking
    via [Sonar Qube](https://sonarcloud.io).

    -   For any false positives or smells you decide not to resolve please
        clearly outline why not.

- [ ] Avoid OS dependant code (e.g. Windows or Linux).


## Coding Conventions

- [ ] Utility classes should end with the word Utilities (i.e not Util
    or Utility).

- [ ] Use a class specific logger and call it `LOGGER` using
    `java.util.logging.Logger`. Do not use `System.out.println` or
    `System.err.println`.

    -   Note you should not use `org.apache.log4j.Logger` because it
        won’t appear in the logs.

- [ ] Use `StandardCharsets.UTF_8.name()` instead of `“UTF-8”`.

- [ ] When developing a plugin, the plugin parameters have a label and
    description.

    -   Note that whenever a parameter has a default value, mention it
        in the parameters description.

- [ ] If your class extends `Plugin.class` and uses the
    `@ServiceProvider` annotation then the class name must end with the
    word `Plugin`.

- [ ] Ensure new plugins have been added to the corresponding
    `*PluginRegistry`.

- [ ] Plugin parameter constants should be built using the following
    conventions:

    -   The constant should end in `_PARAMETER\_ID`.

    -   An example of creating a plugin parameter looks like:

    ``` java
    public static final String MERGE_TYPE_PARAMETER_ID = PluginParameter.buildId(MergeNodesPlugin.class, "merge_type");
    ```

    -   In the `createParameters` method do the following:

    ``` java
    final PluginParameter<SingleChoiceParameterValue> mergeType = SingleChoiceParameterType.build(MERGE_TYPE_PARAMETER_ID);
    ```

- [ ] `QUERY_NAME_PARAMETER` and `DATETIME_RANGE_PARAMETER` are defined
    as singletons within `CoreGlobalParameters` so reference them like this:

    ``` java
    final PluginParameter<StringParameterValue> queryName = CoreGlobalParameters.QUERY_NAME_PARAMETER;
    ```

- [ ] If you’re using `javafx.stage.Stage` as a dialog, replace it with
    `ConstellationDialog` which makes sure your dialog remains on top
    (i.e is modal aware).

- [ ] Classes that match `*AttributeDescriptionV*`, `*IOProviderV*` or
    `*AttributeUpdateProvider` should be saved in a sub package named
    ***compatibility***.

    -   Classes that match `*AttributeDescriptionV*` or `*IOProviderV*`
        should be annotated with `@deprecated`.


## Documentation

- [ ] When documenting use the following conventions:

    -   **nodes** rather than **vertices**

    -   Use active voice. For example

        -   GOOD:

            -   *To extract the foo, manipulate the bar*

        -   BAD:

            -   *The foo can be extracted by manipulating the bar*

    -   Give direct instructions (avoid *will* and *if*)

        -   GOOD:

            -   *To increase the volume, turn the dial*

            -   *Turn the dial to increase the volume*

            -   *Turning the dial increases the volume*

        -   OK:

            -   *Turning the dial will increase the volume*

        -   BAD:

            -   *If you turn the dial, the volume will increase*

    -   Exception to rule above: Use conditional statements for warnings
        or unlikely events.

        -   GOOD:

            -   *Clicking the big red button will activate the
                self-destruct*

            -   *If you click on the big red button, the self-destruct
                will activate*

        -   OK:

            -   *Click on the big red button to activate the
                self-destruct*

            -   *To activate the self-destruct, click the big red
                button*

    -   **you** rather than **we**

        -   GOOD:

            -   *You should save your graph before continuing*

        -   BAD:
            -   *We recommend saving your graph before continuing*
