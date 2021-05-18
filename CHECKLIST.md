# CODE REVIEW CHECKLIST

The following is a list of common things that should be checked when
submitting a pull request.

- [ ] Javadoc classes and methods

- [ ] All changes are unit tested

- [ ] Include screenshots and animated GIFs in your pull request
    whenever possible.

- [ ] Format your code using the default NetBeans formatter settings.
    For specifics see the [style guide](STYLE_GUIDELINES.md).

- [ ] Ensure that new ***leaks*** or ***code smells*** are not
    introduced by parsing you code using `sonar-scanner` and checking
    via [Sonar Qube](https://sonarcloud.io)

- [ ] Document code based on the [style guide](STYLE_GUIDELINES.md)

- [ ] Avoid OS dependant code (e.g. Windows or Linux)

- [ ] Utility methods should end with the word Utilities (i.e not Util
    or Utility)

- [ ] Update the [change log](CHANGELOG.md) as a means to notify
    developers or important improvements or changes

    -   You must start with Added, Fixed, Improved, Moved, Removed,
        Renamed or Updated and sort them alphabetically

- [ ] Update whatsnew.txt if you want to inform users about your change

- [ ] Update the html help pages as required

- [ ] Use `java.util.logging.Logger` instead of `System.out.println` or
    `System.err.println`.

    -   Note you should not use `org.apache.log4j.Logger` because it
        won’t appear in the logs.

- [ ] Use StandardCharsets.UTF\_8.name() instead of “UTF-8”

- [ ] Classes that match `*AttributeDescriptionV*`, `*IOProviderV*` or
    `*AttributeUpdateProvider` should be saved in a sub package named
    ***compatibility***.

    -   Classes that match `*AttributeDescriptionV*` or `*IOProviderV*`
        should be annotated with `@deprecated`.

- [ ] When developing a plugin, the plugin parameters have a label and
    description.

    -   Note that whenever a parameter has a default value, mention it
        in the parameters description.

- [ ] If your class extends `Plugin.class` and uses the
    `@ServiceProvider` annotation then the class name must end with the
    word `Plugin`.

- [ ] Ensure new plugins have been added to the corresponding
    `*PluginRegistry`.

- [ ] If you’re calling arrangement plugins from within your plugin, you
    must add logic to check whether the freeze graph view state is
    enabled like so:
    `java     if (!CoreUtilities.isGraphViewFrozen()) {       PluginExecution.withPlugin(ArrangementPluginRegistry.GRID_COMPOSITE).executeNow(wg);       PluginExecution.withPlugin(CorePluginRegistry.RESET).executeNow(wg);     } else {       PluginExecution.withPlugin(CorePluginRegistry.RESET).executeNow(wg);     }`

- [ ] Plugin parameter constants should be built using the following
    conventions:

    -   The constant should end in \_PARAMETER\_ID.
    -   An example of creating a plugin parameter looks like:

    ``` java
    public static final String MERGE_TYPE_PARAMETER_ID = PluginParameter.buildId(MergeNodesPlugin.class, "merge_type");
    ```

    -   In the `createParameters` method do the following:

    ``` java
    final PluginParameter<SingleChoiceParameterValue> mergeType = SingleChoiceParameterType.build(MERGE_TYPE_PARAMETER_ID);
    ```

- [ ] QUERY\_NAME\_PARAMETER and DATETIME\_RANGE\_PARAMETER are defined
    as singletons within CoreGlobalParameters.java

    -   Can be accessed by referring to them directly, for example:

    ``` java
    final PluginParameter<StringParameterValue> queryName = CoreGlobalParameters.QUERY_NAME_PARAMETER;
    ```

- [ ] If you’re using `javafx.stage.Stage` as a dialog, replace it with
    `ConstellationDialog` which makes sure your dialog remains on top
    (i.e is modal aware).

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
            -   \*Clicking the big red button will activate the
                self-destruct
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
