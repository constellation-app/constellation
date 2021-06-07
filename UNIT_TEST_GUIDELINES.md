# Unit Test Guidelines

Here are a few helpful guidelines for writing unit tests for Constellation.

## What To Include

- Ideally, the unit tests you write for a function should cover any edge cases 
that could be encountered when running it.

- For functions that take a graph as input, you should include a test with a 
null or empty graph and one with a non-empty graph. The non-empty graph should 
ideally contain the minimum number of graph elements, attributes, and attribute 
values required to ensure the function works as intended.

- If your unit test requires the JFX toolkit to be initialised, wrap the test 
code in the following:
<!-- -->

    if (!GraphicsEnvironment.isHeadless()) {
        //insert test code here
    }

    - Doing this allows the code to pass in our CI which will otherwise hold up 
until it times out. The catch though is that the CI is passing the test 
essentially by skipping it which is not ideal. As a result, this advice will 
likely change once a better solution is found.

## What Not To Include

- Functions that return the same value every time (e.g. one line functions like 
`getName()`) don't need to be unit tested. It's not that they can't be unit 
tested but rather that doing so won't really add any value. Instead, the focus 
should be on testing functions whose outputs can differ depending on inputs and 
current state.

- If you can, try to avoid using `Thread.sleep()` in your unit tests as this is 
not a reliable way to ensure something finishes (i.e. the time required for 
something to finish on your local system might be different to that of the CI 
(or any other system)).

## Other Things of Note

- If you need to edit the access modifiers of any part of a class in order to 
enable testing, do the following:
    - Constants and Functions: Change the access modifier to appropriate level 
(usually protected will suffice, don't make public unless absolutely necessary)
    - Fields: Add a getter function (leave the access modifier as is)
